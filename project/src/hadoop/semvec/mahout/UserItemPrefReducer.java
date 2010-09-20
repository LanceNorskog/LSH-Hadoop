package semvec.mahout;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/*
 * For a dimension, randomly project all users and items onto a line from 0 to 1.
 * For each user+preference, pull the item towards the user.
 * 
 * Write projected format with index discriminators: userid U/I dimension spot
 * 
 */

/*
 * Divide total offset by # of items
 */

public class UserItemPrefReducer extends
		Reducer<LongWritable, TupleWritable, Text, Text> {

	// shift preferences from 1 to 5 -> -2 to 2.
	public static final Float BIAS = 3f;
	// scaling for pref values
	public static final Float SCALE = 0.2f;
	
	public int trims = 0;
	public int adjustments = 0;
//	public double tug = 0;
	public int users = 0;
	public int items = 0;

	protected void reduce(
			LongWritable key,
			Iterable<TupleWritable> values,
			Reducer<LongWritable, TupleWritable, Text, Text>.Context context)
			throws java.io.IOException, InterruptedException {
		float tug = 0;
		int nitems = 0;
//		Map<Long,Float> userSpots = new HashMap<Long,Float>();
		Set<Long> users = new HashSet<Long>();
		Map<Long,Float> itemSpots = new HashMap<Long,Float>();
		Map<Long,ChemicalFloat> ratings = new HashMap<Long, ChemicalFloat>();
		Long dim = key.get();

		StringBuilder sb = new StringBuilder();
		for (TupleWritable data : values) {
			if (! users.contains(data.getUserID())) {
				collectUser(context, dim, sb, data);
				users.add(data.getUserID());
			}
			
			itemSpots.put(data.getItemID(), data.getItemSpot());
			ChemicalFloat rating = ratings.get(data.getItemID());
			if (null == rating) {
				rating = new ChemicalFloat();
				ratings.put(data.getItemID(), rating);
			} 
			
			// each user pulls the item towards himself
			float pref = (data.getPref() - BIAS);
			float delta = data.getUserSpot() - data.getItemSpot();
			if (delta > 0)
			{
				pref = Math.min(pref * SCALE,delta);
			} else {
				pref = Math.min(pref * SCALE, -delta);
				pref = -pref;
			}
				
			rating.f += pref;
		}
		int divisor = users.size();
		for(Long itemID: itemSpots.keySet()) {
			sb.setLength(0);
			sb.append(itemID.toString());
			sb.append(' ');
			sb.append("I");
			sb.append(" ");
			sb.append(dim.toString());
			sb.append(' ');
			Float spot = itemSpots.get(itemID);
			float bump = ratings.get(itemID).f;
			spot += (bump * SCALE)/divisor;
			if (spot >= 1.0)
				spot = 0.9999999f;
			if (spot <= 0.0)
				spot = 0.00000001f;
			sb.append(spot.toString());
			context.write(null, new Text(sb.toString()));
		}		
	}

	private void collectUser(
			Reducer<LongWritable, TupleWritable, Text, Text>.Context context,
			Long dim, StringBuilder sb, TupleWritable data) throws IOException,
			InterruptedException {
		sb.setLength(0);
		Long userID = data.getUserID();
		sb.append(userID.toString());
		sb.append(' ');
		sb.append("U");
		sb.append(" ");
		sb.append(dim.toString());
		sb.append(' ');
		Float spot = data.getUserSpot();
		sb.append(spot.toString());
		context.write(null, new Text(sb.toString()));
	}
}

/*
 * reduce the object load from incrementing the ratings
 */
class ChemicalFloat {
	public float f;
	
	ChemicalFloat() {
		f = 0.0f;
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return ((Float) f).hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return ((Float) f).equals(((ChemicalFloat)o).f);
	}
}
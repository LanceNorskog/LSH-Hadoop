package semvec.mahout;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lsh.hadoop.LSHDriver;

import org.apache.hadoop.conf.Configuration;
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
	public Float bias = 0f;
	// scaling for pref values
	public Float scale = 0f;
	
	public int projections = 0;
	public int trims = 0;
	public int adjustments = 0;
	public int users = 0;
	public int items = 0;
	public int pinned = 0;

	@Override
	protected void setup(org.apache.hadoop.mapreduce.Reducer<LongWritable,TupleWritable,Text,Text>.Context context) throws IOException ,InterruptedException {
		Configuration conf = context.getConfiguration();
		String scaleString = conf.get(LSHDriver.SCALE);
		String biasString = conf.get(LSHDriver.BIAS);
		scale = Float.parseFloat(scaleString);
		bias = Float.parseFloat(biasString);	
	};
	
	protected void reduce(
			LongWritable key,
			Iterable<TupleWritable> values,
			Reducer<LongWritable, TupleWritable, Text, Text>.Context context)
			throws java.io.IOException, InterruptedException {
		float tug = 0;
		int nitems = 0;
		Set<Long> users = new HashSet<Long>();
		Map<Long,Float> itemSpots = new HashMap<Long,Float>();
		Map<Long,ChemicalFloat> ratings = new HashMap<Long, ChemicalFloat>();
		Long dim = key.get();
		Float minUserSpot = Float.MIN_VALUE;
		Float maxUserSpot = Float.MAX_VALUE;

		StringBuilder sb = new StringBuilder();
		for (TupleWritable data : values) {
			if (! users.contains(data.getUserID())) {
				Float spot = data.getUserSpot();
				if (spot < minUserSpot)
					minUserSpot = spot;
				if (spot < maxUserSpot)
					maxUserSpot = spot;
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
			float pref = (data.getPref() - bias);
			float delta = data.getUserSpot() - data.getItemSpot();
			if (delta > 0)
			{
				pref = Math.min(pref * scale, delta);
			} else {
				pref = Math.min(pref * scale, -delta);
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
			spot += (bump * scale)/divisor;
			pinned++;
			if (spot >= 1.0)
				spot = maxUserSpot;
			else if (spot <= 0.0)
				spot = minUserSpot;
			else
				pinned--;
			sb.append(spot.toString());
			context.write(null, new Text(sb.toString()));
			projections++;
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
	
	@Override
	protected void cleanup(org.apache.hadoop.mapreduce.Reducer<LongWritable,TupleWritable,Text,Text>.Context context) throws IOException ,InterruptedException {
		System.err.println("REPORT: # projections: " + projections + ", pinned: " + pinned);
	};
	
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
package semvec.mahout;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/*
 * For a dimension, randomly project all users and items onto a dimension from 0 to 1.
 * For each user+preference, pull the item towards the user.
 * The farther the item from the user, the harder the user pulls.
 * 
 * Write LSH format with index discriminators: U/I dim # spot
 */

public class DimReducer extends
		Reducer<LongWritable, TupleWritable, Text, Text> {

	public static final Float SCALE = 0.05f;

	protected void reduce(
			LongWritable key,
			Iterable<TupleWritable> values,
			Reducer<LongWritable, TupleWritable, Text, Text>.Context context)
			throws java.io.IOException, InterruptedException {
		Map<Long,Float> userSpots = new HashMap<Long,Float>();
		Map<Long,Float> itemSpots = new HashMap<Long,Float>();
		Long dim = key.get();

		for (TupleWritable data : values) {
			float itemSpot = data.getItemSpot();
			long userID = data.getUserID();
			if (! userSpots.containsKey(userID)) {
				userSpots.put(userID, data.getUserSpot());				
			}
			float userSpot = data.getUserSpot();
			float pref = data.getPref() * SCALE;
			float nudge;
			nudge = (userSpot - itemSpot) * pref;
			itemSpot += nudge;
			if (itemSpot < 0.0f)
				itemSpot = 0.00000000001f;
			if (itemSpot > 1.0f)
				itemSpot = 0.99999999999f;
			itemSpots.put(data.getItemID(), itemSpot);
		}
		
		StringBuilder sb = new StringBuilder();
		for(Long userID: userSpots.keySet()) {
			sb.setLength(0);
			sb.append("U");
			sb.append(" ");
			sb.append(dim.toString());
			sb.append(' ');
			sb.append(userID.toString());
			Float spot = userSpots.get(userID);
			sb.append(' ');
			sb.append(spot.toString());
			context.write(null, new Text(sb.toString()));
		}
		for(Long itemID: itemSpots.keySet()) {
			sb.setLength(0);
			sb.append("I");
			sb.append(" ");
			sb.append(dim.toString());
			sb.append(' ');
			sb.append(itemID.toString());
			Float spot = itemSpots.get(itemID);
			sb.append(' ');
			sb.append(spot.toString());
			context.write(null, new Text(sb.toString()));
		}		
	}
}

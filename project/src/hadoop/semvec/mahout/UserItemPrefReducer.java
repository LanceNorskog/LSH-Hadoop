package semvec.mahout;

import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/*
 * For a dimension, randomly project all users and items onto a line from 0 to 1.
 * For each user+preference, pull the item towards the user.
 * 
 * Write LSH format with index discriminators: U/I dim # spot
 */

public class UserItemPrefReducer extends
		Reducer<LongWritable, TupleWritable, Text, Text> {

	// shift preferences from 1.0/4.0 to 0 to 3
	public static final Float BIAS = 1.0f;
	// scaling for pref values
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
			// ??? draw it out
			float pref = (data.getPref() + BIAS) * SCALE;
			float delta = userSpot - itemSpot;
			if (delta > 0) {
				float nudge = pref;
				itemSpot += nudge;
				if (itemSpot > userSpot)
					itemSpot = userSpot;
			} else {
				float nudge = pref;
				itemSpot -= nudge;
				if (itemSpot < userSpot)
					itemSpot = userSpot;				
			}
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

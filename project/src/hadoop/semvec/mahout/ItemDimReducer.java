package semvec.mahout;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.hadoop.io.serializer.WritableSerialization;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.mahout.cf.taste.hadoop.EntityPrefWritable;
import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.math.VarLongWritable;

/*
 * For a dimension, randomly project all users and items onto a dimension from 0 to 1.
 * For each user+preference, push the item towards or away from that user.
 */

public class ItemDimReducer extends
		Reducer<ItemDimWritable, PrefUserSpotWritable, Object, Object> {

	public static final Integer DIM = 2;
	public static final Float SCALE = 0.05f;
	private Random[] dimensionRandom;
	private boolean[] userSeen;

	protected void setup(
			Reducer<ItemDimWritable, PrefUserSpotWritable, Object, Object>.Context context)
			throws java.io.IOException, InterruptedException {
		dimensionRandom = new Random[DIM];
		for (int dim = 0; dim < DIM; dim++)
			dimensionRandom[dim] = new Random(-dim);
		userSeen = new boolean[DIM];
	}

	protected void reduce(
			ItemDimWritable key,
			Iterable<PrefUserSpotWritable> values,
			Reducer<ItemDimWritable, PrefUserSpotWritable, Object, String>.Context context)
			throws java.io.IOException, InterruptedException {
		long itemID = key.getID();
		long dim = key.getDimension();
		Random rand = new Random(dim);
		float itemSpot = rand.nextFloat();
		List<Long> userIDs = new ArrayList<Long>();

		for (PrefUserSpotWritable pref : values) {
//			long userID = pref.getID();
//			userIDs.add(userID);
//			if ()
//			float userSpot = pref.getUserSpot();
//			float direction = pref.getPref() * SCALE;
//			itemSpot += direction;
		}
		StringBuilder sb = new StringBuilder();
		
		
		
	}
}

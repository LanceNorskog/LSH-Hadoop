package semvec.mahout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.hadoop.mapreduce.Reducer;
import org.apache.mahout.cf.taste.hadoop.EntityPrefWritable;
import org.apache.mahout.common.RandomUtils;

/*
 * For a dimension, randomly project all users and items onto a dimension from 0 to 1.
 * For each user+preference, push the item towards or away from that user.
 */

public class ProjectorReducer extends Reducer<UserDimWritable,EntityPrefWritable,Object,Object>{
	
	protected void reduce(UserDimWritable key, java.lang.Iterable<EntityPrefWritable> values, org.apache.hadoop.mapreduce.Reducer<UserDimWritable,EntityPrefWritable,Object,Object>.Context context) 
		throws java.io.IOException ,InterruptedException {
		long userid = key.getID();
		long dim = key.getDimension();
		Random rand = new Random(dim);
		float userSpot = rand.nextFloat();
		
		for(EntityPrefWritable ep: values) {
			float direction = ep.getPrefValue() - 2.0f;
			
		}
	};
}

package semvec.lsh;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/*
 * Assemble User and Item vectors for each id.
 *
 * Collect semantic vectors into points.
 * Semantic vector DimMapper/DimReducer creates: UorI,dim,ID,spot
 * Map this to ->
 * 	#, UI/dim/spot
 */

public class UserItemMapper extends Mapper<Object, Text, Text, Text> {

	public void map(Object key, Text value, Context context)
	throws IOException, InterruptedException {
		String[] parts = value.toString().split(" ");
		String meta = parts[0] + " " + parts[1] + " " + parts[3];
		context.write(new Text(parts[2]), new Text(meta));
	}
}

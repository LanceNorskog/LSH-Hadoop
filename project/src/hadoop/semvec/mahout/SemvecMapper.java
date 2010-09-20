package semvec.mahout;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/*
 * null/value -> first/rest Mapper
 */

public class SemvecMapper extends Mapper<LongWritable, Text, Text, Text> {
	
	@Override
	protected void map(
			LongWritable key,
			Text value,
			org.apache.hadoop.mapreduce.Mapper<LongWritable, Text, Text, Text>.Context context)
			throws java.io.IOException, InterruptedException {
		String full = value.toString();
		int space = full.indexOf(' ');
		String id = full.substring(0, space);
		context.write(new Text(id), new Text(full.substring(space)));
	}

}

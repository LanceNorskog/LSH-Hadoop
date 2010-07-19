package lsh.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/*
 * Receive one corner with a set of points.
 * Key is the string printout of the Hash code (int,int,int,...int)
 */

/*
 * Output format of "1,2,3 id,double...double|..."
 */

public class CountReducer extends
		Reducer<Text, Text, Text, Text> {
	
	@Override
	public void reduce(Text key, Iterable<Text> values, Context context)
	throws IOException, InterruptedException {
		int count = 0;

		for(Text value: values) {
			count++;
		}
		String value = Integer.toString(count);
		context.write(new Text(key), new Text(value));
	}

}


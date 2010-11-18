package lsh.hadoop;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/*
 * Give count of values for key
 */

public class CountReducer extends
		Reducer<Text, Text, Text, Text> {
	
	@Override
	public void reduce(Text key, Iterable<Text> values, Context context)
	throws IOException, InterruptedException {
		int count = 0;

		Iterator<Text> it = values.iterator();
		while(it.hasNext()){
			count++;
			it.next();
		}
		String value = Integer.toString(count);
		context.write(new Text(key), new Text(value));
	}

}


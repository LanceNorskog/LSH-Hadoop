package lsh.hadoop;

import java.io.IOException;
import lsh.core.Point;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/*
 * Receive one point with a set of corners.
 * Key is the string printout of the id,double...double
 * NOT IMPLEMENTED YET
 */

/*
 * Output format of "1,2,3 id,double...double|..."
 */

public class PointReducer extends
		Reducer<Text, Text, Text, Text> {
	
	@Override
	public void reduce(Text key, Iterable<Text> values, Context context)
	throws IOException, InterruptedException {
		StringBuilder sb = new StringBuilder();

		for(Text value: values) {
			Point point = Point.newPoint(value.toString());
			sb.append(point.id.toString());
			for(int i = 0; i < point.values.length; i++) {
				sb.append(',');
				sb.append(point.values[i]);
			}
			sb.append('|');
		}
		sb.setLength(sb.length() - 1);
		String value = sb.toString();
		context.write(new Text(key), new Text(value));
	}

}


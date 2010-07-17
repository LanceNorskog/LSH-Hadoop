package lsh.hadoop;

import java.io.IOException;
import lsh.core.Point;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/*
 * Receive one corner with a set of points.
 * Key is the string printout of the Hash code (int,int,int,...int)
 */

/*
 * Output format of "1,2,3 id,double...double|..."
 */

public class CornerReducer extends
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


package lsh.hadoop;

import java.io.IOException;

import lsh.core.Corner;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/*
 * Emit one point with a set of corners.
 * Key is the string printout of the id,double...double
 */

/*
 * Output format of "id,double...double[*payload] int,int,int|..."
 */

public class PointReducer extends
		Reducer<Text, Text, Text, Text> {
	
	@Override
	public void reduce(Text key, Iterable<Text> values, Context context)
	throws IOException, InterruptedException {
		StringBuilder sb = new StringBuilder();

		for(Text value: values) {
			Corner corner = Corner.newCorner(value.toString());
			sb.append(corner.toString());
			sb.append('|');
		}
		sb.setLength(sb.length() - 1);
		String value = sb.toString();
		context.write(new Text(key), new Text(value));
	}

}


package semvec.lsh;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import lsh.core.Point;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;

/*
 * Create point->corner output with User points and Item grids.
 * Receive one corner with a set of points.
 * If a corner has a User and an Item version, save it.
 */

/*
 * Receives corners from CornerMapper- with U/I as markers for user point or item grid
 * Output format of "1,2,3 id,double...double*U|...*I"
 */

public class UICornerReducer extends
Reducer<Text, Text, Text, Text> {

	// TODO - what a pain!
	// and to unit test!
	// what limiter
//	private boolean requireUser = true;
//	private boolean requireItem = true;
	// what to save
//	private boolean userCorners = true;
//	private boolean itemCorners = true;
//	private boolean userPoints = true;
//	private boolean itemPoints = true;
	boolean requireBoth = true;
	PrintWriter side = null;
	 float corners = 0;
	 float points = 0;
	 float maxPoints = 0;
	StandardDeviation stddev = new StandardDeviation();
	
@Override
	protected void setup(
			org.apache.hadoop.mapreduce.Reducer<Text, Text, Text, Text>.Context context)
			throws IOException, InterruptedException {
	
		side = new PrintWriter(new File("/tmp/corners.log"));	
	};
	
	@Override
	public void reduce(Text key, Iterable<Text> values, Context context)
	throws IOException, InterruptedException {
		StringBuilder sb = new StringBuilder();
		String corner = key.toString();
		int cpoints = 0;
		int items = 0;
		int users = 0;

		for(Text value: values) {
			if (items + users > 20)
				break;
			String point = value.toString();
			if (point.charAt(point.length() -1) == 'U') {
				users++;
			} else
				items++;
//			if (point.charAt(point.length() -1) == 'U') {
//				side.print(corner);
//				side.println("\t" + value.toString());
//			} else if (point.charAt(point.length() -1) == 'I') {
				cpoints++;
				sb.append(point);
				sb.append('|');
//			} else {
//				throw new InterruptedException("UICornerReduce: where are the User/Item markers?");
//			}
		}
		if (requireBoth && (users == 0 || items ==0))
			return;
		if (sb.length() > 0) {
			// only count points with item values
			corners ++;
			points += cpoints;
			if (maxPoints < cpoints)
				maxPoints = cpoints;
			stddev.increment((double) cpoints);
			sb.setLength(sb.length() - 1);
			String points = sb.toString();
			context.write(new Text(corner), new Text(points));
		}
	}
	
	@Override
	protected void cleanup(
			org.apache.hadoop.mapreduce.Reducer<Text, Text, Text, Text>.Context context)
			throws IOException, InterruptedException {
		side.flush();
		side.close();
		Double stddevres = stddev.getResult();
		System.err.println("REPORT: corners: " + corners + ", maxPoints: " + maxPoints + ", points: " + points + ", mean:" + (points / corners) + ", stddev: " + stddevres.toString().substring(0, 3));
	};
	

}


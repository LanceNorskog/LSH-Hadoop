package semvec.lsh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lsh.core.Point;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/*
 * Create point->corner output with User points and Item grids.
 * Receive one corner with a set of points.
 * If a corner has a User and an Item version, save it.
 */

/*
 * Output format of "1,2,3 id,double...double*U|...*I"
 */

public class UICornerReducer extends
		Reducer<Text, Text, Text, Text> {

	// TODO - what a pain!
	// and to unit test!
	// what limiter
	private boolean requireUser = true;
	private boolean requireItem = true;
	// what to save
	private boolean userCorners = true;
	private boolean itemCorners = true;
	private boolean userPoints = true;
	private boolean itemPoints = true;
	
	@Override
	public void reduce(Text key, Iterable<Text> values, Context context)
	throws IOException, InterruptedException {
		StringBuilder sb = new StringBuilder();
		String corner = key.toString();
		List<String> user = null;
		List<String> item = null;

		for(Text value: values) {
			String point = value.toString();
			if (point.charAt(point.length() -1) == 'U') {
				if (null == user) 
					user = new ArrayList<String>();
				user.add(point);
			} else if (point.charAt(point.length() -1) == 'I') {
					if (null == item) 
						item = new ArrayList<String>();
					item.add(point);
			} else {
				throw new InterruptedException("UICornerReduce: where are the User/Item markers?");
			}
		}
		if (null != user && user != item) {
			for(String point: user) {
				sb.append(point.toString());
				sb.append('|');
			}
			for(String point: item) {
				sb.append(point.toString());
				sb.append('|');
			}
		}
		sb.setLength(sb.length() - 1);
		String value = sb.toString();
		context.write(new Text(corner), new Text(value));
	}

}


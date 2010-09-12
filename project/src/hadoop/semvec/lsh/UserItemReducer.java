package semvec.lsh;

import java.io.IOException;

import lsh.core.Point;
import lsh.hadoop.LSHDriver;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/*
 * Assemble User and Item vectors for each id.
 */

/*
 * Receives: User/Item ID as key, UorI/dim/spot
 * Output: User/Item id,point,"U" or "I" as payload
 */

public class UserItemReducer extends
		Reducer<Text, Text, Text, Text> {
	int dimension = -1;
	
	protected void setup(org.apache.hadoop.mapreduce.Reducer<Text,Text,Text,Text>.Context context) throws IOException ,InterruptedException {
		Configuration conf = context.getConfiguration();
		String dim = conf.get(LSHDriver.DIMENSION);
		dimension = Integer.parseInt(dim);
	}
	
	@Override
	public void reduce(Text key, Iterable<Text> values, Context context)
	throws IOException, InterruptedException {
		double[] userPoints = null;
		double[] itemPoints = null;
		Long id = Long.parseLong(key.toString());
		for(Text value: values) {
			String[] parts = value.toString().split(" ");
			double[] vector;
			if (parts[0].equals("U")) {
				if (null == userPoints)
					userPoints  = new double[dimension];
				vector = userPoints;
			} else {
				if (null == itemPoints)
					itemPoints  = new double[dimension];
				vector = itemPoints;
			}
			int dim = Integer.parseInt(parts[1]);
			float spot = Float.parseFloat(parts[2]);
			vector[dim] = spot;
		}
		if (null != userPoints) {
			Point p = new Point(id.toString(), userPoints, "U");
			context.write(null, new Text(p.toString()));
		}
		if (null != itemPoints) {
			Point p = new Point(id.toString(), itemPoints, "I");
			context.write(null, new Text(p.toString()));
		}
	}

}


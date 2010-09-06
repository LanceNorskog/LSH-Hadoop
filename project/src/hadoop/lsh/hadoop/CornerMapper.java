package lsh.hadoop;

import java.io.IOException;
import java.util.Set;
import java.util.StringTokenizer;

import lsh.core.Corner;
import lsh.core.CornerGen;
import lsh.core.Hasher;
import lsh.core.Point;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/*
 * Tyler Neylon's Python example in his SODA 2010 paper.
 * N-dimensional orthogonal projection.
 * square/triangle slicing algorithm.
 * Order is ^dimensions.
 * 
 * Input files are:
 *   id,d0,d1,d2...dn
 *   no spaces
 *   
 * hadoop 0.20.0 API
 */

import lsh.hadoop.LSHDriver;

public class CornerMapper extends Mapper<Object, Text, Text, Text> {
	CornerGen cg;

	@Override
	protected void setup(
			org.apache.hadoop.mapreduce.Mapper<Object, Text, Text, Text>.Context context)
			throws IOException, InterruptedException {
		Configuration conf = context.getConfiguration();
		String hasherClass = conf.get(LSHDriver.HASHER);
		String gridsize = conf.get(LSHDriver.GRIDSIZE);

		try {
			
			Hasher hasher = (Hasher) Class.forName(hasherClass).newInstance();
			String parts[] = gridsize.split("[ ,]");
			double[] stretch = new double[parts.length];
			for(int i = 0; i < parts.length; i++) {
				stretch[i] = Double.parseDouble(parts[i]);
			}
			hasher.setStretch(stretch);
			cg = new CornerGen(hasher, stretch);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new InterruptedException(e.toString());
		}
	};

	public void map(Object key, Text value, Context context)
	throws IOException, InterruptedException {

		Point point = Point.newPoint(value.toString());
		Set<Corner> hashes = cg.getHashSet(point);
		for (Corner corner : hashes) {
			context.write(new Text(corner.toString()), new Text(point
					.toString()));
		}
	}
}

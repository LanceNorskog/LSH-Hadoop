package lsh.hadoop;

import java.io.IOException;
import java.util.Set;
import java.util.StringTokenizer;

import lsh.core.Corner;
import lsh.core.CornerGen;
import lsh.core.Point;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/*
 * Emit Point->Corner items
 */

public class PointMapper extends Mapper<Object, Text, Text, Text> {
	CornerGen cg;
	
	@Override
	protected void setup(
			org.apache.hadoop.mapreduce.Mapper<Object, Text, Text, Text>.Context context)
			throws IOException, InterruptedException {
		Configuration conf = context.getConfiguration();
		String hasher = conf.get(LSHDriver.HASHER);
		String gridsize = conf.get(LSHDriver.GRIDSIZE);

		try {
			cg = new CornerGen(hasher, gridsize);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new InterruptedException(e.toString());
		}
	};



	public void map(Object key, Text value, Context context)
			throws IOException, InterruptedException {
		StringTokenizer itr = new StringTokenizer(value.toString());

		while (itr.hasMoreTokens()) {
			Point point = Point.newPoint(itr.nextToken().toString());
			Set<Corner> hashes = cg.getHashSet(point);
			System.out.println("-------------");
			System.out.println(point.toString());
			for(Corner corner: hashes) {
				System.out.println("corner: " + corner.toString());
				context.write(new Text(point.toString()), new Text(corner.toString()));
			}
		}
	}
}

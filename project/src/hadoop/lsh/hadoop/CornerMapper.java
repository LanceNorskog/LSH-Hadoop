package lsh.hadoop;

import java.io.IOException;
import java.util.Set;
import java.util.StringTokenizer;

import lsh.core.Corner;
import lsh.core.CornerGen;
import lsh.core.Point;

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

public class CornerMapper extends Mapper<Object, Text, Text, Text> {
	CornerGen cg = new CornerGen();

	public void map(Object key, Text value, Context context)
			throws IOException, InterruptedException {
		StringTokenizer itr = new StringTokenizer(value.toString());

		while (itr.hasMoreTokens()) {
			Point point = Point.newPoint(itr.nextToken().toString());
			Set<Corner> hashes = cg.getHashSet(point);
			for(Corner corner: hashes) {
				context.write(new Text(corner.toString()), new Text(point.toString()));
			}
		}
	}
}

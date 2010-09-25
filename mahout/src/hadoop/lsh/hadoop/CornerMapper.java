package lsh.hadoop;

import java.io.IOException;
import java.util.Set;

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
	int minHash = Integer.MAX_VALUE;
	int maxHash = Integer.MIN_VALUE;	
	int minGeneratedHash = Integer.MAX_VALUE;
	int maxGeneratedHash = Integer.MIN_VALUE;


	@Override
	protected void setup(
			org.apache.hadoop.mapreduce.Mapper<Object, Text, Text, Text>.Context context)
	throws IOException, InterruptedException {
		Configuration conf = context.getConfiguration();
		String hasherClass = conf.get(LSHDriver.HASHER);
		String gridsize = conf.get(LSHDriver.GRIDSIZE);
		String dimSize = conf.get(LSHDriver.DIMENSION);
		String minValue = conf.get(LSHDriver.MINVALUE);
		String maxValue = conf.get(LSHDriver.MAXVALUE);

		try {
			
			Hasher hasher = (Hasher) Class.forName(hasherClass).newInstance();
			int dimensions;
			double[] stretch;
			double size = 1.0;
			if (null != dimSize) {
				dimensions = Integer.parseInt(dimSize);
				if (null != gridsize) {
					size = Double.parseDouble(gridsize);
				}
				stretch = new double[dimensions];
				for(int i = 0; i < stretch.length; i++) {
					stretch[i] = size;
				}
			} else if (null != gridsize) {
				String parts[] = gridsize.split("[ ,]");
				stretch = new double[parts.length];
				for(int i = 0; i < parts.length; i++) {
					stretch[i] = Double.parseDouble(parts[i]);
				}
				dimensions = parts.length;
			} else {
				throw new IOException("CornerMapper: Need dimension or gridsize parameters.");
			}
			hasher.setStretch(stretch);
			cg = new CornerGen(hasher, stretch);
			double[] limit = new double[dimensions];
			if (null != minValue || null != maxValue) {
				double d = Double.parseDouble(minValue);
				for(int i = 0; i < limit.length; i++)
					limit[i] = d;
				int[] hashed = cg.hasher.hash(limit);
				minHash = hashed[0];
				d = Double.parseDouble(maxValue);
				for(int i = 0; i < limit.length; i++)
					limit[i] = d;
				hashed = cg.hasher.hash(limit);
				maxHash = hashed[0];
				System.err.println("minhash: " + minHash + ", maxHash: " + maxHash);
			}
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
			int i = 0;
			for(; i < corner.hashes.length; i++) {
				if (corner.hashes[i] < minGeneratedHash)
					minGeneratedHash = corner.hashes[i];
				if (corner.hashes[i] > maxGeneratedHash)
					maxGeneratedHash = corner.hashes[i];
				if (corner.hashes[i] < minHash)
					break;
				if (corner.hashes[i] > maxHash)
					break;
			}
			if (i == corner.hashes.length) {
			context.write(new Text(corner.toString()), new Text(point
					.toString()));
			}
		}
		hashes.clear();
	}
	
	@Override
	protected void cleanup(org.apache.hadoop.mapreduce.Mapper<Object,Text,Text,Text>.Context context) throws IOException ,InterruptedException {
		System.err.println("REPORT: min generated hash:" + minGeneratedHash + ", max generated hash: " + maxGeneratedHash);
	};
	
}

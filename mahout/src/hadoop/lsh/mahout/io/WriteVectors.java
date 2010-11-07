package lsh.mahout.io;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Random;

import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;

import lsh.core.Lookup;
import lsh.core.Point;

public class WriteVectors {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		boolean doUser = false;
		boolean doItem = true;
		boolean doPoints = true;
		boolean doCorners  = false;
		boolean doName = false;
		int n = 0;
		boolean random = false;
		boolean csv = true;
		
		if (args[0].equals("-r")) {
			random = true;
			n++;
		}
		File input = new File(args[n]);
		File fOut = new File(args[n+1]);
		fOut.delete();
		DataOutput dout;
		OutputStream fOutStream = new FileOutputStream(fOut);
		Reader lshReader = new FileReader(input);
		dout = new DataOutputStream(fOutStream);
		if (random) {
			doRandom(dout, 1500, 150);
		} else if (csv) {
			doCSV(doUser, doPoints, doCorners, new PrintWriter(fOut), lshReader);
		}
		else {
		
			doPoints(doUser, doPoints, doCorners, dout, lshReader);
		} 
		fOutStream.flush();
		fOutStream.close();
	}

	private static void doCSV(boolean doUser, boolean doPoints,
			boolean doCorners, PrintWriter printWriter, Reader lshReader) throws IOException {
		Lookup box = new Lookup(doPoints, doCorners);
		if (doPoints) {
			box.loadPoints(lshReader, doUser ? "U" : "I");
		} else {
			box.loadCorners(lshReader, doUser ? "U" : "I");
		}
		for(Point p: box.points) {
			StringBuilder sb = new StringBuilder();
			double[] values = p.values;
			for(int i = 0; i < values.length; i++) {
				sb.append(values[i]);
				sb.append(',');
			}
			sb.setLength(sb.length() -1);
			printWriter.println(sb.toString());
		}

	}

	private static void doRandom(DataOutput dout, int rows, int dimensions) throws IOException {
		Random rnd = new Random(0);
		
		dout.writeInt(rows);
		for(int row = 0; row < rows; row++) {
			double values[] = new double[dimensions];
			for(int i = 0; i < dimensions; i++) {
				values[i] = normal(rnd);
			}
			Vector v = new DenseVector(values);
			VectorWritable vectorWritable = new VectorWritable(v);
		    vectorWritable.setWritesLaxPrecision(true);
		    vectorWritable.write(dout);
		}
	}
	
	static double normal(Random rnd) {
		return (rnd.nextDouble() + rnd.nextDouble() + rnd.nextDouble() + rnd.nextDouble() + rnd.nextDouble())/6.0;
	}

	private static void doPoints(boolean doUser, boolean doPoints,
			boolean doCorners,
			DataOutput dout, Reader lshReader) throws IOException {
		Lookup box = new Lookup(doPoints, doCorners);
		if (doPoints) {
			box.loadPoints(lshReader, doUser ? "U" : "I");
		} else {
			box.loadCorners(lshReader, doUser ? "U" : "I");
		}
		dout.writeInt(box.points.size());
		for(Point p: box.points) {
			Vector v = new DenseVector(p.values);
			VectorWritable vectorWritable = new VectorWritable(v);
		    vectorWritable.setWritesLaxPrecision(true);
		    vectorWritable.write(dout);
		}
	}
}

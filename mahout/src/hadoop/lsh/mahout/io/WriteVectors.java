package lsh.mahout.io;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.jasper.tagplugins.jstl.core.Out;
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
		
		File input = new File(args[n]);
		Reader lshReader = new FileReader(input);
		Lookup box = new Lookup(doPoints, doCorners);
		if (doPoints) {
			box.loadPoints(lshReader, doUser ? "U" : "I");
		} else {
			box.loadCorners(lshReader, doUser ? "U" : "I");
		}
		DataOutput dout;
		dout = new DataOutputStream(System.out);
		for(Point p: box.points) {
			Vector v = new DenseVector(p.values);
			VectorWritable vectorWritable = new VectorWritable(v);
		    vectorWritable.setWritesLaxPrecision(true);
		    vectorWritable.write(dout);
		}
	}
}

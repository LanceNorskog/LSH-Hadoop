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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.utils.vectors.io.SequenceFileVectorWriter;
import org.apache.mahout.utils.vectors.io.VectorWriter;

import org.apache.mahout.utils.vectors.io.VectorWriter;

import lsh.core.Lookup;
import lsh.core.Point;

/*
 * Read LSH format and write as CSV or Mahout vector file.
 * 
 * Usage:
 * 	-u do user values - default item
 *  -n add 'name'        - ID value
 * 	-m do Mahout vectors - default CSV, no header
 */

public class WriteVectors {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		boolean doUser = false;
		boolean doPoints = true;
		boolean doName = false;
		int n = 0;
		boolean csv = true;

		while(true) {
			if (args[n].equals("-c")) {
				doPoints = false;
				n++;
			} else if (args[n].equals("-p")) {
				doPoints = true;
				n++;
			} else if (args[n].equals("-u")) {
				doUser = true;
				n++;
			} else if (args[n].equals("-n")) {
				doName = true;
				n++;
			} else if (args[n].equals("-m")) {
				csv = false;
				n++;
			} else if (args[n].charAt(0) == '-') {
				throw new Exception("don't know options: " + args[n]);
			} else
				break;
		}
		File input = new File(args[n]);
		String outputFile = args[n+1];
		File fOut = new File(outputFile);
		fOut.delete();
		Reader lshReader = new FileReader(input);
		if (csv) {
			fOut.delete();
			DataOutput dout;
			OutputStream fOutStream = new FileOutputStream(fOut);
			dout = new DataOutputStream(fOutStream);
			doCSV(doUser, doPoints, new PrintWriter(fOut), lshReader);
			fOutStream.flush();
			fOutStream.close();
		}
		else {
			doMahout(doUser, doPoints, outputFile, lshReader);
		} 
	}

	private static void doCSV(boolean doUser, boolean doPoints,
			PrintWriter printWriter, Reader lshReader) throws IOException {
		Lookup box = new Lookup(doPoints, !doPoints);
		if (doPoints) {
			box.loadPoints(lshReader, doUser ? "U" : "I");
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
		} else {
			box.loadCorners(lshReader, doUser ? "U" : "I");
		}

	}

	private static void doMahout(boolean doUser, boolean doPoints,
			String outFile, Reader lshReader) throws IOException {
		Lookup box = new Lookup(doPoints, !doPoints);
		VectorWriter vWriter = getSeqFileWriter(outFile);
		if (doPoints) {
			box.loadPoints(lshReader, doUser ? "U" : "I");
			List<Vector> one = new ArrayList<Vector>();
			one.add(new DenseVector());
			long sequence = 0;
			for(Point p: box.points) {
				Vector v = new DenseVector(p.values);
				one.set(0, v);
				sequence = vWriter.write(one);
			}
			vWriter.close();
		} else {
			box.loadCorners(lshReader, doUser ? "U" : "I");
		}
	}
	
	  private static VectorWriter getSeqFileWriter(String outFile) throws IOException {
		    Path path = new Path(outFile);
		    Configuration conf = new Configuration();
		    FileSystem fs = FileSystem.get(conf);
		    SequenceFile.Writer seqWriter = SequenceFile.createWriter(fs, conf, path, LongWritable.class,
		      VectorWritable.class);
		    return new SequenceFileVectorWriter(seqWriter);
		  }
		  

}

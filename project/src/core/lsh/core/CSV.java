package lsh.core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class CSV {

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
		StringBuilder sb = new StringBuilder();
		for(Point p: box.points) {
			if (doName) {
				sb.append(p.id);
				sb.append(',');
			}
			for(int i = 0; i < p.values.length; i++) {
				sb.append(Double.toString(p.values[i]));
				sb.append(',');
			}
			sb.setLength(sb.length() - 1);
			System.out.println(sb);
			sb.setLength(0);
		}
	}
}

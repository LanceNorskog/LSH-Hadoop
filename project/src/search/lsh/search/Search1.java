package lsh.search;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import lsh.core.Corner;
import lsh.core.Hasher;
import lsh.core.Lookup;
import lsh.core.OrthonormalHasher;
import lsh.core.Point;

/*
 * Search 
 * Usage: file or directory
 */
public class Search1 {
	final Lookup lookup;
	
	public Search1(Lookup lookup) {
		this.lookup = lookup;
	}
	
	int getNearestPoints(Point p, Set<Point> points) {
		int sum = 0;
		
		// check lower left, upper right
		Corner corner = getCorner(p);
		int[] hashes = corner.hashes.clone();
		Set<Point> sp = lookup.corner2points.get(corner);
		if (null != sp) {
			points.addAll(sp);
			sum += sp.size();
		}
		// upper right
		corner.hashes[0]++;
		corner.hashes[1]++;
		sp = lookup.corner2points.get(corner);
		if (null != sp) {
			points.addAll(sp);
			sum += sp.size();
		}
		// choose lower right or lower left
		if (upperTriangle(lookup.hasher, p.values, hashes))
			corner.hashes[0]--;
		else
			corner.hashes[1]--;
		sp = lookup.corner2points.get(corner);
		if (null != sp) {
			points.addAll(sp);
			sum += sp.size();
		}
		return sum;
	}

	// is the point in the upper-right triangle or the lower-left triangle
	// ask Tyler for the right way to do this!
	private boolean upperTriangle(Hasher hasher, double[] p, int[] hashes) {
//		double[] ll = new double[hashes.length];
//		hasher.unhash(hashes, ll);
		hashes[0]++;
		double[] lr = new double[hashes.length];
		hasher.unhash(hashes, lr);
		hashes[1]++;
//		double[] ur = new double[hashes.length];
//		hasher.unhash(hashes, ur);
		hashes[0]--;
		double[] ul = new double[hashes.length];
		hasher.unhash(hashes, ul);
		
		double lengthlr = distance(lr, p);
		double lengthul = distance(ul, p);
		return lengthul < lengthlr;
	}

	private double distance(double[] lr, double[] p) {
		double sum = 0;
		for(int i = 0; i < lr.length; i++ ) {
			sum += ((lr[i] - p[i]) * (lr[i] - p[i]));
		}
		return Math.sqrt(sum);
	}

	public Corner getCorner(Point p) {
		int[] grid = lookup.hasher.hash(p.values);
		Corner corner = new Corner(grid);
		return corner;
	}
	
	/*
	 * Usage: file of corner->point
	 */

	public static void main(String[] args) throws IOException {
		Hasher h = new OrthonormalHasher(2, 1d);
		Lookup l = new Lookup(h, true, true, true, true, true, true);
		Reader fr = new FileReader(args[0]);
		l.loadCP(fr, null);
		String g32 = "fetch";
		double[] d32 = new double[2];
		if (args.length == 3) {
			d32[0] = Double.parseDouble(args[1]);
			d32[1] = Double.parseDouble(args[2]);
			findNeighbors(l, g32, d32);
		} else while (true) {
			System.out.println("Type X Y");
			byte[] ba = new byte[1000];
			int bytes = System.in.read(ba);
			String s = new String(ba, 0, bytes);
			String[] numbers = s.split(" ");
			
			try {
				d32[0] = Double.parseDouble(numbers[0]);
				d32[1] = Double.parseDouble(numbers[1]);
			} catch (Throwable t) {
				System.out.println("Bad numbers");
				continue;
			}
			findNeighbors(l, g32, d32);
		}
	}

	private static void findNeighbors(Lookup l, String g32, double[] d32) {
		{
		Point p32 = new Point(g32, d32, null);
		Search1 sr1 = new Search1(l);
		Set<Point> points = new HashSet<Point>();
		int n = sr1.getNearestPoints(p32, points);
		if (n == 0)
			System.out.println("No neighbors for: " + p32);
		else {
			System.out.println("Neighbors for: " + p32);
			for(Point p: points) {
				System.out.println(p.toString());
			}
		}
		}
	}
}

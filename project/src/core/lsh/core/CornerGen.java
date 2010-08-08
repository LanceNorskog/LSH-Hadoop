package lsh.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * Tyler Neylon's Python example in his SODA 2010 paper.
 * N-dimensional orthogonal projection.
 * square/triangle slicing algorithm.
 * Order is ^dimensions.
 * 
 * Input files are:
 *   id,d0,d1,d2...dn
 *   no spaces
 */


/*
 * Guts of hash code set generator.
 * 
 * Generate set of Corners from input point and given hasher.
 */

public class CornerGen {
	public final double[] stretch;
	public final Hasher hasher;
	
	public CornerGen() {
		stretch = new double[2];
		stretch[0] = 1.0d;
		stretch[1] = 1.0d;
		hasher = new OrthonormalHasher(this.stretch);
	}

	public CornerGen(String hashClass, String gridsize) throws Exception {
		try {
			hasher = (Hasher) Class.forName(hashClass).newInstance();
		} catch (Exception e) {
			System.err.println("Bogus classname: " + hashClass);
			throw e;
		}
		String[] parts = gridsize.split("[ ,]");
		stretch = new double[parts.length];
		for(int i = 0; i < parts.length; i++) {
			stretch[i] = Double.parseDouble(parts[i]);
		}
		hasher.setStretch(stretch);
	}

	public Set<Corner> getHashSet(Point point) {
		Set<Corner> corners = new HashSet<Corner>();
		int hash[] = hasher.hash(point.values);
		corners.add(new Corner(hash.clone()));
		double remainder[] = new double[hash.length];
		double unhashed[] = new double[hash.length];
		hasher.unhash(hash, unhashed);
		for(int i = 0; i < hash.length; i++) {
			remainder[i] = point.values[i] - unhashed[i]; 
		}
		int permuted[] = permute(remainder);
		for(int dim = 0; dim < permuted.length; dim++) {
			hash[permuted[dim]]++;
//			hash[permuted[dim]] = hasher.add(hash[permuted[dim]], 1);
//			hash[permuted[dim]] = hash[permuted[dim]]++;
			corners.add(new Corner(hash.clone()));
		}
		return corners;
	}

	/*
	 * return 0-n sorted by hash[0-n], then reversed
	 */
	int[] permute(double[] remainder) {
		Pair[] pairs = new Pair[remainder.length];
		for(int dim = 0; dim < remainder.length; dim++) {
			pairs[dim] = new Pair(remainder[dim], dim);
		}
		Arrays.sort(pairs, new Reverse());
		int[] permuted = new int[remainder.length];
		for(int dim = 0; dim < remainder.length; dim++) {
			permuted[dim] = pairs[dim].order;
		}
		return permuted;
	}
	
	public double[] backproject(Corner corner) {
		double[] inverse = new double[stretch.length];
		for(int i = 0; i < stretch.length; i++) {
			inverse[i] = corner.hashes[i] * stretch[i];
		}
		return inverse;
	}
	
	static public void main(String[] args) {
		List<Pair> points = new ArrayList<Pair>();
		points.add(new Pair(1.1,3));
		points.add(new Pair(2.2,0));
		points.add(new Pair(4.6,5));
		Collections.sort(points, new Reverse());
		points.hashCode();
		CornerGen cg = new CornerGen();
		double point[] = {1.1, 0.3};
		Set<Corner> corners = cg.getHashSet(new Point("one", point));
		corners.hashCode();
	}

}

class Pair {
	double value = 0;
	int order = 0;

	public Pair(double value, int order) {
		this.value = value;
		this.order = order;
	}
	
	public String toString() {
		return value + " x " + order;
	}
}

/* Sort by remainder value in reverse */
class Reverse implements Comparator<Pair> {

	@Override
	public int compare(Pair p1, Pair p2) {
		if (p1.value < p2.value)
			return 1;
		else if (p1.value > p2.value)
			return -1;
		else
			return 0;
	}

}

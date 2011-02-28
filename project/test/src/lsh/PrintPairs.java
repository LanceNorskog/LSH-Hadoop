package lsh;

import lsh.core.Hasher;
import lsh.core.OrthonormalHasher;
import lsh.core.VertexTransitiveHasher;

public class PrintPairs {
	static double[] STRETCH = {0.999, 0.9999};

	static public void main(String args[]) {
		Hasher hasher = new OrthonormalHasher();
		hasher.setStretch(STRETCH);
		for(int i = 0; i < 5; i++)
			for(int j = 0; j < 5; j++) {
//				for(int k = 0; k < 5; k++) {
					double[] d2 = new double[2];
					d2[0] = i*0.27;
					d2[1] = j*0.67;
//					d2[2] = k;
					printcsv(hasher, d2);
//				}
			}
	}

	static double manhattan(int[] a, int[] b) {
		double sum = 0;
		for(int i = 0; i < a.length; i++) {
			sum += Math.abs(a[i] - b[i]);
		}
		return sum;
	}


	private static void print(Hasher hasher, double[] p) {
		double[] zeroP = {0,0,0};
		int[] zero = hasher.hash(zeroP);
		int[] hash = hasher.hash(p);
		System.out.println("(" + p[0] + "," + p[1] + "," + p[2] + ")" + " -> " + 
				"(" + hash[0] + "," + hash[1] + "," + hash[2] + ") dist: " + (manhattan(zero, hash)/3.0));
	}

	private static void printcsv(Hasher hasher, double[] p) {
		double[] zeroP = {0,0};
		int[] zero = hasher.hash(zeroP);
		int[] hash = hasher.hash(p);
		System.out.println(p[0] + "," + p[1] + "," + hash[0] + "," + hash[1] + "," + (manhattan(zero, hash)/2.0));
	}
}

package lsh;

import lsh.core.Hasher;
import lsh.core.OrthonormalHasher;
import lsh.core.VertexTransitiveHasher;

public class PrintPairs {
    static double[] STRETCH = {0.999, 0.9999};

    static public void main(String args[]) {
	Hasher hasher = new VertexTransitiveHasher();
	hasher.setStretch(STRETCH);
	for(int i = 0; i < 2; i++)
	    for(int j = 0; j < 2; j++) {
		int[] hash = new int[2];
		hash[0] = i;
		hash[1] = j;
		printcsv(hasher, hash);
	    }
    }

//    private static void print(Hasher hasher, double[] p) {
//	double[] zeroP = {0,0,0};
//	int[] zero = hasher.hash(zeroP);
//	int[] hash = hasher.hash(p);
//	System.out.println("(" + p[0] + "," + p[1] + "," + p[2] + ")" + " -> " + 
//		"(" + hash[0] + "," + hash[1] + "," + hash[2] + ") dist: " + (manhattan(zero, hash)/3.0));
//    }

    private static void printcsv(Hasher hasher, int[] hash) {
	double[] zeroP = {0,0};
	hasher.unhash(hash, zeroP);
	System.out.println(hash[0] + "," + hash[1] + "," + zeroP[0] + "," + zeroP[1]);
//	System.out.println("(" + hash[0] + "," + hash[1] + ") -> (" + zeroP[0] + "," + zeroP[1] + ")");
}
}

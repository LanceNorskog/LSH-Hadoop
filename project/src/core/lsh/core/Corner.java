package lsh.core;

import java.util.Iterator;

/*
 * Corner containing grid hashes and associated points
 * 
 * Native String representation- 0,1,2,3,...,n
 * 
 * Beginning to think this should be a static class and just toss int[] around?
 */

public class Corner {
	public final int[] hashes;
	
	public Corner(int[] corner) {
		this.hashes = corner;
	}
	
	@Override
	public boolean equals(Object other) {
		int[] ohash = ((Corner) other).hashes;
		for(int i = 0; i < hashes.length; i++) {
			if (hashes[i] != ohash[i])
				return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		int sum = 0;
		for(int i = 0; i < hashes.length; i++) {
			sum += hashes[i]*i;
		}
		return sum;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < hashes.length; i++) {
			sb.append(hashes[i]);
			sb.append(',');
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}
	
	public static Corner newCorner(String blob) {
		String[] parts = blob.split(",");
		
		int[] hashes = new int[parts.length];
		for(int i = 0; i < parts.length; i++) {
			hashes[i] = Integer.parseInt(parts[i]);
		}
		return new Corner(hashes);
	}
}


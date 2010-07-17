package lsh.core;

/*
 * Corner containing grid hashes and associated points
 * 
 * Native String representation- 0,1,2,3,...,n
 */

public class Corner {
	final int[] hashes;
	
	public Corner(int[] corner) {
		this.hashes = corner;
	}
	
	@Override
	public boolean equals(Object other) {
		return hashes.equals(((Corner) other).hashes);
	}
	
	@Override
	public int hashCode() {
		return hashes.hashCode();
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

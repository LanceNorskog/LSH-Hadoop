package lsh.core;

/*
 * Corner containing grid hashes and associated points
 * 
 * Native String representation- [id],0,1,2,3,...,n[*payload]
 * 
 * Can be without points
 */

public class Corner {
	public final String id;
	// can be null for "runt" corners
	public final int[] hashes;
	public final String payload;

	public Corner(int[] corner) {
		this.hashes = corner;
		this.id = null;
		this.payload = null;
	}
	
	public Corner(int[] corner, String id, String payload) {
		this.hashes = corner;
		this.id = id;
		this.payload = payload;
	}
	
	@Override
	public boolean equals(Object other) {
		Corner co = (Corner) other;
		if (null != id && null != co.id)
			return id.equals(co.id);
		int[] ohash = co.hashes;
		for(int i = 0; i < hashes.length; i++) {
			if (hashes[i] != ohash[i])
				return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		if (null != id)
			return id.hashCode();
		int sum = 0;
		for(int i = 0; i < hashes.length; i++) {
			sum += hashes[i]*i;
		}
		return sum;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (null != id)
			sb.append(id);
		if (null != hashes)
			for(int i = 0; i < hashes.length; i++) {
				sb.append(',');
				sb.append(hashes[i]);
			}
		if (null != payload)
			sb.append('*' + payload);
		return sb.toString();
	}
	
	public static Corner newCorner(String blob) {
		return newCorner(blob, false);
	}
	
	public static Corner newCorner(String blob, boolean runt) {
		Point p = Point.newPoint(blob, runt);
		int[] hashes = null;
		if (! runt) {
			hashes = new int[p.values.length];
			for(int i = 0; i < p.values.length; i++) {
				hashes[i] = (int) p.values[i];
			}
		}
		return new Corner(hashes, p.id, p.payload);
	}
}


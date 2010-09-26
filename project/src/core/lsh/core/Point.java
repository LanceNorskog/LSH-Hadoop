package lsh.core;

/*
 * ID,vector
 * String representation: id,v0,v1,...vn
 * ID is identity - there can be 2 points with same position
 * payload is random junk helps with map/reduce- really M/R should help
 */

public class Point {
	private static final String SPLIT = ",";
	private static final String MARKER = "*";
	private static final String MARKER_ESC = "\\*";
	public final String id;
	public final double[] values;
	public final String payload;
	
	
//	public Point(String id, int size) {
//		this.id = id;
//		values = new double[size];
//	}
//	
	public Point(String id, double[] values, String payload) {
		this.id = id;
		this.values = values;
		this.payload = payload;
	}
		
	// There's some interface for this
	public static Point newPoint(String line) {
		String[] full = line.split(MARKER_ESC);
		
		String[] parts = full[0].split(SPLIT);
		double[] values = new double[parts.length - 1];
		for(int i = 0; i < parts.length - 1; i++) {
			values[i] = Double.parseDouble(parts[i + 1]);
		}	
		return new Point(parts[0], values, full[1]);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(id);
		for(int i = 0; i < values.length; i++) {
			sb.append(',');
			sb.append(values[i]);
		}
		if (null != payload) {
			sb.append(MARKER);
			sb.append(payload);
		}
		return sb.toString();
	}
	
	// assumes other point is in same ID space as this
	// blows up with separate user/item spaces
	
	@Override
	public boolean equals(Object other) {
		// ignore vector & payload
		return id.equals(((Point)other).id);
	}
	
	@Override
	public int hashCode() {
		// ignore vector & payload
		return id.hashCode();
	}

}

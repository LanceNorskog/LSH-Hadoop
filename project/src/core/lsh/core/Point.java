package lsh.core;

/*
 * ID,vector
 * String representation: id,v0,v1,...vn
 * ID is identity - there can be 2 points with same position
 */

public class Point {
	public final String id;
	public final double[] values;
	
	
	public Point(String id, int size) {
		this.id = id;
		values = new double[size];
	}
	
	public Point(String id, double[] values) {
		this.id = id;
		this.values = values;
	}
		
	// There's some interface for this
	public static Point newPoint(String line) {
		String[] parts = line.split(",");
		double[] values = new double[parts.length - 1];
		for(int i = 0; i < parts.length - 1; i++) {
			values[i] = Double.parseDouble(parts[i + 1]);
		}	
		return new Point(parts[0], values);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(id);
		for(int i = 0; i < values.length; i++) {
			sb.append(',');
			sb.append(values[i]);
		}
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		return id.equals(((Point)other).id);
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}

}

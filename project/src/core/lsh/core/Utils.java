package lsh.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Various utilities to load point_corner and corner_point formats
 * and simple point and corners formats
 */

public class Utils {

	// very important - assumes multiple points per corner
	// id2corner means point.id to corner
	static public int load_corner_points_format(Reader r, String payload1, Lookup l1, String payload2, Lookup l2) throws IOException {
		BufferedReader lnr = new BufferedReader(r);
		String line;
		int lines = 0;
		int dimensions = -1;
//		System.err.println("Loading corners... ");
		while (null != (line = lnr.readLine())) {
			String parts[] = line.split("[ \t]");
			String[] pipes = new String[0];
			if (parts.length > 1)
				pipes = parts[1].split("\\|");
			String spec = parts[0];
			Corner corner = Corner.newCorner(spec);
			if (null != l1.corners)
				l1.corners.add(corner);
			if (null != l2 && null != l2.corners)
				l2.corners.add(corner);
			for(int i = 0; i < pipes.length; i++) {
				Point p = Point.newPoint(pipes[i]);
				dimensions = p.values.length;
				// I cannot believe it. String returns false for a null. 
				if (null == payload1 || p.payload.equals(payload1)) {
					addPair(l1.ids, l1.points, l1.corners, l1.id2point, l1.id2corner, l1.corner2ids, l1.corner2points, l1.point2corners, corner, p);
				}
				if (null != payload2 && p.payload.equals(payload2)) {
					addPair(l2.ids, l2.points, l2.corners, l2.id2point, l2.id2corner, l2.corner2ids, l2.corner2points, l2.point2corners, corner, p);
				}
			}
			lines++;
//			if (lines % 1000 == 0) {
//				System.err.println("\t" + lines);
//			}
		}	
		return dimensions;
	}

	// very important - assumes multiple points per corner
	// id2corner means point.id to corner
	static public int load_point_corners_format(Reader r, String payload1, Lookup l1, String payload2, Lookup l2) throws IOException {
		BufferedReader lnr = new BufferedReader(r);
		int dimensions = -1;

		String line;
		while (null != (line = lnr.readLine())) {
			String parts[] = line.split("[ \t]");
			String[] pipes = parts[1].split("\\|");
			Point point = Point.newPoint(parts[0]);
			dimensions = point.values.length;
			if (!payload1.equals(point.payload)) {
				addPoint(l1.ids, l1.points, l1.id2point, point);
				for(int i = 0; i < pipes.length; i++) {
					Corner corner = Corner.newCorner(pipes[i]);
					addPair(l1.ids, l1.points, l1.corners, l1.id2point, l1.id2corner, l1.corner2ids, l1.corner2points, l1.point2corners, corner, point);
				}
			} else if (payload2.equals(point.payload)) {
				addPoint(l2.ids, l2.points, l2.id2point, point);
				for(int i = 0; i < pipes.length; i++) {
					Corner corner = Corner.newCorner(pipes[i]);
					addPair(l2.ids, l2.points, l2.corners, l2.id2point, l2.id2corner, l2.corner2ids, l2.corner2points, l2.point2corners, corner, point);
				}
			}
		}
		return dimensions;
	}

	// read point file, or point -> corner file
	static public int load_point(Reader r, Set<Point> points, Set<String> ids,
			Map<String, Point> id2point, String payload) throws IOException {
		BufferedReader lnr = new BufferedReader(r);
		int dimensions = -1;

		String line;
		while (null != (line = lnr.readLine())) {
			String[] parts = line.split("[ \t]");
			Point point = Point.newPoint(parts[0]);
			dimensions = point.values.length;
			if (null != payload ) { 
				if (null == point.payload || !payload.equals(point.payload)) {
					continue;
				}
			}
			addPoint(ids, points, id2point, point);
		}	
		return dimensions;
	}

	static public void addPair(Set<String> ids, Set<Point> points, Set<Corner> corners, 
			Map<String, Point> id2point, Map<String, Corner> id2corner, Map<Corner, Set<String>> corner2ids, Map<Corner, Set<Point>> corner2points,
			Map<Point, Set<Corner>> point2corners, Corner corner, Point point) {
		if (null != ids)
			ids.add(point.id);
		if (null != points)
			points.add(point);
		if (null != corners)
			corners.add(corner);
		if (null != id2point)
			id2point.put(point.id, point);
		if (null != id2corner) 
			id2corner.put(point.id, corner);
		if (null != corner2ids) {
			Set<String> bag = corner2ids.get(corner);
			if (null == bag) {
				bag = new HashSet<String>();
				corner2ids.put(corner, bag);
			}
			bag.add(point.id);
		}
		if (null != corner2points) {
			Set<Point> bag = corner2points.get(corner);
			if (null == bag) {
				bag = new HashSet<Point>();
				corner2points.put(corner, bag);
			}
			bag.add(point);
		}
		if (null != point2corners) {
			Set<Corner> bag = point2corners.get(corner);
			if (null == bag) {
				bag = new HashSet<Corner>();
				point2corners.put(point, bag);
			}
			bag.add(corner);			
		}
	}

	static public void addPoint(Set<String> ids, Set<Point> points, Map<String, Point> id2point, 
			Point point) {
		if (null != ids)
			ids.add(point.id);
		if (null != points)
			points.add(point);
		if (null != id2point)
			id2point.put(point.id, point);
	}
	
	
	// read corners, or corner part of a corner -> points file
	public static int load_corner(Reader r, Set<Corner> corners,
			Set<String> ids, Map<String, Corner> id2corner, String payload) throws IOException {
	    int dimensions = -1;
		BufferedReader lnr = new BufferedReader(r);

		String line;
		while (null != (line = lnr.readLine())) {
			String[] parts = line.split("[ \t]");
			Corner corner = Corner.newCorner(parts[0], parts.length > 1 ? parts[1] : null);
			dimensions = corner.hashes.length;
			if (null != payload ) { 
				if (null == corner.payload || !payload.equals(corner.payload)) {
					continue;
				}
			}
			addCorner(ids, corners, id2corner, corner);
		}	
		return dimensions;
	}

	private static void addCorner(Set<String> ids, Set<Corner> corners,
			Map<String, Corner> id2corner, Corner corner) {
		if (null != ids)
			ids.add(corner.id);
		if (null != corners)
			corners.add(corner);
		if (null != id2corner)
			id2corner.put(corner.id, corner);
		
	}

}

package lsh.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Various utilities
 */

public class Utils {

	// load in various data structures from the grid->points format
	// all structures are optional
	// slower than could be but uses hashed everything

	static public void load_corner_points_format(Reader r, Set<Point> points, Set<Corner> corners, Set<String> ids, 
			Map<String, Point> id2point, Map<Corner, Set<String>> corner2ids, Map<Corner, Set<Point>> corner2points, Map<Point, Set<Corner>> point2corners, 
			String payload) throws IOException {
		BufferedReader lnr = new BufferedReader(r);
		String line;
		int lines = 0;
		System.err.println("Loading corners... ");
		while (null != (line = lnr.readLine())) {
			String parts[] = line.split("[ \t]");
			String[] pipes = parts[1].split("\\|");
			String id = parts[0];
			Corner corner = Corner.newCorner(id);
			if (null != corners)
				corners.add(corner);
			for(int i = 0; i < pipes.length; i++) {
				Point point = Point.newPoint(pipes[i]);
				if (null == payload || (null != point.payload) && payload.equals(point.payload)) {
					addPair(ids, points, corners, id2point, corner2ids, corner2points, point2corners, corner, point);
				} else {
					payload.hashCode();
				}
			}
			lines++;
			if (lines % 1000 == 0) {
				System.err.println("\t" + lines);
			}
		}		
	}
	// load in various data structures from the point->corners format
	// all structures are optional
	// slower than could be but uses hashed everything

	static public void load_point_corners_format(Reader r, Set<Point> points, Set<Corner> corners, Set<String> ids, 
			Map<String, Point> id2point, Map<Corner, Set<String>> corner2ids, Map<Corner, Set<Point>> corner2points, Map<Point, Set<Corner>> point2corners, 
			String payload) throws IOException {
//		LineNumberReader lnr = new LineNumberReader(r);
		BufferedReader lnr = new BufferedReader(r);

		String line;
		while (null != (line = lnr.readLine())) {
			String parts[] = line.split("[ \t]");
			String[] pipes = parts[1].split("\\|");
			Point point = Point.newPoint(parts[0]);
			if (null != payload ) { 
				if (null == point.payload || !payload.equals(point.payload)) {
					payload.hashCode();
					continue;
				}
			}
			if (null != ids)
				ids.add(point.id);
			if (null != points)
				points.add(point);
			if (null != id2point) {
				id2point.put(point.id, point);
			}
			for(int i = 0; i < pipes.length; i++) {
				Corner corner = Corner.newCorner(pipes[i]);
				addPair(ids, points, corners, id2point, corner2ids, corner2points, point2corners, corner, point);
			}
		}		
	}

	static public void addPair(Set<String> ids, Set<Point> points, Set<Corner> corners, Map<String, Point> id2point, Map<Corner, Set<String>> corner2ids, Map<Corner, Set<Point>> corner2points,
			Map<Point, Set<Corner>> point2corners, Corner corner, Point point) {
		if (null != ids)
			ids.add(point.id);
		if (null != points)
			points.add(point);
		if (null != corners)
			corners.add(corner);
		if (null != id2point)
			id2point.put(point.id, point);
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

}

package lsh.core;

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

	static public void load_corner_points_format(Reader r, Set<Point> points, Set<Corner> corners, Set<String> ids, Map<String, Point> id2point, Map<Corner, Set<Point>> corner2points, Map<Point, Set<Corner>> point2corners) throws IOException {
		LineNumberReader lnr = new LineNumberReader(r);
		String line;
		while (null != (line = lnr.readLine())) {
			String parts[] = line.split("[ \t]");
			String[] pipes = parts[1].split("\\|");
			String id = parts[0];
			Corner corner = Corner.newCorner(id);
			if (null != corners)
				corners.add(corner);
			for(int i = 0; i < pipes.length; i++) {
				Point point = Point.newPoint(pipes[i]);
				addPair(ids, points, corners, id2point, corner2points, point2corners, corner, point);
			}
		}		
	}
	// load in various data structures from the point->corners format
	// all structures are optional
	// slower than could be but uses hashed everything

	static public void load_point_corners_format(Reader r, Set<Point> points, Set<Corner> corners, Set<String> ids, Map<String, Point> id2points, Map<Corner, Set<Point>> corner2points, Map<Point, Set<Corner>> point2corners) throws IOException {
		LineNumberReader lnr = new LineNumberReader(r);
		String line;
		while (null != (line = lnr.readLine())) {
			String parts[] = line.split("[ \t]");
			String[] pipes = parts[1].split("\\|");
			Point point = Point.newPoint(parts[0]);
			if (null != ids)
				ids.add(point.id);
			if (null != points)
				points.add(point);
			if (null != id2points) {
				id2points.put(point.id, point);
			}
			for(int i = 0; i < pipes.length; i++) {
				Corner corner = Corner.newCorner(pipes[i]);
				addPair(ids, points, corners, id2points, corner2points, point2corners, corner, point);
			}
		}		
	}

	static public void addPair(Set<String> ids, Set<Point> points, Set<Corner> corners, Map<String, Point> id2points, Map<Corner, Set<Point>> corner2points,
			Map<Point, Set<Corner>> point2corners, Corner corner, Point point) {
		if (null != ids)
			ids.add(point.id);
		if (null != points)
			points.add(point);
		if (null != corners)
			corners.add(corner);
		if (null != id2points)
			id2points.put(point.id, point);
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

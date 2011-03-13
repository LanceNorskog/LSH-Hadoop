package lsh.core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Holds a dataset in either corner->points or point->corners
 * formats.
 * 
 * Loose Collection-based implementation- does not scale
 */

public class Lookup {
    int dimensions = -1;
	final public Hasher hasher;
	final public Set<Point> points;
	final public Set<Corner> corners;
	final public Set<String> ids;
	final public Map<String,Point> id2point;
	// point.id -> corner
	final public Map<String,Corner> id2corner;
	final public Map<Corner,Set<String>> corner2ids;
	final public Map<Corner, Set<Point>> corner2points;
	final public HashMap<Point, Set<Corner>> point2corners;
	
	public Lookup(Hasher hasher, boolean doPoints, boolean doCorners, boolean doIds, boolean doId2point, 
			boolean doId2corner, boolean doCorner2Ids, boolean doCorner2points, boolean doPoint2corners) {
		this.hasher = hasher;
		points = doPoints ? new HashSet<Point>() : null;
		corners = doCorners ? new HashSet<Corner>() : null;
		id2point = doId2point ? new HashMap<String,Point>() : null;
		id2corner = doId2corner ? new HashMap<String,Corner>() : null;
		corner2ids = doCorner2Ids ? new HashMap<Corner,Set<String>>() : null;
		ids = doIds ? new HashSet<String>() : null;
		corner2points = doCorner2points ? new HashMap<Corner, Set<Point>>() : null;
		point2corners = doPoint2corners ? new HashMap<Point, Set<Corner>>() : null;
	}

	public Lookup(boolean doPoints, boolean doCorners) {
		this(null, doPoints, doCorners, false, false, false, false, false, false);
	}

	public void loadCP(Reader r, String payload) throws IOException {
		dimensions = Utils.load_corner_points_format(r, payload, this, null, null);
	}

	public void loadPC(Reader r, String payload) throws IOException {
		dimensions = Utils.load_point_corners_format(r, payload, this, null, null);
	}

	public void loadPoints(Reader r, String payload) throws IOException {
		dimensions = Utils.load_point(r, points, ids, id2point, payload);
	}

	// untested - ids are corner.id, not point.id?
	public void loadCorners(Reader r, String payload) throws IOException {
		dimensions= Utils.load_corner(r, corners, ids, id2corner, payload);
	}

	private Collection<Corner> getMatchingCorners(String id) {
		Set<Corner> found = new HashSet<Corner>();
		for(Corner corner: corners) {
			if (corner2points.containsKey(corner)) {
				for(Point point: corner2points.get(corner)) {
					if (point.id.equals(id)) {
						if (! found.contains(corner))
							found.add(corner);
					}
				}
			}
		}
		return found;
	}

	/**
	 * Usage: directory/file hasher N boxsize id
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String path = args[0];
		File svg = new File(path);
		Reader r = new FileReader(svg);

		
//		int dim = Integer.parseInt(args[1]);
//		double gridsize = Double.parseDouble(args[2]);

		Lookup lookup = new Lookup(null, true, true, true, true, true, true, true, true);
		lookup.loadCP(r, null);
		if (args.length == 5) {
			Collection<Corner> corners = lookup.getMatchingCorners(args[4]);
			printPoints(corners, args[4]);
		} else while (true){
			System.out.print("Point id: ");
			byte[] bytes = new byte[1024];
			int len = System.in.read(bytes);
			if (len > 0) {
				String id = new String(bytes, "UTF-8");
				Collection<Corner> corners = lookup.getMatchingCorners(args[4]);
				printPoints(corners, id);				
			} else {
				break;
			}
		}
	}

	private static void printPoints(Collection<Corner> corners, String id) {
		System.out.println("Searching for id: " + id);
		System.out.println("# of corners: " + corners.size());
		for(Corner corner: corners) {
			System.out.print("\t" + corner.hashes[0]);
			for(int i = 1; i < corner.hashes.length; i++) {
				System.out.print(",");
				System.out.print(corner.hashes[i]);
			}
			System.out.println();
		}
	}

	public int getDimensions() {
	    return dimensions;
	}
}

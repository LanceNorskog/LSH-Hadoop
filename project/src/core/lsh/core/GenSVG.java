package lsh.core;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * SVG generator for Orthonormal projection output.
 * 
 * Painful, but finally understandable.
 * 
 * 3 layers:
 *    text labels: original screen space, upside doen
 *    hash value space: flipped/scaled/translated
 *    point value space: flipped/scaled/translated
 */

public class GenSVG {
	// outer frame
	int X = 300;
	int Y = 300;
	int buffer = 40; 
	int inner = X - (buffer * 2);
	String thick = " stroke-width='0.05%' ";
	String thin = " stroke-width='0.01%' ";
	String scrawny = " stroke-width='0.005%' ";
	Hasher hasher = null;

	void makeSVG(Reader r, Writer w) throws IOException {		                     
		LineNumberReader lnr = new LineNumberReader(r);
		Set<Corner> corners = new HashSet<Corner>();
		Set<Point> points = new HashSet<Point>();
		Map<Corner, Set<Point>> ortho = new HashMap<Corner, Set<Point>>();
		loadCorners(lnr, corners, points, ortho);
		int[] hmin = new int[2];
		int[] hmax = new int[2];
		double[] pmin = new double[2];
		double[] pmax = new double[2];
		getMinMaxCorners(corners, pmin, pmax, hmin, hmax); // trim - or even remove?
		getMinMaxPoints(points, pmin, pmax);
		// grid min/max
		int[] gmin = new int[2];
		int[] gmax = new int[2];
		getGridSpace(pmin, pmax, gmin, gmax);
		header(w, gmin, gmax);
		setMask(w, gmin, gmax);
		labelGrid(w, gmin, gmax);
		pushGridSpace(w, gmin, gmax);
		frame(w, gmin, gmax);
		pop3(w);
		pushPointSpace(w, gmin, gmax);
//		pushMask(w);
		drawGrid(w, gmin, gmax);
		crossGrid(w, gmin, gmax);
		drawCorners(w, corners);
		drawPoints(w, points);
		drawLines(w, ortho, pmin, pmax);
		pop3(w);
		tail(w);
		w.close();
	}

	private void pushMask(Writer w) throws IOException {
		w.write("<g clip-path='rl(#grid-path)'>\n");
	}

	private void setMask(Writer w, int[] gmin, int[] gmax) throws IOException {
		w.write("<defs>\n");
		w.write("<clipPath id='grid-path' clipPathUnits='userSpaceOnUse' >");
		w.write("<rect x='" + 
		gmin[0] + "' y='" + gmin[1] + "' width='" + 
		(gmax[0] - gmin[0]) + "' height='" + (gmax[1] - gmin[1]) + "'/>\n");
		w.write("</clipPath>\n");
		w.write("</defs>\n");
	}

	private void getGridSpace(double[] pmin, double[] pmax, int[] gmin, int[] gmax) {
		int[] min = hasher.hash(pmin);
		int[] max = hasher.hashUp(pmax);
		min[0]--;
		min[1]--;
		max[0]++;
		max[1]++;
		double[] dmin = new double[2];
		double[] dmax = new double[2];
		hasher.unhash(min, dmin);
		hasher.unhash(max, dmax);
		gmin[0] = (int) Math.floor(dmin[0]);
		gmin[1] = (int) Math.floor(dmin[1]);
		gmax[0] = (int) Math.floor(dmax[0] + 0.9d); // get from hasher
		gmax[1] = (int) Math.floor(dmax[1] + 0.9d);
	}

	private void getMinMaxPoints(Set<Point> points, double[] pmin, double[] pmax) {
		pmin[0] = Double.MAX_VALUE;
		pmax[0] = Double.MIN_VALUE;
		pmin[1] = Double.MAX_VALUE;
		pmax[1] = Double.MIN_VALUE;
		for(Point p: points) {
			if (pmin[0] > p.values[0])
				pmin[0] = p.values[0];
			if (pmax[0] < p.values[0])
				pmax[0] = p.values[0];
			if (pmin[1] > p.values[1])
				pmin[1] = p.values[1];
			if (pmax[1] < p.values[1])
				pmax[1] = p.values[1];
		}
	}

	private void addPoint(Set<Point> points, Map<Corner, Set<Point>> ortho,
			Corner corner, Point point) {
		points.add(point);
		Set<Point> bag = ortho.get(corner);
		if (null == bag) {
			bag = new HashSet<Point>();
			ortho.put(corner, bag);
		}
		bag.add(point);
	}


	private void getMinMaxCorners(Set<Corner> corners, double pmin[], double pmax[], int hmin[], int hmax[]) {		
		hmin[0] = Integer.MAX_VALUE;
		hmax[0] = Integer.MIN_VALUE;
		hmin[1] = Integer.MAX_VALUE;
		hmax[1] = Integer.MIN_VALUE;
		for(Corner corner: corners) {
			if (hmin[0] > corner.hashes[0])
				hmin[0] = corner.hashes[0];
			if (hmax[0] < corner.hashes[0])
				hmax[0] = corner.hashes[0];
			if (hmin[1] > corner.hashes[1])
				hmin[1] = corner.hashes[1];
			if (hmax[1] < corner.hashes[1])
				hmax[1] = corner.hashes[1];
		}
	}

	// change to point space
	private void frame(Writer w, int[] hmin, int[] hmax) throws IOException {
		w.write("<!-- thick green frame -->\n");
		w.write("<g sstroke='green' " + thin + ">\n");
		w.write("	<rect fill='none' rx='0.1%' x='" + 
				hmin[0] + "' y='" + hmin[1] + "' width='" + 
				(hmax[0] - hmin[0]) + "' height='" + (hmax[0] - hmin[0]) + "'/>\n");
		w.write("</g>\n");
	}

	private void drawGrid(Writer w, int[] gmin, int[] gmax) throws IOException {
		int[] h = new int[2];
		double[] p = new double[2];

		w.write("<!-- thin grey grid -->\n");
		w.write("<g stroke='grey'" + thick + ">\n");
		// outer box
		w.write("<line x1='" + gmin[0] + "' y1='" + gmin[1] + "' x2='" + gmin[0] + "' y2='" + gmax[1] + "'/>\n");
		w.write("<line x1='" + gmax[0] + "' y1='" + gmin[1] + "' x2='" + gmax[0] + "' y2='" + gmax[1] + "'/>\n");
		w.write("<line x1='" + gmin[0] + "' y1='" + gmax[1] + "' x2='" + gmax[0] + "' y2='" + gmax[1] + "'/>\n");
		w.write("<line x1='" + gmin[0] + "' y1='" + gmin[1] + "' x2='" + gmax[0] + "' y2='" + gmin[1] + "'/>\n");
		w.write("</g>");
		w.write("<g stroke='grey'" + thin + ">\n");
		// vertical
		w.write("<!--     vertical lines -->\n");
		for(int x = gmin[0] - 5; x <= gmax[0] + 5; x++) {
			for(int y = gmin[1] - 5; y < gmax[1] + 5; y++) {
				h[0] = x;
				h[1] = y;
				hasher.unhash(h, p);
				w.write("<line ");
				w.write("x1='" + p[0] + "' y1='" + p[1] + "' ");
				h[1] = h[1] + 1;
				hasher.unhash(h, p);
				w.write("x2='" + p[0] + "' y2='" + p[1] + "' />\n");
			}
		}
		//		// horizontal
		w.write("<!--     horizontal lines -->\n");
		for(int x = gmin[0] - 5; x < gmax[0] + 5; x++) {
			for(int y = gmin[1] - 5; y <= gmax[1] + 5; y++) {
				h[0] = x;
				h[1] = y;
				hasher.unhash(h, p);
				w.write("<line ");
				w.write("x1='" + p[0] + "' y1='" + p[1] + "' ");
				h[0]++;
				hasher.unhash(h, p);
				w.write("x2='" + p[0] + "' y2='" + p[1] + "' />\n");
			}
		}
		w.write("</g>\n");
		// diagonals
		w.write("<!--     diagonal lines -->\n");
		w.write("<g stroke='grey' " + thin + ">\n");
		for(int x = gmin[0] - 5; x < gmax[0] + 5; x++) {
			for(int y = gmin[1] - 5; y < gmax[1] + 5; y++) {
				h[0] = x;
				h[1] = y;
				hasher.unhash(h, p);
				w.write("<line ");
				w.write("x1='" + p[0] + "' y1='" + p[1] + "' ");
				h[0]++;
				h[1]++;
				hasher.unhash(h, p);
				w.write("x2='" + p[0] + "' y2='" + p[1] + "' />\n");
			}
		}
		w.write("</g>\n");
	}

	// clip points outside nominal space
	private void clip(int[] hmin, int[] hmax, double[] p) {
		if (hmin[0] > p[0])
			p[0] = hmin[0];
		if (hmax[0] < p[0])
			p[0] = hmax[0];
		if (hmin[1] > p[1])
			p[1] = hmin[1];
		if (hmax[1] < p[1])
			p[1] = hmax[1];

	}

	// label from the upside-down frame, not in the numerical space
	private void labelGrid(Writer w, int[] gmin, int[] gmax) throws IOException {
		int lx = buffer;
		int rx = X - buffer;
		int ly = Y - buffer;
		int hy = buffer;
		w.write("<!-- text labels -->\n");
		w.write("<g id='labels' font-family='Verdana' font-size='10'>\n");
		String xFudge = "dx='-0.5em' dy='20px'"; // 0, 0 - em
		String yFudge= "dx='-30px' dy='0px'"; // 0 - em, 0
		w.write("<text x='" + lx + "' y='" + ly + "'> <tspan " + xFudge + ">" + gmin[0] + "</tspan></text>\n");
		w.write("<text x='" + lx + "' y='" + ly + "'> <tspan " + yFudge + ">" + gmin[1] + "</tspan></text>\n");
		w.write("<text x='" + rx + "' y='" + ly + "'> <tspan " + xFudge + ">" + gmax[0] + "</tspan></text>\n");
		w.write("<text x='" + lx + "' y='" + hy + "'> <tspan " + yFudge + ">" + gmax[1] + "</tspan></text>\n");
		w.write("</g>\n");

	}

	private void crossGrid(Writer w, int[] gmin, int[] gmax) throws IOException {
		w.write("<!--     origin lines -->\n");
	if (0 >= gmin[0] && 0 <= gmax[0] && 0 >= gmin[1] && 0 <= gmax[1]) {
			w.write("<g" + thin + "stroke='green'>\n");
			w.write("<line x1='" + gmin[0] + "' y1='"+ 0 +"' x2='" + gmax[0] + "' y2='"+ 0 +"' />\n");
			w.write("<line y1='" + gmin[1] + "' x1='0' y2='" + gmax[1] + "' x2='0' />\n");
			w.write("</g>\n");
		}

	}

	private void drawPoints(Writer w, Set<Point> points) throws IOException {
		w.write("<!-- blue points -->\n");
		w.write("<g" + thin + "stroke='blue'>\n");
		for(Point point: points) {
//			w.write("<rect " + thick + " x='" + point.values[0] + "' y='" + point.values[1] + "' width='0.00002%' height='0.00002%'/>\n");
			w.write("<circle cx='" + point.values[0] + "' cy='" + point.values[1] + "' r='0.01%'/>\n");
		}
		w.write("</g>\n");
	}

	private void drawCorners(Writer w, Set<Corner> corners) throws IOException {
		w.write("<!-- red corners -->\n");
		w.write("<g" + thin + "stroke='red'>\n");
		for(Corner corner: corners) {
			double[] p = new double[2];
			hasher.unhash(corner.hashes, p);
			w.write("<circle cx='" + p[0] + "' cy='" + p[1] + "' r='0.01%'/>\n");
		}
		w.write("</g>\n");
	}

	// draws in point space, not hash space, so has to convert inline
	private void drawLines(Writer w, Map<Corner, Set<Point>> ortho, double[] pmin, double[] pmax) throws IOException {
		w.write("<!-- lines from points to corners -->\n");
		w.write("<g" + thin + "stroke='red' stroke-dasharray='0.01%,0.03%'>\n");
		for(Corner corner: ortho.keySet()) {
			Set<Point> points = ortho.get(corner);
			for(Point point: points) {
				double[] p = new double[2];
				hasher.unhash(corner.hashes, p);
				w.write("<line" + " x1='" + p[0] + "' y1='" + p[1] + "' x2='" + point.values[0] + "' y2='" + point.values[1] + "'/>\n");
			}
		}
		w.write("</g>\n");
	}

	void header(Writer w, int[] hmin, int[]hmax) throws IOException {
		String viewBox = (hmin[0] - 2) + " " + (hmin[1] - 2) + " " + (hmax[0] + 2) + " " + (hmax[1] + 2);
		viewBox = "0 0 " + X + " " + Y;
		w.write("<?xml version='1.0' encoding='iso-8859-1' standalone='no'?>\r\n" + 
				"<!DOCTYPE svg PUBLIC '-//W3C//DTD SVG 1.0//EN' 'http://www.w3.org/TR/SVG/DTD/svg10.dtd'>\r\n" + 
				"<svg viewBox='" + viewBox + "'  xmlns='http://www.w3.org/2000/svg' " +
		"xmlns:xlink='http://www.w3.org/1999/xlink'>\r\n");
		w.write("<g stroke-width='2px' stroke='blue'><rect fill='none' x='" + 0 + "' y='" + 0 + "' height='" + X + "' width='" + Y + "'/></g>\n");
	}

	// flip, scale to target space, translate to inner frame
	// in hash scale
	void pushGridSpace(Writer w, int[] hmin, int[] hmax) throws IOException {
		String matrix = "1 0 0 -1 " + buffer + " " + (Y - buffer);
		w.write("<!-- push grid space -->\n");
		w.write("<g transform='matrix(" + matrix + ")' fontsize='1'>\n");
		w.write("<g transform='scale(" + (X-buffer*2)/(hmax[0] - hmin[0]) + "," + (Y-buffer*2)/(hmax[0] - hmin[0]) + ")'>\n");
		w.write("<g transform='translate(" +(0 - hmin[0]) + "," + (0 - hmin[1]) + ")'>\n");
	}

	// same in point scale
	private void pushPointSpace(Writer w, int[] gmin, int[] gmax) throws IOException {
		double minx = gmin[0];
		double maxx = gmax[0];
		double miny = gmin[1];
		double maxy = gmax[1];
		String matrix = "1 0 0 -1 " + buffer + " " + (Y - buffer);
		w.write("<!-- push point space -->\n");
		w.write("<g transform='matrix(" + matrix + ")' fontsize='1' >\n");
		w.write("<g transform='scale(" + (X-buffer*2.0)/(maxx - minx) + "," + (Y-buffer*2.0)/(maxy - miny) + ")'>\n");
		w.write("<g transform='translate(" +(0 - minx) + "," + (0 - miny) + ")' clip-path='url(#grid-path)'>\n");	
	}

	private void pop3(Writer w) throws IOException {
		w.write("</g>\n");
		w.write("</g>\n");
		w.write("</g>\n");
	}

	private void tail(Writer w) throws IOException {
		w.write("</svg>\n");
	}

	private void loadCorners(LineNumberReader lnr, Set<Corner> corners,
			Set<Point> points, Map<Corner, Set<Point>> ortho)
	throws IOException {
		String line;
		while (null != (line = lnr.readLine())) {
			String parts[] = line.split("[ \t]");
			String[] pipes = parts[1].split("\\|");
			Corner corner = Corner.newCorner(parts[0]);
			corners.add(corner);
			for(int i = 0; i < pipes.length; i++) {
				Point point = Point.newPoint(pipes[i]);
				addPoint(points, ortho, corner, point);
			}
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 * 
	 * Load up output of hadoop job in /tmp/IN/pairwhatsit
	 */
	public static void main(String[] args) throws IOException {

		File data = new File(args[0]);
		File svg = new File(args[1]);
		Reader r = new FileReader(data);
		svg.delete();
		svg.createNewFile();
		Writer w = new FileWriter(svg);
		GenSVG gsvg = new GenSVG();
		gsvg.hasher = new VertexTransitiveHasher(2, 2.0d);
		gsvg.makeSVG(r, w);
	}

}

package lsh.solr;


import org.apache.solr.common.SolrDocument;

import lsh.core.CornerGen;
import lsh.core.Point;

/*
 * Interpret input field values as lat/lon or 2D points.
 */

public class GeoIndexer implements Indexer {
	final CornerGen cg;

	public GeoIndexer(CornerGen cg) {
		this.cg = cg;
	}

	@Override
	public Point getCorners(SolrDocument doc, String idField, String[] pointFields) {
		// Only part that is hard-coded to geographic data- PointType or lat/lon fields
		double[] point = new double[2];
		if (pointFields.length == 1) {
			Object o = (Object) doc.getFieldValue(pointFields[0]);
			String parts[] = ((String) o).split(",");
			point[0] = Double.parseDouble(parts[0]);
			point[1] = Double.parseDouble(parts[1]);
		} else {
			Object o0 = (Object) doc.getFieldValue(pointFields[0]);
			Object o1 = (Object) doc.getFieldValue(pointFields[1]);
			if (o0 instanceof String) {
				point[0] = Double.parseDouble((String) o0);
				point[1] = Double.parseDouble((String) o1);
			} else if (o0 instanceof Float) { 
				point[0] = new Double((Float) o0);
				point[1] = new Double((Float) o1);
			}else { 
				point[0] = (Double) o0;
				point[1] = (Double) o1;
			}
		}
//		return cg.getHashSet(new Point((String) doc.getFieldValue("id"), point));
		return new Point((String) doc.getFieldValue("id"), point, null);
	}
}

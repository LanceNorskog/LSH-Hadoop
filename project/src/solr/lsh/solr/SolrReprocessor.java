package lsh.solr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import lsh.core.Corner;
import lsh.core.CornerGen;
import lsh.core.Hasher;
import lsh.core.OrthonormalHasher;
import lsh.core.Point;

/*
 * Read all solr documents, generate neighbor id and point fields, and store them in neighbor documents.
 * Supports lat/lon fields or any "lat,lon" Point fields.
 */

public class SolrReprocessor {
	 SolrServer server;
	final String NNPrefix;
	final String idField = "id";
	final String[] neighborPointFields;
	final String neighborIdFields;
	final String[] pointFields;
	final CornerGen cg;
	final Indexer indexer;

	public SolrReprocessor(String idField, String[] pointFields, String NNPrefix,
			Hasher hasher, double[] stretch, String prefix, SolrServer server) throws Exception {
		this.NNPrefix = NNPrefix;
		this.pointFields = pointFields;
		this.cg = new CornerGen(hasher, stretch);
		indexer = new GeoIndexer(this.cg);
		this.neighborIdFields = NNPrefix + idField + "s";
		this.neighborPointFields = new String[pointFields.length];
		for (int i = 0; i < pointFields.length; i++) {
			this.neighborPointFields[0] = NNPrefix + pointFields[i] + "s";
		}
		this.server = server;
	}

	SolrDocumentList getDocs() throws SolrServerException {
		SolrDocumentList docs = null;
		SolrQuery query = new SolrQuery("store:[-180,-180 TO 180,180]");
		query.setRows(30);
		QueryResponse response = server.query(query);
		docs = response.getResults();
		return docs;
	}

	// Build list of corner->[integer,..] indexes into documents list
	void processDocuments(SolrDocumentList docs) {
		Map<Corner, List<Integer>> corner2ids = new HashMap<Corner, List<Integer>>();
		for(int i = 0; i < docs.size(); i++) {
			SolrDocument doc = docs.get(i);
			Set<Corner> corners = indexDocumentGeo(doc);
			for(Corner corner: corners) {
				List<Integer> ids = corner2ids.get(corner);
				if (null == ids) {
					ids = new ArrayList<Integer>();
					corner2ids.put(corner, ids);
				}
				ids.add(i);
			}	 
		}
		for(SolrDocument doc: docs) {
			doc.setField(neighborIdFields, new ArrayList<Object>());
			for(String f: neighborPointFields) {
				doc.setField(f, new ArrayList<Object>());
			}
		}
		for(SolrDocument doc: docs) {
			Object id = doc.getFieldValue("id");
			Object[] p = new Object[this.pointFields.length];
			for(int i = 0; i < p.length; i++) {
				p[i] = doc.getFieldValue(this.pointFields[i]);
			}
			Set<Corner> corners = cg.getHashSet(indexer.getCorners(doc, idField, pointFields)); 
			for(Corner corner: corners) {
				List<Integer> indexes = corner2ids.get(corner);

				for(Integer index: indexes) {
					SolrDocument neighbor = docs.get(index);
					Collection<Object> nids = neighbor.getFieldValues(neighborIdFields);
					Object nid = neighbor.getFieldValue("id");
					if (!id.equals(nid) && ! nids.contains(id)) {
						nids.add(id);
						for(int i = 0; i < neighborPointFields.length; i++) {
							Collection<Object> npoints = neighbor.getFieldValues(neighborPointFields[i]);
							npoints.add(p[i]);
						}
					}
				}
			}	 
		}	 
	}

	// Only part that is hard-coded to geographic data- PointType or lat/lon fields
	Set<Corner> indexDocumentGeo(SolrDocument doc) {
		double[] point = new double[2];
		if (this.pointFields.length == 1) {
			Object o = (Object) doc.getFieldValue(this.pointFields[0]);
			String parts[] = ((String) o).split(",");
			point[0] = Double.parseDouble(parts[0]);
			point[1] = Double.parseDouble(parts[1]);
		} else {
			Object o0 = (Object) doc.getFieldValue(this.pointFields[0]);
			Object o1 = (Object) doc.getFieldValue(this.pointFields[1]);
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
		return cg.getHashSet(new Point((String) doc.getFieldValue("id"), point));
	}

	public static void main(String[] args) throws Exception {
		String idField = "id";
		String pointFields[] = {"store" };
		double[] stretch = {1.0, 1.0};
		String prefix = "neighbor";
		Hasher hasher = new OrthonormalHasher(stretch);
		String url = "http://localhost:8983/solr";
		SolrServer server = new CommonsHttpSolrServer(url);
		SolrReprocessor sr = new SolrReprocessor(idField, pointFields, prefix, hasher, stretch, prefix, server);
		SolrDocumentList docs = sr.getDocs();
		sr.processDocuments(docs);
		sr.printNeighbors(docs, true, false);		
	}

	private void printNeighbors(SolrDocumentList docs, boolean ids, boolean points) {
		for(SolrDocument doc: docs) {
			Collection<Object> neighborIds = doc.getFieldValues(this.neighborIdFields);
			System.out.println((String) doc.getFieldValue("id") + ": " + neighborIds.size());
			List<Collection<Object>> neighborPoints = new ArrayList<Collection<Object>>();
			Iterator<Object> itIds = neighborIds.iterator();
			List<Iterator<Object>> itPoints = new ArrayList<Iterator<Object>>();
			for(int i = 0; i < this.neighborPointFields.length; i++) {
				neighborPoints.add(doc.getFieldValues(this.neighborPointFields[i]));
				itPoints.add(neighborPoints.get(i).iterator());
			}
			for(int i = 0; i < neighborIds.size(); i++) {
				if (ids)
					System.out.print("\t" + itIds.next());
				if (points) {
					System.out.print("\t");

					for(int j = 0; j < this.neighborPointFields.length; j++) {
						System.out.print(itPoints.get(j).next() + ",");
					}
				}
				System.out.println();
			}

		}
	}

}
package lsh.sample;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;

import lsh.core.Corner;
import lsh.core.CornerGen;
import lsh.core.Point;

/*
 * Read all solr documents, generate neighbor id and point fields, and store them in neighbor documents.
 * Supports lat/lon fields or any "lat,lon" Point fields
 */

public class SolrReprocessor {
	String url = "http://localhost:8983/solr";
	final CommonsHttpSolrServer server;
	final String NNPrefix;
	final String idField = "id";
//	final String pointField;
	final String latField;
	final String lonField;
	final String[] neighborPointFields;
	final String neighborIdFields;
	final String[] pointFields;
	final CornerGen cg;

	public SolrReprocessor(String pointField, String NNPrefix, Double distance) throws Exception {
		this.server = new CommonsHttpSolrServer( url );
		this.NNPrefix = NNPrefix;
		this.pointFields = new String[1];
		this.pointFields[0] = pointField;
		latField = null;
		lonField = null;
		this.cg = new CornerGen("lsh.core.OrthonormalHasher",
					distance.toString() + "," + distance.toString());
		this.neighborIdFields = NNPrefix + idField + "s";
		this.neighborPointFields = new String[1];
		this.neighborPointFields[0] = NNPrefix + pointField + "s";

		}

	public SolrReprocessor(String latField, String lonField, String NNPrefix, Double distance) throws Exception {
		this.server = new CommonsHttpSolrServer( url );
		this.NNPrefix = NNPrefix;
		this.latField = latField;
		this.lonField = lonField;
		this.pointFields = new String[2];
		this.pointFields[0] = this.latField;
		this.pointFields[1] = this.lonField;
//		this.pointField = null;
		this.cg = new CornerGen("lsh.core.OrthonormalHasher",
					distance.toString() + "," + distance.toString());
		this.neighborIdFields = NNPrefix + idField + "s";
		this.neighborPointFields = new String[1];
		this.neighborPointFields[0] = NNPrefix + latField + "s";
		this.neighborPointFields[1] = NNPrefix + latField + "s";
	}

	 SolrDocumentList getDocs() throws SolrServerException {
		SolrDocumentList docs = null;
		SolrQuery query = new SolrQuery("store:[-180,-180 TO 180,180]");
		query.setRows(30);
		QueryResponse response = server.query(query);
		docs = response.getResults();
		SolrDocument doc = docs.get(0);
		return docs;
	}
	
	 // Build list of corner->[integer,..] indexes into documents list
	 void processDocuments(SolrDocumentList docs) {
		 Map<Corner, List<Integer>> corner2ids = new HashMap<Corner, List<Integer>>();
		 for(int i = 0; i < docs.size(); i++) {
			 SolrDocument doc = docs.get(i);
			 Set<Corner> corners = indexDocument(doc);
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
			 Set<Corner> corners = indexDocument(doc);
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

//	private void addPoint(Object nid, Object[] p, SolrDocument neighbor) {
//		Collection<Object> npoints;
//		Object point;
//		npoints = neighbor.getFieldValues(neighborPointField);
//		point = neighbor.getFieldValue(pointField);
//		if (!p.equals(point) && ! npoints.contains(p)) {
//			npoints.add(p);
//		}
//	}
	 
	Set<Corner> indexDocument(SolrDocument doc) {
		double[] point = new double[2];
		if (this.pointFields.length == 1) {
			Object o = (Object) doc.getFieldValue(this.pointFields[0]);
			String parts[] = ((String) o).split(",");
			point[0] = Double.parseDouble(parts[0]);
			point[1] = Double.parseDouble(parts[1]);
		} else {
			Object o = (Object) doc.getFieldValue(this.pointFields[0]);
			point[0] = Double.parseDouble((String) o);
			o = (Object) doc.getFieldValue(this.pointFields[1]);
			point[1] = Double.parseDouble((String) o);
		}
		return cg.getHashSet(new Point((String) doc.getFieldValue("id"), point));
	}
	
	public static void main(String[] args) throws Exception {
//		SolrReprocessor sr = new SolrReprocessor("lat", "lon", "neighbors", 0.5);
		SolrReprocessor sr = new SolrReprocessor("store", "neighbors", 0.5);
		SolrDocumentList docs = sr.getDocs();
		sr.processDocuments(docs);
		sr.printNeighbors(docs);		
	}

	private void printNeighbors(SolrDocumentList docs) {
		for(SolrDocument doc: docs) {
			Collection<Object> neighborIds = doc.getFieldValues(this.neighborIdFields);
			System.out.println((String) doc.getFieldValue("id") + ": " + neighborIds.size());
//			printFieldValues(doc, neighborIds);
			Collection<Object>[] neighborPoints = new Collection[this.neighborPointFields.length];
			for(int i = 0; i < this.neighborPointFields.length; i++) {
				neighborPoints[i] = doc.getFieldValues(this.neighborPointFields[i]);
			}
			Iterator<Object> it = neighborIds.iterator();
			for(int neighbors = 0; neighbors < neighborIds.size(); neighbors++) {
				System.out.print("\t" + it.next() + "\t");
				for(int i = 0; i < this.neighborPointFields.length; i++) {
					System.out.print(doc.getFieldValues(this.neighborPointFields[i]) + ",");
				}
			}
			System.out.println();
		}
	}

	private void printFieldValues(SolrDocument doc, Collection<Object> neighbors) {
		System.out.println((String) doc.getFieldValue("id") + ": " + neighbors.size());
		ArrayList<String> sortable = new ArrayList<String>();
		for(Object o: neighbors) {
			sortable.add((String) o);
		}
		Collections.sort(sortable);
		for(String s: sortable) {
			System.out.println("\t" + s);
		}
	}







}

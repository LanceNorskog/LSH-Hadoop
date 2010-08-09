package lsh.sample;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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

public class SolrReprocessor {
	String url = "http://localhost:8983/solr";
	final String neighborPointField = "location";
	final CommonsHttpSolrServer server;
	final String pointField;
	final String latField;
	final String lonField;
	final String neighborIdField;
	final CornerGen cg;

	public SolrReprocessor(String pointField, String neighborField, Double distance) throws Exception {
		this.server = new CommonsHttpSolrServer( url );
		this.pointField = pointField;
		this.cg = new CornerGen("lsh.core.OrthonormalHasher",
					distance.toString() + "," + distance.toString());
		this.neighborIdField = neighborField;
		latField = null;
		lonField = null;
	}

	public SolrReprocessor(String latField, String lonField, String neighborField, Double distance) throws Exception {
		this.server = new CommonsHttpSolrServer( url );
		this.latField = latField;
		this.lonField = lonField;
		this.cg = new CornerGen("lsh.core.OrthonormalHasher",
					distance.toString() + "," + distance.toString());
		this.neighborIdField = neighborField;
		this.pointField = null;
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
			 doc.setField(neighborIdField, new ArrayList<Object>());
			 doc.setField(neighborPointField, new ArrayList<Object>());
		 }
		 for(SolrDocument doc: docs) {
			 Object id = doc.getFieldValue("id");
			 Object p = doc.getFieldValue(this.pointField);
			 Set<Corner> corners = indexDocument(doc);
			 for(Corner corner: corners) {
				 List<Integer> indexes = corner2ids.get(corner);
				 
				 for(Integer index: indexes) {
					 SolrDocument neighbor = docs.get(index);
					 Collection<Object> nids = neighbor.getFieldValues(neighborIdField);
					 Object nid = neighbor.getFieldValue("id");
					 if (!id.equals(nid) && ! nids.contains(id)) {
						 nids.add(id);
						 Collection<Object> npoints = neighbor.getFieldValues(neighborPointField);
						 npoints.add(p);
					 }
				 }
			 }	 
		 }	 
	 }

	private void addPoint(Object nid, Object p, SolrDocument neighbor) {
		Collection<Object> npoints;
		Object point;
		npoints = neighbor.getFieldValues(neighborPointField);
		point = neighbor.getFieldValue(pointField);
		if (!p.equals(point) && ! npoints.contains(p)) {
			npoints.add(p);
		}
	}
	 
	Set<Corner> indexDocument(SolrDocument doc) {
		double[] point = new double[2];
		if (null != this.pointField) {
			Object o = (Object) doc.getFieldValue(this.pointField);
			String parts[] = ((String) o).split(",");
			point[0] = Double.parseDouble(parts[0]);
			point[1] = Double.parseDouble(parts[1]);
		} else {
			Object o = (Object) doc.getFieldValue(this.latField);
			point[0] = Double.parseDouble((String) o);
			o = (Object) doc.getFieldValue(this.latField);
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
			Collection<Object> neighborIds = doc.getFieldValues(this.neighborIdField);
			printFieldValues(doc, neighborIds);
			Collection<Object> neighborPoints = doc.getFieldValues(this.neighborPointField);
			printFieldValues(doc, neighborPoints);
//			System.out.println();
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

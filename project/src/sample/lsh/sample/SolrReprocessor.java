package lsh.sample;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
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
	final CommonsHttpSolrServer server;
	final String pointField;
	final String neighborField;
	final CornerGen cg;

	public SolrReprocessor(String pointField, String neighborField, Double distance) throws Exception {
		this.server = new CommonsHttpSolrServer( url );
		this.pointField = pointField;
		this.cg = new CornerGen("lsh.core.OrthonormalHasher",
					distance.toString() + "," + distance.toString());
		this.neighborField = neighborField;
	}

	 SolrDocumentList getDocs() throws SolrServerException {
		SolrDocumentList docs = null;
		SolrQuery query = new SolrQuery("store:[44,-180 TO 46,180]");
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
			 doc.setField(neighborField, new ArrayList<Object>());
		 }
		 for(SolrDocument doc: docs) {
			 Object id = doc.getFieldValue("id");
			 Set<Corner> corners = indexDocument(doc);
			 for(Corner corner: corners) {
				 List<Integer> indexes = corner2ids.get(corner);
				 
				 for(Integer index: indexes) {
					 SolrDocument neighbor = docs.get(index);
					 Collection<Object> nids = neighbor.getFieldValues(neighborField);
					 Object nid = neighbor.getFieldValue("id");
					 if (!id.equals(nid) && ! nids.contains(id)) {
						 nids.add(id);
					 }
				 }
			 }	 
		 }	 
		 docs.hashCode();
	 }
	 
	Set<Corner> indexDocument(SolrDocument doc) {
		double[] point = new double[2];
		Collection<String> x = doc.getFieldNames();
		Object s = (Object) doc.getFieldValue("store");
		if (null == s || !(s instanceof String))
			((String) s).hashCode();
		String parts[] = ((String) s).split(",");
		point[0] = Double.parseDouble(parts[0]);
		point[1] = Double.parseDouble(parts[1]);
		return cg.getHashSet(new Point((String) doc.getFieldValue("id"), point));
	}
	
//	void addNeighbors() {
//	{ 
//		List<String> locations = new ArrayList<String>();
//		List<Object> ids = new ArrayList<Object>();
//		for(Corner corner: corners) {
//			ids.add( )
//			double[] unhashed = new double[2];
//			cg.hasher.unhash(corner.hashes, unhashed);
//			String grid = unhashed[0] + "," + unhashed[1];
//			neighbors.add(grid);
//		}
//	}
	
	
	
	
	public static void main(String[] args) throws Exception {
//		SolrReprocessor sr = new SolrReprocessor("lat", "lon", "neighbors", 0.5);
		SolrReprocessor sr = new SolrReprocessor("store", "neighbors", 0.5);
		SolrDocumentList docs = sr.getDocs();
		sr.processDocuments(docs);
		
		
		
	}







}

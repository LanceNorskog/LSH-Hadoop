package lsh.solr;

import java.io.IOException;
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
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;

import lsh.core.Corner;
import lsh.core.CornerGen;
import lsh.core.Hasher;
import lsh.core.OrthonormalHasher;
import lsh.core.Point;

/*
 * Read all solr documents, generate neighbor id and point fields, and store them in neighbor documents.
 * Supports lat/lon fields or any "lat,lon" Point fields.
 * Saves lat/lon neighbor fields as neighborpoints field.
 * 
 * Does not scale.
 */

public class SolrReprocessor {
	final String NNPrefix;
	final String idField = "id";
	final String neighborIds;
	final String[] pointFields;
	final CornerGen cg;
	final Indexer indexer;
	final String neighborPoints;

	public SolrReprocessor(String idField, String[] pointFields, String NNPrefix,
			Hasher hasher, double[] stretch, String prefix) throws Exception {
		this.NNPrefix = NNPrefix;
		this.pointFields = pointFields;
		this.cg = new CornerGen(hasher, stretch);
		indexer = new GeoIndexer(this.cg);
		this.neighborIds = NNPrefix + "ids";
		this.neighborPoints = NNPrefix + "points";
	}

	SolrDocumentList getDocs(SolrServer server, String queryString) throws SolrServerException {
		SolrQuery query = new SolrQuery(queryString);
		query.setRows(50);
		return getDocs(server, query);
	}

	private SolrDocumentList getDocs(SolrServer server, SolrParams query) throws SolrServerException {
		SolrDocumentList docs;
		QueryResponse response = server.query(query);
		docs = response.getResults();
		return docs;
	}
	
	private void pushDocs(SolrServer server, SolrDocumentList docs) throws SolrServerException, IOException {
		server.deleteByQuery("*:*");
		server.commit(); 
		for(SolrDocument doc: docs) {
			SolrInputDocument idoc = new SolrInputDocument();
			for(String f: doc.getFieldNames()) {
				idoc.addField(f, doc.getFieldValue(f));
			}
			server.add(idoc);
		}
		server.commit();
	}

	// Build list of corner->[integer,..] indexes into documents list
	void processDocuments(SolrDocumentList docs) {
		Map<Corner, List<Integer>> corner2ids = new HashMap<Corner, List<Integer>>();
		for(int i = 0; i < docs.size(); i++) {
			SolrDocument doc = docs.get(i);
			Set<Corner> corners = cg.getHashSet(indexer.getCorners(doc, idField, pointFields)); //indexDocumentGeo(doc);
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
			doc.setField(neighborIds, new ArrayList<Object>());
			doc.setField(neighborPoints, new ArrayList<Object>());
		}
		for(SolrDocument doc: docs) {
			Object id = doc.getFieldValue("id");
			String point = "";
			for(int i = 0; i < this.pointFields.length; i++) {
				point += doc.getFieldValue(this.pointFields[i]) + ",";
			}
			if (point.length() > 1)
				point = point.substring(0, point.length() - 1);
			Set<Corner> corners = cg.getHashSet(indexer.getCorners(doc, idField, pointFields)); 
			for(Corner corner: corners) {
				List<Integer> indexes = corner2ids.get(corner);

				for(Integer index: indexes) {
					SolrDocument neighbor = docs.get(index);
					Collection<Object> nids = neighbor.getFieldValues(neighborIds);
					Object nid = neighbor.getFieldValue("id");
					if (!id.equals(nid) && ! nids.contains(id)) {
						nids.add(id);
						Collection<Object> npoints = neighbor.getFieldValues(neighborPoints);
						npoints.add(point);
					}
				}
			}	 
		}	 
	}

	public static void main(String[] args) throws Exception {
		String idField = "id";
//		String pointFields[] = {"store" };
//		double[] stretch = {1.0, 1.0};
		String pointFields[] = {"lat", "lng" };
		double[] stretch = {0.1, 0.1};
		String prefix = "neighbor";
		Hasher hasher = new OrthonormalHasher(stretch);
		String url = "http://localhost:8983/solr/raw";
		SolrServer rawserver = new CommonsHttpSolrServer(url);
		url = "http://localhost:8983/solr/processed";
		SolrServer processedserver = new CommonsHttpSolrServer(url);
		SolrReprocessor sr = new SolrReprocessor(idField, pointFields, prefix, hasher, stretch, prefix);
		SolrDocumentList docs = sr.getDocs(rawserver, "*:*");
		sr.processDocuments(docs);
		sr.pushDocs(processedserver, docs);
		sr.printNeighbors(docs, true, false);		
	}

	private void printNeighbors(SolrDocumentList docs, boolean ids, boolean points) {
		for(SolrDocument doc: docs) {
			Collection<Object> neighborIds = doc.getFieldValues(this.neighborIds);
			System.out.println((String) doc.getFieldValue("id") + ": " + neighborIds.size());
			Iterator<Object> itIds = neighborIds.iterator();
			Iterator<Object> itPoints = doc.getFieldValues(this.neighborPoints).iterator();
			for(int i = 0; i < neighborIds.size(); i++) {
				if (ids)
					System.out.print("\t" + itIds.next());
				if (points) {
					System.out.print("\t");

					System.out.print(itPoints.next() + ",");
				}
				System.out.println();
			}

		}
	}

}
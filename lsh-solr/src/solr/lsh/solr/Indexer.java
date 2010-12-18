package lsh.solr;

import org.apache.solr.common.SolrDocument;

//import lsh.core.Corner;
import lsh.core.Point;

public interface Indexer {
	
	public Point getCorners(SolrDocument doc, String idField, String[] pointFields);
//	Set<Corner> getCorners(SolrDocument doc, String idField, String[] pointFields);

}

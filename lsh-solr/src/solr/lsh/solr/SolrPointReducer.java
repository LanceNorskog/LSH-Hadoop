package lsh.solr;

import java.io.IOException;

import lsh.core.Corner;
import lsh.core.Point;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/*
 * Emit one point with a set of corners.
 * Key is the string printout of the id,double...double
 */

/*
 * Output format of "payload|int,int,int&..."
 */

public class SolrPointReducer extends
Reducer<Text, Text, Text, Text> {

  @Override
  public void reduce(Text key, Iterable<Text> values, Context context)
  throws IOException, InterruptedException {
    StringBuilder sb = new StringBuilder();
    Point point = Point.newPoint(key.toString());

    sb.append(point.payload);
    sb.append('|');
    for(Text value: values) {
      Corner corner = Corner.newCorner(value.toString());
      sb.append(corner.toString());
      sb.append('&');
    }
    sb.setLength(sb.length() - 1);
    String value = sb.toString();
    context.write(null, new Text(value));
  }

}


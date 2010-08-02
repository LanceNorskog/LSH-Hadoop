package org.apache.hadoop.mapreduce.lib.input;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

/*
 * Configurable CSV line reader.
 * Variant of TextInputReader that harvests fields from CSV input lines
 * 
 * Use case: 
 *  text data files with one or more data items per line. 
 *  This class rips apart the data items with regex patterns, then assembles a new value with 0 or more values.
 *  Items have one common separator, or the first item has a different separator.
 *  The output includes 1 or more items from the list, chosen by order.
 *  The output items are separated by the given 'replace' fields- 
 *  again, the first may be different than the rest.
 *  
 * 	pattern1/pattern2 are these separators - as Java Regex patterns
 *  replace1/replace2 
 *  order = n1,n2,n3...nn  
 * 
 *  See unit test for examples
 */

public class CSVTextFormat extends FileInputFormat<LongWritable, Text> {

	private static final String FORMAT_PATTERN1 = "mapreduce.csvinput.pattern1";
	private static final String FORMAT_PATTERN2 = "mapreduce.csvinput.pattern2";
	private static final String FORMAT_REPLACE1 = "mapreduce.csvinput.replace1";
	private static final String FORMAT_REPLACE2 = "mapreduce.csvinput.replace2";
	private static final String FORMAT_ORDER = "mapreduce.csvinput.order";

	@Override
	public RecordReader<LongWritable, Text> 
	createRecordReader(InputSplit split,
			TaskAttemptContext context) throws IOException {
		Configuration conf = context.getConfiguration();
		String pattern1 = conf.get(FORMAT_PATTERN1);
		String pattern2 = conf.get(FORMAT_PATTERN2);
		String replace1 = conf.get(FORMAT_REPLACE1);
		String replace2 = conf.get(FORMAT_REPLACE2);
		String order = conf.get(FORMAT_ORDER);
		if (null == pattern1 || null == replace1 || null == order) {
			throw new IOException("CSVTextFormat: missing parameter pattern1/replace1/order");
		}
		return new FlexibleRecordReader(pattern1, pattern2, replace1, replace2, order);
	}

	  @Override
	  protected boolean isSplitable(JobContext context, Path file) {
	    CompressionCodec codec = 
	      new CompressionCodecFactory(context.getConfiguration()).getCodec(file);
	    return codec == null;
	  }
	  
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FlexibleRecordReader frr;

		String test;
		frr = new FlexibleRecordReader("::", null, " ", null, "0,1,2");
		/*
		test = frr.unpackValue("a");
		// "a"
		test = frr.unpackValue("a::b");
		// "a b"
		test = frr.unpackValue("a::b::c");
		// "a b,c"
		 * */

		frr = new FlexibleRecordReader(",", ",", ",", ",", "0,5,6");
		test = frr.unpackValue("id,1,2,3,4,lat,long,7");
		// "id,lat,long"
		test = frr.unpackValue(",1,2,3,4,,long,7");
		// ",,long"
		test.hashCode();
	}
}

class FlexibleRecordReader extends LineRecordReader {
	final String pattern1, pattern2, replace1, replace2;
	final int[] order;
	final int[] reverse;

	public FlexibleRecordReader(String pattern1, String pattern2,
			String replace1, String replace2, String order) {
		super();
		this.pattern1 = pattern1;
		this.pattern2 = pattern2;
		this.replace1 = replace1;
		this.replace2 = replace2;
		String[] parts = order.split(",");
		this.order = new int[parts.length];
		int max = -1;
		for(int i = 0; i < parts.length; i++){
			this.order[i] = Integer.parseInt(parts[i]);
			if (this.order[i] > max)
				max = this.order[i];
		}
		this.reverse = new int[max + 1];
		for(int i = 0; i < max; i++) {
			this.reverse[i] = -1;
		}
		for(int i = 0; i < parts.length; i++) {
			this.reverse[this.order[i]] = i;
		}
	}

	@Override
	public Text getCurrentValue() {
		Text value = super.getCurrentValue();
		return new Text(unpackValue(value.toString()));
	}

	/*
	String unpackValueSimple(String line) {
		StringBuilder sb = new StringBuilder();
		String tail = null;
		String[] first = line.split(pattern1);
		sb.append(first[0]);
		sb.append(replace1);
		tail = replace1;
		if (null != pattern2) {
			for(int i = 1; i < first.length; i++) {
				String[] second = first[i].split(pattern2);
				for(String s: second) {
					sb.append(s);
					tail = (null != replace2) ? replace2 : replace1;
					sb.append(tail);
				}
			}
		} else {
			for(int i = 1; i < first.length; i++) {
				sb.append(first[i]);
				tail = (null != replace2) ? replace2 : replace1;
				sb.append(tail);
			}		
		}
		sb.setLength(sb.length() - tail.length());
		return sb.toString();
	}
	*/

	String unpackValue(String line) {
		try {
			String out[] = new String[order.length];
			String[] first = line.split(pattern1);
			int max = 1;
			if (null != pattern2) {
				int offset = 1;
				for(int i = 1; i < first.length; i++) {
					String[] second = first[i].split(pattern2);
					for(int j = 0; j < second.length; j++) {
						if (j + offset < reverse.length && reverse[j + offset] != -1) {
							out[reverse[j + offset]] = second[j];
						}
					}
					offset += second.length;
				}
			} else {
				for(int i = 1; i < first.length; i++) {
					if (i < reverse.length && reverse[i] != -1) {
						out[reverse[i]] = first[i];
					}				
				}		
			}

			if (reverse[0] != -1)
				out[reverse[0]] = first[0];
			StringBuilder sb = new StringBuilder();
			if (null != out[0])
				sb.append(out[0]);
			if (out.length > 1) {
				sb.append(replace1);
				for(int i = 1; i < out.length - 1; i++) {
					if (null != out[i])
						sb.append(out[i]);
					if (null != replace2)
						sb.append(replace2);
					else 
						sb.append(replace1);
				}
			}
			if (null != out[out.length - 1])
				sb.append(out[out.length - 1]);
			return sb.toString();
		} catch (ArrayIndexOutOfBoundsException e) {
			// does this need a jobconf string also?
			throw new Error("Not enough values: " + line);
		}
	}

}
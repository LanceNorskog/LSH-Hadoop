package lsh.sample;

import java.io.File;

import lsh.hadoop.PointMapper;
import lsh.hadoop.PointReducer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.CSVTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/*
 * Solr cinema example
 */

public class SolrDriver {
	public static void main(String[] args) throws Exception {
		Job job = new Job( );
		job.setJobName("Solr cinema");
		Configuration conf = job.getConfiguration();
		conf.addResource("solr-cinema-site.xml");
		String[] otherArgs = new GenericOptionsParser(job.getConfiguration(), args).getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: SolrDriver <in> <out>");
			System.exit(2);
		}
		//	    job.setJarByClass(CornerDriver.class);
		job.setInputFormatClass(CSVTextInputFormat.class);
		job.setMapperClass(PointMapper.class);
		job.setReducerClass(PointReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		rmdir(new File(otherArgs[1]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}

	static void rmdir(File dir) {
		if (null == dir.listFiles())
			return;
		for(File f: dir.listFiles()) {
			f.delete();
		}
		dir.delete();
	}
}

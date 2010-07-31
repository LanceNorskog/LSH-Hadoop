package lsh.hadoop;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import lsh.sample.ElNinoTextFormat;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/*
 * Drive LSH map-reduce jobs
 * 
 * Allow multiple configuration files 
 */

public class LSHDriver {
	public static String IN = "lsh.hadoop.LSHDriver.in";
	public static String OUT = "lsh.hadoop.LSHDriver.out";
	public static String INPUT_FORMAT = "lsh.hadoop.LSHDriver.inputFormat";
	public static String MAPPER = "lsh.hadoop.LSHDriver.mapper";
	public static String REDUCE = "lsh.hadoop.LSHDriver.reducer";
	public static String HASHER = "lsh.hadoop.LSHDriver.hasher";
	public static String GRIDSIZE = "lsh.hadoop.LSHDriver.gridsize";
	
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		for(String xml: args) {
			if (xml.endsWith(".xml")){
				conf.addResource(new Path(args[0]));
			}
		}

		String x = conf.get(MAPPER);
		x = conf.get(REDUCE);
		x = conf.get(IN);
		x = conf.get(OUT);
		Job job = new Job(conf, "From Python 2d version");
		//	    job.setJarByClass(LSHDriver.class);
		job.setMapperClass((Class<? extends Mapper>) Class.forName(conf.get(MAPPER)));
		job.setReducerClass((Class<? extends Reducer>) Class.forName(conf.get(REDUCE)));
		job.setInputFormatClass((Class<? extends InputFormat>) Class.forName(conf.get(INPUT_FORMAT)));
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(conf.get(IN)));
		rmdir(new File(conf.get(OUT)));
		FileOutputFormat.setOutputPath(job, new Path(conf.get(OUT)));
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

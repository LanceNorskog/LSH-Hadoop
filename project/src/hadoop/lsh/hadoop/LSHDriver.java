package lsh.hadoop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import semvec.mahout.TupleWritable;

/*
 * Drive LSH map-reduce jobs
 * 
 * Allow multiple configuration files 
 */

public class LSHDriver {
	public static final String MINVALUE = "lsh.hadoop.LSHDriver.minValue";
	public static final String MAXVALUE = "lsh.hadoop.LSHDriver.maxValue";
	public static final String IN = "lsh.hadoop.LSHDriver.in";
	public static final String OUT = "lsh.hadoop.LSHDriver.out";
	public static final String INPUT_FORMAT = "lsh.hadoop.LSHDriver.inputFormat";
	public static final String OUTPUT_KEY = "lsh.hadoop.LSHDriver.outputKeyClass";
	public static final String OUTPUT_VALUE = "lsh.hadoop.LSHDriver.outputValueClass";
	public static final String MAPPER = "lsh.hadoop.LSHDriver.mapper";
	public static final String REDUCE = "lsh.hadoop.LSHDriver.reducer";
	public static final String HASHER = "lsh.hadoop.LSHDriver.hasher";
	public static final String GRIDSIZE = "lsh.hadoop.LSHDriver.gridsize";
	public static final String DIMENSION = "lsh.hadoop.LSHDriver.dimension";
	
	// yeah yeah GenericOptionsParser
	public static void main(String[] args) throws Exception {
		boolean common = false;
		String commonFile = null;
		List<String> jobFiles = new ArrayList<String>();
		for(String xml: args) {
			if (common) {
				commonFile = xml;
				common = false;
			} else if (xml.equals("-c")) {
				common = true;
			} else {
				jobFiles.add(xml);
			}
		}
		for(String xml: jobFiles) {
			if (xml.endsWith(".xml") && ! xml.equals(commonFile)){
				LSHDriver.removeOutputDir(xml);
			}
		}
		for(String xml: jobFiles) {
			if (xml.endsWith(".xml")){
				LSHDriver.runJob(commonFile, xml);
			}
		}
	}
	
	public static void removeOutputDir(String siteFile) throws IOException, InterruptedException {
		Job job = new Job();
		Configuration conf = job.getConfiguration();
		conf.addResource(new Path(siteFile));
		File outputDir = new File(conf.get(OUT));
		rmdir(outputDir);
		if (outputDir.exists())
			throw new IOException("Cannot remove output dir: " + outputDir.toString());
	}

	public static void runJob(String commonFile, String siteFile) throws Exception {
		Job job = new Job();
		Configuration conf = job.getConfiguration();
		if (null != commonFile) 
			conf.addResource(new Path(commonFile));
		conf.addResource(new Path(siteFile));

		//	    job.setJarByClass(LSHDriver.class);
		String mapper = conf.get(MAPPER);
		job.setMapperClass((Class<? extends Mapper>) Class.forName(mapper));
		if (null != conf.get(REDUCE))
			job.setReducerClass((Class<? extends Reducer>) Class.forName(conf.get(REDUCE)));
		if (null != conf.get(INPUT_FORMAT))
			job.setInputFormatClass((Class<? extends InputFormat>) Class.forName(conf.get(INPUT_FORMAT)));
		if (null != conf.get(OUTPUT_KEY))
			job.setOutputKeyClass((Class<? extends Writable>) Class.forName(conf.get(OUTPUT_KEY)));
		else
			job.setOutputKeyClass(Text.class);
		if (null != conf.get(OUTPUT_VALUE))
			job.setOutputValueClass((Class<? extends InputFormat>) Class.forName(conf.get(OUTPUT_VALUE)));
		else
			job.setOutputValueClass(Text.class);

//		job.setOutputKeyClass(Text.class);
//		job.setOutputValueClass(Text.class);
		Path inpath = new Path(conf.get(IN));
		FileInputFormat.addInputPath(job, inpath);
		Path outpath = new Path(conf.get(OUT));
		FileOutputFormat.setOutputPath(job, outpath);
		if (! job.waitForCompletion(true))
			throw new Exception("Job failed:" + siteFile);
	}

	static void rmdir(File dir) throws IOException, InterruptedException {
		if (null == dir.listFiles())
			return;
		for(File f: dir.listFiles()) {
			if (f.isDirectory())
				rmdir(f);
			f.delete();
		}
		dir.delete();
//		Runtime rt = Runtime.getRuntime();
//		Process p=rt.exec("rm -r \\cygdrive\\c\\" + dir.getAbsolutePath()); 
//		p.waitFor();
//		int x = p.exitValue();
//		p.hashCode();
	}
}

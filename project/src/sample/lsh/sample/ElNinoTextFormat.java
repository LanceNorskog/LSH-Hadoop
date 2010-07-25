package lsh.sample;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

/*
 * Import El Nino whale tracking data
 * Hadoop reader
 * 
 * format: id,,,,lat,long,,,...
 */

public class ElNinoTextFormat extends TextInputFormat {

	@Override
	public RecordReader<LongWritable, Text> 
		createRecordReader(InputSplit split,
			TaskAttemptContext context) {
		return new ElNinoRecordReader();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}
}

class ElNinoRecordReader extends LineRecordReader {

	@Override
	public Text getCurrentValue() {
		Text value = super.getCurrentValue();
		String vs = value.toString();
		String[] fields = vs.split("[ \t]");

		// id,lat/long
		return new Text(fields[0] + "," + fields[5] + "," + fields[6]);
	}

}
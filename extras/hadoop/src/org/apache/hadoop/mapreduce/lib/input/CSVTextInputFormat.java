package org.apache.hadoop.mapreduce.lib.input;

import java.io.IOException;
import java.util.Random;

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

/*
 * Configurable CSV line reader.
 * Variant of TextInputReader that harvests fields from CSV input lines
 * 
 * Use case: 
 *  text data files with one or more data items per line. 
 *  This class rips apart the data items with regex patterns, 
 *  then assembles a new value with 0 or more values.
 *  Items have one common separator, or the first item has a different separator.
 *  The output includes 1 or more items from the list, chosen by order.
 *  The output items are separated by the given 'replace' fields- 
 *  again, the first may be different than the rest.
 *  
 * 	pattern1/pattern2 are these separators - as Java Regex patterns
 *  replace1/replace2 
 *  order = n1,n2,n3...nn  
 *  
 *  pattern2/replace2 are not required
 *  
 *  Includes option for sampling, allowing disjoint sampled sets from different passes
 * 
 *  See main() for examples
 *  
 *  TODO: add date format interpretation: extract Day Of Week from date strings in GroupLens
 *  
 *  How did you envision this modification? Support combined file whatever that is?:

 * org.apache.hadoop.mapred.lib.CombineFileRecordReader<K, V>:144
 * curReader = rrConstructor.newInstance(new Object []
 *    {split, jc, reporter, Integer.valueOf(idx)});
 *    
 *    Another use case: one Wikipedia format is:

1: 1664968
2: 3 747213 1664968 1691047 4095634 5535664

which would read in as:

1: 1664968
2: 3 
2: 747213 
2: 1664968
etc. 
 */

public class CSVTextInputFormat extends FileInputFormat<LongWritable, Text> {

  private static final String FORMAT_PATTERN1 = "mapreduce.csvinput.pattern1";
  private static final String FORMAT_PATTERN2 = "mapreduce.csvinput.pattern2";
  private static final String FORMAT_REPLACE1 = "mapreduce.csvinput.replace1";
  private static final String FORMAT_REPLACE2 = "mapreduce.csvinput.replace2";
  private static final String FORMAT_ORDER = "mapreduce.csvinput.order";
  private static final String FORMAT_PAYLOAD = "mapreduce.csvinput.payload";
  private static final String FORMAT_SAMPLE_SEED = "mapreduce.csvinput.sample.seed";
  private static final String FORMAT_SAMPLE_MIN = "mapreduce.csvinput.sample.min";
  private static final String FORMAT_SAMPLE_MAX = "mapreduce.csvinput.sample.max";

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
    String payload = conf.get(FORMAT_PAYLOAD);
    String sampleSeed = conf.get(FORMAT_SAMPLE_SEED);
    String sampleMin = conf.get(FORMAT_SAMPLE_MIN);
    String sampleMax = conf.get(FORMAT_SAMPLE_MAX);
    if (null == pattern1 || null == replace1 || null == order) {
      throw new IOException("CSVTextFormat: missing parameter pattern1/replace1/order");
    }
    Sampler sampler = null;
    if (null != sampleMax) {
      sampler = new Sampler(sampleSeed, sampleMin, sampleMax);
    }
    return new FlexibleRecordReader(pattern1, pattern2, replace1, replace2, order, payload, sampler);
  }

  @Override
  protected boolean isSplitable(JobContext context, Path file) {
    CompressionCodec codec = 
      new CompressionCodecFactory(context.getConfiguration()).getCodec(file);
    return codec == null;
  }

}

class FlexibleRecordReader extends LineRecordReader {
  final String pattern1, pattern2, replace1, replace2;
  final int[] order;
  final int[] reverse;
  final String payload;
  final Sampler sampler;


  public FlexibleRecordReader(String pattern1, String pattern2,
      String replace1, String replace2, String order, String payload, Sampler sampler) {
    super();
    this.pattern1 = pattern1;
    this.pattern2 = pattern2;
    this.replace1 = replace1;
    this.replace2 = replace2;
    this.payload = payload;
    this.sampler = sampler;
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
    this.hashCode();
  }

  // weird advance thing makes this more complex
  @Override
  public boolean nextKeyValue() throws IOException {
    if (null == sampler)
      return super.nextKeyValue();
    boolean skip = !sampler.sample();
    while (skip) {
      boolean last = super.nextKeyValue();
      if (!last)
        return false;
      // we have now officially skipped a line
      skip = !sampler.sample();
    }
    return super.nextKeyValue();
  }

  @Override
  public Text getCurrentValue() {
    Text value = super.getCurrentValue();
    String line = value.toString();
    String unpacked = unpackValue(line);
    if (null != payload) {
      return new Text(unpacked + payload + line);
    } else {
      return new Text(unpacked);
    }
  }

  String unpackValue(String line) {
    try {
      String out[] = new String[order.length];
      String[] first = line.split(pattern1);
      if (first.length == line.length() + 1) {
        // it's a magic regex character
        // and, yes, this could be done better
        first = line.split("\\" + pattern1);
      }
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

final class Sampler {
  final Random rnd;
  final double sampleMin;
  final double sampleMax;

  Sampler( String sampleSeed, String sampleMin, String sampleMax) {
    this.sampleMax = Double.parseDouble(sampleMax);
    if (null != sampleMin) {
      this.sampleMin = Double.parseDouble(sampleMin);
    } else {
      this.sampleMin = 0.0;
    }
    if (null != sampleSeed) {
      this.rnd = new Random(Long.parseLong(sampleSeed));
    } else {
      this.rnd = new Random();
    }	
  }

  boolean sample() {
    double sample = rnd.nextDouble();
    return (sample >= sampleMin && sample < sampleMax);
  }
}
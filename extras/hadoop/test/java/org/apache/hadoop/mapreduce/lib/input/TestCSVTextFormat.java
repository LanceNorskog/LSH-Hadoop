package org.apache.hadoop.mapreduce.lib.input;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.junit.Test;

import junit.framework.TestCase;

public class TestCSVTextFormat extends TestCase {

  @Test
  public void testFRRGood() {
    FlexibleRecordReader frr;

    frr = new FlexibleRecordReader("::", null, " ", null, "0,1,2", null, null);
    assertEquals("a  ", frr.unpackValue("a"));
    assertEquals("a b ", frr.unpackValue("a::b"));
    assertEquals("a b c", frr.unpackValue("a::b::c"));

    frr = new FlexibleRecordReader(",", ",", ",", ",", "0,5,6", null, null);
    assertEquals("id,lat,long", frr.unpackValue("id,1,2,3,4,lat,long,7"));
    assertEquals("id,,long", frr.unpackValue("id,1,2,3,4,,long,7"));
    assertEquals(",lat,", frr.unpackValue(",1,2,3,4,lat,,7"));
    assertEquals(",,long", frr.unpackValue(",1,2,3,4,,long,7"));
  }

    @Test
    public void testFRRBad() {
      FlexibleRecordReader frr;
  
      frr = new FlexibleRecordReader(",", ",", ",", ",", "0,5,6", null, null);
      try {
        frr.unpackValue(null);
      } catch(Exception e) {
  
      }
      try {
        frr.unpackValue("");
      } catch(Exception e) {
  
      }
      try {
        frr.unpackValue("x");
      } catch(Exception e) {
  
      }
      try {
        frr.unpackValue("x,y");
      } catch(Exception e) {
  
      }
      try {
        frr.unpackValue("x,y,z");
      } catch(Exception e) {
  
      }
      try {
        frr.unpackValue(null);
      } catch(Exception e) {
  
      }
    }

  @Test
  public void testCSVTIFConfig() throws IOException {
    CSVTextInputFormat csvtif = new CSVTextInputFormat();
    Configuration conf = new Configuration();
//    InputStream is = new InputStream(simpleConf);
//    conf.addResource();
    conf.set(simpleConf[0][0], simpleConf[0][1]);
    conf.set(simpleConf[1][0], simpleConf[1][1]);
    conf.set(simpleConf[2][0], simpleConf[2][1]);
    TaskAttemptID tid = new TaskAttemptID();
    TaskAttemptContext tac = new TaskAttemptContext(conf, tid);
    FlexibleRecordReader frr = (FlexibleRecordReader) csvtif.createRecordReader(null, tac);
    assertEquals("a,b,c", frr.unpackValue("a::b::c"));
  }

  // TODO: payload and sampler tests
  
  String[][] simpleConf = {
      {"mapreduce.csvinput.pattern1", "::"},
      {"mapreduce.csvinput.replace1", ","},
      {"mapreduce.csvinput.order", "0,1,2"}
  };
}

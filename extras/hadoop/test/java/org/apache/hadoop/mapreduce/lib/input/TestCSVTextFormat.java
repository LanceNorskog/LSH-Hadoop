package org.apache.hadoop.mapreduce.lib.input;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobHistory.TaskAttempt;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.hsqldb.lib.StringInputStream;
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
    InputStream is = new StringInputStream(simpleConf);
    conf.addResource(is);
    TaskAttemptID tid = new TaskAttemptID();
    TaskAttemptContext tac = new TaskAttemptContext(conf, tid);
    FlexibleRecordReader frr = (FlexibleRecordReader) csvtif.createRecordReader(null, tac);
    assertEquals("a,b,c", frr.unpackValue("a::b::c"));
  }

  // TODO: payload and sampler tests

  static final String simpleConf = 
    "<?xml version=\"1.0\"?>" +
    "<configuration>\r\n" + 
    "    <property>\r\n" + 
    "        <name>mapreduce.csvinput.pattern1</name>\r\n" + 
    "        <value>::</value>\r\n" + 
    "        <description>CSV input field separator #1</description>\r\n" + 
    "    </property>\r\n" + 
    "\r\n" + 
    "    <property>\r\n" + 
    "        <name>mapreduce.csvinput.replace1</name>\r\n" + 
    "        <value>,</value>\r\n" + 
    "        <description>CSV input field separator replacement #1</description>\r\n" + 
    "    </property>\r\n" + 
    "\r\n" + 
    "    <property>\r\n" + 
    "        <name>mapreduce.csvinput.order</name>\r\n" + 
    "        <value>0,1,2</value>\r\n" + 
    "        <description>CSV input field harvest order.</description>\r\n" + 
    "    </property>\r\n" + 
    "</configuration>" +
    "";
}

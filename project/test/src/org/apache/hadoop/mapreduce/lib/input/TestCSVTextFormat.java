package org.apache.hadoop.mapreduce.lib.input;

import junit.framework.TestCase;

public class TestCSVTextFormat extends TestCase {
	
	public void testGood() {
		FlexibleRecordReader frr;

		frr = new FlexibleRecordReader("::", null, " ", null, "0,1,2");
		assertEquals("a", frr.unpackValue("a"));
		assertEquals("a b", frr.unpackValue( frr.unpackValue("a::b")));
		assertEquals("a b,c", frr.unpackValue("a::b::c"));

		frr = new FlexibleRecordReader(",", ",", ",", ",", "0,5,6");
		assertEquals("id,lat,long", frr.unpackValue("id,1,2,3,4,lat,long,7"));
		assertEquals("id,,long", frr.unpackValue("id,1,2,3,4,,,7"));
		assertEquals(",lat,", frr.unpackValue(",1,2,3,4,lat,,7"));
		assertEquals(",,long", frr.unpackValue(",1,2,3,4,,long,7"));

	}
	
	public void testBad() {
		FlexibleRecordReader frr;

		frr = new FlexibleRecordReader(",", ",", ",", ",", "0,5,6");
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

}

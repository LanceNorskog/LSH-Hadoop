package lsh.core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class CSV {

  /**
   * @param args
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException {
    boolean doUser = false;
    boolean doPoints = true;
    boolean doName = false;
    int n = 0;

    File input = new File(args[n]);
    Reader lshReader = new FileReader(input);
    Lookup box = new Lookup(doPoints, !doPoints);
    if (doPoints) {
      box.loadPoints(lshReader, doUser ? "U" : "I");
    } else {
      box.loadCorners(lshReader, doUser ? "U" : "I");
    }
    StringBuilder sb = new StringBuilder();
    if (doPoints) {
     for(Point p: box.points) {
        if (doName) {
          sb.append(p.id);
          sb.append(',');
        }
        for(int i = 0; i < p.values.length; i++) {
          sb.append(Double.toString(p.values[i]));
          sb.append(',');
        }
        sb.setLength(sb.length() - 1);
        System.out.println(sb);
        sb.setLength(0);
      }
    } else {
      for(Corner c: box.corners) {
        for(int i = 0; i < c.hashes.length; i++) {
          sb.append(Integer.toString(c.hashes[i]));
          sb.append(',');
        }
        sb.setLength(sb.length() - 1);
        System.out.println(sb);
        sb.setLength(0);
      }
      
    }
  }
}

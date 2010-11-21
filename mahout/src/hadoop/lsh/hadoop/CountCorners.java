package lsh.hadoop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.PrintWriter;

public class CountCorners {

  public static void main(String[] args) throws IOException {
    LineNumberReader lnr = new LineNumberReader(new InputStreamReader(System.in));
    PrintStream pw = System.out;
    String line = null;
    while ((line = lnr.readLine()) != null) {
      String parts[] = line.split("[\t ]");
      String corners = parts[0];
      String points[] = parts[1].split("|");
      int num = points.length;
      pw.println(corners + " " + num);
    }
  }

}

package hack;

import java.util.Date;
import java.util.Random;

public class Gaussian {

  /**
   * @param args
   */
  public static void main(String[] args) {
    Random rnd = new Random();
    gaussian(rnd);
//    System.out.println("Date: " + new Date().toString());
//    long start = System.currentTimeMillis();
//    gaussian(rnd);
//    System.out.println("gaussian: " + (System.currentTimeMillis() - start));
//     start = System.currentTimeMillis();
//     g01(rnd);
//     System.out.println("gaussian 01: " + (System.currentTimeMillis() - start));
//      start = System.currentTimeMillis();
//      linear(rnd);
//      System.out.println("linear: " + (System.currentTimeMillis() - start));
//       start = System.currentTimeMillis();
//       System.out.println("Date: " + new Date().toString());

  }

  private static void g01(Random rnd) {
    for (int i = 0; i < 50000; i++) {
      double d = rnd.nextDouble() + rnd.nextDouble() + rnd.nextDouble() + rnd.nextDouble()
      + rnd.nextDouble() + rnd.nextDouble() + rnd.nextDouble() + rnd.nextDouble() + rnd.nextDouble();
      d /= 9;
//      System.out.println(i+ "," + d);
    }
  }
  private static void gtest01(Random rnd) {
    int dropped = 0;
    for (int i = 0; i < 5000; i++) {
      double d = rnd.nextGaussian() + rnd.nextGaussian() + rnd.nextGaussian() + rnd.nextGaussian()
      + rnd.nextGaussian() + rnd.nextGaussian() + rnd.nextGaussian() + rnd.nextGaussian() + rnd.nextGaussian()
      + rnd.nextGaussian() + rnd.nextGaussian() + rnd.nextGaussian() + rnd.nextGaussian();
      d /= 13;
      if (d > 0.5 || d < -0.5) {
        dropped++;
        continue;
      }
     System.out.println(i+ "," + d);
    }
    System.out.println("Dropped: " + dropped);
  }


  private static void gaussian(Random rnd) {
    int dropped = 0;
    for (int i = 0; i < 5000; i++) {
      double d = rnd.nextGaussian()/6;
      if (d > 0.5 || d < -0.5) {
        dropped++;
        continue;
      }
      System.out.println(i+ "," + d);
    }
    System.out.println("Dropped: " + dropped);
}

  private static void linear(Random rnd) {
    for (int i = 0; i < 50000; i++) {
      double d = rnd.nextDouble();
 
//      System.out.println(i+ "," + d);
    }
  }

}

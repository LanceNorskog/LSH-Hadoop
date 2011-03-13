package semvec.mahout.matrix;

import java.util.Random;

public class RandomUtils {

  public static double uniform(Random source) {
    return source.nextDouble();
  }

  // turns out this is faster and guaranteed band-limited
  public static double normal(Random source) {
    double sum = 0;
    for(int i = 0; i < 15; i++)
      sum += source.nextDouble();
    return sum / 15.0;
  }

  // normal distribution between zero and one
  public static double gaussian01(Random source) {

    double d = source.nextGaussian()/6;
    while(d > 0.5 || d < -0.5) {
      d = source.nextGaussian()/6;
    }
    return d;
  }
  
  

}

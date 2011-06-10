package org.apache.mahout.math;

public class MurmurHashRand {

  long seed;
  
  public MurmurHashRand(long seed) {
    this.seed = seed;
  }
  
  private double nextGaussian = Double.NaN;

  /** {@inheritDoc} */
  public double nextGaussian() {
      final double random;
      if (Double.isNaN(nextGaussian)) {
          // generate a new pair of gaussian numbers
          final double x = nextDouble();
          final double y = nextDouble();
          final double alpha = 2 * Math.PI * x;
          final double r      = Math.sqrt(-2 * Math.log(y));
          random       = r * Math.cos(alpha);
          nextGaussian = r * Math.sin(alpha);
      } else {
          // use the second element of the pair already generated
          random = nextGaussian;
          nextGaussian = Double.NaN;
      }
      return random;
  }

  private double nextDouble() {
    // TODO Auto-generated method stub
    return 0;
  }


}

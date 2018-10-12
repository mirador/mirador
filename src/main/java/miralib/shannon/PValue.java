package miralib.shannon;

import miralib.data.DataSlice2D;
import miralib.data.Variable;
import miralib.utils.Project;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

public class PValue extends Statistics {
  public static double MAX_SCORE = 9;
  public static double MIN_VALUE = 10E-9;
  protected static double SELF_SCORE = -2 * Math.log10(Float.MIN_VALUE);

  static public float[] calculate(DataSlice2D slice, Project prefs) {
    if (slice.notComparable()) return new float[] {0, 1};

    int[] res = BinOptimizer.calculate(slice, prefs.binAlgorithm);
    int binx = res[0];
    int biny = res[1];
//    if (binx < 2 || biny < 2) return new float[] {0, 1};
    
    float ixy = MutualInformation.calculate(slice, binx, biny);

    if (slice.self()) return new float[] {ixy, 0};

    float pval = pvalue(slice, ixy, binx, biny, prefs);
    return new float[] {ixy, pval};
  }

  // This method requires the binning already calculated on the slice itself
  static public float calculate(DataSlice2D slice, float ixy, Project prefs) {
    if (slice.self()) return 0;
    if (slice.notComparable()) return 1;
    return pvalue(slice, ixy, slice.binx, slice.biny, prefs);
  }

  static public float getScore(DataSlice2D slice, float pval) {
    float res = 0;
    if (0 < pval) {
      res = -(float)Math.log10(pval);
    } else if (slice.self()) {
      res = (float)SELF_SCORE;
    } else {
      res = 0;
    }
    if (Float.isNaN(res)) res = 0;
    return res;
  }

  static protected float pvalue(DataSlice2D slice, float ixy, int binx, int biny, Project prefs) {
    float pval = 0;
    if (Float.isNaN(ixy) || Float.isInfinite(ixy)) {
      pval = 0;
    } else if (prefs.depTest == DependencyTest.NO_TEST) {
      pval = 0;
    } else if (prefs.depTest == DependencyTest.SURROGATE_GAUSS) {
      pval = (float) surrogateGaussP(slice, ixy, prefs.binAlgorithm, prefs.surrCount);
    } else if (prefs.depTest == DependencyTest.SURROGATE_GENERAL) {
      pval = (float) surrogateGeneralP(slice, ixy, prefs.binAlgorithm);
    } else if (prefs.depTest == DependencyTest.GAMMA_TEST) {
      pval = (float) gammaTestP(ixy, binx, biny, slice.values.size());
    }
    if (pval < Float.MIN_VALUE) {
      pval = Float.MIN_VALUE;
    }
    return pval;
  }
  
  static protected double surrogateGaussP(DataSlice2D slice, float ixy,
                                          int binAlgo, int scount) {
    float zs = getSurrogateGaussDistribution(slice, ixy, binAlgo, scount);
    try { 
      // Not so sure about getting the P-value from the statistic zs in this way...
      NormalDistribution normDist = new NormalDistribution();
      return 1 - normDist.cumulativeProbability(zs);
    } catch (Exception ex) {
      return 1;
    }       
  }
  
  static protected double surrogateGeneralP(DataSlice2D slice, float ixy,
                                            int binAlgo) {
    // We don't have the distribution under the null hypothesis assumption
    return 0;
  }
  
  static protected double gammaTestP(float ixy, int binx, int biny, int count) {
    double shapePar = (binx - 1) * (biny - 1) / 2d;
    double scalePar = 1d / count;
    try {
      GammaDistribution gammaDist = new GammaDistribution(shapePar, scalePar);
      return 1 - gammaDist.cumulativeProbability(ixy);
    } catch (Exception ex) {
      return 1;
    }    
  }
}

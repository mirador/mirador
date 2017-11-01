package miralib.shannon;

import miralib.data.DataSlice2D;
import miralib.data.Variable;
import miralib.utils.Project;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

public class PValue {
  public static double MIN_VALUE = 10E-9;
  protected static double SELF_SCORE = -2 * Math.log10(Float.MIN_VALUE);

  static public float[] calculate(DataSlice2D slice, Project prefs) {
    Variable varx = slice.varx;
    Variable vary = slice.vary;

    if (varx.weight() || vary.weight() || (varx.subsample() && vary.subsample())) {
      // weight variables are not comparable, or subsample variables between 
      // each other
      return new float[] {0, 0};
    } 
        
    int count = slice.values.size();
    int[] res = BinOptimizer.calculate(slice, prefs.binAlgorithm);
    int binx = res[0];
    int biny = res[1];
    if (binx < 2 || biny < 2) return new float[] {0, 1};
    
    float ixy = MutualInformation.calculate(slice, binx, biny);
    float pval = 0;
    
    if (varx == vary) return new float[] {ixy, pval};
            
    if (Float.isNaN(ixy) || Float.isInfinite(ixy)) {
      pval = 0;
    } else if (prefs.depTest == DependencyTest.NO_TEST) {
      pval = 0;
    } else if (prefs.depTest == DependencyTest.SURROGATE_GAUSS) {
      pval = (float)surrogateGauss(slice, ixy, prefs.binAlgorithm, prefs.surrCount);            
    } else if (prefs.depTest == DependencyTest.SURROGATE_GENERAL) {      
      pval = (float)surrogateGeneral(slice, ixy, prefs.binAlgorithm);
    } else if (prefs.depTest == DependencyTest.GAMMA_TEST) {
      pval = (float)gammaTest(ixy, binx, biny, count);
    }
    if (pval < Float.MIN_VALUE) {
      pval = Float.MIN_VALUE;
    }
    
    return new float[] {ixy, pval};
  }

  static public float getScore(DataSlice2D slice, float pval) {
    float res = 0;
    if (0 < pval) {
      res = -(float)Math.log10(pval);
    } else if (slice.varx == slice.vary) {
      res = (float)SELF_SCORE;
    } else {
      res = 0;
    }
    if (Float.isNaN(res)) res = 0;
    return res;
  }
  
  static protected double surrogateGauss(DataSlice2D slice, float ixy,
                                         int binAlgo, int scount) {
    int sbinx = 0;
    int sbiny = 0;         
    float meani = 0;
    float meaniSq = 0;
    float stdi = 0; 
    for (int i = 0; i < scount; i++) {
      DataSlice2D surrogate = slice.shuffle();          
      if (i == 0) {
        int[] sres = BinOptimizer.calculate(surrogate, binAlgo);
        sbinx = sres[0];
        sbiny = sres[1];
      }
      float smi = MutualInformation.calculate(surrogate, sbinx, sbiny);      
      meani += smi;
      meaniSq += smi * smi;
    }
    meani /= scount;
    meaniSq /= scount;
    stdi = (float)Math.sqrt(Math.max(0, meaniSq - meani * meani));      
    float zs = Math.abs((ixy - meani) / stdi);
    
    try { 
      // Not so sure about getting the P-value from the statistic zs in this
      // way...
      NormalDistribution normDist = new NormalDistribution();
      return 1 - normDist.cumulativeProbability(zs);
    } catch (Exception ex) {
      return 1;
    }       
  }
  
  static protected double surrogateGeneral(DataSlice2D slice, float ixy, 
                                           int binAlgo) {
    // We don't have the distribution under the null hypothesis assumption
    return 0;
  }
  
  static protected double gammaTest(float ixy, int binx, int biny, int count) {
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

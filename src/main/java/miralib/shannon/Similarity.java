/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.shannon;

import miralib.data.DataSlice2D;
import miralib.data.Variable;
import miralib.math.Numbers;
import miralib.utils.Project;

/**
 * Similarity score between two variables.
 *
 */

public class Similarity extends Statistics {
  static public float calculate(DataSlice2D slice, float pvalue, Project prefs) {
    if (slice.self()) return 1;
    if (!slice.comparable()) return 0;

    int[] res = BinOptimizer.calculate(slice, prefs.binAlgorithm);
    int binx = res[0];
    int biny = res[1];
    
    float ixy = MutualInformation.calculate(slice, binx, biny);
    boolean indep = independenceTest(slice, ixy, binx, biny, pvalue, prefs);
    if (indep) return 0;
    else return calculate(slice, ixy, binx, biny);
  }

  // This method requires the binning already calculated on the slice itself
  static public float calculate(DataSlice2D slice, float ixy, float pvalue, Project prefs) {
    if (slice.self()) return 1;
    if (!slice.comparable()) return 0;

    boolean indep = independenceTest(slice, ixy, slice.binx, slice.biny, pvalue, prefs);
    if (indep) return 0;
    else return calculate(slice, ixy);
  }

  static public float calculate(DataSlice2D slice, float ixy) {
    return calculate(slice, ixy, slice.binx, slice.biny);
  }

  static public float calculate(DataSlice2D slice, float ixy, int binx, int biny) {
    float hxy = JointEntropy.calculate(slice, binx, biny);
    float w;
    if (Numbers.equal(0.0, hxy)) {
      w = 0;
    } else {
      w = Numbers.constrain(ixy / hxy, 0, 1);
      if (Float.isNaN(w)) w = 0;
    }
    return w;
  }

  // Overloaded so user can quickly compare different algorithms
  static public float calculate(DataSlice2D slice, Project prefs, int depTest, int numTail, int binAlgo, float pvalue) {
    //prefs.pvalue() = pvalue;
    //int binAlgo = prefs.binAlgo;
    if (slice.self()) return 1;
    if (!slice.comparable()) return 0;

    int[] res = BinOptimizer.calculate(slice, prefs.binAlgorithm);
    int binx = res[0];
    int biny = res[1];

    float ixy = MutualInformation.calculate(slice, binx, biny);
    boolean indep = independenceTest(slice, ixy, binx, biny, prefs, depTest, numTail, binAlgo, pvalue);
    if (indep) return 0;
    else return calculate(slice, ixy, binx, biny);
  }
}

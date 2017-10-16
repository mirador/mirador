/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.shannon;

import miralib.data.DataSlice1D;
import miralib.data.Value1D;
import miralib.math.Numbers;
import miralib.utils.Project;

/**
 * Calculation of the marginal entropy of a 1D slice.
 *
 */

public class MarginalEntropy {
  static public float calculate(DataSlice1D slice, Project prefs) {
    int nbin = BinOptimizer.calculate(slice, prefs.binAlgorithm);
    return calculate(slice, nbin);
  }
  
  static public float calculate(DataSlice1D slice, int nbin) {
    if (nbin < 2) return 0;
    
    float sbin = 1.0f / nbin;
    double[] counts = new double[nbin];
    
    double total = 0;
    for (Value1D value: slice.values) {
      int bx = Numbers.constrain((int)(value.x / sbin), 0, nbin - 1);    
      counts[bx] += value.w;
      total += value.w;
    }
          
    double entropy = 0;
    for (int bx = 0; bx < nbin; bx++) {
      double px = counts[bx] / total;
      double hbin = 0 < px ? -px * Math.log(px) : 0; 
      entropy += hbin;
    }
    
    if (entropy < 0 || Double.isNaN(entropy)) return 0;
    
    // Finite size correction
    double correction = (nbin - 1) / (2 * total);
    return (float)(entropy + correction);       
  }
}

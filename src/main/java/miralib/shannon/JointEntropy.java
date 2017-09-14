/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.shannon;

import miralib.data.DataSlice2D;
import miralib.data.Value2D;
import miralib.math.Numbers;
import miralib.utils.Project;

/**
 * Calculation of the joint entropy of a 2D slice.
 *
 */

public class JointEntropy {
  static public float calculate(DataSlice2D slice, Project prefs) {
    int[] nbins = BinOptimizer.calculate(slice, prefs.binAlgorithm);
    return calculate(slice, nbins[0], nbins[1]);
  }
  
  static public float calculate(DataSlice2D slice, int nbinx, int nbiny) {
    if (nbinx < 2 || nbiny < 2) return 0;
    
    float sbinx = 1.0f / nbinx;
    float sbiny = 1.0f / nbiny;
    double[][] counts = new double[nbinx][nbiny];
    
    double total = 0;
    for (Value2D value: slice.values) {
      int bx = Numbers.constrain((int)(value.x / sbinx), 0, nbinx - 1);  
      int by = Numbers.constrain((int)(value.y / sbiny), 0, nbiny - 1);  
      counts[bx][by] += value.w;
      total += value.w;
    }
    
    double entropy = 0;
    int nonzero = 0;      
    for (int bx = 0; bx < nbinx; bx++) {
      for (int by = 0; by < nbiny; by++) {          
        double pxy = counts[bx][by] / total;
        
        double hbin = 0;
        if (0 < pxy) {
          nonzero++;
          hbin = -pxy * Math.log(pxy);
        }
        
        entropy += hbin;
      }
    }    
    
    if (entropy < 0 || Double.isNaN(entropy)) return 0;
    
    // Finite size correction
//    double correction = (nbinx * nbiny - 1) / (2 * total);
    double correction = (nonzero - 1) / (2 * total);
    return (float)(entropy + correction);    
  }  
}

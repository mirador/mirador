/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.shannon;

import miralib.data.DataSlice2D;
import miralib.data.Value2D;
import miralib.utils.Project;

/**
 * Calculation of the mutual information of a 2D slice.
 *
 */


public class MutualInformation {
  static public float calculate(DataSlice2D slice, Project prefs) {
    int[] nbins = BinOptimizer.calculate(slice, prefs.binAlgorithm);
    return calculate(slice, nbins[0], nbins[1]);    
  }
  
  static public float calculate(DataSlice2D slice, int nbinx, int nbiny) {
    if (nbinx < 2 || nbiny < 2) return 0;
    
    float sbinx = 1.0f / nbinx;
    float sbiny = 1.0f / nbiny;
    double[] countsx = new double[nbinx];
    double[] countsy = new double[nbiny];    
    double[][] counts = new double[nbinx][nbiny];
    
    boolean singlebx = true;
    boolean singleby = true;
    int lastbx = -1;
    int lastby = -1;   
    double total = 0;
    for (Value2D value: slice.values) {
      int bx = (int)Math.min(value.x / sbinx, nbinx - 1);  
      int by = (int)Math.min(value.y / sbiny, nbiny - 1);
      
      if (bx < 0 || by < 0) {
        System.err.println("Error: a bin index is negative: " + bx + " " + nbinx + "| " + by + " " + nbiny);
        continue;
      }
      
      counts[bx][by] += value.w;
      countsx[bx] += value.w;
      countsy[by] += value.w;
      
      if (lastbx != -1 && lastbx != bx) {
        singlebx = false;
      }
      if (lastby != -1 && lastby != by) {
        singleby = false;
      }
      
      lastbx = bx;
      lastby = by;        
      total += value.w;
    }
    
    // Pairs with a only one occupied bin along X or Y are considered 
    // independent, because the statistics are insufficient 
    if (singlebx || singleby) {
      return 0;        
    }
    
    double information = 0;
    int nonzero = 0; 
    for (int bx = 0; bx < nbinx; bx++) {
      for (int by = 0; by < nbiny; by++) {         
        double pxy = counts[bx][by] / total;
        double px = countsx[bx] / total;
        double py = countsy[by] / total;

        double ibin = 0; 
        if (0 < pxy && 0 < px && 0 < py) {
          nonzero++;
          ibin = pxy * (Math.log(pxy / (px * py)));
        }

        information += ibin;
      }
    }    
    
    if (information < 0 || Double.isNaN(information)) return 0;
    
    // Finite size correction: "The Mutual Information: Detecting and evaluating
    // dependencies between variables", pp S234.
    // nonzeroBins instead of binCountX * binCountY?
    double correction = (nonzero - nbinx - nbiny + 1) / (2 * total);
    return (float)Math.max(0, information - correction);        
  }  
}

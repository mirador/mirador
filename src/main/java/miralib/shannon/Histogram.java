package miralib.shannon;

import java.util.ArrayList;

import miralib.data.DataSlice2D;
import miralib.data.Value2D;
import miralib.math.Numbers;
import miralib.utils.Project;

public class Histogram {
  static public ArrayList<ArrayList<Double>> calculate(DataSlice2D slice, Project prefs) {
    int[] nbins = BinOptimizer.calculate(slice, prefs.binAlgorithm);
    return calculate(slice, nbins[0], nbins[1]);
  }
  
  static public int[][] calculate2DArray(DataSlice2D slice, int binAlgo) {
	  int[] nbins = BinOptimizer.calculate(slice, binAlgo);
	  return calculate2DArray(slice, nbins[0], nbins[1]);
  }
  
  static public ArrayList<ArrayList<Double>> calculate(DataSlice2D slice, int nbinx, int nbiny) {
    if (nbinx < 2 || nbiny < 2) return null;
    
    float sbinx = 1.0f / nbinx;
    float sbiny = 1.0f / nbiny;
    double[][] counts = new double[nbinx][nbiny];
    
    for (Value2D value: slice.values) {
      int bx = Numbers.constrain((int)(value.x / sbinx), 0, nbinx - 1);  
      int by = Numbers.constrain((int)(value.y / sbiny), 0, nbiny - 1);  
      counts[bx][by] += value.w;
    }
    
    ArrayList<ArrayList<Double>> hist = new ArrayList<ArrayList<Double>>();
    for (int bx = 0; bx < nbinx; bx++) {
      ArrayList<Double> colx = new ArrayList<Double>();
      for (int by = 0; by < nbiny; by++) {
        colx.add(counts[bx][by]);
      }
      hist.add(colx);
    }
    
    return hist;
  }  
  
  static public int[][] calculate2DArray(DataSlice2D slice, int nbinx, int nbiny) {
	    if (nbinx < 2 || nbiny < 2) return null;
	    
	    float sbinx = 1.0f / nbinx;
	    float sbiny = 1.0f / nbiny;
	    int[][] counts = new int[nbinx][nbiny];
	    
	    for (Value2D value: slice.values) {
	      int bx = Numbers.constrain((int)(value.x / sbinx), 0, nbinx - 1);  
	      int by = Numbers.constrain((int)(value.y / sbiny), 0, nbiny - 1);  
	      counts[bx][by] += value.w;
	    }
	    return counts;
	  }
  
}
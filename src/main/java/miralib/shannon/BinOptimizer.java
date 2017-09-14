/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.shannon;

import java.util.ArrayList;

import processing.core.PApplet;
import miralib.data.DataSlice1D;
import miralib.data.DataSlice2D;
import miralib.data.Value1D;
import miralib.data.Value2D;
import miralib.math.Numbers;
import miralib.utils.Log;

/**
 * Optimal Histogram bin size calculation. Three different methods can be chosen
 * to select the number of bins:
 * 1. Rice's rule
 * 2. Scott's normal reference rule
 * 3. Poisson point process from Shimazaki and Shinomoto:
 * http://toyoizumilab.brain.riken.jp/hideaki/res/histogram.html
 * 4. Cross-validation from Solis
 * https://maikolsolis.wordpress.com/2014/04/26/optimizing-histogram-cross-validation/
 */

public class BinOptimizer {
  final static public int RICE     = 0;
  final static public int SCOTT    = 2;
  final static public int POISSON  = 3;
  final static public int CROSSVAL = 4;
  
  // These parameters dramatically affect the performance of the optimization algorithm
  static int MAX_SEARCH_SAMPLE_SIZE = 1000; // Won't evaluate more than this number of bins while searching for the optimal size
  static int MAX_HIST_BINS = 100;           // No more than this number of bins per variable.
  static int MAX_RES_SAMPLE_SIZE = 10;      // Number of values sampled to search for minimum difference
  static int MAX_HIST_SAMPLE_SIZE = 10000;  // number of values used to estimate the histograms during optimization
  static boolean PRINT_ERRORS = false;
  
  static public int calculate(DataSlice1D slice, int method) {
    if (slice.varx.categorical()) return (int)slice.countx;
      
    int size = slice.values.size();    
    int hsize = size / 2;
        
    int minNBins, maxNBins;
    if (slice.countx < 5) {
      minNBins = maxNBins = (int)slice.countx;
    } else {
      minNBins = 2;
      long lcount = slice.countx;
      int icount = Integer.MAX_VALUE < lcount ? Integer.MAX_VALUE : (int)lcount;
      float res = (float)res(slice.values);
      maxNBins = Numbers.min((int)(1.0f/res) + 1, icount, hsize);
    }
    
    if (method == RICE) {
      int n = (int)(2 * Math.pow(size, 1d/3d));
      return PApplet.constrain(n, minNBins, maxNBins);
    } else if (method == SCOTT) {
      double[] res = slice.getMeanStd();
      int n = (int)(Math.pow(size, 1d/3d) / (3.5f * res[1]));
      return PApplet.constrain(n, minNBins, maxNBins);
    }
    
    int numValues = maxNBins - minNBins + 1;
    
    if (minNBins <= 0 || maxNBins <= 0 || numValues <= 0) {
      if (PRINT_ERRORS) {
        Log.message("Unexpected error number of bin values is negative. Bin limits: " + 
                    "[" + minNBins + ", " + maxNBins + "]");
      }
      return 1;
    }
    
    int minn = (minNBins + maxNBins)/2;
    
    float minc = Float.MAX_VALUE;
    int mod = Math.max(1, numValues / MAX_SEARCH_SAMPLE_SIZE);
    for (int i = 0; i < numValues; i += mod) {
      int n = minNBins + i;
      float bsize = 1.0f / n;
      double[] counts = hist1D(slice.values, n);
      
      float c = 0;
      if (method == POISSON) {
        c = (float)poissonCost(size, bsize, counts);  
      } else if (method == CROSSVAL) {
        c = (float)crossvalCost(size, bsize, counts);        
      } 
      
      if (c < minc) {
        minc = c;
        minn = n;
      }           
    }
    return minn;
  }

  static public int[] calculate(DataSlice2D slice, int method) {
    if (slice.varx.categorical() && slice.vary.categorical()) {
      return new int[] {(int)slice.countx, (int)slice.county};
    }
    
    int size = slice.values.size();
    int sqsize = (int)Math.sqrt(size / 2);
    
    int minNBins0, maxNBins0;
    if (slice.varx.categorical()) {
      minNBins0 = maxNBins0 = (int)slice.countx;
    } else if (slice.countx < 5) {
      minNBins0 = maxNBins0 = (int)slice.countx;
    } else {
      minNBins0 = 2;
      long lcount = slice.countx;
      int icount = Integer.MAX_VALUE < lcount ? Integer.MAX_VALUE : (int)lcount;
      float res = (float)resx(slice.values);   
      maxNBins0 = Numbers.min((int)(1.0f/res) + 1, icount, sqsize);
    }
    
    int minNBins1, maxNBins1;
    if (slice.vary.categorical()) {
      minNBins1 = maxNBins1 = (int)slice.county;
    } else if (slice.county < 5) {
      minNBins1 = maxNBins1 = (int)slice.county;
    } else {
      minNBins1 = 2;
      long lcount = slice.county;
      int icount = Integer.MAX_VALUE < lcount ? Integer.MAX_VALUE : (int)lcount;
      float res = (float)resy(slice.values);            
      maxNBins1 = Numbers.min((int)(1.0f/res) + 1, icount, sqsize);
    }
    
    if (method == RICE) {
      int nxy = (int)Math.sqrt((2 * Math.pow(size, 1d/3d)));
      int nx = PApplet.constrain(nxy, minNBins0, maxNBins0);
      int ny = PApplet.constrain(nxy, minNBins1, maxNBins1);
      return new int[] {nx, ny};      
    } else if (method == SCOTT) {
      double[] resx = slice.getMeanStdX();
      double[] resy = slice.getMeanStdY();      
      int nx = PApplet.constrain((int)(Math.pow(size, 1d/3d) / (3.5f * resx[1])), 
                                 minNBins0, maxNBins0);
      int ny = PApplet.constrain((int)(Math.pow(size, 1d/3d) / (3.5f * resy[1])), 
                                 minNBins1, maxNBins1);      
      return new int[] {nx, ny};
    }  
    
    int blen0 = maxNBins0 - minNBins0 + 1;
    int blen1 = maxNBins1 - minNBins1 + 1;
    int numValues = blen0 * blen1; 
        
    if (minNBins0 <= 0 || maxNBins0 <= 0 || blen0 <= 0 || 
        minNBins1 <= 0 || maxNBins1 <= 1 || blen1 <= 0) {
      if (PRINT_ERRORS) {
        Log.message("Unexpected error number of bin values is negative. Bin limits: " + 
                    "[" + minNBins0 + ", " + maxNBins0 + "] x " + 
                    "[" + minNBins1 + ", " + maxNBins1 + "]");
      }
      return new int[] {1, 1};
    }
      
    int minn0 = (minNBins0 + maxNBins0)/2;
    int minn1 = (minNBins1 + maxNBins1)/2;
    float minc = Float.MAX_VALUE;
    int mod = Math.max(1, numValues / MAX_SEARCH_SAMPLE_SIZE);
    for (int i = 0; i < numValues; i += mod) {
      int n0 = i / blen1 + minNBins0;
      int n1 = i % blen1 + minNBins1;
      float bsize0 = 1.0f / n0; 
      float bsize1 = 1.0f / n1;
      float barea = bsize0 * bsize1;          
      double[][] counts = hist2D(slice.values, n0, n1);
      
      float c = 0;
      if (method == POISSON) {
        c = (float)poissonCost(size, barea, counts);  
      } else if (method == CROSSVAL) {
        c = (float)crossvalCost(size, barea, counts);        
      }
      
      if (c < minc) {
        minc = c;
        minn0 = n0;
        minn1 = n1;
      }           
    }
    return new int[] {minn0, minn1};
  } 

  static protected double poissonCost(int n, double h, double[] counts) {
    double[] res = countsMeanDev(counts);
    double k = res[0];
    double v = res[1];
    double cost = (2 * k - v) / (n * n * h * h);
    return cost;
  }  
  
  static protected double poissonCost(int n, double h, double[][] counts) {
    double[] res = countsMeanDev(counts);
    double k = res[0];
    double v = res[1];
    double cost = (2 * k - v) / (n * n * h * h);
    return cost;
  }
  
  static protected double crossvalCost(int n, double h, double[] counts) {
    double sum = countsSqSum(counts);
    double cost = 2 / ((n - 1) * h) - ((n + 1) / ((n * n) * (n - 1) * h)) * sum;
    return cost;  
  }  
  
  static protected double crossvalCost(int n, double h, double[][] counts) {
    double sum = countsSqSum(counts);
    double cost = 2 / ((n - 1) * h) - ((n + 1) / ((n * n) * (n - 1) * h)) * sum;
    return cost;  
  }

  static public double[] hist1D(ArrayList<Value1D> values, int bnum) {    
    double[] counts = new double[bnum];
    float bsize = 1.0f / bnum;
    int mod = Math.max(1, values.size() / MAX_HIST_SAMPLE_SIZE);
    for (int i = 0; i < values.size(); i += mod) {
      Value1D value = values.get(i);
      int bin = Numbers.constrain((int)(value.x / bsize), 0, bnum - 1);
      counts[bin] += value.w;
    }
    return counts; 
  }

  static public double[][] hist2D(ArrayList<Value2D> values,                                
                                  int bnumx, int bnumy) {
    double[][] counts = new double[bnumx][bnumy];
    float bsizex = 1.0f / bnumx; 
    float bsizey = 1.0f / bnumy; 
    int mod = Math.max(1, values.size() / MAX_HIST_SAMPLE_SIZE);
    for (int i = 0; i < values.size(); i += mod) {
      Value2D value = values.get(i);
      int binx = Numbers.constrain((int)(value.x / bsizex), 0, bnumx - 1);
      int biny = Numbers.constrain((int)(value.y / bsizey), 0, bnumy - 1);    
      counts[binx][biny] += value.w;
    }
    return counts; 
  }

  static protected double[] countsMeanDev(double[] counts) {
    int n = counts.length;
    double sum = 0;  
    double sumsq = 0;
    for (int i = 0; i < n; i++) {
      double count = counts[i];
      sum += count;
      sumsq += count * count; 
    }
    // VERY IMPORTANT: Do NOT use a variance that uses N-1 to divide the sum of 
    // squared errors. Use the biased sample variance in the method.
    double mean = sum / n;
    double meansq = sumsq / n;
    double dev = Math.max(0, meansq - mean * mean);    
    return new double[] {mean, dev};    
  }    
  
  static protected double countsSqSum(double[] counts) {
    int n = counts.length;
    double sumsq = 0;
    for (int i = 0; i < n; i++) {
      double count = counts[i];
      sumsq += count * count; 
    }
    return sumsq;
  }
  
  static protected double res(ArrayList<Value1D> values) {
    double res = Double.POSITIVE_INFINITY;
    int mod = Math.max(1, values.size() / MAX_RES_SAMPLE_SIZE);
    for (int i = 0; i < values.size(); i += mod) {
      Value1D vali = values.get(i);
      for (int j = 0; j < values.size(); j++) {
        Value1D valj = values.get(j);
        double diff = Math.abs(valj.x - vali.x);
        if (0 < diff) {
          res = Math.min(res, diff);
        }        
      }
    }    
    return Math.max(res, 1.0d / MAX_HIST_BINS);
  } 
    
  static protected double[] countsMeanDev(double[][] counts) {
    int ni = counts.length;
    int nj = counts[0].length;
    int n = ni * nj;
    double sum = 0;
    double sumsq = 0;
    for (int i = 0; i < ni; i++) {
      for (int j = 0; j < nj; j++) {
        double count = counts[i][j];
        sum += count;
        sumsq += count * count; 
      }
    }
    // VERY IMPORTANT: Do NOT use a variance that uses N-1 to divide the sum of 
    // squared errors. Use the biased sample variance in the method.    
    double mean = sum / n;
    double meansq = sumsq / n;
    double dev = Math.max(0, meansq - mean * mean);    
    return new double[] {mean, dev};
  }
  
  static protected double countsSqSum(double[][] counts) {
    int ni = counts.length;
    int nj = counts[0].length;
    double sumsq = 0;
    for (int i = 0; i < ni; i++) {
      for (int j = 0; j < nj; j++) {
        double count = counts[i][j];
        sumsq += count * count; 
      }
    }
    return sumsq;
  } 
  
  static protected double resx(ArrayList<Value2D> values) {
    double res = Double.POSITIVE_INFINITY;
    int mod = Math.max(1, values.size() / MAX_RES_SAMPLE_SIZE);
    for (int i = 0; i < values.size(); i += mod) {
      Value2D vali = values.get(i);
      for (int j = 0; j < values.size(); j++) {
        Value2D valj = values.get(j);
        double diff = Math.abs(valj.x - vali.x);
        if (0 < diff) {
          res = Math.min(res, diff);
        }        
      }
    }
    return Math.max(res, 1.0d / MAX_HIST_BINS);
  }  

  static protected double resy(ArrayList<Value2D> values) {
    double res = Double.POSITIVE_INFINITY;
    int mod = Math.max(1, values.size() / MAX_RES_SAMPLE_SIZE);
    for (int i = 0; i < values.size(); i += mod) {
      Value2D vali = values.get(i);
      for (int j = 0; j < values.size(); j++) {
        Value2D valj = values.get(j);
        double diff = Math.abs(valj.y - vali.y);
        if (0 < diff) {
          res = Math.min(res, diff);
        }        
      }
    }    
    return Math.max(res, 1.0d / MAX_HIST_BINS);
  }
    
  static public String algorithmToString(int algo) {
    if (algo == RICE) {
      return "RICE";
    } else if (algo == SCOTT) {
      return "SCOTT";
    } else if (algo == POISSON) {
      return "POISSON";
    } else if (algo == CROSSVAL) {
      return "CROSSVAL";
    }
    String err = "Unsupported similarity algorithm: " + algo;
    Log.error(err, new RuntimeException(err));
    return "unsupported";    
  }
  
  static public int stringToAlgorithm(String name) {
    name = name.toUpperCase();
    if (name.equals("RICE")) {
      return RICE;
    } else if (name.equals("SCOTT")) {
      return SCOTT;
    } else if (name.equals("POISSON")) {
      return POISSON;
    } else if (name.equals("CROSSVAL")) {
      return CROSSVAL;
    } 
    String err = "Unsupported similarity algorithm: " + name;
    Log.error(err, new RuntimeException(err));
    return -1;
  }  
}

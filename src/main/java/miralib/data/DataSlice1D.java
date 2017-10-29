/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.data;

import java.util.ArrayList;

import miralib.math.Numbers;
import processing.data.TableRow;

/**
 * 1-dimensional data slice, i.e.: all the (normalized) data values for a single
 * variable from the rows that satisfy the range conditions. 
 *
 */

public class DataSlice1D {
  static public int MAX_SLICE_SIZE = 1000000;
  
  public Variable varx;
  public DataRanges ranges;
  public ArrayList<Value1D> values;
  public long countx;
  public float missing;
  
  public DataSlice1D(Variable varx, DataRanges ranges) {
    this.varx = varx;
    this.values = new ArrayList<Value1D>();
    
    // Create a copy of the ranges, because they can change after the slice 
    // has been constructed    
    this.ranges = new DataRanges(ranges);
  }
  
  public DataSlice1D(DataSource data, Variable varx, DataRanges ranges) {
    this(data, varx, ranges, null);
  }
  
  public DataSlice1D(DataSource data, Variable varx, DataRanges ranges, 
                     Variable varl) {
    this.varx = varx;
    this.values = new ArrayList<Value1D>();
    
    // Create a copy of the ranges, because they can change after the slice 
    // has been constructed    
    this.ranges = new DataRanges(ranges);
    
    init(data, varl);
  } 
  
  public void dispose() {
    values.clear();
  } 
  
  public void add(Value1D value) {
    values.add(value);
  }

  public Value1D add(double x, double w) {
    Value1D value = new Value1D(x, w);
    values.add(value);
    return value;
  } 
  
  public void setMissing(float missing) {
    this.missing = missing;
  }
  
  public void setCount(long countx) {
    this.countx = countx;
  } 
  
  public void normalizeWeights(double factor) {
    for (Value1D val: values) {
      val.w *= factor;
    }
  }
  
  public double[] getMeanStd() {
    double mean = 0;
    double meanSq = 0;
    double std = 0; 
    for (Value1D val: values) {
      double x = val.x * val.w;
      mean += x;
      meanSq += x * x;
    }
    mean /= values.size();
    meanSq /= values.size();
    std = Math.sqrt(Math.max(0, meanSq - mean * mean));
    return new double[] {mean, std};
  }
  
  protected void init(DataSource data, Variable varl) {
    int ntot = 0;
    int nmis = 0;
    double wsum = 0;
    int rcount = data.getRowCount();
    float p = (float)MAX_SLICE_SIZE / (float)rcount;
    for (int r = 0; r < rcount; r++) {
      if (p < 1 && p < Math.random()) continue;
      TableRow row = data.getRow(r);       
      if (!DataSet.insideRanges(row, ranges)) continue;
      ntot++;      
      double valx = varx.getValue(row, ranges);
      double w = varx.getWeight(row);
      if (valx < 0 || w < 0) {
        nmis++;
        continue;
      }      
      Value1D val = add(valx, w);  
      if (varl != null && val != null) {
        val.label = varl.formatValue(row);        
      }      
      wsum += w;
    }
    long countx = varx.getCount(ranges);
    setCount(countx);
    setMissing((float)nmis/(float)ntot);
    double factor = (ntot - nmis) / wsum;
    if (Numbers.different(factor, 1)) {
      normalizeWeights(factor);  
    }    
  }
}

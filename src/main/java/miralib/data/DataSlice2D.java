/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.data;

import java.util.ArrayList;
import java.util.Collections;

import processing.data.TableRow;
import miralib.math.Numbers;
import miralib.utils.Project;

/**
 * 2-dimensional data slice, i.e.: all the (normalized) data value pairs for two 
 * variables from the rows that satisfy the range conditions. 
 *
 */

public class DataSlice2D {
  public Variable varx, vary;
  public DataRanges ranges;
  public ArrayList<Value2D> values;
  public long countx, county;
  public float missing;
  
  public DataSlice2D(Variable varx, Variable vary, DataRanges ranges) {
    this.varx = varx;
    this.vary = vary;
    this.values = new ArrayList<Value2D>();
    
    // Create a copy of the ranges, because they can change after the slice 
    // has been constructed.    
    this.ranges = new DataRanges(ranges);
  }
  
  public DataSlice2D(DataSource data, Variable varx, Variable vary, DataRanges ranges, int maxSize) {
    this(data, varx, vary, ranges, null, maxSize);
  }
  
  public DataSlice2D(DataSource data, Variable varx, Variable vary, 
                     DataRanges ranges, Variable varl, int maxSize) {
    this.varx = varx;
    this.vary = vary;
    this.values = new ArrayList<Value2D>();
    
    // Create a copy of the ranges, because they can change after the slice 
    // has been constructed.    
    this.ranges = new DataRanges(ranges);
    
    init(data, varl, maxSize);
  }  
  
  public DataSlice2D shuffle() {
    ArrayList<Value1D> valuesx = new ArrayList<Value1D>();
    ArrayList<Value1D> valuesy = new ArrayList<Value1D>();    
    for (Value2D val: values) {
      valuesx.add(new Value1D(val.x, val.w));
      valuesy.add(new Value1D(val.y, val.w));
    }
    Collections.shuffle(valuesx);
    Collections.shuffle(valuesy);
    DataSlice2D shuffled = new DataSlice2D(varx, vary, ranges);



    for (int n = 0; n < values.size(); n++) {
      shuffled.add(new Value2D(valuesx.get(n), valuesy.get(n))); 
    }    
    shuffled.countx = countx;
    shuffled.county = county;
    shuffled.missing = missing;
    return shuffled;  
  }
  
  public void dispose() {
    values.clear();
  }
  
  public void add(Value2D value) {
    values.add(value);
  }

  public Value2D add(double x, double y, double w) {
    Value2D value = new Value2D(x, y, w);
    values.add(value);
    return value;
  }  
  
  public void setMissing(float missing) {
    this.missing = missing;
  }
  
  public void setCount(long sizex, long sizey) {
    this.countx = sizex;
    this.county = sizey;
  }
  
  public void normalizeWeights(double factor) {
    for (Value2D val: values) {
      val.w *= factor;
    }
  }
  
  public double[] getMeanStdX() {
    double mean = 0;
    double meanSq = 0;
    double std = 0; 
    for (Value2D val: values) { 
      double x = val.x * val.w;
      mean += x;
      meanSq += x * x;
    }
    mean /= values.size();
    meanSq /= values.size();
    std = Math.sqrt(Math.max(0, meanSq - mean * mean));
    return new double[] {mean, std};
  }
  
  public double[] getMeanStdY() {
    double mean = 0;
    double meanSq = 0;
    double std = 0; 
    for (Value2D val: values) { 
      double y = val.y * val.w;
      mean += y;
      meanSq += y * y;
    }
    mean /= values.size();
    meanSq /= values.size();
    std = Math.sqrt(Math.max(0, meanSq - mean * mean));
    return new double[] {mean, std};
  }    
  
  public DataSlice1D getSliceX() {
    DataSlice1D slice = new DataSlice1D(varx, ranges);
    for (Value2D val: values) {
      slice.add(val.x, val.w);
    }
    slice.setCount(countx);
    slice.setMissing(missing);
    return slice;
  }
  
  public DataSlice1D getSliceY() {
    DataSlice1D slice = new DataSlice1D(vary, ranges);
    for (Value2D val: values) {
      slice.add(val.y, val.w);
    }
    slice.setCount(county);
    slice.setMissing(missing);
    return slice;
  }

  public ContingencyTable getContingencyTable(int nbinx, int nbiny) {
    return new ContingencyTable(this, nbinx, nbiny);
  }  
  
  public ContingencyTable getContingencyTable(Project prefs) {
    return new ContingencyTable(this, prefs.binAlgorithm);
  }
  
  protected void init(DataSource data, Variable varl, int maxSize) {
    int ntot = 0;
    int nmis = 0;    
    double wsum = 0;  
    int rcount = data.getRowCount();
    float p = (float)maxSize / (float)rcount;
    for (int r = 0; r < rcount; r++) {
      if (p < 1 && p < Math.random()) continue;
      TableRow row = data.getRow(r);        
      if (!DataSet.insideRanges(row, ranges)) continue;
      ntot++;
      double valx = varx.getValue(row, ranges);
      double valy = vary.getValue(row, ranges);
      double w = Variable.getWeight(row, varx, vary);
      if (valx < 0 || valy < 0 || w < 0) {
        nmis++;
        continue;
      }
      Value2D val = add(valx, valy, w);
      if (varl != null && val != null) {
        val.label = varl.formatValue(row);        
      }
      wsum += w;
    }
    long countx = varx.getCount(ranges);
    long county = vary.getCount(ranges);    
    setCount(countx, county);
    setMissing((float)nmis/(float)ntot);    
    double factor = (ntot - nmis) / wsum;    
    if (Numbers.different(factor, 1)) {
      normalizeWeights(factor);
    }
  }
}

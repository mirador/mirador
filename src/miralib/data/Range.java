/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.data;

import java.util.ArrayList;

import processing.data.TableRow;

/**
 * Base abstract class to define variable ranges.
 *
 */

abstract public class Range {
  public Variable var;
  
  public Range(Variable var) {
    this.var = var;
  }
  
  public Range(Range another) {
    this.var = another.var;
  }
  
  public void set(double min, double max) { set(min, max, true); }
  abstract public void set(double min, double max, boolean normalized);
  abstract public void set(ArrayList<String> values);
  abstract public void set(String... values);
  
  abstract public void reset();
  abstract public void update(TableRow row);
    
  abstract public boolean inside(TableRow row);
  
  abstract public double getMin();
  abstract public double getMax();
  abstract public long getCount();
  
  abstract public int getRank(String value);
  abstract public int getRank(String value, Range supr);
  abstract public ArrayList<String> getValues(); 
  
  abstract public double snap(double value);
  
  abstract public double normalize(int value);
  abstract public double normalize(long value);
  abstract public double normalize(float value);
  abstract public double normalize(double value);

  abstract double denormalize(double value);
  
  abstract public int constrain(int value);
  abstract public long constrain(long value);
  abstract public float constrain(float value);
  abstract public double constrain(double value);  
  
  protected double normalizeImpl(double value) {
    double min = getMin(); 
    double max = getMax();    
    double f = (value - min) / (max - min);
    if (f < 0 || 1 < f) f = -1; // This is important, the -1 is used to identify missing values in the dataset.
    return f;
  }  
  
  protected double constrainImpl(double value) {
    return (value < getMin()) ? getMin() : ((value > getMax()) ? getMax() : value);
  }
  
  static public Range create(Range that) {
    Range range = null;
    if (that instanceof NumericalRange) {
      range = new NumericalRange((NumericalRange)that);
    } else if (that instanceof CategoricalRange) {
      range = new CategoricalRange((CategoricalRange)that);
    } else if (that instanceof DateRange) {
      range = new DateRange((DateRange)that);
    } else {
      throw new RuntimeException("Wrong range type");
    }
    return range;
  }
}

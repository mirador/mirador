/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.data;

import java.util.ArrayList;

import org.joda.time.DateTime;

import processing.data.TableRow;

public class DateRange extends Range {
  protected DateTime mind, maxd;
  
  public DateRange(Variable var) {
    super(var);
  }

  public DateRange(DateRange that) {
    super(that);
    this.mind = new DateTime(that.mind).withTimeAtStartOfDay();
    this.maxd = new DateTime(that.maxd).withTimeAtStartOfDay();
  }
  
  public void set(double min, double max, boolean normalized) {
    if (normalized) {
      long minl = Math.round(var.range.denormalize(min));
      long maxl = Math.round(var.range.denormalize(max));
      mind = new DateTime(minl).withTimeAtStartOfDay();
      maxd = new DateTime(maxl).withTimeAtStartOfDay();
    } else {
      long minl = Math.round(min);
      long maxl = Math.round(max);
      mind = new DateTime(minl).withTimeAtStartOfDay();
      maxd = new DateTime(maxl).withTimeAtStartOfDay();
    }    
  }

  public void set(ArrayList<String> values) {
    String[] array = new String[values.size()]; 
    values.toArray(array);
    set(array);    
  }

  public void set(String... values) {
    if (values == null) return;
    String dat0, dat1;
    if (values.length == 1) {      
      String[] datarray = values[0].split(",");
      if (datarray.length == 2) {
        dat0 = datarray[0];
        dat1 = datarray[1];       
      } else {
        dat0 = dat1 = "";
      }      
    } else {
      dat0 = values[0];
      dat1 = values[1];
    }
    mind = DateVariable.parse(dat0);
    maxd = DateVariable.parse(dat1);
    if (mind == null) mind = new DateTime("1900-01-01").withTimeAtStartOfDay();
    if (maxd == null) maxd = new DateTime("2099-12-31").withTimeAtStartOfDay();
  }

  public void reset() {
    if (mind == null) mind = new DateTime("2099-12-31").withTimeAtStartOfDay();
    if (maxd == null) maxd = new DateTime("1900-01-01").withTimeAtStartOfDay();
  }

  public void update(TableRow row) {
    int idx = var.getIndex();
    String value = row.getString(idx);
    DateTime dat = DateVariable.parse(value);
    if (dat != null) {
      if (dat.compareTo(mind) < 0) mind = new DateTime(dat);
      if (0 < dat.compareTo(maxd)) maxd = new DateTime(dat);      
    }    
  }

  public boolean inside(TableRow row) {
    int idx = var.getIndex();
    String value = row.getString(idx);
    DateTime dat = DateVariable.parse(value);
    if (dat != null) {
      return 0 <= dat.compareTo(mind) && dat.compareTo(maxd) <= 0;       
    }
    return false;
  }

  public double getMin() {
    return mind.getMillis();
  }

  public double getMax() {
    return maxd.getMillis();
  }

  public long getCount() {
    return Long.MAX_VALUE;
  }

  public int getRank(String value) {
    return -1;
  }

  public int getRank(String value, Range supr) {
    return -1;
  }

  public ArrayList<String> getValues() {
    ArrayList<String> values = new ArrayList<String>();    
    values.add(DateVariable.print(mind));
    values.add(DateVariable.print(maxd));
    return values;
  }

  public double snap(double value) {
    return constrain((long)value);
  }

  public double normalize(int value) {
    return normalizeImpl(value); 
  }
  
  public double normalize(long value) {
    return normalizeImpl(value); 
  }
  
  public double normalize(float value) {
    return normalizeImpl(value);
  }
  
  public double normalize(double value) {
    return normalizeImpl(value);
  }
  
  public double denormalize(double value) {
    double min = getMin(); 
    double max = getMax();        
    return min + value * (max - min);    
  } 

  public int constrain(int value) {
    return (int)constrainImpl(value);
  }
  
  public long constrain(long value) {
    return (long)constrainImpl(value);
  }
  
  public float constrain(float value) {
    return (float)constrainImpl(value);
  }
  
  public double constrain(double value) {
    return constrainImpl(value);
  }
  
  public boolean equals(Object that) {
    if (this == that) return true;
    if (that instanceof DateRange) {
      DateRange range = (DateRange)that;
      return this.mind.compareTo(range.mind) == 0 && this.maxd.compareTo(range.maxd) == 0;
    }
    return false;
  }
  
  public String toString() {
    String val0 = DateVariable.print(mind);
    String val1 = DateVariable.print(maxd);    
    return val0 + "," + val1;
  }
}

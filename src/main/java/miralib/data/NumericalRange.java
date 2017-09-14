/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.data;

import java.util.ArrayList;
import miralib.math.Numbers;
import processing.data.Table;
import processing.data.TableRow;

/**
 * Range for a numerical variable (a minimum and a maximum value).
 *
 */

public class NumericalRange extends Range {
  protected int type;
  
  protected int mini, maxi;
  protected long minl, maxl;
  protected float minf, maxf;
  protected double mind, maxd;  
  
  public NumericalRange(Variable var) {
    super(var);
    this.type = var.type();
  }
  
  public NumericalRange(NumericalRange that) {
    super(that);
    this.type = that.type;
    if (type == Table.INT) {
      this.mini = that.mini;
      this.maxi = that.maxi;
    } else if (type == Table.LONG) {
      this.minl = that.minl;
      this.maxl = that.maxl;
    } else if (type == Table.FLOAT) {
      this.minf = that.minf;
      this.maxf = that.maxf;
    } else if (type == Table.DOUBLE) {
      this.mind = that.mind;
      this.maxd = that.maxd;
    }
  }
  
  public void set(double min, double max, boolean normalized) {
    if (normalized) {
      if (type == Table.INT) {
        mini = (int)Math.round(var.range.denormalize(min));
        maxi = (int)Math.round(var.range.denormalize(max));
      } else if (type == Table.LONG) {
        minl = Math.round(var.range.denormalize(min));
        maxl = Math.round(var.range.denormalize(max));
      } else if (type == Table.FLOAT) {
        minf = (float)var.range.denormalize(min);
        maxf = (float)var.range.denormalize(max);
      } else if (type == Table.DOUBLE) {
        mind = var.range.denormalize(min);
        maxd = var.range.denormalize(max);
      }
    } else {
      if (type == Table.INT) {
        mini = (int)Math.round(min);
        maxi = (int)Math.round(max);
      } else if (type == Table.LONG) {
        minl = Math.round(min);
        maxl = Math.round(max);
      } else if (type == Table.FLOAT) {
        minf = (float)min;
        maxf = (float)max;
      } else if (type == Table.DOUBLE) {
        mind = min;
        maxd = max;
      }
    }
  }
  
  public void set(ArrayList<String> values) {
    String[] array = new String[values.size()]; 
    values.toArray(array);
    set(array);
  }  
  
  public void set(String... values) {
    if (values == null) return;
    String val0, val1;
    if (values.length == 1) {
      String[] valarray = values[0].split(",");
      if (valarray.length == 2) {
        val0 = valarray[0];
        val1 = valarray[1];       
      } else {
        val0 = val1 = "";
      }
    } else {
      val0 = values[0];
      val1 = values[1];     
    }
    if (type == Table.INT) {
      mini = Numbers.parseInt(val0);
      maxi = Numbers.parseInt(val1);
    } else if (type == Table.LONG) {
      minl = Numbers.parseLong(val0);
      maxl = Numbers.parseLong(val1);
    } else if (type == Table.FLOAT) {
      minf = Numbers.parseFloat(val0);
      maxf = Numbers.parseFloat(val1);
    } else if (type == Table.DOUBLE) {
      mind = Numbers.parseDouble(val0);
      maxd = Numbers.parseDouble(val1);
    }
  }  
  
  public void reset() {
    if (type == Table.INT) {
      mini = Integer.MAX_VALUE;
      maxi = Integer.MIN_VALUE;      
    } else if (type == Table.LONG) {
      minl = Long.MAX_VALUE;
      maxl = Long.MIN_VALUE;      
    } else if (type == Table.FLOAT) {
      minf = Float.MAX_VALUE;
      maxf = Float.MIN_VALUE;      
    } else if (type == Table.DOUBLE) {
      mind = Double.MAX_VALUE;
      maxd = Double.MIN_VALUE;      
    }
  }
  
  public void update(TableRow row) {
    int idx = var.getIndex();
    if (type == Table.INT) {
      int value = row.getInt(idx);
      mini = Math.min(mini, value);
      maxi = Math.max(maxi, value);
    } else if (type == Table.LONG) {
      long value = row.getLong(idx);
      minl = Math.min(minl, value);
      maxl = Math.max(maxl, value);      
    } else if (type == Table.FLOAT) {
      float value = row.getFloat(idx);
      minf = Math.min(minf, value);
      maxf = Math.max(maxf, value);
    } else if (type == Table.DOUBLE) {
      double value = row.getDouble(idx);
      mind = Math.min(mind, value);
      maxd = Math.max(maxd, value);        
    }    
  }
  
  public boolean inside(TableRow row) {
    int idx = var.getIndex();
    if (type == Table.INT) {
      int v = row.getInt(idx);
      return mini <= v && v <= maxi;
    } else if (type == Table.LONG) {
      long v = row.getLong(idx);
      return minl <= v && v <= maxl;
    } else if (type == Table.FLOAT) {
      float v = row.getFloat(idx);
      return minf <= v && v <= maxf;
    } else if (type == Table.DOUBLE) {
      double v = row.getDouble(idx);
      return mind <= v && v <= maxd;
    } else {
      return false;
    }     
  }
  
  public double getMin() {
    if (type == Table.INT) {
      return mini;
    } else if (type == Table.LONG) {
      return minl;
    } else if (type == Table.FLOAT) {
      return minf;
    } else if (type == Table.DOUBLE) {
      return mind;
    } else {
      return 0;
    }    
  }
  
  public double getMax() {
    if (type == Table.INT) {
      return maxi;
    } else if (type == Table.LONG) {
      return maxl;
    } else if (type == Table.FLOAT) {
      return maxf;
    } else if (type == Table.DOUBLE) {
      return maxd;
    } else {
      return 0;
    }        
  }
  
  public long getCount() {
    if (type == Table.INT) {
      return maxi - mini + 1;
    } else if (type == Table.LONG) {
      return maxl - minl + 1;
    } else if (type == Table.FLOAT) {
      return Long.MAX_VALUE;
    } else if (type == Table.DOUBLE) {
      return Long.MAX_VALUE;
    } else {
      return 0;
    } 
  }
  
  public int getRank(String value) {
    return -1;
  }
  
  public int getRank(String value, Range supr) {
    return -1;  
  }
  
  public ArrayList<String> getValues() {
    ArrayList<String> values = new ArrayList<String>();
    if (type == Table.INT) {
      values.add(Numbers.nfc(mini));
      values.add(Numbers.nfc(maxi));
    } else if (type == Table.LONG) {
      values.add(Numbers.nfc(minl));
      values.add(Numbers.nfc(maxl));
    } else if (type == Table.FLOAT) {
      values.add(Numbers.nfc(minf, 2));
      values.add(Numbers.nfc(maxf, 2));
    } else if (type == Table.DOUBLE) {
      values.add(Numbers.nfc(mind, 2));
      values.add(Numbers.nfc(maxd, 2));
    }
    return values;
  }
    
  public double snap(double value) {
    if (type == Table.INT) {
      return constrain((int)Math.round(value));
    } else if (type == Table.LONG) {
      return constrain(Math.round(value));
    } else if (type == Table.FLOAT) {      
      return constrain((float)value);
    } else if (type == Table.DOUBLE) {     
      return constrain(value);
    } else {
      return value;
    }
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
    if (that instanceof NumericalRange) {
      NumericalRange range = (NumericalRange)that; 
      if (this.var != range.var || this.type != range.type) return false;
      if (type == Table.INT) {
        return this.mini == range.mini && this.maxi == range.maxi;
      } else if (type == Table.LONG) {
        return this.minl == range.minl && this.maxl == range.maxl;
      } else if (type == Table.FLOAT) {
        return Numbers.equal(this.minf, range.minf) && Numbers.equal(this.maxf, range.maxf);  
      } else if (type == Table.DOUBLE) {
        return Numbers.equal(this.mind, range.mind) && Numbers.equal(this.maxd, range.maxd);
      }
      return false;
    }
    return false;
  }
  
  public String toString() {
    String val0 = "";
    String val1 = "";
    if (type == Table.INT) {
      val0 = String.valueOf(mini);
      val1 = String.valueOf(maxi);
    } else if (type == Table.LONG) {
      val0 = String.valueOf(minl);
      val1 = String.valueOf(maxl);
    } else if (type == Table.FLOAT) {
      val0 = String.valueOf(minf);
      val1 = String.valueOf(maxf);
    } else if (type == Table.DOUBLE) {
      val0 = String.valueOf(mind);
      val1 = String.valueOf(maxd);
    }
    return val0 + "," + val1;
  }
}

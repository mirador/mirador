/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.data;

import java.util.ArrayList;

import miralib.math.Numbers;
import processing.data.Table;
import processing.data.TableRow;

/**
 * Subclass of Variable to represent a numerical variable (integer, long, float
 * or double).
 *
 */

public class NumericalVariable extends Variable {  
  // Missing values for numerical variables   
  final static public int MISSING_INT       = Integer.MIN_VALUE;
  final static public long MISSING_LONG     = Long.MIN_VALUE;
  final static public float MISSING_FLOAT   = Float.NEGATIVE_INFINITY;
  final static public double MISSING_DOUBLE = Double.NEGATIVE_INFINITY;  
  
  protected int type;
 
  protected int[] spValInt;
  protected long[] spValLong;
  protected float[] spValFloat;
  protected double[] spValDouble;  
  
  public NumericalVariable(String name, int index, int type) {
    super(name, index);
    this.type = type;
    range = new NumericalRange(this);
  }

  public void initValues(String valstr) {
    String[] all = valstr.split(";");    
    if (1 < all.length) {
      // There are special values, will override the min/max that were computed
      // directly from the data.
      String[] minmax = all[0].split(",");
      range.set(minmax); 
      String[] spval = new String[all.length - 1];
      for (int i = 0; i < spval.length; i++) {
        spval[i] = all[i + 1].split(":")[0];
      }
      if (type == Table.INT) {        
        spValInt = new int[spval.length];
        for (int i = 0; i < spval.length; i++) {
          spValInt[i] = Numbers.parseInt(spval[i]);     
        }
      } else if (type == Table.LONG) {
        spValLong = new long[spval.length];
        for (int i = 0; i < spval.length; i++) {
          spValLong[i] = Numbers.parseLong(spval[i]);     
        }
      } else if (type == Table.FLOAT) {
        spValFloat = new float[spval.length];
        for (int i = 0; i < spval.length; i++) {
          spValFloat[i] = Numbers.parseFloat(spval[i]);     
        }      
      } else if (type == Table.DOUBLE) {
        spValDouble = new double[spval.length];
        for (int i = 0; i < spval.length; i++) {
          spValDouble[i] = Numbers.parseDouble(spval[i]);     
        }      
      }      
    }
  }
  
  public Range createRange(double val0, double val1) {
    Range range = new NumericalRange(this);    
    range.set(val0, val1, false);
    return range;    
  }
  
  public Range createRange(double min, double max, boolean normalized) {
    Range range = new NumericalRange(this);
    range.set(min, max, normalized);
    return range;
  }
  
  public Range createRange(ArrayList<String> values) {
    Range range = new NumericalRange(this);   
    range.set(values);
    return range;
  }
    
  public Range createRange(String... values) {
    Range range = new NumericalRange(this);
    range.set(values);
    return range;
  }
  
  public int type() {
    return type;
  }
  
  public boolean discrete() {
    return type == Table.INT || type == Table.LONG;  
  }
  
  public boolean numerical() {
    return true;
  }
  
  public boolean categorical() {
    return false;
  }
  
  public boolean string() {
    return false;
  }  
  
  public boolean missing(TableRow row) {
    boolean miss = true;
    if (type == Table.INT) {
      int value = row.getInt(index);
      miss = value == MISSING_INT || specialValue(value);
    } else if (type == Table.LONG) {
      long value = row.getLong(index); 
      miss = value == MISSING_LONG || specialValue(value);
    } else if (type == Table.FLOAT) {
      float value = row.getFloat(index);
      miss = value == MISSING_FLOAT || specialValue(value);      
    } else if (type == Table.DOUBLE) {
      double value = row.getDouble(index);
      miss = value == MISSING_DOUBLE || specialValue(value);
    }
    return miss;
  }
  
  public double getValue(String str, boolean normalized) {
    double value = 0;
    if (type == Table.INT) {
      value = Numbers.parseInt(str);
    } else if (type == Table.LONG) {
      value = Numbers.parseLong(str);
    } else if (type == Table.FLOAT) {
      value = Numbers.parseFloat(str);
    } else if (type == Table.DOUBLE) {
      value = Numbers.parseDouble(str);
    }
    
    if (normalized) {
      return range.normalize(value);        
    } else {
      return value;
    }    
  }
  
  public double getValue(TableRow row, Range sel, boolean normalized) {
    double value = 0;
    if (type == Table.INT) {
      int ivalue = row.getInt(index);
      if (ivalue == MISSING_INT || specialValue(ivalue)) return -1;
      value = ivalue;      
    } else if (type == Table.LONG) {
      long lvalue = row.getLong(index);
      if (value == MISSING_LONG || specialValue(lvalue)) return -1;
      value = lvalue;      
    } else if (type == Table.FLOAT) {
      float fvalue = row.getFloat(index);
      if (value == MISSING_FLOAT || specialValue(fvalue)) return -1;
      value = fvalue;      
    } else if (type == Table.DOUBLE) {
      double dvalue = row.getDouble(index);
      if (value == MISSING_DOUBLE || specialValue(dvalue)) return -1;
      value = dvalue;      
    }
    
    if (normalized) {
      if (sel == null) {
        return range.normalize(value);
      } else {
        return sel.normalize(value);
      }        
    } else {
      return value;
    }
  }
  
  public String formatValue(TableRow row) {
    if (type == Table.INT) {
      int value = row.getInt(index);
      if (value == MISSING_INT || specialValue(value)) return "missing";
      return Numbers.nfc(value);
    } else if (type == Table.LONG) {
      long value = row.getLong(index);
      if (value == MISSING_LONG || specialValue(value)) return "missing";
      return Numbers.nfc(value);
    } else if (type == Table.FLOAT) {
      float value = row.getFloat(index);
      if (value == MISSING_FLOAT || specialValue(value)) return "missing";
      return Numbers.nfc(value, 2);
    } else if (type == Table.DOUBLE) {
      double value = row.getDouble(index);
      if (value == MISSING_DOUBLE || specialValue(value)) return "missing";
      return Numbers.nfc(value, 2);
    } else {
      return "";
    }
  }  
  
  public String formatValue(double value, boolean normalized) { 
    if (type == Table.INT) {
      int ival = normalized ? (int)Math.round(range.denormalize(value)) : (int)Math.round(value);
      if (ival == MISSING_INT || specialValue(ival)) return "missing";
      return Numbers.nfc(ival);
    } else if (type == Table.LONG) {
      long lval = normalized ? Math.round(range.denormalize(value)) : Math.round(value);
      if (lval == MISSING_LONG || specialValue(lval)) return "missing";
      return Numbers.nfc(lval);
    } else if (type == Table.FLOAT) {
      float fval = normalized ? (float)range.denormalize(value) : (float)value;
      if (fval == MISSING_FLOAT || specialValue(fval)) return "missing";
      return Numbers.nfc(fval, 2);
    } else if (type == Table.DOUBLE) {
      double dval = normalized ? range.denormalize(value) : value;      
      if (dval == MISSING_DOUBLE || specialValue(dval)) return "missing";
      return Numbers.nfc(dval, 2);
    } else {
      return "";
    }
  }
  
  public String formatValue(double value, Range sel) {    
    if (type == Table.INT) {
      int ival = sel == null ? (int)Math.round(range.denormalize(value)) : (int)Math.round(sel.denormalize(value));
      if (ival == MISSING_INT || specialValue(ival)) return "missing";
      return Numbers.nfc(ival);
    } else if (type == Table.LONG) {
      long lval = sel == null ? Math.round(range.denormalize(value)) : Math.round(sel.denormalize(value));
      if (lval == MISSING_LONG || specialValue(lval)) return "missing";
      return Numbers.nfc(lval);
    } else if (type == Table.FLOAT) {
      float fval = sel == null ? (float)range.denormalize(value) : (float)sel.denormalize(value);
      if (fval == MISSING_FLOAT || specialValue(fval)) return "missing";
      return Numbers.nfc(fval, 2);
    } else if (type == Table.DOUBLE) {
      double dval = sel == null ? range.denormalize(value) : sel.denormalize(value);      
      if (dval == MISSING_DOUBLE || specialValue(dval)) return "missing";
      return Numbers.nfc(dval, 2);
    } else {
      return "";
    }    
  }
  
  public boolean valueAlias(String value) {
    return false;
  }

  public double snapValue(double value, Range sel, boolean normalized) {
    if (normalized) {
      double denorm = 0;
      if (sel == null) {
        denorm = range.denormalize(value);
        if (type == Table.INT) {
          return range.normalize((int)Math.round(denorm));  
        } else if (type == Table.LONG) {
          return range.normalize(Math.round(denorm));
        } else {
          return range.normalize(denorm);
        }
      } else {
        denorm = sel.denormalize(value);
        if (type == Table.INT) {
          return sel.normalize((int)Math.round(denorm));  
        } else if (type == Table.LONG) {
          return sel.normalize(Math.round(denorm));
        } else {
          return sel.normalize(denorm);
        }        
      }      
    } else {
      if (sel == null) {
        return range.snap(value);
      } else {
        return sel.snap(value);
      }
    }
  }
  
  public String formatRange(Range sel, boolean humanReadable) {
    ArrayList<String> values = sel == null? range.getValues() : sel.getValues();
    if (humanReadable) {
      return values.get(0) + " to " + values.get(1);  
    } else {
      return values.get(0) + "," + values.get(1);  
    }    
  }
  
  protected double getWeightImpl(TableRow row) {
    if (type == Table.INT) {
      return row.getInt(index) ;
    } else if (type == Table.LONG) {      
      return row.getLong(index);
    } else if (type == Table.FLOAT) {
      return row.getFloat(index);      
    } else if (type == Table.DOUBLE) {
      return row.getDouble(index);
    } else {
      return 1d;
    }    
  }
  
  protected boolean specialValue(int value) {
    if (spValInt == null || spValInt.length == 0) return false;
    else {
      for (int i = 0; i < spValInt.length; i++) {
        if (value == spValInt[i]) {
          return true;  
        }
      }
      return false;  
    }
  }

  protected boolean specialValue(long value) {
    if (spValLong == null || spValLong.length == 0) return false;
    else {
      for (int i = 0; i < spValLong.length; i++) {
        if (value == spValLong[i]) return true;  
      }
      return false;  
    }
  }  

  protected boolean specialValue(float value) {
    if (Float.isNaN(value)) return true;
    if (spValFloat == null || spValFloat.length == 0) return false;
    else {
      for (int i = 0; i < spValFloat.length; i++) {
        if (value == spValFloat[i]) return true;  
      }
      return false;  
    }
  }   

  protected boolean specialValue(double value) {
    if (Double.isNaN(value)) return true;
    if (spValDouble == null || spValDouble.length == 0) return false;
    else {
      for (int i = 0; i < spValDouble.length; i++) {
        if (value == spValDouble[i]) return true;  
      }
      return false;
    }
  }  
}

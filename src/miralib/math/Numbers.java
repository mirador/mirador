/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.math;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Basic utilities to deal with numeric values.
 * 
 */

public class Numbers {
  static public float FLOAT_EPS   = Float.MIN_VALUE;
  static public double DOUBLE_EPS = Double.MIN_VALUE;
  
  // For number conversion
  protected static NumberFormat nfi = null;
  protected static NumberFormat nfl = null;
  protected static NumberFormat nff = null;
  protected static NumberFormat nfd = null;  
  protected static DecimalFormat dff = null;
  
  // Calculation of the Machine Epsilon for float precision. From:
  // http://en.wikipedia.org/wiki/Machine_epsilon#Approximation_using_Java
  static {
    float eps = 1.0f;

    do {
      eps /= 2.0f;
    } while ((float)(1.0 + (eps / 2.0)) != 1.0);

    FLOAT_EPS = eps;
  }

  static {
    double eps = 1.0f;

    do {
      eps /= 2.0f;
    } while (1.0 + (eps / 2.0) != 1.0);

    DOUBLE_EPS = eps;
  }  
  
  static public boolean equal(int a, int b) {
    return a == b;
  }

  static public boolean different(int a, int b) {
    return a != b;
  }    
  
  static public boolean equal(long a, long b) {
    return a == b;
  }

  static public boolean different(long a, long b) {
    return a != b;
  }      
  
  static public boolean equal(float a, float b) {
    return Math.abs(a - b) < FLOAT_EPS;
  }

  static public boolean different(float a, float b) {
    return Math.abs(a - b) >= FLOAT_EPS;
  }  

  static public boolean whole(float a) {
    return equal(a, (int)a);
  }
  
  static public boolean equal(double a, double b) {
    return Math.abs(a - b) < DOUBLE_EPS;
  }

  static public boolean different(double a, double b) {
    return Math.abs(a - b) >= DOUBLE_EPS;
  }  

  static public boolean whole(double a) {
    return equal(a, (int)a);
  }
  
  static public float map(float value, float start1, float stop1,
                                       float start2, float stop2) {
    return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
  }
  
  static public double map(double value, double start1, float stop1,
                                         double start2, float stop2) {
    return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
  }
  
  static public int constrain(int amt, int low, int high) {
    return (amt < low) ? low : ((amt > high) ? high : amt);
  }
  
  static public float constrain(float amt, float low, float high) {
    return (amt < low) ? low : ((amt > high) ? high : amt);
  }  
  
  static public double constrain(double amt, double low, double high) {
    return (amt < low) ? low : ((amt > high) ? high : amt);
  }
  
  static public final int min(int a, int b, int c) {
    return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
  }  
  
  static public final float min(float a, float b, float c) {
    return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
  }
  
  static public final double min(double a, double b, double c) {
    return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
  }
  
  static final public int parseInt(String what) {
    return parseInt(what, 0);
  }

  static final public int parseInt(String what, int otherwise) {
    try {
      int offset = what.indexOf('.');
      if (offset == -1) {
        return Integer.parseInt(what);
      } else {
        return Integer.parseInt(what.substring(0, offset));
      }
    } catch (NumberFormatException e) { }
    return otherwise;
  } 
  
  static final public float parseFloat(String what) {
    return parseFloat(what, Float.NaN);
  }

  static final public float parseFloat(String what, float otherwise) {
    try {
      return new Float(what).floatValue();
    } catch (NumberFormatException e) { }

    return otherwise;
  }  
  
  static public long parseLong(String what) {
    return parseLong(what, 0);
  }  
  
  static public long parseLong(String what, int otherwise) {
    try {
      int offset = what.indexOf('.');
      if (offset == -1) {
        return Long.parseLong(what);
      } else {
        return Long.parseLong(what.substring(0, offset));
      }
    } catch (NumberFormatException e) { }
    return otherwise;
  }  
  
  static public double parseDouble(String what) {
    return parseDouble(what, Double.NaN);
  }

  static public double parseDouble(String what, double otherwise) {
    try {
      return new Double(what).doubleValue();
    } catch (NumberFormatException e) { }
    return otherwise;
  }
  
  static public String nfc(int num) {
    if (nfi == null) nfi = NumberFormat.getInstance();
    nfi.setGroupingUsed(false);
    nfi.setMinimumIntegerDigits(0);
    return nfi.format(num);
  }  

  static public String nfc(long num) {
    if (nfl == null) nfl = NumberFormat.getInstance();
    nfl.setGroupingUsed(false);
    nfl.setMinimumIntegerDigits(0);
    return nfl.format(num);
  }

  static public String nfc(float num, int decimals) {
    if (nff == null) nff = NumberFormat.getInstance();  
    nff.setGroupingUsed(true);
    nff.setMinimumFractionDigits(0);
    nff.setMaximumFractionDigits(decimals);
    return nff.format(num);
  }

  static public String nfc(double num, int decimals) {
    if (nfd == null) nfd = NumberFormat.getInstance();
    nfd.setGroupingUsed(true);  
    nfd.setMinimumFractionDigits(0);
    nfd.setMaximumFractionDigits(decimals);
    return nfd.format(num);
  }  
  
  static public String dfc(double num) {
    if (dff == null) dff = new DecimalFormat("0.0E0");
    return dff.format(num);
  }  
}


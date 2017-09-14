/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import miralib.math.Numbers;
import processing.data.TableRow;

/**
 * Range for a categorical variable (a set of categories).
 *
 */

public class CategoricalRange extends Range {
  protected ArrayList<String> catset;
  
  public CategoricalRange(Variable var) {
    super(var);
    catset = new ArrayList<String>();
  }
  
  public CategoricalRange(CategoricalRange that) {
    super(that);    
    catset = new ArrayList<String>(that.catset);
  }
  
  public void set(double min, double max, boolean normalized) { }
  
  public void set(ArrayList<String> values) {
    if (values != null && 0 < values.size()) {
      String val0 = values.get(0);
      if (var.valueAlias(val0)) {
        // If passing aliases, will convert them to valid values to set 
        // categorical range. 
        HashMap<String, String> aliases = ((CategoricalVariable)var).aliases;
        catset.clear();
        for (String cat: aliases.keySet()) {
          String alias = aliases.get(cat);
          if (values.contains(alias)) {
            catset.add(cat);
          }          
        }        
      } else {
        catset.clear();
        catset.addAll(values);        
      }
    }
  }
  
  public void set(String... values) {
    if (values == null) return;
    ArrayList<String> valarray;
    if (values.length == 1) {
      valarray = new ArrayList<String>();
      String valstr = values[0];
      String[] categories = valstr.split(";");
      for (int i = 0; i < categories.length; i++) {
        String[] parts = categories[i].split(":", 2);
        valarray.add(parts[0]);        
      }      
    } else {
      valarray = new ArrayList<String>(Arrays.asList(values));
    }
    set(valarray); 
  }
  
  public void reset() {
    catset.clear();
  }
  
  public void update(TableRow row) {    
    String value = row.getString(var.index);
    if (catset.indexOf(value) == -1) {   
      catset.add(value);
    }
  }  
  
  public double getMin() {
    return 0;
  }
 
  public double getMax() {
    return catset.size() - 1;
  }
  
  public long getCount() {
    return catset.size();
  } 
  
  public int getRank(String value) {
    return catset.indexOf(value);
  }
  
  public int getRank(String value, Range supr) {
    if (catset.indexOf(value) == -1) return -1;
    int rank = 0;
    for (String cat: supr.getValues()) {
      if (cat.equals(value)) break;
      if (-1 < catset.indexOf(cat)) {
        rank++;  
      }
    }
    return rank;
  }
  
  public ArrayList<String> getValues() {
    return new ArrayList<String>(catset);
  }  
  
  public boolean inside(TableRow row) {
    String value = row.getString(var.index);
    return value != null && -1 < catset.indexOf(value);
  }

  public double snap(double value) {
    return constrain((int)Math.round(value));
  }
  
  public double normalize(int value) {
    return normalizeImpl(value);
  }
  
  public double normalize(long value) {
    return -1; 
  }
  
  public double normalize(float value) {
    return -1; 
  }
  
  public double normalize(double value) {
    return -1; 
  }
  
  public double denormalize(double value) {
    double min = getMin(); 
    double max = getMax() + 1;
    int val = (int)Math.floor(min + value * (max - min));
    val = Numbers.constrain(val, (int)min, (int)max);
    return val;    
  } 
    
  public int constrain(int value) {
    return (int)constrainImpl(value);
  }
  
  public long constrain(long value) {
    return -1; 
  }
  
  public float constrain(float value) {
    return -1; 
  }
  
  public double constrain(double value) {
    return -1; 
  }
  
  public boolean equals(Object that) {
    if (this == that) return true;
    if (that instanceof CategoricalRange) {
      CategoricalRange range = (CategoricalRange)that;
      if (this.var != range.var) return false;
      return this.catset.containsAll(range.catset) && range.catset.containsAll(this.catset);      
    }
    return false;
  }  
  
  public String toString() {
    String str = "";
    for (String value: catset) {
      if (!str.equals("")) str += ",";
      str += value;
    }
    return str;  
  } 
}

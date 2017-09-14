/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.data;

import java.util.ArrayList;
import java.util.HashMap;

import miralib.utils.Log;
import processing.data.Table;
import processing.data.TableRow;

/**
 * Subclass of Variable to represent a categorical variable.
 *
 */

public class CategoricalVariable extends Variable {
  protected HashMap<String, String> aliases;
  
  public CategoricalVariable(String name, int index) {
    super(name, index);
    aliases = new HashMap<String, String>();
    range = new CategoricalRange(this);    
  }
  
  public void initValues(String valstr) {
    valstr = valstr.trim();
    String[] categories = valstr.split(";");
    ArrayList<String> values = range.getValues();
    ArrayList<String> ordered = new ArrayList<String>();
    for (int i = 0; i < categories.length; i++) {
      String[] parts = categories[i].split(":", 2);
      if (parts.length != 2) {
        throw new RuntimeException("Malformed category string for variable " + 
                                   name + " (" + alias + ")");   
      }      
      if (-1 < values.indexOf(parts[0])) {
        ordered.add(parts[0]);
        aliases.put(parts[0], parts[1]);
//        Log.message("Alias for " + parts[0] + " is " + parts[1]);
      }
    }
    // Using the order of the values as specified in the metadata, and putting
    // the ones not in the metadata at the end.
    ArrayList<String> surplus = new ArrayList<String>();
    for (String cat: values) {
      if (!ordered.contains(cat)) surplus.add(cat);
    }
    ordered.addAll(surplus);
    range.set(ordered);     
  }  
  
  public void initRange(DataSource data) {
    super.initRange(data);
    for (String cat: range.getValues()) {
      aliases.put(cat, cat);
    }
  }  
  
  public Range createRange(double val0, double val1) {
    Range range = new CategoricalRange(this);    
    range.set(val0, val1, false);
    return range;    
  }
  
  public Range createRange(double min, double max, boolean normalized) {
    Range range = new CategoricalRange(this);
    range.set(min, max, normalized);
    return range;
  }
  
  public Range createRange(ArrayList<String> values) {
    Range range = new CategoricalRange(this);   
    range.set(values);
    return range;
  }
    
  public Range createRange(String... values) {
    Range range = new CategoricalRange(this);
    range.set(values);
    return range;
  }
  
  public int type() {
    return Table.CATEGORY;
  }
    
  public boolean discrete() {
    return true;  
  }
  
  public boolean numerical() {
    return false;
  }
  
  public boolean categorical() {
    return true;
  }
   
  public boolean string() {
    return false;
  }
  
  public boolean missing(TableRow row) {
    String value = row.getString(index);
    return value == null || value.equals(missingString);
  }

  public double getValue(String str, boolean normalized) {
    int rank = range.getRank(str);
    if (normalized) {
      return range.normalize(rank);              
    } else {
      return rank;
    }    
  }  
    
  public double getValue(TableRow row, Range sel, boolean normalized) {    
    String value = row.getString(index);
    if (value == null) return -1;
    int rank = sel == null ? range.getRank(value) : sel.getRank(value, range);  
        
    if (normalized) {
      if (sel == null) {
        return range.normalize(rank);              
      } else {
        return sel.normalize(rank);  
      }
    } else {
      return rank;
    }
  } 
  
  public String formatValue(TableRow row) {
    String value = row.getString(index);    
    String alias = aliases.get(value);
    return alias == null ? "missing" : alias;
  }   
  
  public String formatValue(double value, boolean normalized) {
    int rank = normalized ? (int)Math.round(range.denormalize(value)) : (int)Math.round(value);
    if (0 <= rank && rank < range.getCount()) {
      String sval = range.getValues().get(rank);
      String alias = aliases.get(sval);
      return alias == null ? "missing" : alias;      
    } else {
      return "missing";
    }    
  }
  
  public String formatValue(double value, Range sel) {
    int rank = sel == null ? (int)Math.round(range.denormalize(value)) : (int)Math.round(sel.denormalize(value));
    long count = sel == null ? range.getCount() : sel.getCount();
    if (0 <= rank && rank < count) {
      String sval = sel == null ? range.getValues().get(rank) : sel.getValues().get(rank);
      String alias = aliases.get(sval);
      return alias == null ? "missing" : alias;      
    } else {
      return "missing";
    } 
  } 
  
  public String formatValue(String value) {
    String alias = aliases.get(value);
    return alias == null ? "missing" : alias;
  }
    
  public boolean valueAlias(String value) {
    return aliases.values().contains(value);  
  } 
  
  public double snapValue(double value, Range sel, boolean normalized) {
    if (normalized) {
      if (sel == null) {
        return (int)Math.round(range.denormalize(value));
      } else {
        return (int)Math.round(sel.denormalize(value));
      }
    } else {
      return (int)Math.round(value);
    }
  }
  
  public String formatRange(Range sel, boolean humanReadable) {
    ArrayList<String> values = sel == null? range.getValues() : sel.getValues();
    String str = "";
    if (humanReadable) {
      for (String value: values) {
        String alias = aliases.get(value);
        if (alias == null) continue;
        if (!str.equals("")) str += " | ";
        str += alias;
      }      
    } else {
      for (String value: values) {
        String alias = aliases.get(value);
        if (alias == null) continue;
        if (!str.equals("")) str += ";";
        str += value + ":" + alias;
      }      
    }
    return str;       
  }  
  
  protected double getWeightImpl(TableRow row) {
    String msg = "Weight variable " + name + " (" + alias + ") is not numeric";
    Log.error(msg, new RuntimeException(msg));
    return 0;
  }
}

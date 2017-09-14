/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.data;

import java.util.ArrayList;

import miralib.utils.Log;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import processing.data.TableRow;

public class DateVariable extends Variable {
  
  // Standard ISO8601 formatter:
  // http://en.wikipedia.org/wiki/ISO_8601
//  protected static DateTimeFormatter fmtParse = ISODateTimeFormat.dateTime();  
  protected static DateTimeFormatter fmtParse = DateTimeFormat.forPattern("yyyy-MM-dd");  
  protected static DateTimeFormatter fmtPrint = DateTimeFormat.forPattern("d MMM, yy");

  // For Custom Formatters, check the user guide:
  // http://www.joda.org/joda-time/userguide.html
  // i.e.:
  // 
    
  public DateVariable(String name, int index) {
    super(name, index);
    range = new DateRange(this);
  }

  public void initValues(String valstr) {
    String[] all = valstr.split(";");    
    if (1 < all.length) {
      // There are special values, will override the min/max that were computed
      // directly from the data.
      String[] minmax = all[0].split(",");
      range.set(minmax);
    }    
  }

  public Range createRange(double val0, double val1) {
    Range range = new DateRange(this);    
    range.set(val0, val1, false);
    return range;    
  }
  
  public Range createRange(double min, double max, boolean normalized) {
    Range range = new DateRange(this);
    range.set(min, max, normalized);
    return range;
  }
  
  public Range createRange(ArrayList<String> values) {
    Range range = new DateRange(this);   
    range.set(values);
    return range;
  }
    
  public Range createRange(String... values) {
    Range range = new DateRange(this);
    range.set(values);
    return range;
  }  
  
  public int type() {
    return MiraTable.DATE;
  }

  public boolean discrete() {
    return true;
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
    String value = row.getString(index);
    return value == null || value.equals(missingString);
  }

  public double getValue(String str, boolean normalized) {
    DateTime dat = parsePrint(str);    
    if (dat == null) return -1;    
    double value = dat.getMillis();
    if (normalized) {
      return range.normalize(value);              
    } else {
      return value;
    }    
  }    
  
  public double getValue(TableRow row, Range sel, boolean normalized) {
    String value = row.getString(index);
    
    DateTime date = parse(value); 
    if (date == null) return -1; 
      
    long millis = date.getMillis();
    
    if (normalized) {
      if (sel == null) {
        return range.normalize(millis);
      } else {
        return sel.normalize(millis);
      }        
    } else {
      return millis;
    }    
  }

  public String formatValue(TableRow row) {
    String value = row.getString(index);    
    DateTime date = parse(value); 
    if (date == null) return "missing";    
    return print(date);
  }

  public String formatValue(double value, boolean normalized) {    
    long millis = normalized ? Math.round(range.denormalize(value)) : (long)value;
    DateTime date = new DateTime(millis);    
    return print(date);
  }

  public String formatValue(double value, Range sel) {
    long millis = sel == null ? Math.round(range.denormalize(value)) : Math.round(sel.denormalize(value));
    DateTime date = new DateTime(millis);    
    return print(date);
  }

  public boolean valueAlias(String value) {
    return false;
  }

  public double snapValue(double value, Range sel, boolean normalized) {
    if (normalized) {
      double denorm = 0;
      if (sel == null) {
        denorm = Math.round(range.denormalize(value));
        return range.normalize(Math.round(denorm));
      } else {
        denorm = Math.round(sel.denormalize(value));
        return sel.normalize(Math.round(denorm));
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
    String msg = "Datet variable " + name + " (" + alias + ") cannot be used as a weight";
    Log.error(msg, new RuntimeException(msg));
    return 0;
  }
  
  public static boolean validDate(String str) {
    return parse(str) != null;
  }
  
  public static DateTime parse(String str) {
    DateTime date = null;
    try {    
      date = fmtParse.parseDateTime(str).withTimeAtStartOfDay();     
    } catch (Exception e) { 
//      e.printStackTrace();
    }
    return date;    
  }

  public static DateTime parsePrint(String str) {
    DateTime date = null;
    try {    
      date = fmtPrint.parseDateTime(str).withTimeAtStartOfDay();     
    } catch (Exception e) { 
//      e.printStackTrace();
    }
    return date;    
  }  
  
  public static String print(DateTime dat) {
    return dat.toString(fmtPrint);    
  }
  
  public static void setParsePattern(String pattern) {
    fmtParse = DateTimeFormat.forPattern(pattern);      
  }
  
  public static void setPrintPattern(String pattern) {
    fmtPrint = DateTimeFormat.forPattern(pattern);     
  }
}

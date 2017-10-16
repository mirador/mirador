/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.data;

import java.util.ArrayList;
import java.util.Arrays;

import miralib.math.Numbers;
import miralib.shannon.BinOptimizer;
import miralib.utils.Log;
import miralib.utils.Project;
import processing.data.Table;
import processing.data.TableRow;

/**
 * Class holding all the meta-information needed to define a variable in the 
 * dataset (name, alias, min/max range, etc.)
 *
 */

abstract public class Variable implements DataTree.Item {
  final static public int UNDEFINED   = 0;
  final static public int LINEAR      = 1;
  final static public int EXPONENTIAL = 2;
  
  protected String name;
  protected String alias;  
  protected int index;
  protected Range range;
  protected float missing;
  
  protected boolean weight;
  protected boolean subsample;
  protected Variable weightVar;
  
  protected boolean open;  
  protected boolean include;
  protected boolean column;
  protected boolean covariate;
  protected boolean sortKey;
     
  protected static String missingString;
  
  public Variable(String name, int index) {
    this.name = name;
    this.alias = name;
    this.index = index;
    this.missing = 0;
    
    this.weight = false;
    this.subsample = false;
    this.weightVar = null;
    
    this.include = true;
    this.column = true;
    this.covariate = false;
    this.sortKey = false;
    this.open = true;    
  }
  
  static public Variable create(int index, String name, int type) {
    Variable var = null;
    if (type == Table.STRING) {
      var = new StringVariable(name, index);
    } else if (type == Table.INT) {
      var = new NumericalVariable(name, index, Table.INT);
    } else if (type == Table.LONG) {
      var = new NumericalVariable(name, index, Table.LONG);
    } else if (type == Table.FLOAT) {
      var = new NumericalVariable(name, index, Table.FLOAT);
    } else if (type == Table.DOUBLE) {
      var = new NumericalVariable(name, index, Table.DOUBLE);
    } else if (type == Table.CATEGORY) {
      var = new CategoricalVariable(name, index);
    } else if (type == MiraTable.DATE) {
      var = new DateVariable(name, index);      
    } else {
      String err = "variable " + (index + 1) + " (" + name + ") is of an unsupported type: " + Variable.formatType(type);
      Log.error(err, new RuntimeException(err));
    }    
    return var;
  }  
  
  static public void setMissingString(String str) {
    missingString = str;
  }
  
  public Range range() { return Range.create(range); }
  
  abstract public Range createRange(double val0, double val1);
  abstract public Range createRange(double min, double max, boolean normalized);  
  abstract public Range createRange(ArrayList<String> values);
  abstract public Range createRange(String... values);
  
  public void initRange(DataSource data) {
    range.reset();
    for (int r = 0; r < data.getRowCount(); r++) {
      TableRow row = data.getRow(r);
      if (missing(row)) continue;
      range.update(row);
    }
  }
  
  public int getScaling(DataSlice1D slice, Project prefs) {
    int scaling = LINEAR;
    int bcount = BinOptimizer.calculate(slice, prefs.binAlgorithm);
    if (bcount <= 0) return UNDEFINED;
    
    float bsize = 1.0f / bcount;
    double[] weightSum = new double[bcount];
    Arrays.fill(weightSum, 0);
    double totWeight = 0;
    for (Value1D value: slice.values) {      
      int bin = Numbers.constrain((int)(value.x / bsize), 0, bcount - 1);      
      weightSum[bin] += value.w;
      totWeight += value.w;
    }
    for (int bin = 0; bin < bcount; bin++) {
      if (weightSum[bin] / totWeight > 0.9) {
        scaling = EXPONENTIAL;
        break;
      }
    } 

    return scaling;
  }

  abstract public void initValues(String valstr);
  
  public int getItemType() {
    return DataTree.VARIABLE_ITEM;
  }
  
  public String getName() {
    return name;
  }  

  public int getFirstChild() { 
    return index;    
  }
  
  public int getLastChild() {
    return index;
  }

  public boolean canOpen() {
    return true;
  }
  
  public boolean open() {
    return open;
  }
  
  public void setOpen() {
    open = true;  
  }
  
  public void setClose() {
    open = false;
  }
  
  public int getColumnSelection() {
    return column ? DataTree.ALL : DataTree.NONE;
  }
  
  public void setColumnSelection(int sel) { 
    column = sel == DataTree.NONE ? false : true;
  }
  
  public void selectAllColumns() {
    column = true; 
  }
  
  public void deselectAllColumns() {
    column = false;
  }  
  
  public int getIndex() {
    return index;
  } 
  
  public boolean include() {
    return include;
  }
  
  public boolean column() {
    return column;
  }
  
  public boolean covariate() {
    return covariate;
  }
  
  public boolean sortKey() {
    return sortKey;
  }
  
  public void setAlias(String alias) {
    this.alias = alias;  
  }
  
  public String getAlias() {
    return alias;
  }      
  
  public boolean matchName(String query) {
    return -1 < name.toLowerCase().indexOf(query.toLowerCase());
  }

  public boolean matchAlias(String query) {
    return -1 < alias.toLowerCase().indexOf(query.toLowerCase());
  }  
  
  abstract public int type();
  abstract public boolean discrete();
  abstract public boolean numerical();
  abstract public boolean categorical();
  abstract public boolean string();
  
  public long getCount() {
    return range.getCount();
  }
  
  public long getCount(DataRanges ranges) {
    return getCount(ranges.get(this));
  }
  
  public long getCount(Range sel) {
    return sel == null ? range.getCount() : sel.getCount();
  }
  
  abstract public boolean missing(TableRow row);  
  
  public boolean maxRange(Range sel) { return range.equals(sel); }
  
  public ArrayList<String> getValues() { return range.getValues(); }
  
  public double getValue(String str) {
    return getValue(str, true);
  }

  abstract public double getValue(String str, boolean normalized); 
  
  public double getValue(TableRow row, DataRanges ranges) {
    return getValue(row, ranges.get(this), true);
  }
  
  public double getValue(TableRow row, Range sel) {
    return getValue(row, sel, true);
  }  
  abstract public double getValue(TableRow row, Range sel, boolean normalized);
  abstract public String formatValue(TableRow row);
  public String formatValue(double value) {
    return formatValue(value, true);  
  }
  abstract public String formatValue(double value, boolean normalized);
  
  public String formatValue(double value, DataRanges ranges) {
    return formatValue(value, ranges.get(this));
  }
  abstract public String formatValue(double value, Range sel);
  
  public String formatValue(String value) { return value; }
  
  abstract public boolean valueAlias(String value);
  
  public double snapValue(double value, Range sel) {
    return snapValue(value, sel, true);  
  }
  abstract public double snapValue(double value, Range sel, boolean normalized);
  
  public String formatRange() {
    return formatRange(range, true);
  }
  
  public String formatRange(boolean humanReadable) {
    return formatRange(range, humanReadable);  
  }
  
  public String formatRange(Range sel) {
    return formatRange(sel, true);
  }
  
  abstract public String formatRange(Range sel, boolean humanReadable);
  
  public double normalize(int value) { return range.normalize(value); }
  public double normalize(long value) { return range.normalize(value); }
  public double normalize(float value) { return range.normalize(value); }
  public double normalize(double value) { return range.normalize(value); }
  
  public boolean weight() {
    return weight;
  }
  
  public void setWeight() {
    weight = true;
  }

  public boolean subsample() {
    if (weight) return subsample;
    else return weightVar != null && weightVar.subsample;
  } 
  
  public void setSubsample() {
    subsample = true;
  }
    
  public void setWeightVariable(Variable var) {
    weightVar = var;  
  }
  
  public boolean weighted() {
    return weightVar != null;
  }
  
  public double getWeight(TableRow row) {
    if (weightVar != null) {
      return weightVar.getWeightImpl(row);
    } else {
      return 1d;
    }
  }
  
  public boolean insideSample(TableRow row) {
    if (weight) {
      return 0 < getWeightImpl(row);
    } else if (weightVar != null) {
      return !weightVar.missing(row) && 0 < weightVar.getWeightImpl(row);
    } else {
      return true;
    }
  } 
  
  abstract protected double getWeightImpl(TableRow row);
    
  static public int getType(String name) {
    if (name.equals("int") || name.equals("integer")) {
      return Table.INT;
    } else if (name.equals("long")) {
      return Table.LONG;
    } else if (name.equals("float")) {
      return Table.FLOAT;
    } else if (name.equals("double")) {
      return Table.DOUBLE;
    } else if (name.equals("category")) {
      return Table.CATEGORY;
    } else if (name.equals("date")) {
      return MiraTable.DATE;
    } else if (name.equals("String")) {
      return Table.STRING;
    }
    return -1;
  }

  static public String formatType(int type) {
    if (type == Table.INT) {
      return "int";
    } else if (type == Table.LONG) {
      return "long";
    } else if (type == Table.FLOAT) {
      return "float";
    } else if (type == Table.DOUBLE) {
      return "double";
    } else if (type == Table.CATEGORY) {
      return "category";
    } else if (type == MiraTable.DATE) {
      return "date";      
    } else if (type == Table.STRING) {
      return "String";
    }
    return "unknown";
  }  
  
  static public double getWeight(TableRow row, Variable varx, Variable vary) {
    if (varx.weight() || vary.weight()) {
      return 1d;
    } else if (!varx.weighted() || !vary.weighted()) {
      return varx.weighted() ? varx.getWeight(row) : vary.getWeight(row);        
    } else if (varx.subsample() && vary.subsample()) {
      return 0d; // subsamples are not comparable
    } else if (!varx.subsample() && !vary.subsample()) {
      return Math.min(varx.getWeight(row), vary.getWeight(row));
    } else if (varx.subsample()) {
      return varx.getWeight(row);
    } else if (vary.subsample()) {
      return vary.getWeight(row);
    } else {
      return 1d; 
    }     
  }
}

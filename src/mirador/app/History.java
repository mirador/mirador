/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import java.util.ArrayList;
import java.util.HashSet;

import miralib.data.Range;
import miralib.data.Variable;

/**
 * Stores the visualization history: set of visible plots, current ranges,  
 * sorting variable, p-value, missing threshold, plot type, and selected 
 * variables.
 *
 */

public class History {
  protected MiraApp app;
  protected HashSet<VariablePair> pairs;
  protected HashSet<VariableRange> ranges;
  protected VariablePair selPair;
  protected Variable sortVar;
  protected float pvalue;
  protected float misst;
  protected int plotType;
    
  public History(MiraApp app) {
    this.app = app;
    
    pairs = new HashSet<VariablePair>();
    ranges = new HashSet<VariableRange>();
    selPair = null;
    sortVar = null;
    pvalue = -1;
    misst = -1;
    plotType = -1;
  }
  
  public void addPair(Variable varx, Variable vary) {
    VariablePair pair = new VariablePair(varx, vary);
    if (pairs.add(pair)) {
      System.err.println("+PAIR\t" + app.millis() + " " + varx.getName() + ":" + varx.getAlias() +"\t" + vary.getName() + ":" + vary.getAlias());
    }    
  }
  
  public void removePair(Variable varx, Variable vary) {
    VariablePair pair = new VariablePair(varx, vary);
    if (pairs.remove(pair)) {
      System.err.println("-PAIR\t" + app.millis() + " " + varx.getName() + ":" + varx.getAlias() +"\t" + vary.getName() + ":" + vary.getAlias());
    }
  }
  
  public void addRange(Variable var, Range range) {
    VariableRange vrange = new VariableRange(var, range);
    if (ranges.add(vrange)) {
      System.err.println("+RANGE\t" + app.millis() + " " + var.getName() + ":" + var.getAlias() +"\t" + var.formatRange(range, false));      
    }
  }
  
  public void removeRange(Variable var, Range range) {
    VariableRange vrange = new VariableRange(var, range);
    if (ranges.remove(vrange)) {
      System.err.println("-RANGE\t" + app.millis() + " " + var.getName() + ":" + var.getAlias() +"\t" + var.formatRange(range, false));
    }
  }
  
  public void replaceRange(Variable var, Range range) {
    VariableRange vrange = null;
    for (VariableRange r: ranges) {
      if (r.var == var) {
        System.out.println("Found range for " + var.getName() + r.range.toString());
        vrange = r;
        break;
      }
    }
    if (vrange != null) {
      ranges.remove(vrange);
      vrange = new VariableRange(var, range);
      if (ranges.add(vrange)) {
        System.err.println("~RANGE\t" + app.millis() + " " + var.getName() + ":" + var.getAlias() +"\t" + var.formatRange(range, false));        
      }      
    }
  }
  
  public void clearRanges() {
    Object[] array = ranges.toArray();
    for (Object obj: array) {
      if (ranges.remove(obj)) {
        VariableRange vrange = (VariableRange)obj;
        Variable var = vrange.var;
        Range range = vrange.range;
        System.err.println("-RANGE\t" + app.millis() + " " + var.getName() + ":" + var.getAlias() +"\t" + var.formatRange(range, false));
      }
    }
  }
  
  class VariablePair {
    Variable varx, vary;
    
    public VariablePair(Variable varx, Variable vary) {
      this.varx = varx;
      this.vary = vary;
    }

    public boolean equals(Object obj) {
      if (obj instanceof VariablePair) {
        VariablePair that = (VariablePair)obj;
        return this.varx == that.varx && this.vary == that.vary;
      } else {
        return false;  
      }
    }
    
    public int hashCode() {
      return 31 + 7 * varx.hashCode() + 11 * vary.hashCode();
    } 
  }
  
  class VariableRange {
    Variable var;
    Range range;
    
    public VariableRange(Variable var, Range range) {
      this.var = var;
      this.range = range;
    }
    
    public boolean equals(Object obj) {
      if (obj instanceof VariablePair) {
        VariableRange that = (VariableRange)obj;
        return this.var == that.var && this.range == that.range;
      } else {
        return false;  
      }
    }
    
    public int hashCode() {
      return 31 + 7 * var.hashCode() + 11 * range.hashCode();
    }     
  }
}

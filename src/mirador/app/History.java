/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import java.util.HashSet;

import mirador.views.View;
import miralib.data.Range;
import miralib.data.Variable;
import miralib.math.Numbers;
import miralib.utils.Project;

/**
 * Stores the visualization history: set of visible plots, current ranges,  
 * sorting variable, p-value, missing threshold, plot type, and selected 
 * variables.
 *
 */

public class History {
  protected MiraApp app;
  protected Project prj;
  protected HashSet<VariablePair> pairs;
  protected HashSet<VariableRange> ranges;

  protected float pvalue;
  protected float misst;
  protected int plotType;

  protected VariablePair selPair;
  protected Variable sortVar;
  protected boolean openProfile;
    
  public History(MiraApp app, Project prj, int ptype) {
    this.app = app;
    this.prj = prj;
    
    pairs = new HashSet<VariablePair>();
    ranges = new HashSet<VariableRange>();
    
    pvalue = -1;
    misst = -1;
    plotType = -1;
    
    selPair = null;
    sortVar = null;
    openProfile = false;
    
    setPValue(prj.pvalue());
    setMissingThreshold(prj.missingThreshold());
    setPlotType(ptype);
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
  
  public void removeRange(Variable var) {
    VariableRange vrange = null;
    for (VariableRange r: ranges) {
      if (r.var == var) {
        vrange = r;
        break;
      }
    }
    if (vrange != null) {
      Range range = vrange.range;
      ranges.remove(vrange);
      System.err.println("-RANGE\t" + app.millis() + " " + var.getName() + ":" + var.getAlias() +"\t" + var.formatRange(range, false));
    }
  }
  
  public void replaceRange(Variable var, Range range) {
    VariableRange vrange = null;
    for (VariableRange r: ranges) {
      if (r.var == var) {
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
  
  public void setPValue(float pvalue) {
    if (Numbers.different(this.pvalue, pvalue)) {
      this.pvalue = pvalue;
      System.err.println("PVALUE\t" + app.millis() + "\t" + pvalue);
    }
  }
  
  public void setMissingThreshold(float misst) {
    if (Numbers.different(this.misst, misst)) {
      this.misst = misst;
      System.err.println("MISSING\t" + app.millis() + "\t" + misst);
    }    
  }
  
  public void setPlotType(int plotType) {
    if (this.plotType != plotType) {
      this.plotType = plotType;
      System.err.println("PLOT\t" + app.millis() + "\t" + View.typeToString(plotType));
    }
  }
  
  public void setSelectedPair(Variable varx, Variable vary) {
    if (varx != null && vary != null) {
      VariablePair pair = new VariablePair(varx, vary);
      if (!pair.equals(selPair)) {
        selPair = pair;
        System.err.println("SELECT\t" + app.millis() + "\t" + varx.getName() + ":" + varx.getAlias() +"\t" + vary.getName() + ":" + vary.getAlias());
      }      
    } else if (selPair != null) {
      selPair = null;
      System.err.println("SELECT\t" + app.millis() + "\tNONE");
    }
  }
  
  public void sort(Variable var) {
    if (sortVar != var) {
      sortVar = var;
      System.err.println("SORT\t" + app.millis() + "\t" + var.getName() + ":" + var.getAlias());
    }
  }
  
  public void unsort() {
    if (sortVar != null) {
      sortVar = null;
      System.err.println("SORT\t" + app.millis() + "\tNONE");
    }
  }
  
  public void openProfile() {
    if (!openProfile) {
      openProfile = true;
      System.err.println("+PROFILE\t" + app.millis());
    }
  }
  
  public void closeProfile() {
    if (openProfile) {
      openProfile = false;
      System.err.println("-PROFILE\t" + app.millis());
    }    
  }  
  
  public String read() {
    return "";
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

/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import java.util.HashSet;

import miralib.data.Variable;

/**
 * Stores the visualization history: set of visible plots, current ranges, 
 * plot type, p-value and missing threshold.
 *
 */

public class History {
  protected HashSet<VariablePair> pairs;
  
  public History() {
    pairs = new HashSet<VariablePair>();
  }
  
  public void addPair(Variable varx, Variable vary) {
    VariablePair pair = new VariablePair(varx, vary);
    if (pairs.add(pair)) {
      System.err.println("Added pair " + varx.getName() + " " + vary.getName() + " to history");
    }    
  }
  
  public void removePair(Variable varx, Variable vary) {
    VariablePair pair = new VariablePair(varx, vary);
    if (pairs.remove(pair)) {
      System.err.println("Removed pair " + varx.getName() + " " + vary.getName() + " from history");
    }    
  }
  
  protected class VariablePair {
    Variable varx, vary;
    
    public VariablePair(Variable varx, Variable vary) {
      this.varx = varx;
      this.vary = vary;
//      System.out.println("Created " + this.varx.getName() + " " + this.vary.getName());
    }

    public boolean equals(VariablePair obj) {      
      VariablePair that = (VariablePair)obj;
      return this.varx == that.varx && this.vary == that.vary;      
    } 
    
    public boolean equals(Object obj) {
//      System.err.println("Comparing " + this + " " + obj);
      if (obj instanceof VariablePair) {
        VariablePair that = (VariablePair)obj;
        return equals(that);
//        return this.varx == that.varx && this.vary == that.vary;
      } else {
        return false;  
      }
    }
    
    public int hashCode() {
      return 31 + 7 * varx.hashCode() + 11 * vary.hashCode();
    } 
  }
}

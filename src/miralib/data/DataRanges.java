/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import miralib.utils.Log;

/**
 * Dictionary that links variables with their respective ranges.
 *
 */

@SuppressWarnings("serial")
public class DataRanges extends HashMap<Variable, Range> {
  static final public int NO_CHANGE      = 0;
  static final public int ADDED_RANGE    = 1;
  static final public int REMOVED_RANGE  = 2;
  static final public int MODIFIED_RANGE = 3;
  
  public DataRanges() {
    super();
  }
  
  public DataRanges(DataRanges ranges) {
    super();
    Set<Variable> variables = ranges.keySet();
    for (Variable var: variables) {
      if (var == null) {
        Log.message("Found null variable in the ranges, something is going on (threading problems maybe)");
        continue;
      }
      this.put(var, Range.create(ranges.get(var)));
    }
  }
  
  synchronized public Range get(Object key) {
    return super.get(key);
  }
  
  synchronized public Set<Variable> keySet() {
    return super.keySet();
  }
  
  synchronized public int update(Variable var, Range range) {
    int result = NO_CHANGE;
    Range range0 = get(var);
    if (range0 == null) {
      if (!var.maxRange(range)) {
        // Adding range for variable var for the first time.
        put(var, range);
        result = ADDED_RANGE;  
      }      
    } else if (!range.equals(range0)) {    
      if (var.maxRange(range)) {
        // Removing range for variable var as it is set to its maximum range.
        remove(var);
        result = REMOVED_RANGE;
      } else {
        // Replacing range0 by range1 for new variable.
        put(var, range);
        result = MODIFIED_RANGE;
      }
    }
    return result;      
  }
  
  public String toString() {
    Iterator<Entry<Variable, Range>> i = entrySet().iterator();
    if (!i.hasNext())
        return "";

    StringBuilder sb = new StringBuilder();
    for (;;) {
      Entry<Variable, Range> e = i.next();
      Variable var = e.getKey();
      Range range = e.getValue();
      sb.append(var.getName() + ":" + var.getAlias().replace("'", "\\'"));
      sb.append(" = ");
      sb.append(var.formatRange(range, true));
      if (!i.hasNext()) {
        return sb.toString();
      }
      sb.append('\n').append(' ');
    }
  }
}

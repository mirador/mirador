/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mira.app;

import lib.ui.SoftFloat;
import lib.ui.Interface;
import miralib.data.Range;
import miralib.data.Variable;

/**
 * Base abstract class to define range selectors.
 *
 */

abstract public class RangeSelector extends MiraWidget {
  protected Variable selVar;
  protected SoftFloat offset;
  protected int bkColor;

  public RangeSelector(Interface intf, float x, float y, float w, float h, 
                       Variable svar) {
    super(intf, x, y, w, h);
    selVar = svar;
    offset = null;
  }
  
  public void setBackgroundColor(int bkColor) {
    this.bkColor = bkColor;
  }
  
  public void setOffset(SoftFloat offset) {
    this.offset = offset;
  }

  public float getFullHeight() {
   return bounds.h.getTarget(); 
  }

  public Variable getVariable() {
    return selVar;
  }
  
  abstract public Range getRange();
  
  protected float childLeft() {
    if (offset == null) return super.childLeft();
    else return super.childLeft() - offset.get();
  }
}

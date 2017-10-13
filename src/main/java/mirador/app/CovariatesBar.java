/* COPYRIGHT (C) 2014-16 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import processing.core.PApplet;
import processing.core.PFont;
import mui.Display;
import mui.Interface;
import mui.Widget;
import mui.SoftFloat;
import miralib.data.Variable;

/**
 * Subclass of ColumnScoller specialized to handle an horizontal scroll of 
 * covariate boxes.
 *
 */

public class CovariatesBar extends Scroller<Covariate> {
  protected float covarHeightMax;
  protected float covWidth, covHeight;

  public CovariatesBar(Interface intf, float x, float y, float w, float h,
                      float iw, float ih, float ihmax) {
    super(intf, x, y, w, h);
    covWidth = iw;
    covHeight = ih;
    covarHeightMax = ihmax;
  }

  public void draw() {
//    fill(color(120));
//    rect(0, 0, width, height);

    fill(color(150));
    rect(getDragBoxLeft(), getDragBoxTop(), getDragBoxWidth(), getDragBoxHeight());
  }
  
  public boolean inside(float x, float y) {
    return insideItems(x, y); 
  }    
  
  protected void handleResize(int newWidth, int newHeight) {
    float w0 = bounds.w.get();
    float w1 = newWidth - mira.optWidth;    
    float dw = w1 - w0;
    bounds.w.set(w1);
//    visX1.setTarget(visX1.getTarget() + dw);
  }

  protected int getCount() {
    return data.getCovariateCount();  
  }
  
  protected Variable getVariable(int i) {
    return data.getCovariate(i);
  }  
  
  protected int getIndex(Variable var) {
    return data.getCovariate(var);
  }



  @Override
  protected int getTotalItemCount() {
    // This is problematic, as columns can be added and removed...
    return data.getVariableCount();
  }

  @Override
  protected Scroller<Covariate>.Item createItem(int index) {
    Variable cvar = data.getColumn(index);
//    float h = item.open() ? heightOpen : heightClose;
    Covariate cov = new Covariate(intf, 0, 0, covWidth, covHeight, covarHeightMax, cvar,false /*defaultAnimation(event)*/);;
    addChild(cov, Widget.TOP_LEFT_CORNER);
    Scroller<Covariate>.Item item = new Item(index, cov);
    return item;
  }

  @Override
  protected boolean itemIsOpen(int index) {
    return data.getVariable(index).covariate();
  }

  @Override
  protected float getTargetWidth(int index) {
    return covWidth;
  }

  @Override
  protected float getTargetHeight(int index) {
    return covHeight;
    // What happens when the covariate is open?
  }
}

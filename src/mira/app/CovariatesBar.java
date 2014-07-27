/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mira.app;

import processing.core.PApplet;
import processing.core.PFont;
import lib.ui.SoftFloat;
import lib.ui.Interface;
import miralib.data.Variable;

/**
 * Sublcass of ColumnScoller specialized to handle an horizontal scroll of 
 * covariate boxes.
 *
 */

public class CovariatesBar extends ColumnScroller {
  protected float covarHeightMax;
  protected int bColor;
  protected PFont hFont;
  protected PFont pFont;
  protected int hColor;
  protected int pColor;
  protected int dColor;
  protected int oColor;
   
  public CovariatesBar(Interface intf, float x, float y, float w, float h,
                      float iw, float ih, float ihmax) {
    super(intf, x, y, w, h, iw, ih);
    covarHeightMax = ihmax;
  }

  public void setup() {
    bColor = getStyleColor("CovVarBox", "background-color");
    hFont = getStyleFont("CovVarBox.h2", "font-family", "font-size");
    hColor = getStyleColor("CovVarBox.h2", "color");
    pFont = getStyleFont("CovVarBox.p", "font-family", "font-size");
    pColor = getStyleColor("CovVarBox.p", "color");
    dColor = getStyleColor("CovVarBox.Dismiss", "color");
    oColor = getStyleColor("CovVarBox.open", "background-color");    
  }
  
  public boolean inside(float x, float y) {
    return insideItems(x, y); 
  }    
  
  protected void handleResize(int newWidth, int newHeight) {
    float w0 = bounds.w.get();
    float w1 = newWidth - mira.optWidth;    
    float dw = w1 - w0;
    bounds.w.set(w1);
    visX1.setTarget(visX1.getTarget() + dw);    
  }
  
  protected Item createItem(Variable var, float w, float h, int event) {
    boolean anim = event == WIDGET_UPDATE;     
    Covariate cov = new Covariate(var, w, h, anim);
    if (event == ITEMS_UPDATE) cov.show();
    return cov;
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
  
  protected class Covariate extends Item {
    boolean open;
    float crossw = 10;
    RangeSelector selector;
    SoftFloat maxAlpha;
    
    Covariate(Variable var, float w, float h, boolean anim) {
      super(var, w, h, anim);
      maxAlpha = new SoftFloat(255);
      open = false;
      
      if (var.numerical()) {
        selector = new NumericalRangeSelector(intf, x.get() + 10, h + 5, w - 20, 50, var);        
      } else if (var.categorical()) {
        selector = new CategoricalRangeSelector(intf, x.get() + 10, h + 5, w - 20, 50, var);
      }
      selector.setBackgroundColor(oColor);
      selector.setOffset(visX0);
      if (anim) selector.targetX(x.getTarget() + 20);
      addChild(selector, TOP_LEFT_CORNER);      
    }
    
    void dispose() { 
//      Log.message("remove " + selector);
      removeChild(selector);
    }
    
    void update() {
      super.update();
      selector.targetX(x.getTarget() + 10);
      maxAlpha.update();
    }
    
    void draw() {  
      float x0 = x.get() - visX0.get();
      float y0 = y.get();
      noStroke();
      if (itemHeight < h.get()) {
        fill(oColor, maxAlpha.getCeil());
        rect(x0 + padding, y0, w - 2 * padding, h.get());

        fill(bColor, maxAlpha.getCeil());
        rect(x0 + padding, y0, w - 2 * padding, itemHeight);
      } else {
        fill(bColor, maxAlpha.getCeil());
        rect(x0 + padding, y0, w - 2 * padding, h.get());    	  
      }
      
      textFont(hFont);      
      fill(hColor, maxAlpha.getCeil());
      String label = var.getName();
      String alias = var.getAlias();
      if (!label.equals(alias)) label += ": " + alias;      
      label = chopStringRight(label, w - padding - crossw - 20);
      text(label, x0 + 10, y0 + hFont.getSize() + 6);
      
      textFont(pFont);
      String range = var.formatRange(mira.ranges.get(var));
      range = chopStringRight(range, w - padding - crossw - 20);
      text(range, x0 + 10, y0 + textLeading() + hFont.getSize() + 4);
      
      drawDismiss();
    }

    void drawDismiss() {
      float x0 = x.get() - visX0.get() + w - padding - crossw - 10;
      float y0 = y.get() + itemHeight/2 - crossw/2;      
      stroke(dColor, maxAlpha.getCeil());
      strokeWeight(1);
      line(x0, y0, x0 + crossw, y0 + crossw);
      line(x0, y0 + crossw, x0 + crossw, y0);
    }

    boolean insideDismiss(float mx, float my) {
      float x0 = x.get() - visX0.get() + w - padding - crossw - 10;
      float y0 = y.get() + itemHeight/2 - crossw/2;
      return x0 <= mx && mx <= x0 + crossw && y0 <= my && my <= y0 + crossw; 
    }
    
    void mouseReleased() {
      if (!inside(mouseX, mouseY)) return;
        
      if (insideDismiss(mouseX, mouseY)) {
        data.removeCovariate(var);
        markedForRemoval = true;
      } else {
        open = !open;
        if (open) {
          float h1 = PApplet.min(covarHeightMax, itemHeight + 5 + selector.getFullHeight() + 10);
          
          y.setTarget(-(h1 - itemHeight));
          h.setTarget(h1);
          selector.targetY(y.getTarget() + itemHeight + 5);
          selector.setHeight(h1 - itemHeight - 5);
        } else {
          y.setTarget(0);
          h.setTarget(itemHeight);
          selector.targetY(itemHeight + 5);
          selector.setHeight(50);
        }
      }
    }
    
    void show() {
      maxAlpha.set(0);
      maxAlpha.setTarget(255);
      selector.show();
    }
  }  
}

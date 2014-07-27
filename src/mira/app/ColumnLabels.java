/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mira.app;

import processing.core.PApplet;
import processing.core.PFont;
import lib.ui.SoftFloat;
import lib.ui.Interface;
import miralib.data.Variable;

/**
 * Sublcass of ColumnScoller specialized to handle an horizontal scroll of 
 * column labels.
 *
 */

public class ColumnLabels extends ColumnScroller {
  protected float labelHeightMax;
  protected int bColor;
  protected PFont hFont;
  protected int hColor;
  protected float hLead;
  protected PFont pFont;
  protected int pColor;
  
  public ColumnLabels(Interface intf, float x, float y, float w, float h, 
                      float iw, float ih, float ihmax) {
    super(intf, x, y, w, h, iw, ih);
    labelHeightMax = ihmax;
  }
  
  public void setup() {
    bColor = getStyleColor("ColVarBox", "background-color");
    hFont = getStyleFont("ColVarBox.h2", "font-family", "font-size");
    hColor = getStyleColor("ColVarBox.h2", "color");
    hLead = getStyleSize("ColVarBox.h2", "line-height");
    
    pFont = getStyleFont("p", "font-family", "font-size");
    pColor = color(255);    
  }
  
  protected boolean ready() {
    return calledSetup;
  }
  
  protected void handleResize(int newWidth, int newHeight) {
    float w0 = bounds.w.get();
    float w1 = newWidth - mira.optWidth - mira.varWidth;    
    float dw = w1 - w0;
    bounds.w.set(w1);    
    visX1.setTarget(visX1.getTarget() + dw);
  }

  public void mouseDragged() {
    mira.browser.dragColumns(pmouseX - mouseX);
    dragging = true;
  }

  public void mouseReleased() {
    if (dragging) {
      mira.browser.snapColumns(); 
    } else {
      for (Item item: visItems.values()) {
        item.mouseReleased();
      }    
    }         
  }  
  
  public void jumpTo(int idx) {
    mira.browser.dragColumns(jumpToImpl(idx));
  }
  
  protected Item createItem(Variable var, float w, float h, int event) {
    // TODO: figure out what's the problem when animating sorting...
//    boolean anim = data.sorting() ? defaultAnimation(event) : false;
    return new Label(var, w, h, false /*defaultAnimation(event)*/);
  }
  
  protected int getCount() {
    return data.getColumnCount();  
  }
  
  protected Variable getVariable(int i) {
    return data.getColumn(i);
  }
  
  protected int getIndex(Variable var) {
    return data.getColumn(var);
  }
  
  protected class Label extends Item {
    boolean open;
    float crossw = 10;
    long t0;
    boolean ready;
    RangeSelector selector;
    
    protected boolean axisMode;
    protected SoftFloat axisAlpha;     
    
    Label(Variable var, float w, float h, boolean anim) {
      super(var, w, h, anim);
      open = false;
      
      if (var.numerical()) {
        selector = new NumericalRangeSelector(intf, 10 + x.get(), 55, w - 20, 40, var);        
      } else if (var.categorical()) {
        selector = new CategoricalRangeSelector(intf, 10 + x.get(), 55, w - 20, h - 55, var);
      }
      selector.setBackgroundColor(bColor);
      selector.setOffset(visX0);
      selector.hide(false);
      if (anim) selector.targetX(10 + x.getTarget());
      addChild(selector, TOP_LEFT_CORNER);
      t0 = millis();
      ready = false;
      
      axisMode = false;
      axisAlpha = new SoftFloat(0); 
    }
    
    void dispose() { 
//      Log.message("remove " + selector);
      removeChild(selector);
    }

    void update() {
      super.update();
      if (!ready && 500 < millis() - t0) {
        selector.show(true);
        ready = true;
      }
      
      if (mira.browser.getColAxis() == var && !axisMode) {
        axisMode = true;
        axisAlpha.setTarget(230);
      }
      if (mira.browser.getColAxis() != var && axisMode) {
        axisMode = false;
        axisAlpha.set(0);
      }
      axisAlpha.update();     
    }    
    
    void updatePosition() {
      super.updatePosition();
      selector.copyX(x, 10);      
//      selector.targetX(10 + x.getTarget());
    }    
    
    void draw() {
      float x0 = x.get() - visX0.get();
      float y0 = y.get();
      noStroke();
      fill(bColor);
      rect(x0 + padding, y0, w - 2 * padding, h.get());
      if (ready) drawDismiss();
    }
    
    void postDraw() {
      float x0 = x.get() - visX0.get();
      float y0 = y.get();
            
      String label;
      float w1 = w - 2 * padding;
      float h1 = h.get();      
      if (axisMode) {
        noStroke();
        fill(color(187, 185, 179), axisAlpha.getCeil());
        rect(x0 + padding, y0, w1, h1);
      } 
      
      fill(hColor);
      textFont(hFont);
      textLeading(hLead);
      label = var.getName();
      String alias = var.getAlias();
      if (!label.equals(alias)) label += ": " + alias;
      text(label, x0 + 10, y0 + 7, w - padding - crossw - 20, 35);  
      
      if (axisMode) {
        fill(pColor);
        textFont(hFont);
        textLeading(hLead);
        label = mira.browser.getColLabel(); 
        float vw = textWidth(label);
        if (vw <= w1) {
          float x1 = mouseX - vw/2;
          if (x1 < x0 + padding) x1 = x0 + padding;
          if (x0 + w1 < x1 + vw) x1 = x0 + w1 - vw;
          text(label, x1, y0 + h1 - 7);          
        } else {
          // This value string is too long, breaking it into two lines.
          text(label, x0 + 5, y0 + h1 - 34, w1 - 5, 40);           
        }
      }
    }
    
    void drawDismiss() {
      float x0 = x.get() - visX0.get() + w - padding - crossw - 10;
      float y0 = y.get() + 10;
      
      stroke(hColor, 100);
      strokeWeight(1);
      line(x0, y0, x0 + crossw, y0 + crossw);
      line(x0, y0 + crossw, x0 + crossw, y0);
    }

    boolean insideDismiss(float mx, float my) {
      float x0 = x.get() - visX0.get() + w - padding - crossw - 10;
      float y0 = y.get() + 10;
      return x0 <= mx && mx <= x0 + crossw && y0 <= my && my <= y0 + crossw; 
    }
    
    void mouseReleased() {
      if (!inside(mouseX, mouseY)) return;
        
      if (insideDismiss(mouseX, mouseY)) {
        mira.browser.closeColumn(var);
        markedForRemoval = true;
      } else {
        float x1 = selector.top() + selector.getFullHeight();
        if (itemHeight < x1) {
          open = !open;
          if (open) {
            float h1 = PApplet.min(labelHeightMax, x1);
            h.setTarget(h1);
            selector.targetHeight(h1 - selector.top());
          } else {
            h.setTarget(itemHeight);
            selector.targetHeight(itemHeight - 55);            
          }          
        }        
      }
    }    
  }
}

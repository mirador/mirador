/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import processing.core.PApplet;
import processing.core.PFont;
import mui.Display;
import mui.Interface;
import mui.SoftFloat;
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
  protected Variable requestedVar;
  
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

  public void update() {
    super.update();
    if (requestedVar != null) {
      mira.browser.openColumn(requestedVar);
      requestedVar = null;
    }


    //    int idx = data.getColumn(jumpVar);
//    if (-1 < idx) {
//      colLabels.jumpTo(idx);
//      hscroll.scrollTo(idx);
//    }



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
    // TODO: Make into CCS size parameters
    int crossw = Display.scale(10);
    int marginx = Display.scale(10);
    int marginy = Display.scale(7);
    int posy = Display.scale(55);
    int numh = Display.scale(40);
    int dismissy = Display.scale(10); 
    int labelh = Display.scale(35);
    int breakx = Display.scale(5);
    int breaky = Display.scale(34);
    int breakh = Display.scale(40);

    boolean open;
    long t0;
    boolean ready;
    RangeSelector selector;
    
    protected boolean axisMode;
    protected SoftFloat axisAlpha;     
    
    Label(Variable var, float w, float h, boolean anim) {
      super(var, w, h, anim);
      open = false;
      
      if (var.numerical()) {
        selector = new NumericalRangeSelector(intf, marginx + x.get(), posy, w - marginx*2, numh, var);        
      } else if (var.categorical()) {
        selector = new CategoricalRangeSelector(intf, marginx + x.get(), posy, w - marginx*2, h - posy, var);
      } else {
        System.out.println("fuck tu " + var.getName() + " " + var.type());
      }
      selector.setBackgroundColor(bColor);
      selector.setOffset(visX0);
      selector.hide(false);
      if (anim) selector.targetX(marginx + x.getTarget());
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
      if (!ready && 500 < millis() - t0 && visible()) {
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
      selector.copyX(x, marginx);      
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
      text(label, x0 + marginx, y0 + marginy, w - padding - crossw - marginx*2, labelh);  
      
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
          text(label, x1, y0 + h1 - marginy);          
        } else {
          // This value string is too long, breaking it into two lines.
          text(label, x0 + breakx, y0 + h1 - breaky, w1 - breakx, breakh);           
        }
      }
    }

    void drawDismiss() {
      float x0 = x.get() - visX0.get() + w - padding - crossw - marginx;
      float y0 = y.get() + dismissy;

      if (keyPressed(ALT) && insideDismiss(mouseX ,mouseY)) {
        if (mira.browser.data.getColumnCount() == 1) {
          noFill();
          stroke(hColor, 100);
          strokeWeight(2);
          ellipse(x0 + 0.5f * crossw, 0.5f * y0 + crossw, crossw, crossw);
        } else {
          fill(hColor, 100);
          noStroke();
          ellipse(x0 + 0.5f * crossw, 0.5f * y0 + crossw, crossw, crossw);
        }
      } else {
        stroke(hColor, 100);
        strokeWeight(1);
        line(x0, y0, x0 + crossw, y0 + crossw);
        line(x0, y0 + crossw, x0 + crossw, y0);
      }
    }

    boolean insideDismiss(float mx, float my) {
      float x0 = x.get() - visX0.get() + w - padding - crossw - marginx;
      float y0 = y.get() + dismissy;
      return x0 <= mx && mx <= x0 + crossw && y0 <= my && my <= y0 + crossw; 
    }
    
    void mouseReleased() {
      if (!inside(mouseX, mouseY)) return;
        
      if (insideDismiss(mouseX, mouseY)) {
        if (keyPressed(ALT)) {
          if (mira.browser.data.getColumnCount() == 1) {
            mira.browser.openAllColumns();
            requestedVar = var;
          } else {
            mira.browser.closeColumnsBut(var);
          }
        } else {
          mira.browser.closeColumn(var);
          markedForRemoval = true;          
        }
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
            selector.targetHeight(itemHeight - posy);            
          }          
        }        
      }
    }    
  }
}

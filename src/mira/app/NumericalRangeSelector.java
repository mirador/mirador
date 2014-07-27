/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mira.app;

import processing.core.PApplet;
import processing.core.PFont;
import lib.ui.SoftFloat;
import lib.ui.Interface;
import miralib.data.NumericalRange;
import miralib.data.NumericalVariable;
import miralib.data.Range;
import miralib.data.Variable;

/**
 * Selector for numerical variables. It uses a slider to define the min-max 
 * range of the variable.
 *
 */

public class NumericalRangeSelector extends RangeSelector {
  protected RangeBar rangeBar;
  protected DragHandle leftHandle;
  protected DragHandle rightHandle;
  protected double leftValue, rightValue;
  protected PFont pFont;
  protected int pColor, sColor, uColor, bkColor, brColor;
  protected float brWeight;
  protected boolean setRange0;
  protected SoftFloat defAnim;
  protected boolean requestedRangeUpdate;
  protected boolean resort;
  
  public NumericalRangeSelector(Interface intf, float x, float y, 
                                float w, float h, Variable svar) {
    super(intf, x, y, w, h, svar);
    captureKeys = true;
    
    setRange0 = false;
    defAnim = new SoftFloat(255);
    defAnim.setDamping(0.1f);
  }
  
  public void setup() {
    float rw = getStyleSize("NumRangeSelector.Box", "width");
    float rh = getStyleSize("NumRangeSelector.Box", "height");
    float sw = getStyleSize("NumRangeSelector.Selector", "width");
    float sh = getStyleSize("NumRangeSelector.Selector", "height");
    float tw = rw;
    float th = rh + sh;
    float scalex = width / tw;
    float scaley = height / th;
    float scalexy = PApplet.sqrt(scalex * scaley);
    
    pFont = getStyleFont("NumRangeSelector.Box.p", "font-family", "font-size", scalexy);
    pColor = getStyleColor("NumRangeSelector.Box.p", "color");
    
    bkColor = getStyleColor("NumRangeSelector.Box", "background-color");
    brColor = getStyleColor("NumRangeSelector.Box", "border-color");
    brWeight = getStyleSize("NumRangeSelector.Box", "border-width");
    
    sColor = getStyleColor("NumRangeSelector.Selector", "selected-color");
    uColor = getStyleColor("NumRangeSelector.Selector", "unselected-color");
        
    Range range = mira.ranges.get(selVar);
    if (range == null) {
      leftValue = 0;
      rightValue = 1;      
    } else {
      leftValue = selVar.normalize(range.getMin());
      rightValue = selVar.normalize(range.getMax());
    }    
    rangeBar = new RangeBar(0, scaley * sh, width, scaley * rh);
    leftHandle = new DragHandle((float)leftValue * width, 0, scalex * sw, scaley * sh, DragHandle.LEFT);
    rightHandle = new DragHandle((float)rightValue * width - scalex * sw, 0, scalex * sw, scaley * sh, DragHandle.RIGHT);
  }
 
  public float getFullHeight() {
    return bounds.h.getTarget() + pFont.getSize(); 
   } 
  
  public void lostFocus() {
    rangeBar.lostFocus();
  }
   
  public boolean inside(float x, float y) {
    return rangeBar.inside(x - left, y - top) ||
           leftHandle.inside(x - left, y - top) || 
           rightHandle.inside(x - left, y - top);
  }
  
  public void update() {
    defAnim.update();
    
    boolean rbup = rangeBar.update();
    boolean lhup = leftHandle.update();
    boolean rhup = rightHandle.update();    
    if (requestedRangeUpdate && !rbup && !lhup && !rhup) {
      mira.updateRanges(this, resort);
      requestedRangeUpdate = false;
    }    
  }
  
  public void draw() {
    rangeBar.draw();
    leftHandle.draw();
    rightHandle.draw();
  }
  
  public void mousePressed() {
    rangeBar.mousePressed();    
    leftHandle.mousePressed();
    rightHandle.mousePressed();
  }
  
  public void mouseDragged() {
    leftHandle.mouseDragged();
    rightHandle.mouseDragged();    
  }

  public void mouseReleased() {
    leftHandle.mouseReleased();
    rightHandle.mouseReleased();    
  } 
  
  public void keyPressed() {
    rangeBar.keyPressed();
    if (key == ESC) {
      if (intf.isSelectedWidget(this)) intf.selectWidget(null);
    }
  }
  
  public Range getRange() {
    Range range = new NumericalRange(selVar);    
    range.set(leftValue, rightValue);
    return range;
  }  
  
  protected boolean setRange() {        
    return 0 < leftValue || rightValue < 1 || 
           rangeBar.leftStr.isFocused() || rangeBar.rightStr.isFocused();    
  }
  
  public void initValues() {
    Range range = mira.ranges.get(selVar);
    if (range == null) {
      leftValue = 0;
      rightValue = 1;      
    } else {
      leftValue = selVar.normalize(range.getMin());
      rightValue = selVar.normalize(range.getMax());
    }    
  }
  
  public void resetValues(NumericalVariable var) {
    if (selVar != var) return;
    leftValue = 0;
    rightValue = 1;
    
    rangeBar.leftStr.set(selVar.formatValue(leftValue));    
    double sp = selVar.discrete() ? (leftValue < 1 ? 0.5d : 1d) / selVar.getCount() : 0;
    float svalue = PApplet.constrain((float)(leftValue - sp), 0, 1);    
    rangeBar.targetLeft(svalue);    
    leftHandle.target(svalue);
    
    rangeBar.rightStr.set(selVar.formatValue(rightValue));    
    sp = selVar.discrete() ? (0 < rightValue ? 0.5d : 1d) / selVar.getCount() : 0;
    svalue = PApplet.constrain((float)(rightValue + sp), 0, 1);
    rangeBar.targetRight(svalue);
    rightHandle.target(svalue);    
  }
  
  public void setLeftValue(NumericalVariable var, Float value, Boolean normalized) {
    if (selVar != var) return;
    leftValue = normalized ? value : selVar.normalize(value);
    rangeBar.leftStr.set(selVar.formatValue(leftValue));
    rangeBar.setLeft((float)leftValue);
    leftHandle.set((float)leftValue);
  }
  
  public void targetLeftValue(NumericalVariable var, Float value, Boolean normalized) {
    if (selVar != var) return;
    leftValue = normalized ? value : selVar.normalize(value);  
    rangeBar.leftStr.set(selVar.formatValue(leftValue));    
    double sp = selVar.discrete() ? (leftValue < 1 ? 0.5d : 1d) / selVar.getCount() : 0;
    float svalue = PApplet.constrain((float)(leftValue - sp), 0, 1);    
    rangeBar.targetLeft(svalue);    
    leftHandle.target(svalue);  
  }
  
  public void setRightValue(NumericalVariable var, Float value, Boolean normalized) {
    if (selVar != var) return;
    rightValue = normalized ? value : selVar.normalize(value);    
    rangeBar.rightStr.set(selVar.formatValue(rightValue));
    rangeBar.setRight((float)rightValue);
    rightHandle.set((float)rightValue);
  }
  
  public void targetRightValue(NumericalVariable var, Float value, Boolean normalized) {    
    if (selVar != var) return;
    rightValue = normalized ? value : selVar.normalize(value);
    rangeBar.rightStr.set(selVar.formatValue(rightValue));    
    double sp = selVar.discrete() ? (0 < rightValue ? 0.5d : 1d) / selVar.getCount() : 0;
    float svalue = PApplet.constrain((float)(rightValue + sp), 0, 1);
    rangeBar.targetRight(svalue);
    rightHandle.target(svalue);
  }
  
  class RangeBar {
    float x, y, w, h;
    SoftFloat leftSel, rightSel;
    EditableText leftStr, rightStr;
    String toStr, editStr;
    float toStrW, editStrW;
    
    RangeBar(float x, float y, float w, float h) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
      
      leftSel = new SoftFloat(x + (float)(leftValue * w)); 
      rightSel = new SoftFloat(x + (float)(rightValue * w));
      
      leftStr = new EditableText(selVar.formatValue(leftValue));
      rightStr = new EditableText(selVar.formatValue(rightValue));     
      
      toStr = " to ";
      editStr = "enter a range";
      toStrW = editStrW = -1;
    }
    
    boolean update() {
      leftSel.update();
      rightSel.update();
      return leftSel.isTargeting() || rightSel.isTargeting();
    }
    
    void draw() {
      stroke(brColor);
      strokeWeight(brWeight);
      fill(bkColor, 50);
      rect(x, y, w, h);
      
      float xc = x + w/2;
      float yc = (h + 2 - pFont.getSize()) / 2; 
      
      fill(pColor);  
      textFont(pFont);
      
      boolean set = setRange();
      if (setRange0 && !set) {
        defAnim.set(0);
        defAnim.setTarget(255);
      }
      setRange0 = set;
      
      String centerStr = "";
      float centerStrW = 0;
      if (set) {
        centerStr = toStr;
        if (toStrW < 0) toStrW = textWidth(toStr);
        centerStrW = toStrW;
      } else {
        centerStr = editStr;
        if (editStrW < 0) editStrW = textWidth(editStr);
        centerStrW = editStrW;
      }
      
      fill(pColor, defAnim.getCeil());
      text(centerStr, xc - centerStrW/2, y + h - yc - 1);
      
      if (set) {
        noStroke();
        fill(sColor, 35);
        rect(leftSel.get(), y, rightSel.get() - leftSel.get(), h); 
        
        fill(sColor);
        textFont(pFont);
        stroke(sColor);
        float leftStrW = textWidth(leftStr);
        text(leftStr, xc - centerStrW/2 - leftStrW, y + h - yc);
        text(rightStr, xc + centerStrW/2, y + h - yc);
      }
      
      fill(pColor);
      String val0 = selVar.formatValue(0);
      String val1 = selVar.formatValue(1);
      float w1 = textWidth(val1);
      text(val0, x, y + h + pFont.getSize() + 3);
      text(val1, x + w - w1, y + h + pFont.getSize() + 3);
    }
    
    void setLeft(float f) {
      leftSel.set(x + f * w);
    }

    void setRight(float f) {
      rightSel.set(x + f * w);
    }  

    void targetLeft(float f) {
      leftSel.setTarget(x + f * w);
    }

    void targetRight(float f) {
      rightSel.setTarget(x + f * w);
    }    
    
    boolean inside(float rx, float ry) {
      return x <= rx && rx <= x + w && y <= ry && ry <= y + h; 
    }    
    
    void lostFocus() {
      leftStr.setFocused(false);
      rightStr.setFocused(false);      
    }
    
    void mousePressed() {
      if (setRange()) {
        textFont(pFont);
        float leftStrW = textWidth(leftStr);
        float xc = x + w/2;
        float yc = (h - pFont.getSize()) / 2;        
        
        if (leftStr.inside(xc - toStrW/2 - leftStrW, y + h - yc, mouseX, mouseY)) {
          leftStr.setFocused(true);
        } else {
          leftStr.setFocused(false);
        }
        
        if (rightStr.inside(xc + toStrW/2, y + 20, mouseX, mouseY)) {
          rightStr.setFocused(true);
        } else {
          rightStr.setFocused(false);
        }        
        
      } else if (inside(mouseX, mouseY)) {
        if (mouseX  - x < w/2) {
          leftStr.setFocused(true);
        } else {
          rightStr.setFocused(true);          
        }        
      }
    }    
    
    void keyPressed() {
      if (leftStr.isFocused()) {
        if (key == ESC) {
          leftStr.set(selVar.formatValue(leftValue));
          leftStr.setFocused(false);
        } else if (key == ENTER || key == RETURN || key == TAB) {
          float value = leftStr.getFloat();
          if (Float.isNaN(value)) {
            leftStr.set(selVar.formatValue(leftValue));
          } else {
            float rvalue = rightStr.getFloat();
            if (rvalue < value) value = rvalue;            
            value = (float)selVar.snapValue(value, null, false);            
            intf.invoke(NumericalRangeSelector.class, "targetLeftValue", selVar, value, false);
          }
          if (key == TAB) {
            leftStr.setFocused(false);
            rightStr.setFocused(true);
          }
          resort = true;
          requestedRangeUpdate = true;          
        } else {
          leftStr.keyPressed(key, keyCode);
        }
      } else if (rightStr.isFocused()) {
        if (key == ESC) {
          rightStr.set(selVar.formatValue(rightValue));
          rightStr.setFocused(false);    
        } else if (key == ENTER || key == RETURN || key == TAB) {
          float value = rightStr.getFloat();
          if (Float.isNaN(value)) {
            rightStr.set(selVar.formatValue(rightValue));
          } else {
            float lvalue = leftStr.getFloat();
            if (value < lvalue) value = lvalue;
            value = (float)selVar.snapValue(value, null, false);
            intf.invoke(NumericalRangeSelector.class, "targetRightValue", selVar, value, false);
          }
          if (key == TAB) {
            rightStr.setFocused(false);
            leftStr.setFocused(true);            
          }
          resort = true;          
          requestedRangeUpdate = true;
        } else {
          rightStr.keyPressed(key, keyCode);
        }
      }
    }    
  }  
  
  class DragHandle {
    final static int LEFT  = 0;
    final static int RIGHT = 1;
    
    boolean focused;
    boolean dragging;
    boolean targeting;
    
    SoftFloat x0, x1, x2;
    float y0, y1, y2;
    float w;
    float dx0;
    int side; 
    int orientation;
    
    DragHandle(float x, float y, float w, float h, int side) {
      this.side = side;
      this.w = w;
      if (side == LEFT) {
        x0 = new SoftFloat(x); 
        x1 = new SoftFloat(x + 0.2f * w); 
        x2 = new SoftFloat(x + w);
        orientation = RIGHT;
      } else if (side == RIGHT) {
        x0 = new SoftFloat(x + w); 
        x1 = new SoftFloat(x + 0.8f * w); 
        x2 = new SoftFloat(x);
        orientation = LEFT;
      }
      y0 = y;  
      y1 = y + 0.8f * h; 
      y2 = y + h;
      
      focused = false;
      dragging = false;
      targeting = false;
      
      setOrientation();
    }
    
    boolean update() {
      x0.update(); 
      x1.update(); 
      x2.update();
      if (targeting) setOrientation();
      return x0.isTargeting();
    }
    
    void setOrientation() {
      if (side == LEFT) {
        if (w < x0.getTarget() && orientation == RIGHT) {
          // Switch orientation right to left
          x1.setTarget(x0.getTarget() - 0.2f * w); 
          x2.setTarget(x0.getTarget() - w);
          orientation = LEFT;            
        }
        if (x0.getTarget() < w && orientation == LEFT) {
          // Switch orientation left to right
          x1.setTarget(x0.getTarget() + 0.2f * w); 
          x2.setTarget(x0.getTarget() + w);            
          orientation = RIGHT;
        }          
      } else if (side == RIGHT) {
        if (x0.getTarget() < width - w  && orientation == LEFT) {
          // Switch orientation left to right
          x1.setTarget(x0.getTarget() + 0.2f * w); 
          x2.setTarget(x0.getTarget() + w);            
          orientation = RIGHT;
        }
        if (width - w < x0.getTarget() && orientation == RIGHT) {
          // Switch orientation right to left
          x1.setTarget(x0.getTarget() - 0.2f * w); 
          x2.setTarget(x0.getTarget() - w);            
          orientation = LEFT;
        }        
      }      
    }
    
    void draw() {
      noStroke();
      fill(setRange() ? sColor : uColor);
      beginShape(QUAD);
      vertex(x0.get(), y0);
      vertex(x2.get(), y0);
      vertex(x2.get(), y1);
      vertex(x0.get(), y1);      
      endShape();
      beginShape(TRIANGLE);
      vertex(x0.get(), y1);
      vertex(x1.get(), y1);
      vertex(x0.get(), y2);
      endShape();
    }
    
    boolean inside(float rx, float ry) {
      if (orientation == RIGHT) {
        return x0.get() <= rx && rx <= x2.get() && y0 <= ry && ry <= y1; 
      } else if (orientation == LEFT) {
        return x2.get() <= rx && rx <= x0.get() && y0 <= ry && ry <= y1;
      }
      return false;
    }

    void set(float f) {
      targeting = false;
      float dx1 = x1.get() - x0.get();
      float dx2 = x2.get() - x0.get();        
      x0.set(f * width); 
      x1.set(x0.get() + dx1);
      x2.set(x0.get() + dx2);  
    }
    
    void target(float f) {
      targeting = true;
      float dx1 = x1.getTarget() - x0.getTarget();
      float dx2 = x2.getTarget() - x0.getTarget();        
      x0.setTarget(f * width);
      x1.setTarget(x0.getTarget() + dx1);
      x2.setTarget(x0.getTarget() + dx2);      
    }       

    void snap() {
      float value = (float)selVar.snapValue(x0.getTarget() / width, null);        
      if (side == LEFT) {
        intf.invoke(NumericalRangeSelector.class, "targetLeftValue", selVar, value, true);
      } else if (side == RIGHT) {
        intf.invoke(NumericalRangeSelector.class, "targetRightValue", selVar, value, true);
      }
    }
    
    void mousePressed() {
      focused = inside(mouseX, mouseY);
      dx0 = x0.get() - mouseX;
      dragging = false;      
    }
    
    void mouseDragged() {
      if (focused) {
        dragging = true;        
        float value = 0;
        if (side == LEFT) {
          value = PApplet.constrain(mouseX + dx0, 0, rightHandle.x0.get()); 
        } else if (side == RIGHT) {
          value = PApplet.constrain(mouseX + dx0, leftHandle.x0.get(), width);
        }
        value = PApplet.map(value, 0, width, 0, 1);        
        if (side == LEFT) {    
          intf.invoke(NumericalRangeSelector.class, "setLeftValue", selVar, value, true);
        } else if (side == RIGHT) {
          intf.invoke(NumericalRangeSelector.class, "setRightValue", selVar, value, true);
        }
        resort = false;        
        requestedRangeUpdate = true;
      }
    }
    
    void mouseReleased() {
      if (dragging) {
        dragging = false;
        snap();
        resort = true; 
        requestedRangeUpdate = true;
        if (intf.isSelectedWidget(NumericalRangeSelector.this)) {
          // Relinquishing focus so the keyboard can be used to navigate the
          // rows and/or search for variables.
          intf.selectWidget(null);
        }
      }
    } 
  }
}

/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;
import mui.Interface;
import mui.SoftFloat;
import miralib.data.CategoricalRange;
import miralib.data.CategoricalVariable;
import miralib.data.Range;
import miralib.data.Variable;

/**
 * Selector for categorical variables. It uses checkboxes to define the 
 * categories included in the range.
 *
 */

public class CategoricalRangeSelector extends RangeSelector { 
  static public final float WIDTH_FACTOR = 1.7f;
  
  protected ArrayList<CategoryBox> boxes;
  protected boolean dragging;
  protected boolean pressing;
  protected float boxw, boxh, boxsp;
  protected int uColor, sColor;
  protected float brWeight;
  protected PFont pFont;
  protected int pColor;
  protected int bbrColor;
  protected float bbrWeight;
  protected boolean requestedRangeUpdate;
    
  public CategoricalRangeSelector(Interface intf, float x, float y, 
                                  float w, float h, Variable svar) {
    super(intf, x, y, w, h, svar);
    clipBounds(true);
  }

  public void setup() {
    float tw = getStyleSize("CatRangeSelector.Box", "width");
    float scalex = PApplet.max(0.78f, width / tw);
    
    bbrColor = getStyleColor("CatRangeSelector.Box", "border-color");
    bbrWeight = getStyleSize("CatRangeSelector.Box", "border-width");
    
    uColor = getStyleColor("CatRangeSelector.CheckBoxList.Check", "unselected-color");
    sColor = getStyleColor("CatRangeSelector.CheckBoxList.Check", "selected-color");
    brWeight = getStyleSize("CatRangeSelector.CheckBoxList.Check", "border-width");
        
    pFont = getStyleFont("CatRangeSelector.CheckBoxList.p", "font-family", "font-size", scalex);
    pColor = getStyleColor("CatRangeSelector.CheckBoxList.p", "color");
    
    Range range = mira.ranges.get(selVar);
    ArrayList<String> sel0 = range == null ? null : range.getValues();
    
    boxes = new ArrayList<CategoryBox>();
    boxw = getStyleSize("CatRangeSelector.CheckBoxList.Check", "width") * scalex;
    boxh = getStyleSize("CatRangeSelector.CheckBoxList.Check", "height") * scalex;
    boxsp = PApplet.max(2, (boxh/5) * scalex);
    ArrayList<String> values = selVar.getValues();
    for (int i = 0; i < values.size(); i++) {
      String value = values.get(i);
      CategoryBox box = new CategoryBox(value, 0, i * (boxh + boxsp), boxw, boxh, boxsp);
      box.selected = sel0 == null || sel0.contains(value);  
      boxes.add(box);    
    }
  }
  
  public void update() {
    for (CategoryBox box: boxes) {        
      box.update();
    }
    if (requestedRangeUpdate) {
      mira.updateRanges(this, true);
      requestedRangeUpdate = false;
    }
  }
  
  public void draw() {
    boolean vis = false;
    boolean needGradTop = false;
    boolean needGradBot = false;
    for (CategoryBox box: boxes) {        
      box.draw();
      if (!box.contained()) {
        if (vis) {
          needGradBot = true;
          break;
        } else {
          needGradTop = true;
        }
      } else {
        vis = true;
      }
    }
    
    if (needGradTop) {
//      noStroke();
//      beginShape(QUAD);
//      fill(bkColor, 255);
//      vertex(0, 0);
//      vertex(width, 0);
//      fill(bkColor, 0);
//      vertex(width, 10);
//      vertex(0, 10);
//      endShape();
      stroke(bbrColor);
      strokeWeight(bbrWeight);
      line(0, 0, width, 0);      
    }    
    if (needGradBot) {     
//      noStroke();
//      beginShape(QUAD);
//      fill(bkColor, 0);
//      vertex(0, height - 10);
//      vertex(width, height - 10);
//      fill(bkColor);
//      vertex(width, height);
//      vertex(0, height);
//      endShape();
      stroke(bbrColor);
      strokeWeight(bbrWeight);
      line(0, height - 1, width, height - 1); 
    }
  }
  
  public boolean inside(float x, float y) {
    if (!super.inside(x, y)) return false;    
    boolean insideSome = false;
    for (CategoryBox box: boxes) {
      if (!box.contained()) continue;
      if (box.inside(x - left, y - top)) {
        insideSome = true;
        break;
      }
    }
    return insideSome;
  }
  
  public void mousePressed() {    
    dragging = false;    
    if (!pressing) {
      pressing = true;
    }
  }
  
  public void mouseDragged() {
    dragging = true;
    float dy = mouseY - pmouseY;
    
    CategoryBox first = boxes.get(0);
    CategoryBox last = boxes.get(boxes.size() - 1);
    
    if (last.y.getTarget() + dy < height - boxh) {
      dy = height - boxh - last.y.getTarget();
    }
    if (0 < first.y.getTarget() + dy) {
      dy = -first.y.getTarget();
    }
    for (CategoryBox box: boxes) {
      box.drag(dy);
    }    
  }
  
  public void mouseReleased() {
    if (dragging) {
      snap();
    } else {
      CategoryBox selBox = null; 
      for (CategoryBox box: boxes) {
        if (!box.contained()) continue;
        if (box.selected(mouseX, mouseY)) {
          selBox = box;
          break;
        }
      }   
      if (selBox != null) {        
        boolean sel = !selBox.selected;
        boolean all = keyPressed(ALT);
        intf.invoke(CategoricalRangeSelector.class, "selectValue", selVar, selBox.value, sel, all);
        requestedRangeUpdate = true;        
      }
    }
    pressing = false;
  }
  
  public void resetValues(CategoricalVariable var) {
    if (selVar != var) return;
    for (CategoryBox box: boxes) {
      box.selected = true;
    }
  }
  
  public void selectValue(CategoricalVariable var, String value, Boolean sel, Boolean all) {
    if (selVar != var) return;
    for (CategoryBox box: boxes) {
      if (box.value.equals(value)) {
        box.selected = sel;
      } else if (all) {
        box.selected = !sel;
      }
    }
  }

  protected void snap() {
    // Snapping works by looking for the first partially visible box and
    // calculating an offset that aligns it to the top border
    float dy = -1;
    for (CategoryBox box: boxes) {      
      float y0 = box.y.getTarget();
      if (y0 < 0 && 0 < y0 + boxh + boxsp) {        
        if (y0 + boxh/2 < 0) {
          // More than half of the box is outside the view
          if (box != boxes.get(boxes.size() - 1)) {
            dy = -(y0 + boxh) - boxsp;
          } else {
            dy = -y0;
          }
        } else {
          // More than half of the box is inside the view
          dy = -y0;
        }
        
        // Making sure that the first and last boxes can be fully displayed.
        CategoryBox first = boxes.get(0);
        CategoryBox last = boxes.get(boxes.size() - 1);
        float ly0 = last.y.getTarget() + dy;
        float ly1 = ly0 + boxh + boxsp;
        if (ly0 < height && height < ly1) {
          dy = height - last.y.getTarget() - boxh - boxsp;
          float fy0 = first.y.getTarget() + dy;
          float fy1 = fy0 + boxh + boxsp;
          if (fy0 < 0 && 0 < fy1) {
            // Nothing can be done...
            dy = 0;
          }          
        }
        
        for (CategoryBox box1: boxes) {
          box1.y.setTarget(box1.y.getTarget() + dy);
        }        
        break;
      }
    }    
  }
  
  public float getFullHeight() {
    return selVar.getCount() * (boxh + boxsp);
  }
  
  public Range getRange() {
    Range range = new CategoricalRange(selVar);   
    range.set(getSelValues());
    return range;
  }   
  
  protected boolean setRange() {
    for (CategoryBox box: boxes) {        
      if (!box.selected) return true;
    }
    return false;
  }
  
  protected ArrayList<String> getSelValues() {
    ArrayList<String> values = new ArrayList<String>();
    for (CategoryBox box: boxes) {        
      if (box.selected) values.add(box.value);
    }
    return values;
  }

  class CategoryBox {
    float x, w, h, sep;
    SoftFloat y;
    String value;
    String label;
    boolean selected;
    
    CategoryBox(String value, float x, float y, float w, float h, float sep) {
      this.value = value;
      this.x = x;
      this.y = new SoftFloat(y);
      this.w = w;
      this.h = h;
      this.sep = sep;
      selected = true;
      
      textFont(pFont);
      label = chopStringRight(selVar.formatValue(value), width - w - 10);
    }
    
    void update() {
      y.update();
    }
    
    void draw() {
      boolean set = setRange();
      strokeWeight(brWeight);
//      stroke(brColor);
      
      if (selected) {
        stroke(set ? sColor : uColor);
        fill(set ? sColor : uColor);
        rect(x, y.get(), w, h);
      } else {
        stroke(set ? sColor : uColor);
        noFill();
        rect(x, y.get(), w, h);        
      }
      float yc = (h - pFont.getSize()) / 2; 
      fill(pColor);
      textFont(pFont);
      text(label, x + w + 5, y.get() + h - yc);
      
//      textFont(pFont);      
//      float w1 = textWidth(label);
//      if (w1 < width/2 - w) {
//        w1 = PApplet.min(WIDTH_FACTOR * w1, width/2 - w); 
//      }          
//      fill(color(255, 0, 0, 100));
//      rect(x, y.get(), w + 5 + w1, h);
    }
    
    void drag(float dy) {
      y.incTarget(dy);
    }
    
    boolean contained() {
      return 0 <= y.get() && y.get() + h <= height;
    }
    
    boolean inside(float rx, float ry) {
      textFont(pFont);      
      float w1 = textWidth(label);
      if (w1 < width/2 - w) {
        w1 = PApplet.min(WIDTH_FACTOR * w1, width/2 - w); 
      }      
      float x1 = x + w + 5 + w1;
      float y1 = y.get() + h + sep;
      return x <= rx && rx <= x1 && y.get() <= ry && ry <= y1;
    }
    
    boolean selected(float rx, float ry) {
      float x1 = x + w;
      float y1 = y.get() + h;
      return x <= rx && rx <= x1 && y.get() <= ry && ry <= y1;
    }
  }
}

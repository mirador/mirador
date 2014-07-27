/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mira.app;

import java.util.concurrent.FutureTask;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import lib.ui.SoftFloat;
import lib.ui.Interface;
import mira.data.DataSlice2D;
import mira.data.Variable;
import mira.shannon.Similarity;
import mira.views.View;

/**
 * Widget that contains all the plots of a row variable. It is a subclass of
 * ColumnScroller to allow for arbitrary large number of plots.
 *
 */

public class RowPlots extends ColumnScroller {
  protected Variable rowVar;
  protected PFont pFont;
  protected int pColor;
  protected int sColor;
  
  public RowPlots(Interface intf, float x, float y, float w, float h, 
                  float iw, float ih, ColumnScroller init) {
    super(intf, x, y, w, h, iw, ih, init);
  }
  
  public void setup() {
    super.setup();
    pFont = getStyleFont("RowPlots.p", "font-family", "font-size");
    pColor = getStyleColor("RowPlots.p", "color");
    sColor = getStyleColor("RowPlots", "selection-color");
  }
  
  public void setRowVar(Variable rvar) {
    rowVar = rvar;
  }

  public void show(boolean now) {
    if (rowVar.open()) super.show(now);
  } 
  
  public void mouseDragged() {    
    if (pmouseX - mouseX != 0) {
      mira.browser.dragColumns(pmouseX - mouseX);  
    } else if (pmouseY - mouseY != 0) {
      ((RowVariable)parent).dragRows(pmouseY - mouseY);
    }
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
  
  public void hoverIn() {
    mira.browser.setRowAxis(rowVar);
    mira.browser.setColAxis(getHoveredColumn());
  }
  
  public void hoverOut() {
    mira.browser.setRowAxis(null);
    mira.browser.setColAxis(null);    
  }
  
  public void mouseMoved() {
    mira.browser.setRowAxis(rowVar);
    mira.browser.setColAxis(getHoveredColumn());
  }
  
  public String getRowLabel(Variable var) {
    Plot plot = (Plot)visItems.get(var);
    if (plot != null) {
      return plot.getRowLabel();
    }
    return "";    
  }
  
  public String getColLabel(Variable var) {
    Plot plot = (Plot)visItems.get(var);
    if (plot != null) {
      return plot.getColLabel();
    }
    return "";     
  }  
  
  protected Variable getHoveredColumn() {
    for (Item item: visItems.values()) {
      if (item.inside(mouseX, mouseY)) {
        Plot plot = (Plot)item;
        return plot.var;
      }
    }
    return null;
  }
  
  protected boolean ready() {
    return calledSetup;
  }
  
  protected void handleResize(int newWidth, int newHeight) {
    float w0 = bounds.w.get();
    float w1 = newWidth - mira.varWidth - mira.optWidth;    
    float dw = w1 - w0;
    bounds.w.set(w1);
    visX1.setTarget(visX1.getTarget() + dw);    
  }  
  
  protected Item createItem(Variable var, float w, float h, int event) {
    // TODO: figure out what's the problem when animating sorting...
    //boolean anim = data.sorting() ? defaultAnimation(event) : false;
    return new Plot(var, w, h, false/*defaultAnimation(event)*/);
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
  
  protected class Plot extends Item {
    View view;    
    PGraphics pcanvas;
    PGraphics canvas;
    boolean dirty;
    boolean pdirty;
    boolean update;
    boolean depend;
    float missing;
    SoftFloat blendf;
    int corColor, misColor;
    
    @SuppressWarnings("rawtypes")
    FutureTask viewTask, indepTask;
    
    Plot(Variable var, float w, float h, boolean anim) {
      super(var, w, h, anim);
            
      corColor = getStyleColor("RowPlots.Pvalue", "background-color");
      misColor = getStyleColor("RowPlots.MissingData", "background-color");
      
      dirty = true;
      pdirty = true;
      update = false;
      depend = false;
      blendf = new SoftFloat();
    }
    
    void dispose() {
      if (viewTask != null && !viewTask.isDone()) {
        viewTask.cancel(true);
      }
      if (indepTask != null && !indepTask.isDone()) {
        indepTask.cancel(true);
      }
      
      if (pcanvas != null) {
        mira.removeCache(pcanvas);
        pcanvas.dispose();
        pcanvas = null;
      }
      
      if (canvas != null) {
        mira.removeCache(canvas);
        canvas.dispose();
        canvas = null;       
      }
    }

    void update() {
      super.update();
      
      if (showContents) {          
        if (dirty) {          
          dirty = false;
          update = false;
          if (viewTask != null && !viewTask.isDone()) viewTask.cancel(true);        
          viewTask = mira.browser.submitTask(new Runnable() {
            public void run() {
              DataSlice2D slice = data.getSlice(var, rowVar, mira.ranges);
              missing = slice.missing;
              view = View.create(slice, mira.getPlotType());
              update = true;
            }
          }, true);
        }
        if (pdirty) {          
          pdirty = false;
          if (indepTask != null && !indepTask.isDone()) indepTask.cancel(true);
          indepTask = mira.browser.submitTask(new Runnable() {
            public void run() {                  
              DataSlice2D slice = data.getSlice(var, rowVar, mira.ranges);                
              depend = 0 < Similarity.calculate(slice, mira.project.pvalue(), mira.project);              
            }
          }, false);            
        }          
        
        if (update) {
          update = false;
          if (pcanvas == null) {
            pcanvas = intf.createCanvas((int)w, h.getCeil(), MiraApp.RENDERER, MiraApp.SMOOTH_LEVEL);
          }
          if (canvas == null) {
            canvas = intf.createCanvas((int)w, h.getCeil(), MiraApp.RENDERER, MiraApp.SMOOTH_LEVEL);
            pcanvas.beginDraw();
            pcanvas.noStroke();
            pcanvas.fill(color(255));
            pcanvas.rect(0, 0, w, h.getCeil());
            pcanvas.endDraw();
          } else {
            pcanvas.beginDraw();
            pcanvas.image(canvas, 0, 0);
            pcanvas.endDraw();
          }
          
          view.draw(canvas);
          blendf.set(0);
          blendf.setTarget(255);          
        }     
        
        blendf.update();
      }
    }
    
    void draw() {
      float x0 = x.get() - visX0.get() + padding;
      float y0 = y.get() + padding;
      float w0 = w - 2 * padding;
      float h0 = h.get() - 2 * padding;
      
      if (showContents && canvas != null) {        
        int bfa = blendf.getCeil();     
        if (bfa < 255 && pcanvas != null) {
          tint(color(255));
          image(pcanvas, x0, y0, w0, h0);
        }          
        tint(color(255), bfa);
        image(canvas, x0, y0, w0, h0);
        
        if (view != null && (viewTask == null || viewTask.isDone()) && hovered &&
            x0 <= mouseX && mouseX <= x0 + w0 &&
            y0 <= mouseY && mouseY <= y0 + h0) {
          double valx = PApplet.constrain((mouseX - x0) / w0, 0, 1);
          double valy = PApplet.constrain((mouseY - y0) / h0, 0, 1);
          drawSelection(view.getSelection(valx, valy), x0, y0, w0, h0);
        }
        
        if (depend && mira.project.pvalue() < 1) {
          // TODO: show some kind of animation while 
          noStroke();
          fill(corColor);
          triangle(x0, y0, x0 + 13, y0, x0, y0 + 13);
        }
        
        if (mira.project.missingThreshold() <= missing) {
          float x1 = x0 + w0;
          noStroke();
          fill(misColor);
          triangle(x1 - 13, y0, x1, y0, x1, y0 + 13);
        }
      } else {
        noStroke();
        fill(color(255));
        rect(x0, y0, w0, h0);         
      }
      boolean selected = mira.browser.getSelectedRow() == rowVar &&
                         mira.browser.getSelectedCol() == var;
      if (selected) {
        stroke(sColor);
        strokeWeight(3);
        noFill();
        rect(x0, y0, w0, h0);        
      }
    }
    
    void drawSelection(View.Selection sel, 
                       float x0, float y0, float w0, float h0) {
      if (sel == null) return;
      
      sel.scale(x0, y0, w0, h0);
      
      if (sel.isEllipse) {
        // TODO: implement
        noStroke();
        fill(color(0, 0, 0), 50);
        ellipse(sel.x, sel.y, sel.w, sel.h);
      } else {
        noStroke();
        fill(color(0, 0, 0), 50);
        rect(sel.x, sel.y, sel.w, sel.h);
      }
      
      if (sel.hasLabel) {
        textFont(pFont);
        fill(pColor);
        float tw = textWidth(sel.label);          
        float tx = sel.x + sel.w/2 - tw/2;
        if (tx < x0) tx = x0;
        if (x0 + w0 < tx + tw) tx = x0 + w0 - tw;
        
        float ty = 0;
        if (pFont.getSize() < sel.h) { 
          float yc = (sel.h - pFont.getSize()) / 2;
          ty = sel.y + sel.h - yc;      
        } else {
          ty = sel.y - 5;
          if (ty - 5 - pFont.getSize() < y0) ty = sel.y + sel.h + 5 + pFont.getSize();
        }
        
        text(sel.label, tx, ty);
      }      
    }
    
    void dataChanged() {
      dirty = true;
      pdirty = true;
    }
    
    void pvalueChanged() {
      pdirty = true;
    }
    
    String getColLabel() {
      if (view != null && (viewTask == null || viewTask.isDone())) {
        float x0 = x.get() - visX0.get() + padding;
        float y0 = y.get() + padding;
        float w0 = w - 2 * padding;
        float h0 = h.get() - 2 * padding;
        
        double valx = PApplet.constrain((mouseX - x0) / w0, 0, 1);
        double valy = PApplet.constrain((mouseY - y0) / h0, 0, 1);
        
        return view.getLabelX(valx, valy);
      }
      return "";  
    }
    
    String getRowLabel() {
      if (view != null && (viewTask == null || viewTask.isDone())) {
        float x0 = x.get() - visX0.get() + padding;
        float y0 = y.get() + padding;
        float w0 = w - 2 * padding;
        float h0 = h.get() - 2 * padding;
        
        double valx = PApplet.constrain((mouseX - x0) / w0, 0, 1);
        double valy = PApplet.constrain((mouseY - y0) / h0, 0, 1);
        
        return view.getLabelY(valx, valy);  
      }
      return "";
    }   
    
    void mouseReleased() { 
      float x0 = x.get() - visX0.get() + padding;
      float y0 = y.get() + padding;
      float w0 = w - 2 * padding;
      float h0 = h.get() - 2 * padding;
      if (x0 <= mouseX && mouseX <= x0 + w0 &&
          y0 <= mouseY && mouseY <= y0 + h0) {
        boolean selected = mira.browser.getSelectedRow() == rowVar &&
                           mira.browser.getSelectedCol() == var;
        if (!selected) {
          mira.browser.setSelectedRow(rowVar);
          mira.browser.setSelectedCol(var);          
        } else {
          mira.browser.setSelectedRow(null);
          mira.browser.setSelectedCol(null);
        }
      }
    }
    
  }
}

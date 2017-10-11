/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import java.nio.file.Paths;
import java.util.concurrent.FutureTask;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import mui.Display;
import mui.Interface;
import mui.Widget;
import mui.SoftFloat;
import mirador.views.View;
import miralib.data.DataSlice1D;
import miralib.data.DataSlice2D;
import miralib.data.Variable;
import miralib.shannon.Similarity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Widget that contains all the plots of a row variable.
 *
 */

public class RowPlots extends MiraWidget {
  protected Variable rowVar;
  protected PFont pFont;
  protected int pColor;
  protected int sColor;
  protected HashMap<Variable, Plot> plots;
  protected ColScroller scroller;
  protected float plotWidth, plotHeight;
  
  public RowPlots(Interface intf, float x, float y, float w, float h, 
                  float iw, float ih, ColScroller scroller) {
    super(intf, x, y, w, h);
    plotWidth = iw;
    plotHeight = ih;
    this.scroller = scroller;
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
    if (rowVar.open()) {
      super.show(now);
    }
  }

  public Plot createPlot(Variable colVar) {
    Plot plot = new Plot(intf, colVar, rowVar, 0, 0, plotWidth, plotHeight);
    addChild(plot);
    return plot;
  }


  public void close(Variable var) {
    // TODO: need this...? need to check
    for (Widget child: children) {
      Plot plot = (Plot)child;
      if (plot.getColVar() == var) {
        removeChild(plot);
      }
    }
  }


  public void dataChanged() {
    for (Widget child: children) {
      Plot plot = (Plot)child;
      plot.dataChanged();
    }
  }

  public void pvalueChanged() {
    for (Widget child: children) {
      Plot plot = (Plot)child;
      plot.pvalueChanged();
    }
  }





//  public void mouseDragged() {
//    if (pmouseX - mouseX != 0) {
//      mira.browser.dragColumns(pmouseX - mouseX);
//    } else if (pmouseY - mouseY != 0) {
//      ((RowVariable)parent).dragRows(pmouseY - mouseY);
//    }
//    dragging = true;
//  }

//  public void mouseReleased() {
//    if (dragging) {
//      mira.browser.snapColumns();
//    } else {
//      for (Item item: visItems.values()) {
//        item.mouseReleased();
//      }
//    }
//  }
  
//  public void jumpTo(int idx) {
//    mira.browser.dragColumns(jumpToImpl(idx));
//  }
  
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
  
  public void enterPressed() {
    for (Widget child: children) {
      Plot plot = (Plot)child;
      if (plot.selected()) {
        plot.save();
      }
    }
  }
  
  public String getRowLabel(Variable var) {
    for (Widget child: children) {
      Plot plot = (Plot)child;
      if (plot.getRowVar() == var) {
        return plot.getRowLabel();
      }
    }
    return "";
  }
  
  public String getColLabel(Variable var) {
    for (Widget child: children) {
      Plot plot = (Plot)child;
      if (plot.getColVar() == var) {
        return plot.getColLabel();
      }
    }
    return "";
  }
  
  protected Variable getHoveredColumn() {
    for (Widget wt: children) {
      if (wt.inside(mouseX, mouseY)) {
        Plot plot = (Plot)wt;
        return plot.colVar;
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
//    float dw = w1 - w0;
    bounds.w.set(w1);
//    visX1.setTarget(visX1.getTarget() + dw);
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

/*
  protected Item createItem(Variable var, float w, float h, int event) {
    // TODO: figure out what's the problem when animating sorting...
    //boolean anim = data.sorting() ? defaultAnimation(event) : false;
    return new Plot(var, w, h, false defaultAnimation(event));
  }
*/


  /*
  protected class Plot extends Item {
    float triSize = Display.scale(13);

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

      mira.history.addPair(var, rowVar);
    }

    boolean selected() {
      return mira.browser.getSelectedRow() == rowVar &&
             mira.browser.getSelectedCol() == var;
    }

    void dispose() {
      if (viewTask != null && !viewTask.isDone()) {
        viewTask.cancel(true);
      }
      if (indepTask != null && !indepTask.isDone()) {
        indepTask.cancel(true);
      }

      if (pcanvas != null) {
        mira.g.removeCache(pcanvas);
        pcanvas.dispose();
        pcanvas = null;
      }

      if (canvas != null) {
        mira.g.removeCache(canvas);
        canvas.dispose();
        canvas = null;
      }

      mira.history.removePair(var, rowVar);
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
              if (var == rowVar) {
                DataSlice1D slice = data.getSlice(rowVar, mira.ranges);
                missing = slice.missing;
                view = View.create(slice, mira.project.binAlgorithm);
              } else {
                DataSlice2D slice = data.getSlice(var, rowVar, mira.ranges);
                missing = slice.missing;
                view = View.create(slice, mira.getPlotType(), mira.project.binAlgorithm);
              }
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
              float score = 0;
              if (slice.missing < mira.project.missingThreshold()) {
                score = Similarity.calculate(slice, mira.project.pvalue(), mira.project);
              }
              depend = 0 < score;
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

          view.draw(canvas, false);
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
          view.drawSelection(valx, valy, keyPressed(SHIFT), x0, y0, w0, h0,
                             RowPlots.this, pFont, pColor);
        }

        if (depend && mira.project.pvalue() < 1) {
          // TODO: show some kind of animation while calculating the significance...
          noStroke();
          fill(corColor);
          triangle(x0, y0, x0 + triSize, y0, x0, y0 + triSize);
        }

        if (mira.project.missingThreshold() <= missing) {
          float x1 = x0 + w0;
          noStroke();
          fill(misColor);
          triangle(x1 - triSize, y0, x1, y0, x1, y0 + triSize);
        }
      } else {
        noStroke();
        fill(color(255));
        rect(x0, y0, w0, h0);
      }
      if (selected()) {
        stroke(sColor);
        strokeWeight(3);
        noFill();
        rect(x0, y0, w0, h0);
      }
    }

    void draw(PGraphics pg) {
      float x0 = x.get() - visX0.get() + padding;
      float y0 = y.get() + padding;
      float w0 = w - 2 * padding;
      float h0 = h.get() - 2 * padding;

      pg.beginDraw();
      pg.image(canvas, 0, 0);
      if (view != null && (viewTask == null || viewTask.isDone()) && hovered &&
          x0 <= mouseX && mouseX <= x0 + w0 &&
          y0 <= mouseY && mouseY <= y0 + h0) {
        double valx = PApplet.constrain((mouseX - x0) / w0, 0, 1);
        double valy = PApplet.constrain((mouseY - y0) / h0, 0, 1);
        view.drawSelection(valx, valy, keyPressed(SHIFT), pg, pFont, pColor);
      }

      if (depend && mira.project.pvalue() < 1) {
        // TODO: show some kind of animation while calculating the significance...
        pg.noStroke();
        pg.fill(corColor);
        pg.triangle(0, 0, triSize, 0, 0, triSize);
      }

      if (mira.project.missingThreshold() <= missing) {
        pg.noStroke();
        pg.fill(misColor);
        pg.triangle(pg.width - triSize, 0, pg.width, 0, pg.width, triSize);
      }

      pg.noFill();
      pg.stroke(0);
      pg.strokeWeight(2);
      pg.rect(0, 0, pg.width, pg.height);
      pg.endDraw();
    }

    void save() {
      if (showContents && canvas != null) {
        String imgName = var.getName() + "-" + rowVar.getName() + ".pdf";
        String filename = Paths.get(mira.project.dataFolder, imgName).toString();
        PGraphics pdf = intf.createCanvas((int)w, h.getCeil(), PDF, filename);

        if (showContents) {
          view.draw(pdf, true);
        }
      }

//      if (showContents && canvas != null) {
//        PGraphics pg = intf.createCanvas((int)w, h.getCeil(), MiraApp.RENDERER, MiraApp.SMOOTH_LEVEL);
//        draw(pg);
//        
//        String imgName = var.getName() + "-" + rowVar.getName() + ".png";
//        String filename = Paths.get(mira.project.dataFolder, imgName).toString();
//        pg.save(filename);
//      }      
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
        if (!selected()) {
          mira.browser.setSelectedPair(var, rowVar);
        } else {
          mira.browser.setSelectedPair(null, null);
        }
      }
    }
  }
  */
}

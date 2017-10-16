/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import processing.core.PFont;
import mui.Interface;
import mui.Widget;
import miralib.data.Variable;
import java.util.HashMap;
import java.util.ArrayList;

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
  private RowVariable row;
  
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

    // Create new plots for all current columns, and attach them to the scroller item, which will handle positioning
    // and disposal automatically :-)
    ArrayList<ColLabel> cols = scroller.getColLabels();
    for (ColLabel col: cols) {
      Variable cvar = col.getVariable();
      Plot plot = createPlot(cvar);
      System.out.println("Creating plot for " + cvar.getName());
      scroller.attach(col, plot);
    }
  }

  public void show(boolean now) {
    if (rowVar.open()) {
      super.show(now);
    }
  }

  public Plot createPlot(Variable colVar) {
    Plot plot = new Plot(intf, colVar, rowVar, 0, 0, plotWidth, plotHeight);
    addChild(plot, Widget.TOP_LEFT_CORNER);
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

  @Override
  protected void setParent(Widget parent) {
    super.setParent(parent);
    row = (RowVariable)parent;
  }

  @Override
  public boolean showContents() {
    return showContents && (row.showContents());
  }
}

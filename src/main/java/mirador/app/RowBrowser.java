/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import mirador.handlers.ScrollbarHandler;
import mui.Display;
import mui.Interface;
import mui.Widget;
import miralib.data.DataTree;
import miralib.data.Variable;
import processing.core.PApplet;

/**
 * Vertical scroller for group, tables, and variables. It updates dynamically 
 * depending on the visible items.
 *
 */

public class RowBrowser extends MiraWidget {
  protected DataTree tree;
  protected int current;
  protected float heightOpen;
  protected float heightClose;
  protected RowScroller groupScroller, tableScroller, varScroller;
  protected VerticalScrollbar vbar;
  protected HorizontalScrollbar hbar, gbar;
  protected boolean animating = false;

  public RowBrowser(Interface intf, float x, float y, float w, float h,
                    float openh, float closeh) {
    super(intf, x, y, w, h);
    heightOpen = openh;
    heightClose = closeh;
    tree = data.getTree();
    initItems();
  }

  public void update() {
    RowScroller currScroller = (RowScroller)children.get(current);
    if (animating && !currScroller.isPositioning()) {
      animating = false;

      if (currScroller.getTotalHeight() < height) {
        vbar.hide(false);
      } else {
        vbar.show(true);
      }

      if (currScroller == varScroller &&
          mira.browser.width() - mira.varWidth < mira.browser.colLabels.getTotalWidth()) {
        hbar.show(true);
      }

    }
  }

  protected void handleResize(int newWidth, int newHeight) {
    float h1 = newHeight - mira.labelHeightClose;
    bounds.h.set(h1);

    RowScroller currScroller = (RowScroller)children.get(current);
    if (h1 < currScroller.getTotalHeight()) {
      vbar.show(true);
    } else {
      vbar.hide(false);
    }

    if (currScroller == varScroller) {
      float w1 = newWidth - mira.varWidth;
      if (w1 < mira.browser.colLabels.getTotalWidth()) {
        hbar.show(true);
      } else {
        hbar.hide(false);
      }
      vbar.setX(-mira.browser.scrollSize);
    } else {
      vbar.setX(-mira.browser.scrollSize - padding - newWidth + mira.varWidth + mira.optWidth);
    }
  }
  
  public boolean plotsReady() {
    return varScroller.plotsReady();
  }
  
  public void dragColumns(float dx) {
    varScroller.dragColumns(dx);    
  }
  
  public void snapColumns() {
    varScroller.snapColumns();
  }

  public void dataChanged() {
    varScroller.dataChanged();
  } 
  
  public void pvalueChanged() {
    varScroller.pvalueChanged();
  }

  public boolean showingVariables() {
    return children.get(current) == varScroller;
  }

  public void showVariables() {
    while (next(false) != varScroller);    
  }

  public void openRow(Variable var) {
    int idx = data.getVariableIndex(var);
    if (-1 < idx) {
      while (next(false) != varScroller);
      varScroller.setNextIndex(idx);
      updateGroupScrollbar();
    }
  }
  
  public void closeRowsBut(MiraWidget wt) {
    varScroller.closeAllBut(wt);
  }
  
  public void closeColumn(Variable var) {
    varScroller.closeColumn(var);
  }
  
  public String getRowLabel(Variable varx, Variable vary) {
    return varScroller.getRowLabel(varx, vary);
  }

  public String getColLabel(Variable varx, Variable vary) {
    return varScroller.getColLabel(varx, vary);
  }  

  public RowScroller getScroller() {
    return (RowScroller)children.get(current);
  }

  public void setScrollbars(VerticalScrollbar vbar, HorizontalScrollbar hbar, HorizontalScrollbar gbar) {
    this.vbar = vbar;
    this.hbar = hbar;
    this.gbar = gbar;
    if (!varScroller.isActive()) {
      hbar.hide(false);
      vbar.setX(-mira.browser.scrollSize - padding - mira.browser.width() + mira.varWidth);
      float gh = gbar == null ? 0 : mira.browser.scrollSize;
      vbar.setHeight(mira.browser.height() - mira.labelHeightClose - 2 * padding - gh);
    }
    RowScroller currScroller = (RowScroller)children.get(current);
    if (currScroller.getTotalHeight() < height) vbar.hide(false);
  }

  public void updateVertScrollbar() {
    vbar.scrollToFirst();
  }

  public void updateVertScrollbar(int idx) {
    vbar.scrollTo(idx);
  }

  public void updateHorScrollbar() {
    hbar.scrollToFirst();
  }

  public void updateGroupScrollbar() {
    if (gbar != null) gbar.scrollToFirst();
  }

  public void saveSelectedPlot() {
    varScroller.saveSelectedPlot();
  }

  public void keyPressed() {
    RowScroller currScroller = (RowScroller)children.get(current);
    if (key == CODED) {
      if (keyCode == LEFT) {
        prev(mouseX > right());
        updateGroupScrollbar();
      } else if (keyCode == RIGHT) {
        next();
        updateGroupScrollbar();
      } else if (keyCode == UP) {
        currScroller.up();
        updateVertScrollbar();
      } else if (keyCode == DOWN) {
        currScroller.down();
        updateVertScrollbar();
      }
    } else if (key == ENTER || key == RETURN) {
      mira.browser.switchPlotEdges();
    }
  }

  public int getTotItemsCount() {
    RowScroller scroller = (RowScroller)children.get(current);
    return scroller.getTotalCount();
  }

  public int getFirstItemIndex() {
    RowScroller scroller = (RowScroller)children.get(current);
    return scroller.getFirstIndex();
  }

  protected void initItems() {
    if (1 < tree.groups.size()) {
      // Initializing all three scroll levels (group, table, variable)
      groupScroller = new RowScroller(intf, this,0, 0, width, height, heightOpen, heightClose);
      groupScroller.setItems(tree.groups);
      groupScroller.setActive(true);
      addChild(groupScroller);

      tableScroller = new RowScroller(intf,this, width, 0, width, height, heightOpen, heightClose);
      tableScroller.setActive(false);
      tableScroller.setItems(tree.tables);
      addChild(tableScroller);
      
      varScroller = new RowScroller(intf,this,2 * width, 0, width, height, heightOpen, heightClose);
      varScroller.setActive(false);
      varScroller.setItems(tree.variables);      
      addChild(varScroller);
    } else if (1 < tree.tables.size()) {
      // Initializing only two scroll levels (table, variable), because there
      // is only one group
      tableScroller = new RowScroller(intf,this,0, 0, width, height, heightOpen, heightClose);
      tableScroller.setItems(tree.tables);
      tableScroller.setActive(true);
      addChild(tableScroller);

      varScroller = new RowScroller(intf,this, width, 0, width, height, heightOpen, heightClose);
      varScroller.setActive(false);
      varScroller.setItems(tree.variables);      
      addChild(varScroller);
    } else {
      // Initializing only one scroll level (variable), because there
      // is only one group and one table. This is the case when no metadata
      // is provided.
      varScroller = new RowScroller(intf,this, 0, 0, width, height, heightOpen, heightClose);
      varScroller.setItems(tree.variables);
      varScroller.setActive(true);
      addChild(varScroller);
    }
    current = 0;
  }  
  
  protected void showColumnLabels() {
    mira.browser.showColumnLabels();
  }
  
  protected void hideColumnLabels() {
    mira.browser.hideColumnLabels();
  }

  protected RowScroller prev() {
    return prev(true);
  }

  protected RowScroller prev(boolean dragCol) {
    if (dragCol && 0 < mira.browser.getFirstColumn()) {
      mira.browser.dragColumns(-mira.plotWidth);
      return (RowScroller)children.get(current);
    }

    if (0 < current) {
      RowScroller scroller0 = (RowScroller)children.get(current);
      RowScroller scroller1 = (RowScroller)children.get(current - 1);
      scroller0.setActive(false);
      scroller1.setActive(true, false);
      if (scroller1 == varScroller) {
        showColumnLabels();
        vbar.setX(-mira.browser.scrollSize);
        vbar.setHeight(mira.browser.height() - mira.labelHeightClose - mira.browser.scrollSize - 2 * padding);
      } else {
        hideColumnLabels();
        hbar.hide(false);
        vbar.setX(-mira.browser.scrollSize - padding - mira.browser.width() + mira.varWidth);
        float gh = gbar == null ? 0 : mira.browser.scrollSize;
        vbar.setHeight(mira.browser.height() - mira.labelHeightClose - 2 * padding - gh);
      }
      for (Widget wt: children) {
        float x = wt.targetX();
        wt.targetX(x + width);
      }
      vbar.hide(false);
      animating = true;
      updateVertScrollbar();
      current--;
      return scroller1;
    } else {
      return (RowScroller)children.get(0);
    }
  }

  protected RowScroller next(int i) {
    if (current < children.size() - 1) {
      RowScroller scroller0 = (RowScroller)children.get(current);
      RowScroller scroller1 = (RowScroller)children.get(current + 1);
      scroller0.setActive(false, false);
      scroller1.setActive(true);
      if (scroller1 == varScroller) {
        showColumnLabels();
        vbar.setX(-mira.browser.scrollSize);
        vbar.setHeight(mira.browser.height() - mira.labelHeightClose - mira.browser.scrollSize - 2 * padding);
      } else {
        hideColumnLabels();
        hbar.hide(false);
        vbar.setX(-mira.browser.scrollSize - padding - mira.browser.width() + mira.varWidth);
        float gh = gbar == null ? 0 : mira.browser.scrollSize;
        vbar.setHeight(mira.browser.height() - mira.labelHeightClose - 2 * padding - gh);
      }
      int idx = scroller0.items.get(i).getFirstChild();
      scroller1.setNextIndex(idx);
      for (Widget wt: children) {
        float x = wt.targetX();
        wt.targetX(x - width);
      }
      vbar.hide(false);
      animating = true;
      current++;
      return scroller1;
    } else {
      return (RowScroller)children.get(children.size() - 1);
    }
  }

  protected RowScroller next() {
    return next(true);
  }

  protected RowScroller next(boolean dragCol) {
    if (current < children.size() - 1) {
      RowScroller scroller0 = (RowScroller)children.get(current);
      RowScroller scroller1 = (RowScroller)children.get(current + 1);
      scroller0.setActive(false, false);
      scroller1.setActive(true);
      if (scroller1 == varScroller) {
        showColumnLabels();
        vbar.setX(-mira.browser.scrollSize);
        vbar.setHeight(mira.browser.height() - mira.labelHeightClose - mira.browser.scrollSize - 2 * padding);
      } else {
        hideColumnLabels();
        hbar.hide(false);
        vbar.setX(-mira.browser.scrollSize - padding - mira.browser.width() + mira.varWidth);
        float gh = gbar == null ? 0 : mira.browser.scrollSize;
        vbar.setHeight(mira.browser.height() - mira.labelHeightClose - 2 * padding - gh);
      }
      int i = scroller0.getFirstIndex();
      int idx = scroller0.items.get(i).getFirstChild();
      scroller1.setNextIndex(idx);
      for (Widget wt: children) {
        float x = wt.targetX();
        wt.targetX(x - width);
      }
      vbar.hide(false);
      animating = true;
      current++;
      return scroller1;
    } else {
      if (dragCol) mira.browser.dragColumns(mira.plotWidth);
      return (RowScroller)children.get(children.size() - 1);
    }
  }
}

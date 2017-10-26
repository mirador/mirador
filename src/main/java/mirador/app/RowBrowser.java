/* COPYRIGHT (C) 2014-16 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import mui.Interface;
import mui.Widget;
import miralib.data.DataTree;
import miralib.data.Variable;

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
  
  public RowBrowser(Interface intf, float x, float y, float w, float h,
                    float openh, float closeh) {
    super(intf, x, y, w, h);
    heightOpen = openh;
    heightClose = closeh;
    tree = data.getTree();
    initItems();
  }
  
  protected void handleResize(int newWidth, int newHeight) {
    bounds.h.set(newHeight - mira.labelHeightClose);
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
  
  public void showVariables() {
    while (next(false) != varScroller);    
  }
  
  public void openRow(Variable var) {
    int idx = data.getVariableIndex(var);
    if (-1 < idx) {
      while (next(false) != varScroller);
      varScroller.jumpTo(idx);      
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
      intf.selectWidget(scroller1);
      if (scroller1 == varScroller) showColumnLabels();
      else hideColumnLabels();
      for (Widget wt: children) {
        float x = wt.targetX();
        wt.targetX(x + width);
      }
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
      intf.selectWidget(scroller1);
      if (scroller1 == varScroller) showColumnLabels();
      else hideColumnLabels();      
      int idx = scroller0.items.get(i).getFirstChild();
      scroller1.jumpTo(idx, false);
      for (Widget wt: children) {
        float x = wt.targetX();
        wt.targetX(x - width);
      }
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
      intf.selectWidget(scroller1);
      if (scroller1 == varScroller) showColumnLabels();
      else hideColumnLabels();      
      for (Widget wt: children) {
        float x = wt.targetX();    
        wt.targetX(x - width);
      }
      current++;
      return scroller1;
    } else {
      if (dragCol) mira.browser.dragColumns(mira.plotWidth);
      return (RowScroller)children.get(children.size() - 1);
    }
  }  
  
  public void keyPressed() {
    RowScroller currScroller = (RowScroller)children.get(current);
    if (key == CODED) {
      if (keyCode == LEFT) {
        prev(mouseX > right());
      } else if (keyCode == RIGHT) {
        next();
      } else if (keyCode == UP) {
        currScroller.up();            
      } else if (keyCode == DOWN) {
        currScroller.down();
      }
    } else if (key == ENTER || key == RETURN) {
      currScroller.enter();      
    }
  }
  
  protected void initItems() {
    if (1 < tree.groups.size()) {
      // Initializing all three scroll levels (group, table, variable)
      groupScroller = new RowScroller(intf, this,0, 0, width, height, heightOpen, heightClose);
      groupScroller.setItems(tree.groups);
      groupScroller.setActive(true);
      addChild(groupScroller);
      intf.selectWidget(groupScroller);

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
      intf.selectWidget(tableScroller);
      
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
      intf.selectWidget(varScroller);       
    }
    current = 0;
  }  
  
  protected void showColumnLabels() {
    mira.browser.showColumnLabels();
  }
  
  protected void hideColumnLabels() {
    mira.browser.hideColumnLabels();
  }
}

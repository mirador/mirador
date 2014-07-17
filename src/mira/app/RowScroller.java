/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mira.app;

import java.util.ArrayList;

import processing.core.PApplet;
import lib.ui.Interface;
import lib.ui.Widget;
import mira.data.DataTree;
import mira.data.Variable;
import mira.data.VariableContainer;
import mira.data.DataTree.Item;

/**
 * Vertical scroller for group, tables, and variables. It updates dynamically 
 * depending on the visible items.
 *
 * @author Andres Colubri
 */

public class RowScroller extends MiraWidget {
  protected DataTree tree;
  protected int current;
  protected float heightOpen;
  protected float heightClose;
  SingleScroller groupScroller, tableScroller, varScroller;
  
  public RowScroller(Interface intf, float x, float y, float w, float h, 
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
    while (next() != varScroller);    
  }
  
  public void openRow(Variable var) {
    int idx = data.getVariableIndex(var);
    if (-1 < idx) {
      while (next() != varScroller);
      varScroller.jumpTo(idx);      
    }
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
  
  protected SingleScroller prev() {
    if (0 < current) {
      SingleScroller scroller0 = (SingleScroller)children.get(current);
      SingleScroller scroller1 = (SingleScroller)children.get(current - 1);
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
      return (SingleScroller)children.get(0);
    }    
  } 
  
  protected SingleScroller next(int i) {
    if (current < children.size() - 1) {
      SingleScroller scroller0 = (SingleScroller)children.get(current);
      SingleScroller scroller1 = (SingleScroller)children.get(current + 1);
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
      return (SingleScroller)children.get(children.size() - 1);
    }
  }
  
  protected SingleScroller next() {
    if (current < children.size() - 1) {
      SingleScroller scroller0 = (SingleScroller)children.get(current);
      SingleScroller scroller1 = (SingleScroller)children.get(current + 1);
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
      return (SingleScroller)children.get(children.size() - 1);
    }
  }  
  
  public void keyPressed() {
    SingleScroller currScroller = (SingleScroller)children.get(current);    
    if (key == CODED) {
      if (keyCode == LEFT) {
        prev();
      } else if (keyCode == RIGHT) {
        next();
      } else if (keyCode == UP) {
        currScroller.up();            
      } else if (keyCode == DOWN) {
        currScroller.down();
      }
    }    
  }
  
  protected void initItems() {
    if (1 < tree.groups.size()) {
      // Initializing all three scroll levels (group, table, variable)
      groupScroller = new SingleScroller(intf, 0, 0, width, height);
      groupScroller.setItems(tree.groups);
      groupScroller.setActive(true);
      addChild(groupScroller);
      intf.selectWidget(groupScroller);        
      
      tableScroller = new SingleScroller(intf, width, 0, width, height);
      tableScroller.setActive(false);
      tableScroller.setItems(tree.tables);
      addChild(tableScroller);
      
      varScroller = new SingleScroller(intf, 2 * width, 0, width, height);
      varScroller.setActive(false);
      varScroller.setItems(tree.variables);      
      addChild(varScroller);        
    } else if (1 < tree.tables.size()) {
      // Initializing only two scroll levels (table, variable), because there
      // is only one group
      tableScroller = new SingleScroller(intf, 0, 0, width, height);
      tableScroller.setItems(tree.tables);
      tableScroller.setActive(true);
      addChild(tableScroller);
      intf.selectWidget(tableScroller);
      
      varScroller = new SingleScroller(intf, width, 0, width, height);
      varScroller.setActive(false);
      varScroller.setItems(tree.variables);      
      addChild(varScroller);
    } else {
      // Initializing only one scroll level (variable), because there
      // is only one group and one table. This is the case when no metadata
      // is provided.
      varScroller = new SingleScroller(intf, 0, 0, width, height);
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
  
  protected class SingleScroller extends MiraWidget {
    ArrayList<DataTree.Item> items;
    boolean active;
    boolean dragx;
    int pmouseX0, dragx1;
    int savedIdx;
    boolean needShow;
    
    public SingleScroller(Interface intf, float x, float y, float w, float h) {
      super(intf, x, y, w, h);
      active = false;
      savedIdx = -1;
    }
    
    protected void handleResize(int newWidth, int newHeight) {
      bounds.h.set(newHeight - mira.labelHeightClose);
    } 
        
    public void setItems(ArrayList<DataTree.Item> items) {
      this.items = items;
    }

    public void setActive(boolean active) {
      setActive(active, true);
    }
    
    public void setActive(boolean active, boolean changeAlpha) {
      this.active = active;
      if (!active) {
        savedIdx = -1;
        for (Widget child: children) {
          if (changeAlpha) child.hide();
          if (savedIdx == -1 && !child.isMarkedForDeletion()) {
            MiraWidget wt = (MiraWidget)child;            
            savedIdx = wt.idx;
          }
        }
      } else { 
        needShow = changeAlpha;
      }      
    }
    
    public boolean isActive() {
      return active;
    }
    
    public void up() {
      fit(-heightClose);      
    }

    public void down() {
      fit(+heightClose);
    }    
    
    public void prev() {      
      RowScroller.this.prev();
    } 
    
    public void next() {
      RowScroller.this.next();
    }
    
    public void next(int i) {
      RowScroller.this.next(i);
    }
        
    public void dragRows(float dy) {
      if (active) {
        fit(dy);
      }
    }
    
    public boolean plotsReady() {
      boolean ready = true;
      for (Widget child: children) {
        if (child instanceof RowVariable) {
          if (!((RowVariable)child).plotsReady()) {
            ready = false;
            break;
          }
        }
      }      
      return ready;
    }
    
    public void dragColumns(float dx) {
      for (Widget child: children) {
        if (child instanceof RowVariable) {
          ((RowVariable)child).drag(dx);
        }
      }
    }
    
    public void snapColumns() {
      for (Widget child: children) {
        if (child instanceof RowVariable) {
          ((RowVariable)child).snap();
        }
      }      
    }

    public void dataChanged() {
      for (Widget child: children) {
        if (child instanceof RowVariable) {
          ((RowVariable)child).dataChanged();
        }
      }
    }    
    
    public void pvalueChanged() {
      for (Widget child: children) {
        if (child instanceof RowVariable) {
          ((RowVariable)child).pvalueChanged();
        }
      }
    }
    
    public void closeColumn(Variable var) {
      for (Widget child: children) {
        if (child instanceof RowVariable) {
          ((RowVariable)child).close(var);
        }
      }      
    }
    
    public String getRowLabel(Variable varx, Variable vary) {
      for (Widget child: children) {
        if (child instanceof RowVariable) {
          RowVariable rvar = (RowVariable)child;
          if (rvar.getVariable() == vary) return rvar.getRowLabel(varx);
        }
      }
      return "";
    }

    public String getColLabel(Variable varx, Variable vary) {
      for (Widget child: children) {
        if (child instanceof RowVariable) {
          RowVariable rvar = (RowVariable)child;
          if (rvar.getVariable() == vary) return rvar.getColLabel(varx);
        }
      }
      return "";
    } 
    
    public void update() {
      cleanScroll();      
      if (active) {
        if (children.size() == 0) { 
          initScroll();
          if (-1 < savedIdx) jumpTo(savedIdx, false);
        } else if (0 < children.size() && getMarkedForDeletionCount() == 0) {
          updateScroll();          
        }
        if (needShow) {
          for (Widget child: children) child.show(true);
          needShow = false;
        }        
      }      
      fit();
    }
    
    public void mousePressed() {
      if (active) {
        dragx = false;
        pmouseX0 = mouseX;
      }
    }
    
    public void mouseDragged() {
      if (active) {
        int dx = pmouseX - mouseX; 
        int dy = pmouseY - mouseY;      
        if (dy != 0) {
          fit(dy);
          dragx = false;
        } else if (dx != 0) {
          dragx = true;
        }        
      }
    }
    
    public void mouseReleased() {
      if (active) {
        if (dragx) {
          int dx = pmouseX0 - mouseX;
          if (20 < dx) {
            RowScroller.this.next();
          } else if (dx < 20) {
            RowScroller.this.prev();
          }
        } else {
          snap();
        }        
      }
    }
    
    public void mouseReleased(MiraWidget  wt) {
      if (active) {
        if (canOpen(wt.idx)) {
          if (isOpen(wt.idx)) {
            close(wt.idx);
            wt.targetHeight(heightClose);
          } else {
            open(wt.idx);
            wt.targetHeight(heightOpen);
          }      
          updatePositions(wt);
        } else {
          next(wt.idx);
        }
      } 
    }
    
    public void keyPressed(MiraWidget  wt) {
      if (active) {
        if (key == CODED) {
          if (keyCode == LEFT) {
            prev();
          } else if (keyCode == RIGHT) {
            next();
          } else if (keyCode == UP) {
            up();            
          } else if (keyCode == DOWN) {
            down();
          }
        }        
      }      
    }
    
    public void jumpTo(int i) {
      jumpTo(i, true);
    }
    
    public void jumpTo(int i, boolean target) {
      if (0 <= i && i < items.size()) {
        if (children.size() == 0) initScroll();       
        MiraWidget first = (MiraWidget)children.get(0);        
        float h0 = first.targetY();
        float hdif = getHeight(first.idx, i);
        fit(h0 + hdif, target);
      }
    }
    
    public String getName(int i) {
      if (0 <= i && i < items.size()) {
        return items.get(i).getName();
      } else {
        return "";
      }
    }
    
    public boolean canOpen(int i) {
      if (0 <= i && i < items.size()) {
        return items.get(i).canOpen();
      } else {
        return false;
      }      
    }
    
    public boolean isOpen(int i) {
      if (0 <= i && i < items.size()) {
        return items.get(i).open();
      } else {
        return false;
      }      
    }
    
    public void open(int i) {
      if (0 <= i && i < items.size()) {
        items.get(i).setOpen();
      }      
    }

    public void close(int i) {
      if (0 <= i && i < items.size()) {
        items.get(i).setClose();
      }      
    }    
    
    public float getHeight() {
      return getHeight(0, items.size());
    }

    public float getHeight(int i0, int i1) {
      boolean inverted = false;
      if (i1 < i0) {
        int tmp = i0;
        i0 = i1;
        i1 = tmp;
        inverted = true;
      }
      float h = 0;
      for (int i = i0; i < i1; i++) {
        Item itm = items.get(i);
        h += itm.open() ? heightOpen : heightClose;
      }
      
      return inverted ? -h : h;      
    }
    
    public int getWidgetCountTop(MiraWidget first) {
      int n = 0;
      float h = 0;
      float maxh = first.top() - top();      
      while (h <= maxh) {
        n++;
        int i = first.idx - n;
        if (i < 0) break;
        h += items.get(i).open() ? heightOpen : heightClose;
      }
      return n;
    }
    
    public int getWidgetCountBottom(MiraWidget last) {
      int n = 0;
      float h = 0;
      float maxh = bottom() - last.bottom();      
      while (h <= maxh) {
        n++;
        int i = last.idx + n;
        if (i == items.size()) break;
        h += items.get(i).open() ? heightOpen : heightClose;
      }
      return n;
    }
    
    public int getWidgetCountInit() {
      int n = 0;
      float h = 0;     
      while (h <= height) {
        n++;
        if (n == items.size()) break;
        h += items.get(n).open() ? heightOpen : heightClose;
      }
      return n;      
    }
    
    public void updatePositions(MiraWidget prev) {
      int idx = children.indexOf(prev);
      for (int i = idx + 1; i < children.size(); i++) {
        MiraWidget wt = (MiraWidget)children.get(i);
        float y = prev.targetY() + prev.targetHeight();
        wt.targetY(y);
        prev = wt;
      }      
    }
    
    public void fit() {
      fit(0);
    }
    
    public void fit(float dy) {
      fit(dy, true);
    }
    
    public void fit(float dy, boolean target) {
      if (0 < children.size()) {
        float toffset = 0;
        float boffset = 0;
        MiraWidget first = (MiraWidget)children.get(0);
        MiraWidget last = (MiraWidget)children.get(children.size() - 1);
        float h = PApplet.min(height, getHeight());
        
        if (first.idx == 0 && 0 < first.targetY() - dy) {
          toffset = first.targetY() - dy;
        }                
        if (last.idx == items.size() - 1 && last.targetY() + last.height() - dy < h) {
          boffset = h - last.targetY() - last.height() + dy;
        }        
        
        if (dy != 0 || 0 < toffset || 0 < boffset) {
          for (Widget child: children) {
            float y = child.targetY() - dy;
            y -= toffset;
            y += boffset;
            if (target) {
              child.targetY(y);  
            } else {
              child.setY(y); 
            }            
          }          
        }
      }
    }
    
    public void snap() {
      if (0 < children.size()) {
        float offset = 0;
        MiraWidget first = (MiraWidget)children.get(0);
        MiraWidget last = (MiraWidget)children.get(children.size() - 1);
        float h = PApplet.min(height, getHeight());
        
        if (PApplet.abs(first.targetY()) < 30) {
          offset = -first.targetY();
        } else if (PApplet.abs(first.targetY() + first.targetHeight()) < 30) {
          offset = -(first.targetY() + first.targetHeight());
        } else if (PApplet.abs(last.targetY() - h) < 30) { 
          offset = h - last.targetY();
        } else if (PApplet.abs(last.targetY() + last.targetHeight() - h) < 30) {
          offset = h - (last.targetY() + last.targetHeight());
        }
        
        if (0 != offset) {
          for (Widget child: children) {
            float y = child.targetY();
            y += offset;
            child.targetY(y);
          }        
        }
      }
    }   
    
    protected void initScroll() {
      int count = getWidgetCountInit();
      float y = 0;
      for (int i = 0; i < count; i++) {
        float h = items.get(i).open() ? heightOpen : heightClose;
        MiraWidget wt = createScrollWidget(items.get(i), y, h);  
        wt.setIndex(i);
        wt.setTimeOut(REMOVE_ROW_DELAY);
        wt.setDraggable(false);
        addChild(wt);
        y += h;
      }      
    }
    
    protected void updateScroll() {
      // Adding widgets at the top
      MiraWidget first = (MiraWidget)children.get(0);
      if (0 < first.idx && top() < first.top()) {          
        int n = getWidgetCountTop(first);
        int idx0 = PApplet.max(first.idx - n, 0);
        for (int i = first.idx - 1; i >= idx0; i--) {
          float h = items.get(i).open() ? heightOpen : heightClose;
          MiraWidget wt = createScrollWidget(items.get(i), 0, h);
          wt.setIndex(i);
          wt.setTimeOut(REMOVE_ROW_DELAY);
          wt.copyY(first, -wt.targetHeight());
          wt.setDraggable(false);
          addChild(0, wt);
          first = wt;
        }
      }
      
      // Adding widgets at the bottom
      MiraWidget last = (MiraWidget)children.get(children.size() - 1);
      if (last.idx < items.size() - 1 && last.bottom() < bottom() - 1) {
        int n = getWidgetCountBottom(last);
        int idx1 = PApplet.min(last.idx + n, items.size() - 1);
        for (int i = last.idx + 1; i <= idx1; i++) {
          float h = items.get(i).open() ? heightOpen : heightClose;
          MiraWidget wt = createScrollWidget(items.get(i), 0, h);
          wt.setIndex(i);
          wt.setTimeOut(REMOVE_ROW_DELAY);
          wt.copyY(last, +last.targetHeight());
          wt.setDraggable(false);
          addChild(wt);
          last = wt;
        }
      }              
    }

    protected void cleanScroll() {
      for (Widget child: children) {
        MiraWidget wt = (MiraWidget)child;
        if (wt.timedOut()) {
          removeChild(wt);
//          Log.message("Removing scroll item " + wt.idx);
        }
      }      
    }
    
    protected MiraWidget createScrollWidget(Item item, float y, float h) {
      int type = item.getItemType();
      if (type == DataTree.GROUP_ITEM) {
        return new RowGroup(intf, 0, y, width, h, (VariableContainer)item);
      } else if (type == DataTree.TABLE_ITEM) {
        return new RowTable(intf, 0, y, width, h, (VariableContainer)item);
      } else if (type == DataTree.VARIABLE_ITEM) {        
        return new RowVariable(intf, 0, y, width, h, (Variable)item);
      }
      return null;
    }
  }  
}

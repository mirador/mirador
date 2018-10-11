/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

import mirador.handlers.TaskHandler;
import mui.Display;
import mui.Interface;
import mui.Widget;
import miralib.data.Variable;
import miralib.data.VariableContainer;
import mirador.handlers.ScrollbarHandler;
import processing.core.PApplet;

/**
 * The main visualization area containing the row, columns and covariates.
 *
 */

public class VariableBrowser extends MiraWidget {
  public int infoBarH = Display.scale(35);
  public int scrollSize = Display.scale(13);

  protected RowBrowser rowBrowser;
  protected ColumnLabels colLabels;
  protected InformationBar infoBar;
  protected SearchBar searchBar;
  protected CovariatesBar covBar;
  
  protected Variable rowAxis, colAxis;
  protected Variable selRow, selCol;
  protected Variable transRow, transCol;

  protected VerticalScrollbar vscroll;
  protected HorizontalScrollbar hscroll;
  protected HorizontalScrollbar gscroll;

  protected boolean sort0;

  protected boolean drawPlotEdges = false;

  protected TaskHandler taskHandler;


  VariableBrowser(Interface intf, float x, float y, float w, float h) {
    super(intf, x, y, w, h);
    rowAxis = colAxis = null;
    selRow = selCol = null;
  }
  
  public void setup() {
    taskHandler = new TaskHandler();
       
    rowBrowser = new RowBrowser(intf, 0, mira.labelHeightClose + padding,
                                mira.varWidth, height - mira.labelHeightClose,
                                mira.plotHeight, mira.varHeight);
    rowBrowser.clipBounds(true, false, true, false);
    addChild(rowBrowser, TOP_LEFT_CORNER);

    infoBar = new InformationBar(intf, 0, 0, mira.varWidth, infoBarH);    
    addChild(infoBar, TOP_LEFT_CORNER);
    
    searchBar = new SearchBar(intf, 0, infoBarH, mira.varWidth, mira.labelHeightClose - infoBarH);    
    addChild(searchBar, TOP_LEFT_CORNER); 
    
    covBar = new CovariatesBar(intf, 0, -mira.covarHeightClose, width, mira.covarHeightClose,
                              mira.varWidth, mira.covarHeightClose, mira.covarHeightMax);
    covBar.clipBounds(true, false);
    addChild(covBar, BOTTOM_LEFT_CORNER);

    vscroll = new VerticalScrollbar(intf, createVHandler(), -scrollSize, mira.labelHeightClose + 2 * padding,
                                    scrollSize,height - mira.labelHeightClose - scrollSize - 2 * padding);
    addChild(vscroll, TOP_RIGHT_CORNER);

    hscroll = new HorizontalScrollbar(intf, createHHandler(), mira.varWidth + padding, -scrollSize,
                                     width - mira.varWidth - scrollSize - padding, scrollSize);
    addChild(hscroll, BOTTOM_LEFT_CORNER);

    if (1 < rowBrowser.getChildrenCount()) {
      gscroll = new HorizontalScrollbar(intf, createGHandler(), 0, -scrollSize, mira.varWidth - padding, scrollSize);
      addChild(gscroll, BOTTOM_LEFT_CORNER);
    } else {
      gscroll = null;
    }

    rowBrowser.setScrollbars(vscroll, hscroll, gscroll);

    colLabels = new ColumnLabels(intf, mira.varWidth, 0, width - mira.varWidth, mira.labelHeightClose,
            mira.plotWidth, mira.labelHeightClose, mira.labelHeightMax);
    colLabels.clipBounds(true, false);
    addChild(colLabels, TOP_LEFT_CORNER);
    colLabels.hide(false);
    if (data.getGroupCount() == 1 && data.getTableCount() == 1) {
      // No metadata, so the variables are already shown and the labels should
      // be visible as well.
      showColumnLabels();
    }

    // Defining a keymap in the interface so the row scroller will capture the
    // arrow keys irrespective of which widget is currently selected, and likewise
    // with the search bar, which will capture any alphanumeric character. However
    // the mapping can be overridden by widgets that are set to capture keys when
    // they are selected.
    intf.addKeymap(rowBrowser, UP, DOWN, LEFT, RIGHT);
    intf.addKeymap(rowBrowser, ENTER, RETURN);
    intf.addKeymap(searchBar, Interface.ALL_CHARACTERS);
    intf.addKeymap(searchBar, BACKSPACE, DELETE, TAB, ESC);
  }  

  protected void handleResize(int newWidth, int newHeight) {
    bounds.w.set(newWidth - mira.optWidth);
    bounds.h.set(newHeight);
  }
  
  public void update() {
    boolean sort = data.sorting();
    if (!sort && sort0) updateAfterSort();
    sort0 = sort;

    if (transCol != null && transRow != null) {
      openColumn(transCol);
      openRow(transRow);
      transCol = null;
      transRow = null;
    }
  }
  
  public void openRow(Variable var) {
    rowBrowser.openRow(var);
  }
  
  public void closeRowsBut(MiraWidget wt) {
    rowBrowser.closeRowsBut(wt);
  }

  public void openColumn(Variable var) {
    rowBrowser.showVariables();
    int idx = -1;
    if (var.column()) {
      idx = data.getColumn(var);      
    } else {
      data.addColumn(var);
      long t0 = mira.millis();
      // White for a bit to the sort to place the new column in the right spot,
      // in the case the data is sorted.
      while (data.sorting() && mira.millis() - t0 < 1500) {
        Thread.yield();        
      }
      if (!data.sorting()) idx = data.getColumn(var); 
    }
    if (-1 < idx) {
      colLabels.jumpTo(idx);
      hscroll.scrollTo(idx);
    }
    mira.profile.add(var);
  }

  public void openColumn(int idx) {
    if (rowBrowser.showingVariables()) {
      colLabels.jumpTo(idx);
    }
  }
  
  public void openColumns(VariableContainer container) {
    rowBrowser.showVariables();
    ArrayList<Variable> vars = data.getVariables(container);
    data.addColumns(vars);
    mira.profile.add(vars);
  }

  public void openAllColumns() {
    rowBrowser.showVariables();
    ArrayList<Variable> all = data.getTree().getVariables();
    data.addColumns(all);
    mira.profile.add(all);
  }

  public void closeColumn(Variable var) {
    data.removeColumn(var); // Important: removing column from data must happen before updating the UI    
    colLabels.close(var);
    rowBrowser.closeColumn(var);
    mira.profile.remove(var);
  }

  public void closeColumnsBut(Variable var) {
    if (colLabels.isUpdating()) return;
    // Handle situation when columns are sorted by correlation...
    
    ArrayList<Variable> all = data.getVariables();
    data.removeColumns(all, var); // Important: removing column from data must happen before updating the UI
    for (Variable v1: all) {
      if (v1 == var) continue;
      colLabels.close(v1);
      rowBrowser.closeColumn(v1);
    } 
    mira.profile.remove(all, var);    
  }  
  
  public void closeColumns(VariableContainer container) {
    ArrayList<Variable> vars = data.getVariables(container);
    data.removeColumns(vars); // Important: removing column from data must happen before updating the UI
    for (Variable var: vars) {
      colLabels.close(var);
      rowBrowser.closeColumn(var);
    } 
    mira.profile.remove(vars);
  }

  public void openCovariate(Variable var) {
    int idx = -1;
    if (var.covariate()) {
      idx = data.getCovariate(var);
    } else {
      idx = data.addCovariate(var);
      covBar.updateItems();  
    }
    if (-1 < idx) covBar.jumpTo(idx);
  }
  
  public void closeCovariate(Variable var) {
    data.removeCovariate(var); // Important: removing covariate from data must happen before updating the UI
    covBar.close(var);    
  }
  
  public int getFirstColumn() {
    return (int)(colLabels.visX0.getTarget() / mira.plotWidth);
  }

  public int getLastColumn() {
    return (int)(colLabels.visX1.getTarget() / mira.plotWidth);
  } 
  
  public void dragColumns(float dx) {
    if (rowsReady()) {
      for (Widget child: children) {
        if (child instanceof RowBrowser) {
          ((RowBrowser)child).dragColumns(dx);
        } else if (child instanceof ColumnLabels) {
          ((ColumnLabels)child).drag(dx);
        }
      }      
    }    
  }
  
  public void snapColumns() {
    if (rowsReady()) {
      for (Widget child: children) {
        if (child instanceof RowBrowser) {
          ((RowBrowser)child).snapColumns();
        } else if (child instanceof ColumnLabels) {
          ((ColumnLabels)child).snap();
        }
      }      
    }
  }  
  
  public void dataChanged() {
    for (Widget child: children) {
      if (child instanceof RowBrowser) {
        ((RowBrowser)child).dataChanged();
      }
    }
    infoBar.dataChanged();
  }
  
  public void pvalueChanged() {
    for (Widget child: children) {
      if (child instanceof RowBrowser) {
        ((RowBrowser)child).pvalueChanged();
      }
    }
  }
  
  public void showColumnLabels() {
    colLabels.show();
  }
  
  public void hideColumnLabels() {
    colLabels.hide(false);
  }
  
  public void resetSelectors() {
    Set<Variable> variables = mira.ranges.keySet();
    for (Variable var: variables) {
      if (var.numerical()) {
        intf.invoke(NumericalRangeSelector.class, "resetValues", var);  
      } else if (var.categorical()) {
        intf.invoke(CategoricalRangeSelector.class, "resetValues", var);  
      }
    }     
  }
  
  public void setRowAxis(Variable var) {
    rowAxis = var;
  }

  public void setColAxis(Variable var) {
    colAxis = var;
  } 
  
  public Variable getRowAxis() {
    return rowAxis;
  }

  public Variable getColAxis() {
    return colAxis;
  } 
  
  public String getRowLabel() {
    if (rowAxis != null && colAxis != null && colLabels.contains(colAxis)) {
      return rowBrowser.getRowLabel(colAxis, rowAxis);
    }
    return "";
  }

  public String getColLabel() {
    if (rowAxis != null && colAxis != null && colLabels.contains(colAxis)) {
      return rowBrowser.getColLabel(colAxis, rowAxis);
    }
    return "";
  }

  public void requestTranspose(Variable row, Variable col) {
    transRow = col;
    transCol = row;
  }

  public void setSelectedPair(Variable col, Variable row) {
    selCol = col;
    selRow = row;
    mira.history.setSelectedPair(col, row);
  }


  public boolean drawPlotEdges() {
    return drawPlotEdges;
  }

  public void switchPlotEdges() {
    drawPlotEdges = !drawPlotEdges;
  }

  public void saveSelectedPlot() {
    rowBrowser.saveSelectedPlot();
  }

//  public void setSelectedRow(Variable var) {
//    selRow = var;
//  }
//
//  public void setSelectedCol(Variable var) {
//    selCol = var;
//  } 
  
  public Variable getSelectedRow() {
    return selRow;
  }

  public Variable getSelectedCol() {
    return selCol;
  } 
  
  public void saveSelectedPair() {
    
  }

  protected void updateAfterSort() {
    // Any required updates when sorting has concluded.
    if (selCol != null) {
      openColumn(selCol);
    }
  }

  protected boolean rowsReady() {
    boolean ready = true;
    for (Widget child: children) {
      if (child instanceof RowBrowser) {
        if (!((RowBrowser)child).plotsReady()) {
          ready = false;
          break;          
        }
      } else if (child instanceof ColumnLabels) {
        if (!((ColumnLabels)child).ready()) {
          ready = false;
          break;
        }        
      }
    }      
    return ready;
  }

  protected ScrollbarHandler createHHandler() {
    ScrollbarHandler handler = new ScrollbarHandler() {
      public float drag(float newp, float maxd) {
        float x1 = PApplet.constrain(newp, 0, maxd);
        int tot = colLabels.getTotItemsCount() - 1;
        int idx = PApplet.round(PApplet.map(x1, 0, maxd, 0, tot));
        mira.browser.openColumn(idx);
        return x1;
      }
      public void stopDrag() {
        mira.browser.snapColumns();
      }
      public float press(float pos, float maxd) {
        float x1 = PApplet.constrain(pos, 0, maxd);
        int tot = colLabels.getTotItemsCount() - 1;
        int idx = PApplet.round(PApplet.map(x1, 0, maxd, 0, tot));
        mira.browser.openColumn(idx);
        return x1;
      }
      public int pressSlider(float pos, float size) {
        int idx = colLabels.getFirstItemIndex();
        if (pos + 0.5 * size < pos && idx < colLabels.getTotItemsCount() - 1) {
          // one step to the right
          idx++;
          mira.browser.openColumn(idx);
        } else if (0 < idx) {
          // one step to the left
          idx--;
          mira.browser.openColumn(idx);
        }
        return idx;
      }
      public int currentItem() {
        return colLabels.getFirstItemIndex();
      }
      public float itemPosition(int idx, float maxd) {
        int tot = colLabels.getTotItemsCount() - 1;
        float x1 = PApplet.map(idx, 0, tot, 0, maxd);
        return x1;
      }
      public float resize(float news) {
        float w1 = news - mira.optWidth - mira.varWidth - mira.browser.scrollSize - padding;
        return w1;
      }
      public float totalSize() {
        return colLabels.getTotalWidth();
      }
      public float initPosition(float maxd) {
        int tot = colLabels.getTotItemsCount() - 1;
        int idx = colLabels.getFirstItemIndex();
        float x0 = PApplet.map(idx, 0, tot, 0, maxd);
        return x0;
      }
    };
    return handler;
  }

  protected ScrollbarHandler createVHandler() {
    ScrollbarHandler handler = new ScrollbarHandler() {
      public float drag(float newp, float maxd) {
        float y1 = PApplet.constrain(newp, 0, maxd);
        int tot = rowBrowser.getTotItemsCount() - 1;
        int n = PApplet.round(PApplet.map(y1, 0, maxd, 0, tot));
        RowScroller scroller = rowBrowser.getScroller();
        scroller.jumpTo(n);
        return y1;
      }
      public void stopDrag() {
        RowScroller scroller = rowBrowser.getScroller();
        scroller.snap();
      }
      public float press(float pos, float maxd) {
        float y1 = PApplet.constrain(pos, 0, maxd);
        int tot = rowBrowser.getTotItemsCount() - 1;
        int n = PApplet.round(PApplet.map(y1, 0, maxd, 0, tot - 1));
        RowScroller scroller = rowBrowser.getScroller();
        scroller.jumpTo(n);
        return y1;
      }
      public int pressSlider(float pos, float size) {
        RowScroller scroller = rowBrowser.getScroller();
        int idx = scroller.getFirstIndex();
        if (pos + 0.5 * size < pos && idx < rowBrowser.getTotItemsCount() - 1) {
          // one step down
          scroller.down();
        } else if (0 < idx) {
          // one step up
          scroller.up();
        }
        return idx;
      }
      public int currentItem() {
        return rowBrowser.getFirstItemIndex();
      }
      public float itemPosition(int idx, float maxd) {
        int tot = rowBrowser.getTotItemsCount() - 1;
        float y1 = PApplet.map(idx, 0, tot, 0, maxd);
        return y1;
      }
      public float resize(float news) {
        float sh = rowBrowser.showingVariables() ? mira.browser.scrollSize : 0;
        float gh = gscroll == null ? 0 : mira.browser.scrollSize;
        float h1 = news - mira.labelHeightClose - 2 * padding - sh - gh;
        return h1;
      }
      public float totalSize() {
        RowScroller scroller = rowBrowser.getScroller();
        return scroller.getApproxTotalHeight();
      }
      public float initPosition(float maxd) {
        int tot = rowBrowser.getTotItemsCount() - 1;
        int idx = rowBrowser.getFirstItemIndex();
        float y0 = PApplet.map(idx, 0, tot, 0, maxd);
        return y0;
      }
    };
    return handler;
  }

  protected ScrollbarHandler createGHandler() {
    ScrollbarHandler handler = new ScrollbarHandler() {
      public float drag(float newp, float maxd) {
        float x1 = PApplet.constrain(newp, 0, maxd);
        int tot = rowBrowser.getChildrenCount() - 1;
        int idx = PApplet.round(PApplet.map(x1, 0, maxd, 0, tot));
        goTo(idx);
        return x1;
      }
      public void stopDrag() {
//        mira.browser.snapColumns();
      }
      public float press(float pos, float maxd) {
        float x1 = PApplet.constrain(pos, 0, maxd);
        int tot = rowBrowser.getChildrenCount() - 1;
        int idx = PApplet.round(PApplet.map(x1, 0, maxd, 0, tot));
        goTo(idx);
        return x1;
      }
      public int pressSlider(float pos, float size) {
        int idx = rowBrowser.current;
        if (pos + 0.5 * size < pos && idx < children.size() - 1) {
          // one step to the right
          idx++;
          rowBrowser.next();
        } else if (0 < idx) {
          // one step to the left
          idx--;
          rowBrowser.prev();
        }
        return idx;
      }
      public int currentItem() {
        return rowBrowser.current;
      }
      public float itemPosition(int idx, float maxd) {
        int tot = rowBrowser.getChildrenCount() - 1;
        float x1 = PApplet.map(idx, 0, tot, 0, maxd);
        return x1;
      }
      public float resize(float news) {
        return mira.varWidth - padding;
      }
      public float totalSize() {
        return children.size();
      }
      public float initPosition(float maxd) {
        int tot = rowBrowser.getChildrenCount() - 1;
        int idx = rowBrowser.current;
        float x0 = PApplet.map(idx, 0, tot, 0, maxd);
        return x0;
      }
      private void goTo(int idx) {
        int dif = idx - rowBrowser.current;
        for (int i = 0; i < PApplet.abs(dif); i++) {
          if (dif < 0) rowBrowser.prev(false);
          else rowBrowser.next(false);
        }
      }
    };
    return handler;
  }
}

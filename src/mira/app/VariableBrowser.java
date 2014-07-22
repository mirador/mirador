/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mira.app;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

import lib.ui.Interface;
import lib.ui.Widget;
import mira.data.Variable;
import mira.data.VariableContainer;
import processing.core.PApplet;

/**
 * The main visualization area containing the row, columns and covariates.
 *
 */

public class VariableBrowser extends MiraWidget {
  final static public int NUM_FREE_PROCESSORS = 1;
  
  protected ThreadPoolExecutor taskPool1;
  protected ThreadPoolExecutor taskPool2;
  protected RowScroller rowScroller;
  protected ColumnLabels colLabels;
  protected InformationBar infoBar;
  protected SearchBar searchBar;
  protected CovariatesBar covBar;
  
  protected Variable rowAxis, colAxis;
  
  VariableBrowser(Interface intf, float x, float y, float w, float h) {
    super(intf, x, y, w, h);
    rowAxis = colAxis = null;
  }
  
  public void setup() {
    int proc = Runtime.getRuntime().availableProcessors();
    int tot = proc - NUM_FREE_PROCESSORS;
    taskPool1 = (ThreadPoolExecutor)Executors.newFixedThreadPool(PApplet.max(1, (int)(0.7f * tot)));
    taskPool2 = (ThreadPoolExecutor)Executors.newFixedThreadPool(PApplet.max(1, (int)(0.3f * tot)));
       
    rowScroller = new RowScroller(intf, 0, mira.labelHeightClose + 2, mira.varWidth, height - mira.labelHeightClose, 
                                  mira.plotHeight, mira.varHeight);
    rowScroller.clipBounds(true, false, true, false);
    addChild(rowScroller, TOP_LEFT_CORNER);
    
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
    
    infoBar = new InformationBar(intf, 0, 0, mira.varWidth, 35);    
    addChild(infoBar, TOP_LEFT_CORNER);
    
    searchBar = new SearchBar(intf, 0, 35, mira.varWidth, mira.labelHeightClose - 35);    
    addChild(searchBar, TOP_LEFT_CORNER); 
    
    covBar = new CovariatesBar(intf, 0, -mira.covarHeightClose, width, mira.covarHeightClose,
                              mira.varWidth, mira.covarHeightClose, mira.covarHeightMax);
    covBar.clipBounds(true, false);
    addChild(covBar, BOTTOM_LEFT_CORNER);
        
    // Defining a keymap in the interface so the row scroller will capture the
    // arrow keys irrespective of which widget is currently selected, and likewise
    // with the search bar, which will capture any alphanumeric character. However
    // the mapping can be overridden by widgets that are set to capture keys when
    // they are selected.
    intf.addKeymap(rowScroller, UP, DOWN, LEFT, RIGHT);    
    intf.addKeymap(searchBar, Interface.ALL_CHARACTERS);
    intf.addKeymap(searchBar, BACKSPACE, DELETE, TAB, ENTER, RETURN, ESC);
  }  
  
  @SuppressWarnings("unchecked")
  public FutureTask<Object> submitTask(Runnable task, boolean highp) {
    if (highp) {
      return (FutureTask<Object>)taskPool1.submit(task);
    } else {
      return (FutureTask<Object>)taskPool2.submit(task);
    }
  }
  
  protected void handleResize(int newWidth, int newHeight) {
    bounds.w.set(newWidth - mira.optWidth);
    bounds.h.set(newHeight);
  }
  
  public void update() {
    if (Interface.SHOW_DEBUG_INFO && mira.frameCount % 180 == 0) {
      long count1 = taskPool1.getTaskCount() - taskPool1.getCompletedTaskCount();
      long count2 = taskPool2.getTaskCount() - taskPool2.getCompletedTaskCount();
      System.out.println("number of pending high-priority tasks: " + count1);
      System.out.println("number of pending low-priority tasks : " + count2);
    }
  }
  
  public void openRow(Variable var) {
    rowScroller.openRow(var);  
  }

  public void openColumn(Variable var) {
    rowScroller.showVariables();
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
    if (-1 < idx) colLabels.jumpTo(idx);
    mira.profile.add(var);
  }
  
  public void openColumns(VariableContainer container) {
    ArrayList<Variable> vars = data.getVariables(container);
    data.addColumns(vars);
    mira.profile.add(vars);
  }    
  
  public void closeColumn(Variable var) {
    data.removeColumn(var); // Important: removing column from data must happen before updating the UI    
    colLabels.close(var);
    rowScroller.closeColumn(var);
    mira.profile.remove(var);
  }
  
  public void closeColumns(VariableContainer container) {
    ArrayList<Variable> vars = data.getVariables(container);
    data.removeColumns(vars); // Important: removing column from data must happen before updating the UI
    for (Variable var: vars) {
      colLabels.close(var);
      rowScroller.closeColumn(var);
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
  
  public void dragColumns(float dx) {
    if (rowsReady()) {
      for (Widget child: children) {
        if (child instanceof RowScroller) {
          ((RowScroller)child).dragColumns(dx);
        } else if (child instanceof ColumnLabels) {
          ((ColumnLabels)child).drag(dx);
        }
      }      
    }    
  }
  
  public void snapColumns() {
    if (rowsReady()) {
      for (Widget child: children) {
        if (child instanceof RowScroller) {
          ((RowScroller)child).snapColumns();
        } else if (child instanceof ColumnLabels) {
          ((ColumnLabels)child).snap();
        }
      }      
    }
  }  
  
  public void dataChanged() {
    for (Widget child: children) {
      if (child instanceof RowScroller) {
        ((RowScroller)child).dataChanged();
      }
    }
    infoBar.dataChanged();
  }
  
  public void pvalueChanged() {
    for (Widget child: children) {
      if (child instanceof RowScroller) {
        ((RowScroller)child).pvalueChanged();
      }
    }
  }
  
  public void showColumnLabels() {
    colLabels.show();
  }
  
  public void hideColumnLabels() {
    colLabels.hide();
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
      return rowScroller.getRowLabel(colAxis, rowAxis);  
    }
    return "";
  }

  public String getColLabel() {
    if (rowAxis != null && colAxis != null && colLabels.contains(colAxis)) {
      return rowScroller.getColLabel(colAxis, rowAxis);  
    }
    return "";
  }
  
  protected boolean rowsReady() {
    boolean ready = true;
    for (Widget child: children) {
      if (child instanceof RowScroller) {
        if (!((RowScroller)child).plotsReady()) {
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
}

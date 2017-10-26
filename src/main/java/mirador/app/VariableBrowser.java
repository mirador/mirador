/* COPYRIGHT (C) 2014-16 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

import mui.Display;
import mui.Interface;
import mui.Widget;
import miralib.data.Variable;
import miralib.data.VariableContainer;
import processing.core.PApplet;

/**
 * The main visualization area containing the row, columns and covariates.
 *
 */

public class VariableBrowser extends MiraWidget {
  int infoBarH = Display.scale(35);
  
  final static public int NUM_FREE_PROCESSORS = 1;
  
  protected ThreadPoolExecutor taskPool1;
  protected ThreadPoolExecutor taskPool2;
  protected RowBrowser rowBrowser;
  protected ColumnLabels colLabels;
  protected InformationBar infoBar;
  protected SearchBar searchBar;
  protected CovariatesBar covBar;
  
  protected Variable rowAxis, colAxis;
  protected Variable selRow, selCol;

  protected VerticalScrollbar vscroll;
  
  VariableBrowser(Interface intf, float x, float y, float w, float h) {
    super(intf, x, y, w, h);
    rowAxis = colAxis = null;
    selRow = selCol = null;
  }
  
  public void setup() {
    int proc = Runtime.getRuntime().availableProcessors();
    int tot = proc - NUM_FREE_PROCESSORS;
    taskPool1 = (ThreadPoolExecutor)Executors.newFixedThreadPool(PApplet.max(1, (int)(0.7f * tot)));
    taskPool2 = (ThreadPoolExecutor)Executors.newFixedThreadPool(PApplet.max(1, (int)(0.3f * tot)));
       
    rowBrowser = new RowBrowser(intf, 0, mira.labelHeightClose + 2, mira.varWidth, height - mira.labelHeightClose,
                                  mira.plotHeight, mira.varHeight);
    rowBrowser.clipBounds(true, false, true, false);
    addChild(rowBrowser, TOP_LEFT_CORNER);
    
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
    
    infoBar = new InformationBar(intf, 0, 0, mira.varWidth, infoBarH);    
    addChild(infoBar, TOP_LEFT_CORNER);
    
    searchBar = new SearchBar(intf, 0, infoBarH, mira.varWidth, mira.labelHeightClose - infoBarH);    
    addChild(searchBar, TOP_LEFT_CORNER); 
    
    covBar = new CovariatesBar(intf, 0, -mira.covarHeightClose, width, mira.covarHeightClose,
                              mira.varWidth, mira.covarHeightClose, mira.covarHeightMax);
    covBar.clipBounds(true, false);
    addChild(covBar, BOTTOM_LEFT_CORNER);


    vscroll = new VerticalScrollbar(intf, rowBrowser,-50, mira.labelHeightClose + 2, 50, height - mira.labelHeightClose);
    addChild(vscroll, TOP_RIGHT_CORNER);
        
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

  public void setSelectedPair(Variable col, Variable row) {
    selCol = col;
    selRow = row;
    mira.history.setSelectedPair(col, row);
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
}

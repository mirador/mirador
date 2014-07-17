/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mira.app;

import processing.core.PFont;
import lib.ui.Interface;
import mira.data.DataTree;
import mira.data.VariableContainer;

/**
 * Widget that represents a variable table.
 *
 * @author Andres Colubri
 */

public class RowTable extends RowWidget {
  protected VariableContainer rowTable;
  protected PFont hFont;
  protected int hColor;
  protected float hLead;
  protected int bColor;  

  public RowTable(Interface intf, float x, float y, float w, float h, 
                  VariableContainer rtab) {
    super(intf, x, y, w, h);
    rowTable = rtab;
  }

  public void setup() {
    bColor = getStyleColor("TypeDir", "background-color");
    hFont = getStyleFont("TypeDir.h1", "font-family", "font-size");
    hColor = getStyleColor("TypeDir.h1", "color");
    hLead = getStyleSize("TypeDir.h1", "line-height");
    
    CheckBox chkbx = new CheckBox(10, 2 * padding + 5) {
      void updateState() {
        state = rowTable.getColumnSelection();
      }
      void handlePress() {
        int sel = rowTable.getColumnSelection();
        if (sel == DataTree.NONE) {        
          mira.browser.openColumns(rowTable);
        } else {
          mira.browser.closeColumns(rowTable);
        }        
      }
    };
    chkbx.setLabel("Column");
    addCheckBox(chkbx);    
  }  
  
  public void draw() {
    noStroke();
    fill(bColor);
    rect(0, padding, width - padding, height - 2 * padding);    
    
    fill(hColor);
    textFont(hFont);
    textLeading(hLead);
    text(rowTable.getName(), 10, 10, width - 20, 40);
    drawCheckBoxes();
  }  
}

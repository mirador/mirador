/* COPYRIGHT (C) 2014-16 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import processing.core.PFont;
import mui.Display;
import mui.Interface;
import miralib.data.DataTree;
import miralib.data.VariableContainer;

/**
 * Widget that represents a variable table.
 *
 */

public class RowTable extends RowWidget {
  // TODO: Make into CCS size parameters
  float chkOff = Display.scale(5);
  float chkBoxX = Display.scale(10);
  float textX = Display.scale(10);
  float textY = Display.scale(10);
  float textH = Display.scale(40);  
  
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
    
    CheckBox chkbx = new CheckBox(chkBoxX, 2 * padding + chkOff) {
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
    text(rowTable.getName(), textX, textY, width - textX*2, textH); 
    drawCheckBoxes();
  }  
}

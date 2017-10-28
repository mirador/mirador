/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import processing.core.PFont;
import mui.Display;
import mui.Interface;
import miralib.data.DataTree;
import miralib.data.VariableContainer;

/**
 * Widget that represents a variable group.
 *
 */

public class RowGroup extends RowWidget {
  // TODO: Make into CCS size parameters
  float chkOff = Display.scale(5);
  float chkBoxX = Display.scale(10);
  float textX = Display.scale(10);
  float textY = Display.scale(10);
  float textH = Display.scale(40);
  
  protected VariableContainer rowGroup;  
  protected PFont hFont;
  protected float hLead;
  protected int hColor;
  protected int bColor;
  
  public RowGroup(Interface intf, float x, float y, float w, float h, 
                  VariableContainer rgrp) {
    super(intf, x, y, w, h);
    rowGroup = rgrp;
  }
  
  public void setup() {
    bColor = getStyleColor("SubjDir", "background-color");
    hFont = getStyleFont("SubjDir.h1", "font-family", "font-size");
    hColor = getStyleColor("SubjDir.h1", "color");
    hLead = getStyleSize("SubjDir.h1", "line-height");
    
    CheckBox chkbx = new CheckBox(chkBoxX, 2 * padding + chkOff) {
      void updateState() {
        state = rowGroup.getColumnSelection();
      }
      void handlePress() {
        int sel = rowGroup.getColumnSelection();
        if (sel == DataTree.NONE) {        
          mira.browser.openColumns(rowGroup);
        } else {
          mira.browser.closeColumns(rowGroup);
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
    text(rowGroup.getName(), textX, textY, width - textX*3, textH);
    drawCheckBoxes();
  }    
}

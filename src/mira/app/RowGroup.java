/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mira.app;

import processing.core.PFont;
import lib.ui.Interface;
import mira.data.DataTree;
import mira.data.VariableContainer;

/**
 * Widget that represents a variable group.
 *
 */

public class RowGroup extends RowWidget {
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
    
    CheckBox chkbx = new CheckBox(10, 2 * padding + 5) {
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
    text(rowGroup.getName(), 10, 10, width - 20, 40);    
    drawCheckBoxes();
  }    
}

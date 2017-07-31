/* COPYRIGHT (C) 2014-16 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import java.util.ArrayList;

import processing.core.PFont;
import mui.Display;
import mui.Interface;
import mui.SoftFloat;

/**
 * Base row widget used to define RowGroup, RowTable and RowVariable.
 *
 */

public class RowWidget extends MiraWidget {
  float labelPad = Display.scale(1);
  
  protected ArrayList<CheckBox> chkBoxes;
  protected boolean chkPressed;
  
  public RowWidget(Interface intf, float x, float y, float w, float h) {
    super(intf, x, y, w, h);
    chkBoxes = new ArrayList<CheckBox>();
    chkPressed = false;
  }
  
  public void update() {
    updateCheckBoxes();
  }
  
  public void mousePressed() {
    pressCheckBoxes();
  }
  
  public void mouseReleased() {
    if (!chkPressed) {
      ((MiraWidget)parent).mouseReleased(this);
    }
  }    

  public void keyPressed() {
    ((MiraWidget)parent).keyPressed(this);
  }  
  
  public void hoverIn() {
    for (CheckBox chkbx: chkBoxes) chkbx.show();      
  }
  
  public void hoverOut() {
    for (CheckBox chkbx: chkBoxes) chkbx.hide();
  }
  
  protected void addCheckBox(CheckBox chkbx) {
    chkBoxes.add(chkbx);
  }

  protected void updateCheckBoxes() {
    for (CheckBox chkbx: chkBoxes) chkbx.update();
  }
  
  protected void drawCheckBoxes() {
    for (CheckBox chkbx: chkBoxes) chkbx.draw();
  }
  
  protected void pressCheckBoxes() {
    chkPressed = false;
    for (CheckBox chkbx: chkBoxes) {
      if (chkbx.press(mouseX, mouseY)) {
        chkbx.handlePress();
        chkPressed = true;
      }
    }
  }
  
  protected class CheckBox {
    final static public int DESELECTED = 0;
    final static public int PARTIALLY_SELECTED = 1;
    final static public int FULLY_SELECTED = 2;
    
    float x, yoff, w, h;
    int state;
    String label;
    int slColor, smColor, nnColor;
    float brWeight;  
    PFont pFont;
    int pColor;
    SoftFloat maxAlpha;
    
    public CheckBox(float x, float yoff) {
      this.x = x;
      this.yoff = yoff;
      state = DESELECTED;
      label = "";
      maxAlpha = new SoftFloat(0);
      
      w = getStyleSize("DirOptions.CheckBoxList.Check", "width");
      h = getStyleSize("DirOptions.CheckBoxList.Check", "height");
      
      slColor = getStyleColor("DirOptions.CheckBoxList.Check", "all-selected-color");
      smColor = getStyleColor("DirOptions.CheckBoxList.Check", "some-selected-color");
      nnColor = getStyleColor("DirOptions.CheckBoxList.Check", "none-selected-color");
      brWeight = getStyleSize("DirOptions.CheckBoxList.Check", "border-width");
      
      pFont = getStyleFont("DirOptions.CheckBoxList.p", "font-family", "font-size");
      pColor = getStyleColor("DirOptions.CheckBoxList.p", "color");            
    }
   
    void setState(int state) {
      this.state = state; 
    }
    
    void setLabel(String label) {
      this.label = label;
    }
    
    void update() {
      updateState();
      maxAlpha.update();
    }
    
    void updateState() {}
    
    void handlePress() { }
    
    void draw() {
      int alpha = maxAlpha.getCeil();
      if (alpha < 1) return;
      
      float y = height - h - yoff;
      if (state == FULLY_SELECTED) {
        noStroke();
        fill(slColor, alpha);
      } else if (state == PARTIALLY_SELECTED) {
        noStroke();
        fill(smColor, alpha);
      } else if (state == DESELECTED) {
        stroke(nnColor, alpha);
        noFill();        
      }
      rect(x, y, w, h);      

      fill(pColor, alpha);
      textFont(pFont);
      float yc = (h - pFont.getSize()) / 2; 
      text(label, x + w + 5, y + h - yc - labelPad);
    }
    
    void show() {
      maxAlpha.setTarget(255);
    }
    
    void hide() {
      maxAlpha.set(0);
    }    
    
    boolean press(float mx, float my) {
      float y = height - h - yoff;
      return x <= mx && mx <= x + w && y <= my && my <= y + h;      
    }
  }
}  
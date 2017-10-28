/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import processing.core.PApplet;
import processing.core.PFont;
import mui.Display;
import mui.Interface;
import mui.SoftFloat;
import mui.Widget;
import miralib.data.Variable;

/**
 * Widget that represents a row variable, and contains all its plots.
 *
 */

public class RowVariable extends RowWidget {
  int marginx = Display.scale(10);
  int marginy = Display.scale(10);
  int posy = Display.scale(70);
  int numh = Display.scale(50);
  float chkOff = Display.scale(5);
  float chkBoxColX = Display.scale(10);
  float chkBoxCovX = Display.scale(100);
  float labelH = Display.scale(40);
  int axisLabelX = Display.scale(7);
  int chkBoxYOff = Display.scale(33);
  
  float hoverInW = Display.scale(100);
  float hoverOutH = Display.scale(70);
  float titleX = Display.scale(10);
  float titleY = Display.scale(10);
  
  int profBtnX = Display.scale(80);
  int profBtnY = Display.scale(90);
  int profBtnW = Display.scale(115);
  int profBtnH = Display.scale(60);
  int sortOptH = Display.scale(5);
  
  final static public int UNSORTED = 0;
  final static public int SORTING  = 1;
  final static public int SORTED   = 2;

  final static public int UNSORTED_TITLE = 0;
  final static public int SORT_ACTION    = 1;
  final static public int SORTING_STATUS = 2;
  final static public int CANCEL_ACTION  = 3;  
  final static public int SORTED_TITLE   = 4;
  final static public int UNSORT_ACTION  = 5;
  
  protected Variable rowVar;
  protected RowPlots plots;
  protected RangeSelector selector;  
  protected boolean open;
  protected PFont hFont;
  protected int hColor;
  protected float hLead;
  protected int bColor;
  protected PFont pFont;
  protected int pColor;
  
  protected int labelMode;
  protected int sortMode;
  protected SortOptions sortOpt;
  
  protected boolean axisMode;
  protected SoftFloat axisAlpha;
  
  public RowVariable(Interface intf, float x, float y, float w, float h, 
                     Variable rvar) {
    super(intf, x, y, w, h);
    rowVar = rvar;
    
    plots = new RowPlots(intf, 0, 0, mira.width - mira.varWidth - mira.optWidth, mira.plotHeight,
                         mira.plotWidth, mira.plotHeight, mira.browser.colLabels);
    plots.setRowVar(rowVar);
    plots.clipBounds(true);
    addChild(plots, TOP_RIGHT_CORNER);
    
    if (rowVar.numerical()) {
      selector = new NumericalRangeSelector(intf, marginx, posy, width - marginx*2, numh, rowVar);
    } else if (rowVar.categorical()) {
      selector = new CategoricalRangeSelector(intf, marginx, posy, width - marginx*2, mira.plotHeight - posy - padding, rowVar);
    } 
    selector.setInner(true);
    addChild(selector, TOP_LEFT_CORNER);
    open = rowVar.open();
    if (open) {
      plots.show();
      selector.show();
    } else {
      plots.hide(false);
      selector.hide(false);
    }
        
    sortOpt = new SortOptions(intf, 0, 0, width, mira.plotHeight);
    if (rowVar.sortKey()) {
      sortOpt.show(true);
      if (data.sorting()) {
        sortMode = SORTING;
        labelMode = SORTING_STATUS;
      } else {
        sortMode = SORTED;
        labelMode = SORTED_TITLE;        
      }
    } else {
      sortOpt.hide(false);
      sortMode = UNSORTED;
      sortMode = UNSORTED_TITLE;
    }
    addChild(sortOpt, TOP_LEFT_CORNER);
        
    axisMode = false;
    axisAlpha = new SoftFloat(0);
  }
  
  public void setup() {
    bColor = getStyleColor("RowVarBox", "background-color");
    hFont = getStyleFont("RowVarBox.h2", "font-family", "font-size");
    hColor = getStyleColor("RowVarBox.h2", "color");
    hLead = getStyleSize("RowVarBox.h2", "line-height");    
    
    pFont = getStyleFont("p", "font-family", "font-size");
    pColor = color(255);
    
    selector.setBackgroundColor(bColor);
    
    CheckBox chkbxCol = new CheckBox(chkBoxColX, 2 * padding + chkOff) {
      void updateState() {
        state = rowVar.column() ? FULLY_SELECTED : DESELECTED;
      }
      void handlePress() {
        boolean col = rowVar.column();
        if (col) {        
          mira.browser.closeColumn(rowVar);
        } else {
          mira.browser.openColumn(rowVar);
        }        
      }
    };
    chkbxCol.setLabel("Column");
    addCheckBox(chkbxCol);  
    
    CheckBox cchkbxCov = new CheckBox(chkBoxCovX, 2 * padding + chkOff) {
      void updateState() {
        state = rowVar.covariate() ? FULLY_SELECTED : DESELECTED;
      }
      void handlePress() {
        boolean cov = rowVar.covariate();
        if (cov) {        
          mira.browser.closeCovariate(rowVar);
        } else {
          mira.browser.openCovariate(rowVar);
        }        
      }
    };
    cchkbxCov.setLabel("Covariate");
    addCheckBox(cchkbxCov);     
  }
  
  public void update() {
    super.update();
    
    if (mira.browser.getRowAxis() == rowVar && !axisMode) {
      axisMode = true;
      axisAlpha.setTarget(230);
    }
    if (mira.browser.getRowAxis() != rowVar && axisMode) {
      axisMode = false;
      axisAlpha.set(0);
    }
    axisAlpha.update();
    
    if (sortMode == UNSORTED && intf.isEnabled()) {
      updateLabel();
    }
  }
  
  public void draw() {    
    noStroke();
    fill(bColor);
    rect(0, padding, width - padding, height - 2 * padding);    
  }
  
  public void postDraw() { 
    drawCheckBoxes();
    
    String label;
    float w1 = width - padding;
    float h1 = height - 2 * padding;    
    if (axisMode) {
      noStroke();
      fill(color(187, 185, 179), axisAlpha.getCeil());
      rect(0, padding, w1, h1);
    }
    
    fill(hColor);
    textFont(hFont);  
    textLeading(hLead);
    label = rowVar.getName();
    String alias = rowVar.getAlias();
    if (!label.equals(alias)) label += ": " + alias;  
    if (labelMode == SORT_ACTION) {
      label = "Sort " + label;
    } else if (labelMode == SORTING_STATUS) {
      int perc = (int)(data.sortProgress() * 100);
      label = "Sorting " + label + ", " + perc + "% completed"; 
    } else if (labelMode == CANCEL_ACTION) {
      label = "Cancel sorting";
    } else if (labelMode == UNSORT_ACTION) {
      label = "Unsort " + label;
    }
    text(label, marginx, marginy, width - marginx*2, labelH);   
    
    if (axisMode) {
      fill(pColor);
      textFont(hFont);
      label = mira.browser.getRowLabel();
      float vw = textWidth(label);
      float my = mouseY;      
      float[] tby = textTopBottom(my);
      if (tby[0] < 0) my += -tby[0];
      if (h1 < tby[1]) my -= tby[1] - h1;
      text(label, width - vw - axisLabelX, my);      
    }
  }
  
  public void drawCheckBoxes() {
    if (open) {
      CheckBox chkbx = chkBoxes.get(0);
      int alpha = chkbx.maxAlpha.getCeil();
      if (alpha < 1) return;
      
      float y1 = height - chkBoxYOff - padding;  
      noStroke();
      fill(color(221, 220, 217), alpha);
      rect(0, y1, width - padding, chkBoxYOff);
      stroke(color(191, 190, 197), alpha);
      line(0, y1, width - 2 * padding, y1);
    }
    
    super.drawCheckBoxes();
  } 
  
  public void hoverIn() {
    if (open && rowVar.categorical()) {
      selector.setHeight(targetHeight() - hoverInW - padding);
    }    
    super.hoverIn();
  }
  
  public void hoverOut() {
    super.hoverOut();
    if (open && rowVar.categorical()) {
      selector.targetHeight(targetHeight() - hoverOutH - padding);
    }
    labelMode = UNSORTED_TITLE;
  } 
  
  public void mouseReleased() {
    boolean shift = keyPressed(SHIFT);
    if (labelMode == SORT_ACTION) {
      labelMode = SORTING_STATUS;
      sortMode = SORTING;
      for (CheckBox chkbx: chkBoxes) chkbx.hide();
      sortOpt.show();
      data.sort(rowVar, mira.ranges, mira.project.pvalue(), mira.project.missingThreshold());
      mira.profile.clear();
      mira.history.sort(rowVar);
    } else if (sortMode == UNSORTED && !chkPressed) {
      if (shift) {
        mira.browser.closeRowsBut(this);
      } else {
        ((MiraWidget)parent).mouseReleased(this);  
      }      
    }
  }    
  
  public void targetHeight(float h) {
    boolean open1 = rowVar.open(); 
    if (open1 && !open) {
      plots.show();
      selector.show();
    }
    if (!open1 && open) {
      plots.hide(false);
      selector.hide(false);
    }    
    open = open1;   
    
    bounds.h.setTarget(h);
  }   
  
  public void mouseMoved() {
    updateLabel();
  }

  protected void updateLabel() {
    if (open) {      
      if (insideTitle(mouseX, mouseY)) {
        labelMode = SORT_ACTION;
      } else {
        labelMode = UNSORTED_TITLE;
      }
    } else {
      labelMode = UNSORTED_TITLE;
    }    
  }
  
  protected boolean insideTitle(float mx, float my) {
    textFont(hFont);  
    textLeading(hLead);    
    String label = rowVar.getName();
    String alias = rowVar.getAlias();
    if (!label.equals(alias)) label += ": " + alias;    
    if (labelMode == SORT_ACTION) {
      label = "Sort " + label;
    } else if (labelMode == SORTING_STATUS) {
      // Using the cancel message to avoid flickering back and forth between
      // the sorting status and the cancel txt, when user keeps pointer on top
      // of label.
      label = "Cancel sorting"; 
    } else if (labelMode == CANCEL_ACTION) {
      label = "Cancel sorting";
    } else if (labelMode == UNSORT_ACTION) {
      label = "Unsort " + label;
    }

    float f = textWidth(label) / (width - titleX*2);
    int nlines = PApplet.ceil(f);
    float frac = f - (int)f;
    float lineh = textLeading();
    
    float h = (nlines - 1) * lineh;
    float x0 = titleX;
    float x1 = x0 + width - titleX*2;
    float y0 = titleY;
    float y1 = y0 + h;
    float x2 = x0 + frac * (width - titleX*2);
    float y2 = y1 + lineh;
    
    return (x0 <= mx && mx <= x1  && y0 <= my && my <= y1) ||
           (x0 <= mx && mx <= x2  && y1 <= my && my <= y2);
  }
  
  public void keyPressed() {
    ((MiraWidget)parent).keyPressed(this);
  }
  
  public void enterPressed() {
    plots.enterPressed();
  }
  
  public void dragRows(float dy) {
    ((MiraWidget)parent).dragRows(dy);
  }
  
  public void drag(float dx) {
    plots.drag(dx);
  }
  
  public void snap() {
    plots.snap();
  }
  
  public void close(Variable var) {
    plots.close(var);
  }
  
  public void dataChanged() {
    plots.dataChanged();
  }
  
  public void pvalueChanged() {
    plots.pvalueChanged();
  }  
  
  public boolean plotsReady() {
    return plots.ready();
  }
  
  public Variable getVariable() {
    return rowVar;
  }
  
  public String getRowLabel(Variable colVar) {
    return plots.getRowLabel(colVar);
  }
 
  public String getColLabel(Variable colVar) {
    return plots.getColLabel(colVar);
  } 
  
  public void show(boolean now) {
    if (now) {
      opacity.set(0);
      opacity.setTarget(1);
      for (Widget child: children) {
        // Make sure that the selector becomes visible when it is completely
        // inside this box, and the sortOptions cannot be hidden/shown from the
        // parent.
        if (child instanceof SortOptions) continue;
        child.show(false);
      }
    } else {
      super.show(false);
    }
  }
  
  public void hide(boolean target) {
    if (target) opacity.setTarget(0);
    else opacity.set(0);
    for (Widget child: children) {
      if (child instanceof SortOptions) continue;
      child.hide(target);
    }
  }   
  
  protected class SortOptions extends MiraWidget {
    protected ProfileButton profileBtn;
//    protected NetworkButton networkBtn;
    
    public SortOptions(Interface intf, float x, float y, float w, float h) {
      super(intf, x, y, w, h);
      
      profileBtn = new ProfileButton(intf, profBtnX, profBtnY, profBtnW, profBtnH);
      addChild(profileBtn);
//      networkBtn = new NetworkButton(intf, 145, 100, 115, 60);
//      addChild(networkBtn);
      
      draggable = false;
    }
    
    public void update() {
      if (sortMode != UNSORTED) {
        if (data.sorted()) {
          if (sortMode != SORTED) {
            sortMode = SORTED;
            labelMode = SORTED_TITLE;          
          }
        } else if (!rowVar.sortKey()) {
          if (sortMode == SORTED || sortMode == SORTING) {
            sortMode = UNSORTED;
            labelMode = UNSORTED_TITLE;
            hide(false);
          }
        } else if (sortMode != SORTING && data.sorting()) {
          sortMode = SORTING;
          labelMode = SORTING_STATUS;        
        }
        
        updateLabel();
      }
    }
    
    public void draw() {
      noStroke();
      fill(color(172, 210, 237), 240);
      rect(0, padding, width - padding, height - 2 * padding);
             
      float w = PApplet.map(data.sortProgress(), 0, 1, 0, width - padding);
      noStroke();
      fill(color(39, 141, 210));
      rect(0, padding, w, sortOptH); 
    }
    
    public void mouseMoved() {
      updateLabel();
    }
   
    public void mouseReleased() {
      if (labelMode == CANCEL_ACTION) {
        data.stopSorting();
        mira.profile.clear();
      } else if (labelMode == UNSORT_ACTION) {
        data.unsort();
        mira.profile.clear();
        mira.history.unsort();
      }          
    }
    
    public void hoverOut() {
      if (data.sorting()) {
        labelMode = SORTING_STATUS;
      } else {
        labelMode = SORTED_TITLE;  
      }      
    }
    
    protected void updateLabel() {
      if (insideTitle(mouseX, mouseY)) {
        if (sortMode == SORTING) {
          labelMode = CANCEL_ACTION;
        } else if (sortMode == SORTED) {
          labelMode = UNSORT_ACTION;
        }
      } else if (data.sorting()) {
        labelMode = SORTING_STATUS;
      } else {
        labelMode = SORTED_TITLE;  
      }      
    }
  }
  
  protected class ProfileButton extends Widget {
    float x0 = Display.scale(20);
    float y0 = Display.scale(10);
    float x1 = Display.scale(35);
    float y1 = Display.scale(35);      
    float x2 = Display.scale(50);
    float y2 = Display.scale(48);    
    float rad = Display.scale(10);
    float txtX = Display.scale(65);
    float txtY = Display.scale(30);
    
    SoftFloat hoverAlpha;
    
    public ProfileButton(Interface intf, float x, float y, float w, float h) {
      super(intf, x, y, w, h);
      hoverAlpha = new SoftFloat(100);
    }
    
    public void update() {
      hoverAlpha.update();
    }
    
    public void draw() {
      stroke(color(39, 141, 210), hoverAlpha.getCeil());
      strokeWeight(2);
      line(x0, y0, x1, y1);
      line(x1, y1, x2, y2);
      
      noStroke();
      fill(color(39, 141, 210), hoverAlpha.getCeil());
      ellipse(x0, y0, rad, rad);
      ellipse(x1, y1, rad, rad);
      ellipse(x2, y2, rad, rad);
      
      fill(color(255), hoverAlpha.getCeil());
      text("Profile", txtX, txtY);
    }  
    
    public void hoverIn() {
      hoverAlpha.set(0);
      hoverAlpha.setTarget(255);
    }    

    public void hoverOut() {
      hoverAlpha.set(100);
    }
    
    public void handle() {
      mira.profile.open();
    }
  }
  
//  protected class NetworkButton extends Widget {
//    SoftFloat hoverAlpha;
//    
//    public NetworkButton(Interface intf, float x, float y, float w, float h) {
//      super(intf, x, y, w, h);
//      hoverAlpha = new SoftFloat(100);
//    }
//    
//    public void update() {
//      hoverAlpha.update();
//    }    
//    
//    public void draw() {
//      float x0 = 25;
//      float y0 = 10;
//      float x1 = 50;
//      float y1 = 41;      
//      float x2 = 25;
//      float y2 = 48;
//      
//      stroke(color(39, 141, 210), hoverAlpha.getCeil());
//      strokeWeight(2);
//      line(x0, y0, x1, y1);
//      line(x1, y1, x2, y2);
//      line(x2, y2, x0, y0);
//      
//      noStroke();
//      fill(color(39, 141, 210), hoverAlpha.getCeil());
//      ellipse(x0, y0, 10, 10);
//      ellipse(x1, y1, 10, 10);
//      ellipse(x2, y2, 10, 10);
//      
//      fill(color(255), hoverAlpha.getCeil());
//      text("Network", 65, 30); 
//    }
//    
//    public void hoverIn() {
//      hoverAlpha.set(0);
//      hoverAlpha.setTarget(255);
//    }    
//
//    public void hoverOut() {
//      hoverAlpha.set(100);
//    }       
//    
//    public void handle() {
//      mira.network.open();
//    } 
//  }  
}

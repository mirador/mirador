/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import processing.core.PApplet;
import processing.core.PFont;
import mui.Display;
import mui.Interface;
import mui.SoftFloat;
import mui.Widget;
import miralib.data.Variable;
import miralib.math.Numbers;
import miralib.utils.Log;
import miralib.utils.Project;
import miralib.shannon.PValue;

/**
 * Widget containing the profile view in Mirador.
 *
 */

public class Profile extends MiraWidget {
  // TODO: Make into CCS size parameters
  protected float crossw = Display.scale(20);
  protected float handlew = Display.scale(30);
  protected float handleh = Display.scale(30);

  protected float rightm = Display.scale(100);
  protected float topm = Display.scale(200);
  protected float bottomm = Display.scale(100);
  protected float leftm = Display.scale(100);
  
  int expBtnX = Display.scale(150); 
  int expBtnY = Display.scale(75); 
  int expBtnW = Display.scale(150); 
  int expBtnH = Display.scale(50);  
  int progBarH = Display.scale(10);
  
  int corrLabelX = Display.scale(90);
  int highLabelH = Display.scale(50);
  int lowLabelH = Display.scale(40);
  int lowLabelY = Display.scale(40);
  int corrLabelW = Display.scale(80);    
  int labelY = Display.scale(100); 
  
  int handleY = Display.scale(20);
  int selW = Display.scale(20); 
  float fdrY = Display.scale(10);
  float pointRad = Display.scale(15);
  
  int ptLabelOffset = Display.scale(5);
  int dismissX = Display.scale(50);
  
  int exportSelX = Display.scale(40);
  int dragLimitX = Display.scale(30);
  
  int softPointX = Display.scale(30);
  int softPointY = Display.scale(10);  
  
  protected boolean dirty;
  protected boolean sort0;
  protected HashMap<Variable, SoftPoint> points;
  protected boolean requestedClear;
  protected boolean requestedUpdateSelection;
  protected HashSet<Variable> varsToAdd;
  protected HashSet<Variable> varsToRemove;
  
  protected PFont h1Font;
  protected int h1Color;
  protected PFont pFont;
  protected int pColor; 
  protected PFont ptFont;
  protected int ptColor;
  protected int ptInColor;
  protected int ptBrColor;
  protected int selBarColor;

  protected float selRangeLeft;
  protected float selRangeRight;
  protected SelectionHandle selRightHandle;
  
  protected float fdr;
  
  protected Variable hoverVar;
  protected SoftFloat hoverAlpha;
  
  protected ExportButton export;
  
  public Profile(Interface intf, float x, float y, float w, float h) {
    super(intf, x, y, w, h);
  }
  
  public void setup() { 
    points = new HashMap<Variable, SoftPoint>();
    for (int i = 0; i < data.getColumnCount(); i++) {
      Variable var = data.getColumn(i);
      if (var == data.sortKey()) continue;
      SoftPoint pt = new SoftPoint(1.1f, 0);
      points.put(var, pt);
    }
    
    requestedClear = false;
    requestedUpdateSelection = false;
    varsToAdd = new HashSet<Variable>();
    varsToRemove = new HashSet<Variable>();
    
    h1Font = getStyleFont("Profile.h1", "font-family", "font-size");
    h1Color = getStyleColor("Profile.h1", "color");

    pFont = getStyleFont("Profile.p", "font-family", "font-size");
    pColor = getStyleColor("Profile.p", "color"); 
    
    ptInColor = getStyleColor("Profile.Point", "fill-color");
    ptBrColor = getStyleColor("Profile.Point", "border-color");
    ptFont = getStyleFont("Profile.Point.p", "font-family", "font-size");
    ptColor = getStyleColor("Profile.Point.p", "color");
    
    selBarColor = getStyleColor("Profile.Selector", "selection-color");    
    
    export = new ExportButton(intf, -leftm - expBtnX, expBtnY, expBtnW, expBtnH);
    addChild(export, TOP_RIGHT_CORNER);
    
    hoverVar = null;
    hoverAlpha = new SoftFloat(255);
    
    selRangeLeft = 0;
    selRangeRight = 0.25f;    
    initSelHandles();
  }
  
  public void open() {
    requestedUpdateSelection = true;
    show(true);
    mira.history.openProfile();
  }
  
  public void clear() {
    requestedClear = true;
    dirty = true;
  }
 
  public void remove(Variable var) {
    varsToRemove.add(var);
    dirty = true;
  }
  
  public void remove(ArrayList<Variable> vars) {
    remove(vars, null);   
  }

  public void remove(ArrayList<Variable> vars, Variable exvar) {
    varsToRemove.addAll(vars);
    varsToRemove.remove(exvar);
    dirty = true;   
  }  
  
  public void add(Variable var) {
    varsToAdd.add(var);
    dirty = true;
  } 
  
  public void add(ArrayList<Variable> vars) {
    varsToAdd.addAll(vars);
    dirty = true;
  } 
  
  public void update() {
    boolean sort = data.sorting();

    if (sort || sort0 || (data.sorted() && dirty)) {
      updatePoints();
    }

    if (requestedUpdateSelection || sort) {
      updatePointSelection();
    }
    sort0 = sort;

    for (SoftPoint pt: points.values()) {
      pt.update();
    }
    
    hoverAlpha.update();
  }
  
  public void draw() {
    fill(color(40), 247);
    rect(0, 0, width, height);    
    
    float w = PApplet.map(data.sortProgress(), 0, 1, 0, width);
    noStroke();
    fill(color(39, 141, 210));
    rect(0, 0, w, progBarH);
    
    Variable sortVar = data.sortKey();
    String label = sortVar.getName();
    String alias = sortVar.getAlias();
    if (!label.equals(alias)) label += ": " + alias; 
    if (data.sorting()) {
      int perc = (int)(data.sortProgress() * 100);
      label = "Sorting " + label + ", " + perc + "% completed"; 
    }
    
    textFont(h1Font);
    fill(h1Color);
    text(label, rightm, labelY);
    
    textFont(pFont);
    fill(pColor);
    textAlign(RIGHT);
    text("Highest correlation", rightm - corrLabelX, topm, corrLabelW, highLabelH);
    text("Lowest correlation", rightm - corrLabelX, height - bottomm - lowLabelY, corrLabelW, lowLabelH);
    textAlign(LEFT);
    
    drawSelection();
    drawPoints();
    drawDismiss();    
  }

  public void mousePressed() {
    selRightHandle.mousePressed();
  }

  public void mouseDragged() {
    selRightHandle.mouseDragged();
  } 
  
  public void mouseReleased() {
    if (insideDismiss(mouseX, mouseY)) {
      hide(true);
      mira.history.closeProfile();
    } else {
      if (hoverVar != null) {
        hide(true);
        mira.browser.openColumn(hoverVar);
      }
      selRightHandle.mouseReleased();
    }
  }
  
  public void mouseMoved() {
    hoverVar = null;
    for (Variable var: points.keySet()) {
      SoftPoint pt = points.get(var);
      if (pt == null) continue;

      if (PApplet.dist(pt.x(), pt.y(), mouseX, mouseY) < 15) {
        hoverVar = var;
        hoverAlpha.setTarget(100);
        break;
      }
    }
    if (hoverVar == null) hoverAlpha.set(255);
  }

  protected void initSelHandles() {
    float x0 = rightm;
    float x1 = x0 + width - leftm - rightm;
    float y0 = topm - 10;
    float y1 = y0 + height - bottomm - topm;
    
    float selx1 = PApplet.map(selRangeRight, 0, 1, x0, x1);
    selRightHandle = new SelectionHandle(selx1, y1 + handleY, handlew, handleh, SelectionHandle.RIGHT);
  }
   
  protected void drawSelection() {
    float x0 = rightm;
    float x1 = x0 + width - leftm - rightm;
    float y0 = topm - 10;
    float y1 = y0 + height - bottomm - topm;
    
    noStroke();
    fill(selBarColor);
    rect(x0, y1, x1 - x0, selW);

    float selx0 = PApplet.map(selRangeLeft, 0, 1, x0, x1);
    float selx1 = selRightHandle.x0; //PApplet.map(selRangeRight, 0, 1, x0, x1);
    fill(selBarColor);
    rect(selx0, y0, selx1 - selx0, y1 - y0);
    
    fill(color(0), 190);
    rect(selx0, y1, selx1 - selx0, selW);
    
    selRightHandle.draw();
    
    if (mira.project.sortMethod == Project.PVALUE) {
      textFont(pFont);
      fill(pColor);
      textAlign(CENTER);
      text("False Discovery Rate < " + PApplet.nfc(fdr, 2) + "%", selx1, y0 - fdrY);
      textAlign(LEFT);
    }
  }
  
  protected void drawPoints() {
    for (Variable var: points.keySet()) {
      SoftPoint pt = points.get(var);
      if (pt == null) continue;

      int alpha = hoverVar != null && var != hoverVar ? hoverAlpha.getCeil() : 255;
      if (pt.selected) {
        stroke(ptBrColor, alpha);
      } else {;
        noStroke();  
      }      
      fill(ptInColor, alpha);
      ellipse(pt.x(), pt.y(), pointRad, pointRad);
    }
      
    SoftPoint pt = points.get(hoverVar);
    if (pt != null) {
      int alpha = PApplet.ceil(PApplet.map(hoverAlpha.get(), 255, 100, 0, 255));
      String ptLabel = hoverVar.getName();
      String ptAlias = hoverVar.getAlias();
      float score = data.getScore(hoverVar);
      if (!ptLabel.equals(ptAlias)) ptLabel += ": " + ptAlias;      
      if (mira.project.sortMethod == Project.PVALUE) {
        double p = Math.pow(10, -score);
        String value;
        if (p < PValue.MIN_VALUE) {
          value = "* < 1E-9";
        } else {
          if (p > 0.01) {
            value = " = " + Numbers.nfc(p, 2);
          } else {
            value = " = " + Numbers.dfc(p);
          }
          if (p <= mira.project.pvalue()) value = "*" + value;
        }
        ptLabel += " P" + value;
      }
      
      textFont(ptFont);
      fill(ptColor, alpha);
      float tw = textWidth(ptLabel);
      float tx = pt.x() - tw/2;
      float ty = pt.y() - ptFont.getSize() - ptLabelOffset;
      if (width < tx + tw) {
        tx = width - tw - ptLabelOffset;  
      }
      text(ptLabel, tx, ty);
    }
  } 
  
  protected void drawDismiss() {
    float x0 = width - crossw - leftm + dismissX;
    float y0 = 90;
    
    stroke(color(255), 200);
    strokeWeight(1);
    line(x0, y0, x0 + crossw, y0 + crossw);
    line(x0, y0 + crossw, x0 + crossw, y0);
  }

  protected boolean insideDismiss(float mx, float my) {
    float x0 = width - crossw - leftm + dismissX;
    float y0 = 90;
    return x0 <= mx && mx <= x0 + crossw && y0 <= my && my <= y0 + crossw; 
  }

  protected void updatePoints() {
    if (requestedClear) {
      points.clear();
      for (int i = 0; i < data.getColumnCount(); i++) {
        Variable var = data.getColumn(i);
        if (var == data.sortKey()) continue;
        SoftPoint pt = new SoftPoint(1.1f, 0);
        points.put(var, pt);
      }
      dirty = true;
      requestedClear = false;
    }
    
    if (0 < varsToRemove.size()) {
      for (Variable var: varsToRemove) {
        if (points.containsKey(var)) {
          points.remove(var);
        }        
      }
      varsToRemove.clear();
    }
    
    if (0 < varsToAdd.size()) {
      for (Variable var: varsToAdd) {
        if (!points.containsKey(var)) {
          SoftPoint pt = new SoftPoint(1.1f, 0);
          points.put(var, pt);
        }        
      }
      varsToAdd.clear();
    }
    
    int count = 0;
    float maxs = 0;
    float mins = Float.MAX_VALUE;

    for (int i = 0; i < data.getColumnCount(); i++) {
      Variable var = data.getColumn(i);        
      float score = data.getScore(i);
      if (mira.project.sortMethod == Project.PVALUE) {
        double p = Math.pow(10, -score);
        if (p < PValue.MIN_VALUE) score = (float)PValue.MAX_SCORE;
      }

      if (var == data.sortKey() || score <= 0 && !data.sorting()) {
        if (points.containsKey(var)) {
          points.remove(var);
        }
        continue;
      }
      if (!points.containsKey(var)) {
        SoftPoint pt = new SoftPoint(1.1f, 0);
        points.put(var, pt);        
      }
      if (maxs < score) maxs = score;
      if (score < mins) mins = score;
      count++;
    }
    
    int n = 0;
    for (int i = 0; i < data.getColumnCount(); i++) {
      Variable var = data.getColumn(i);
      float score = data.getScore(i);
      if (mira.project.sortMethod == Project.PVALUE) {
        double p = Math.pow(10, -score);
        if (p < PValue.MIN_VALUE) score = (float)PValue.MAX_SCORE;
      }
      SoftPoint pt = points.get(var);
      if (var == data.sortKey() || score <= 0 || pt == null) continue;

      float x = (float)Math.log1p((float)(n)/(float)(count - 1)* (Math.E-1));
      float y = (float)Math.log1p((score - mins)/(maxs - mins) * (Math.E-1));
      
      pt.target(x, y);
      pt.selected = selRangeLeft <= x && x <= selRangeLeft;
      
      n++;
    }
    
    if (data.sorted()) {
      dirty = false;  
    }    
  }
  
  protected void updatePointSelection() {
    Variable lastSel = null; 
    float xmax = -1000;
    for (Variable var: points.keySet()) {
      SoftPoint pt = points.get(var);
      if (pt == null) continue;
      float x = pt.x.get();
      pt.selected = selRangeLeft <= x && x <= selRangeRight;
      if (pt.selected && xmax < x) {
        lastSel = var;
        xmax = x;
      }
    }
    if (mira.project.sortMethod == Project.PVALUE && lastSel != null) {
      int lasti = data.getColumn(lastSel);
      int count = data.getColumnCount();
      ArrayList<Float> fdrList = new ArrayList<Float>();
      for (int i = lasti + 1; i < data.getColumnCount(); i++) {
        float score = data.getScore(i);
        float p = (float)Math.pow(10, -score);      
        float q = Math.min(100 * ((float)count / (i + 1)) * p, 100);
        fdrList.add(q);
      }
      Collections.sort(fdrList);
      fdr = fdrList.get(0);
      requestedUpdateSelection = false;
    }
  }
  
  protected void handleResize(int newWidth, int newHeight) {
    bounds.w.set(newWidth - mira.optWidth);
    bounds.h.set(newHeight);
    
    float y0 = topm - 10;
    float y1 = y0 + newHeight - bottomm - topm;    
    selRightHandle.setY(y1 + handleY);
    // TODO: re-adjust selected range according to new width...
  }
  
  protected class ExportButton extends Widget {
    PFont h2Font;
    int h2Color;
    int bkColor;
    
    public ExportButton(Interface intf, float x, float y, float w, float h) {
      super(intf, x, y, w, h);
    }
    
    public void setup() {
      h2Font = getStyleFont("Profile.Export.h2", "font-family", "font-size");
      h2Color = getStyleColor("Profile.Export.h2", "color");
      bkColor = getStyleColor("Profile.Export", "background-color");
    }
    
    public void draw() {
      int maxa = data.sorting() ? 50 : 255;
        
      noStroke();
      fill(bkColor, maxa);
      rect(0, 0, width, height);
      
      stroke(ptBrColor, maxa);
      fill(ptInColor, maxa);
      ellipse(20, height/2, 15, 15);
      
      fill(h2Color, maxa);
      textFont(h2Font);
      float yc = (height - h2Font.getSize()) / 2;
      text("Export selection", exportSelX, height - yc);
    }    
    
    public void handle() {
      System.out.println("export button: " + visible());
      if (!data.sorting()) {
        ArrayList<Variable> vars = new ArrayList<Variable>();
        ArrayList<Float> scores = new ArrayList<Float>();
        vars.add(data.sortKey());
        scores.add(1.0f);
        for (int i = 0; i < data.getColumnCount(); i++) {
          Variable var = data.getColumn(i);        
          float score = data.getScore(i);
          if (var == data.sortKey() || score <= 0) continue;
          SoftPoint pt = points.get(var);
          if (pt == null) continue;
          if (pt.selected) {
            vars.add(var);
            scores.add(1.0f);
          }
        }      
        mira.exportProfile(vars);        
      }      
    }
  }
  
  class SelectionHandle {
    final static int LEFT  = 0;
    final static int RIGHT = 1;
    
    int fColor;
    
    float x0, x1, x2;
    float y0, y1, y2;
    float w, h;
    float dx0;
    int side; 
    
    boolean focused;
    boolean dragging;
    
    SelectionHandle(float x, float y, float w, float h, int side) {      
      this.side = side;
      this.w = w;
      this.h = h;
      if (side == LEFT) {
        x0 = x; 
        x1 = x - 0.2f * w; 
        x2 = x - w;
      } else if (side == RIGHT) {
        x0 = x; 
        x1 = x + 0.2f * w; 
        x2 = x + w;
      }
      y0 = y;  
      y1 = y + 0.2f * h; 
      y2 = y + h;
      
      fColor = getStyleColor("Profile.Selector", "handle-color");
     
      focused = false;
      dragging = false;   
    }
    
    void setY(float y) {
      y0 = y;  
      y1 = y + 0.2f * h; 
      y2 = y + h;      
    }
    
    void draw() {
      noStroke();
      fill(fColor);
      beginShape(QUAD);
      vertex(x0, y1);
      vertex(x0, y2);
      vertex(x2, y2);
      vertex(x2, y1);      
      endShape();
      beginShape(TRIANGLE);
      vertex(x0, y0);
      vertex(x0, y1);
      vertex(x1, y1);
      endShape();      
    }
    
    boolean inside(float mx, float my) {
      if (side == LEFT) {
        return x2 <= mx && mx <= x0 && y1 <= my && my <= y2; 
      } else if (side == RIGHT) {
        return x0 <= mx && mx <= x2 && y1 <= my && my <= y2;
      }
      return false;
    }
   
    void mousePressed() {
      focused = inside(mouseX, mouseY);
      dx0 = x0 - mouseX;
      dragging = false;      
    }
    
    void mouseDragged() {
      if (focused) {
        float left = rightm;
        float right = left + width - leftm - rightm;
        
        dragging = true;
        float dragx = PApplet.constrain(mouseX + dx0, left, right);
        
        float value = PApplet.constrain(PApplet.map(dragx, left + dragLimitX, right - dragLimitX, 0, 1), 0, 1);
        if (side == LEFT) {
          selRangeLeft = value;
          requestedUpdateSelection = true;
        } else if (side == RIGHT) {
          selRangeRight = value;
          requestedUpdateSelection = true;
        }
        
        float dx1 = x1 - x0;
        float dx2 = x2 - x0;        
        x0 = dragx;
        x1 = x0 + dx1;
        x2 = x0 + dx2;  
      }
    }
    
    void mouseReleased() {
      if (dragging) {
        dragging = false;
      }
    } 
  }
  
  protected class SoftPoint {
    SoftFloat x;
    SoftFloat y;
    boolean selected;
//    int index;
//    int count;

    SoftPoint(float x0, float y0) {
      this.x = new SoftFloat(x0);
      this.y = new SoftFloat(y0);
    } 
    
    SoftPoint() {
      this(0, 0);
    }
    
    void update() {
      x.update();
      y.update();
    }
    
    void target(float x1, float y1) {
      x.setTarget(x1);
      y.setTarget(y1);
    }

    float x() {
      return PApplet.map(x.get(), 0, 1, rightm + softPointX, width - leftm - softPointX);
    }
    
    float y() {
      return PApplet.map(y.get(), 1, 0, topm + softPointY, height - bottomm - softPointY * 2);
    }    
  }
}

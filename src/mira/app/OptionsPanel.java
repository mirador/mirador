/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mira.app;

import java.util.ArrayList;

import lib.math.SoftFloat;
import lib.ui.Interface;
import processing.core.PFont;

/**
 * Panel with main options in Mirador (load data, plot type, P-Value selection, 
 * etc.)
 *
 */

public class OptionsPanel extends MiraWidget {
  int bColor;
  PFont h1Font;
  int h1Color;
  PFont pFont;
  int pColor;    
  int corColor;
  int misColor;
  Button loadBtn, uploadBtn, pdfBtn;
  Options plotOpt, statOpt, mdatOpt;

  public OptionsPanel(Interface intf, float x, float y, float w, float h) {
    super(intf, x, y, w, h);
  }
  
  public void setup() {
    bColor = getStyleColor("OptionsPanel", "background");
    h1Font = getStyleFont("OptionsPanel.h1", "font-family", "font-size");
    h1Color = getStyleColor("OptionsPanel.h1", "color");

    pFont = getStyleFont("OptionsPanel.p", "font-family", "font-size");
    pColor = getStyleColor("OptionsPanel.p", "color");
    
    corColor = getStyleColor("RowPlots.Pvalue", "background-color");
    misColor = getStyleColor("RowPlots.MissingData", "background-color");
    
    loadBtn = new Button(10, 80, 100, 30, "Load Data"); 
    uploadBtn = new Button(10, 115, 100, 30, "Upload Findings");
    pdfBtn = new Button(10, 150, 100, 30, "Save PDF");
    
    plotOpt = new Options(10, 195, 110, 80, "PlotOptions");
    plotOpt.title("Plot Type");
    plotOpt.add("Scatter", "Histogram", "Eikosogram");
    plotOpt.select(mira.getPlotType());
    
    statOpt = new Options(10, 325, 110, 80, "StatsOptions");
    statOpt.title("P-value", corColor);
    statOpt.add("99.9%", "99.5%", "99%", "95%", "90%", "Don't use");
    statOpt.select(mira.getPValue());    
    
    mdatOpt = new Options(10, 525, 110, 80, "SessionOptions");
    mdatOpt.title("Available\ndata", misColor);
    mdatOpt.add("90%", "80%", "60%", "20%", "Don't use");
    mdatOpt.select(mira.getMissingThreshold());    
  }
  
  public void update() {
    plotOpt.update();
    statOpt.update();
    mdatOpt.update();    
  }
  
  public void draw() {
    noStroke();
    fill(bColor);
    rect(0, 0, width, height);

    fill(h1Color);
    textFont(h1Font);
    text(MiraApp.APP_NAME, 10, 50); 
    
    fill(color(0), 150);
    textFont(pFont);
    text(MiraApp.APP_VERSION, 10, 70); 
    
    loadBtn.draw();
    uploadBtn.draw();
    pdfBtn.draw();
    plotOpt.draw();
    statOpt.draw();
    mdatOpt.draw();
    
    // shadows
    beginShape(QUAD);
    fill(color(0), 50);
    vertex(width, 0);
    vertex(width, height);
    fill(color(0), 0);
    vertex(width - 15, height);
    vertex(width - 15, 0);      
    endShape(); 
  }
  
  public void mousePressed() {
    if (loadBtn.select(mouseX, mouseY)) {
      mira.loadDataset();
    } else if (uploadBtn.select(mouseX, mouseY)) {
      mira.uploadSession();
    } else if (pdfBtn.select(mouseX, mouseY)) {
//      mira.uploadSession();      
    } else if (plotOpt.select(mouseX, mouseY)) {
      mira.setPlotType(plotOpt.selected);
    } else if (statOpt.select(mouseX, mouseY)) {
      mira.setPValue(statOpt.selected);
    } else if (mdatOpt.select(mouseX, mouseY)) {
      mira.setMissingThreshold(mdatOpt.selected);
    }
  }  
  
  protected void handleResize(int newWidth, int newHeight) {
    bounds.h.set(newHeight);
  }  
  
  class Button {
    String label;
    int x, y, w, h;
    int color;
    
    Button(int x, int y, int w, int h, String label) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
      this.label = label;
      
      color = getStyleColor("OptionsPanel.Button", "color");
    }
    
    void draw() {
      stroke(color, 220);
      fill(color, 100);
      rect(x, y, w, h);
      
      float center = (h - pFont.getSize()) / 2;
      
      fill(pColor);
      textFont(pFont);
      text(label, x + 5, y + h - center);
    }
    
    boolean select(int mx, int my) {
      return x <= mx && mx <= x + w && y <= my && my <= y + h;
    }
  }
  
  class Options {
    ArrayList<String> list;
    int selected;
    int x, y, w, h;
    String[] title;
    boolean showTab;
    int tColor;
    float bWidth;
    int bColor;
    PFont h1Font;
    int h1Color;
    float sWidth, sHeight, sRight;
    int sColor;    
    float selY0;
    SoftFloat selY;
    
    Options(int x, int y, int w, int h, String style) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
      
      h1Font = getStyleFont("OptionsPanel." + style + ".h1", 
                            "font-family", "font-size");
      h1Color = getStyleColor("OptionsPanel." + style + ".h1", "color");      
      bWidth = getStyleSize("OptionsPanel." + style, "border-top-width");
      bColor = getStyleColor("OptionsPanel." + style, "border-top-color");      
      
      sWidth = getStyleSize("OptionsPanel." + style + ".SelectBox", "width");
      sHeight = getStyleSize("OptionsPanel." + style + ".SelectBox", "height");
      sRight = getStyleSize("OptionsPanel." + style + ".SelectBox", "right");
      sColor = getStyleColor("OptionsPanel." + style + ".SelectBox", "background");
      
      list = new ArrayList<String>();
      
      selY = new SoftFloat();
    }

    void title(String titl) {
      title = titl.split("\n");
      showTab = false;
    }    
    
    void title(String titl, int col) {
      title = titl.split("\n");
      tColor = col;
      showTab = true;
    }
    
    void add(String... options) {
      for (String opt: options) {
        list.add(opt);
      }
    }
    
    void select(int sel) {
      selected = sel;
      selY.set(selected * sHeight);
    }
    
    boolean select(int mx, int my) {
      float x0 = x + 5;
      float x1 = x0 + sWidth;
      float y0 = selY0;
      float y1 = selY0 + list.size() * sHeight;
      
      if (x0 <= mx && mx <= x1 && y0 <= my && my <= y1) {
        int sel = (int)((my - y0) / sHeight);
        if (sel != selected) {
          selected = sel;
          selY.setTarget(selected * sHeight);
          return true; 
        }
      }
      return false;
    }
    
    void update() {
      selY.update();
    }
    
    void draw() {
      stroke(bColor);
      strokeWeight(bWidth);
      line(x, y, x + w - 10, y);
      
      float x1 = x + 5;
      float y0 = y + 10;
      float y1 = y0;
      
      fill(h1Color);
      textFont(h1Font);
      float th = (textAscent() + textDescent()) * 1.1f;

      for (String t: title) {
        y1 += th;
        text(t, x1, y1);        
      }
      
      if (showTab) {
        noStroke();
        fill(tColor);
        y0 += th - textAscent();
        rect(0, y0, 5, y1 - y0);
      }

      y1 += 10;
      selY0 = y1;
      float y2 = y1 + selY.get();
      noStroke();
      fill(sColor);
      rect(x, y2, sWidth, sHeight);
      
      fill(pColor);
      textFont(pFont);

      float center = (sHeight - pFont.getSize()) / 2;
      for (String opt: list) {
        y1 += sHeight;
        text(opt, x1, y1 - center);        
      }
    }
  } 
}

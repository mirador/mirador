/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mirador.views;

import mirador.ui.Widget;
import miralib.data.DataRanges;
import miralib.data.DataSlice1D;
import miralib.data.DataSlice2D;
import miralib.data.Variable;
import miralib.utils.Log;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;

/**
 * Base class for plots in Mirador.
 *
 */

abstract public class View {
  // ...........................................................................
  // Plot types  
  
  final static public int SCATTER    = 0; 
  final static public int HISTOGRAM  = 1;
  final static public int EIKOSOGRAM = 2;
  
  final static public int WHITE = 0xFFFFFFFF;
  final static public int GREY  = 0xFFBFD8F2;
  final static public int BLUE  = 0xFF278DD2;
  
  protected Variable varx, vary;
  protected DataRanges ranges; 
  
  public View(Variable varx, Variable vary, DataRanges ranges) {
    this.varx = varx; 
    this.vary = vary;
    this.ranges = ranges;    
  }
  
  static public View create(DataSlice1D slice, int binAlgo) {
    return new Histogram1D(slice, binAlgo);
  }
  
  static public View create(DataSlice2D slice, int type, int binAlgo) {
    View view = null;
    if (type == SCATTER) {
      view = new Scatter(slice);
    } else if (type == HISTOGRAM) {
      view = new Histogram2D(slice, binAlgo);  
    } else if (type == EIKOSOGRAM) {
      view = new Eikosogram(slice, binAlgo);
    } else {
      String err = "Unsupported view type: " + type;
      Log.error(err, new RuntimeException(err));
    }
    return view;
  }
  
  public String getLabelX(double valx, double valy) {
    return varx.formatValue(valx, ranges); 
  }
  
  public String getLabelY(double valx, double valy) {
    return vary.formatValue(1 - valy, ranges);
  } 
  
  abstract public void draw(PGraphics pg);
  
  abstract public Selection getSelection(double valx, double valy, boolean shift);

  public void drawSelection(double valx, double valy, boolean shift,
                            float x0, float y0, float w0, float h0,
                            Widget wt, PFont font, int color) {
    Selection sel = getSelection(valx, valy, shift);
    if (sel != null) sel.draw(wt, x0, y0, w0, h0, font, color);
  }  
    
  public void drawSelection(double valx, double valy, boolean shift,
                            PGraphics pg, PFont font, int color) {
    Selection sel = getSelection(valx, valy, shift);
    if (sel != null) sel.draw(pg, font, color);
  }
  
  protected int mixColors(int col0, int col1, float f) {
    int a = (int)PApplet.map(f, 0, 1, col0 >> 24 & 0xFF, col1 >> 24 & 0xFF);
    int r = (int)PApplet.map(f, 0, 1, col0 >> 16 & 0xFF, col1 >> 16 & 0xFF);
    int g = (int)PApplet.map(f, 0, 1, col0 >>  8 & 0xFF, col1 >>  8 & 0xFF);
    int b = (int)PApplet.map(f, 0, 1, col0       & 0xFF, col1       & 0xFF);    
    return (a << 24) | (r << 16) | (g << 8) | b;
  } 
  
  public class Selection {
    public float x, y, w, h;
    public int color;
    public String label;
    public boolean hasLabel;
    public boolean isEllipse;
    
    Selection(float x, float y, float w, float h) {
      this.x = x; 
      this.y = y; 
      this.w = w;
      this.h = h;
      this.hasLabel = false;
      this.isEllipse = false;      
    }
    
    public void setLabel(String label) {
      if (label == null || label.equals("")) {
        hasLabel = false;
      } else {
        this.label = label;
        hasLabel = true;
      }      
    }
    
    public void setColor(int color) {
      this.color = color;
    }
    
    public void scale(float x0, float y0, float w0, float h0) {
      x = PApplet.map(x, 0, 1, x0, x0 + w0);
      y = PApplet.map(y, 0, 1, y0, y0 + h0);     
      w = PApplet.map(w, 0, 1, 0, w0);
      h = PApplet.map(h, 0, 1, 0, h0);
      
      if (x < x0) {
        h -= x0 - x;
        x = x0;        
      }
      if (x0 + w0 < x + w) w = x0 + w0 - x;
      
      if (y < y0) {
        h -= y0 - y;
        y = y0; 
      }
      if (y0 + h0 < y + h) h = y0 + h0 - y;      
    }
    
    public void draw(Widget wt, float x0, float y0, float w0, float h0, 
                     PFont font, int color) {
      scale(x0, y0, w0, h0);
      
      if (isEllipse) {
        // TODO: implement
        wt.noStroke();
        wt.fill(wt.color(0, 0, 0), 50);
        wt.ellipse(x, y, w, h);
      } else {
        wt.noStroke();
        wt.fill(wt.color(0, 0, 0), 50);
        wt.rect(x, y, w, h);
      }
      
      if (hasLabel) {
        wt.textFont(font);
        wt.fill(color);
        float tw = wt.textWidth(label);          
        float tx = x + w/2 - tw/2;
        if (tx < x0) tx = x0;
        if (x0 + w0 < tx + tw) tx = x0 + w0 - tw;
        
        float ty = 0;
        if (font.getSize() < h) { 
          float yc = (h - font.getSize()) / 2;
          ty = y + h - yc;      
        } else {
          ty = y - 5;
          if (ty - 5 - font.getSize() < y0) ty = y + h + 5 + font.getSize();
        }
        
        wt.text(label, tx, ty);
      }      
    }
    
    public void draw(PGraphics pg, PFont font, int color) {
      float x0 = 0; 
      float y0 = 0; 
      float w0 = pg.width;
      float h0 = pg.height;
      
      scale(x0, y0, w0, h0);
      
      if (isEllipse) {
        // TODO: implement
        pg.noStroke();
        pg.fill(pg.color(0, 0, 0), 50);
        pg.ellipse(x, y, w, h);
      } else {
        pg.noStroke();
        pg.fill(pg.color(0, 0, 0), 50);
        pg.rect(x, y, w, h);
      }
      
      if (hasLabel) {
        pg.textFont(font);
        pg.fill(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, 255);
        float tw = pg.textWidth(label);          
        float tx = x + w/2 - tw/2;
        if (tx < x0) tx = x0;
        if (x0 + w0 < tx + tw) tx = x0 + w0 - tw;
        
        float ty = 0;
        if (font.getSize() < h) { 
          float yc = (h - font.getSize()) / 2;
          ty = y + h - yc;      
        } else {
          ty = y - 5;
          if (ty - 5 - font.getSize() < y0) ty = y + h + 5 + font.getSize();
        }
                
        pg.text(label, tx, ty);
      }      
    }    
  }
  
  static public String typeToString(int type) {
    if (type == SCATTER) return "SCATTER";
    else if (type == HISTOGRAM) return "HISTOGRAM";
    else if (type == EIKOSOGRAM) return "EIKOSOGRAM";
    else return "UNKNOWN";
  }
}

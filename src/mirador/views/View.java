/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mirador.views;

import miralib.data.DataRanges;
import miralib.data.DataSlice2D;
import miralib.data.Variable;
import miralib.utils.Log;
import processing.core.PApplet;
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
  final static public int BLUE  = 0xFF278DD2;
  
  protected Variable varx, vary;
  protected DataRanges ranges; 
  
  public View(Variable varx, Variable vary, DataRanges ranges) {
    this.varx = varx; 
    this.vary = vary;
    this.ranges = ranges;    
  }
  
  static public View create(DataSlice2D slice, int type) {
    View view = null;
    if (type == SCATTER) {
      view = new Scatter(slice);
    } else if (type == HISTOGRAM) {
      view = new Histogram(slice);  
    } else if (type == EIKOSOGRAM) {
      view = new Eikosogram(slice);
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
  
  abstract public Selection getSelection(double valx, double valy);
  
  protected void drawHistogram(PGraphics pg) {
    
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
  }
  
  static public String typeToString(int type) {
    if (type == SCATTER) return "SCATTER";
    else if (type == HISTOGRAM) return "HISTOGRAM";
    else if (type == EIKOSOGRAM) return "EIKOSOGRAM";
    else return "UNKNOWN";
  }
}

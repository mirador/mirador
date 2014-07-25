/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mira.views;

import java.util.ArrayList;

import lib.math.Numbers;
import mira.data.DataSlice2D;
import mira.data.Value2D;
import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * Scatter plot.
 *
 */

public class ScatterPlot extends View {
  final static public boolean USE_ELLIPSES = true; 
  
  protected ArrayList<Value2D> points; 
  protected double count;
  
  public ScatterPlot(DataSlice2D slice) {
    super(slice.varx, slice.vary, slice.ranges);
    initPoints(slice);
  }

  public void draw(PGraphics pg) {
    pg.beginDraw();
    pg.background(WHITE);
    pg.noStroke();
    long nx, ny;
    float rad = 0;      
    int a;
    if (varx.categorical() && vary.categorical()) {
      a = 200;
    } else {
      nx = 0;
      ny = 0;
      rad = PApplet.map((float)count, 0, pg.width * pg.height, 0.05f, 0.01f);
      a = (int)PApplet.map((float)count, 0, pg.width * pg.height, 70, 10);
    }
    
    nx = varx.getCount(ranges);
    ny = vary.getCount(ranges);
    pg.fill(pg.red(BLUE), pg.green(BLUE), pg.blue(BLUE), a);
    for (Value2D pt: points) {
      float px, py;
      float pw, ph;            
      if (varx.categorical() && vary.categorical()) {
        if (Numbers.equal(pt.w, 0)) continue;
        //x = (float)(pg.width * pt.x);
        //y = pg.height - (float)(pg.height * pt.y);
        
        long i = (long)(pt.x * (nx-1));
        float dx = (float)pg.width / nx;
        px = dx/2 + dx * i;
        
        long j = (long)(pt.y * (ny-1));
        float dy = (float)pg.height / ny;
        py = pg.height - (dy/2 + dy * j);
        
        rad = PApplet.map(PApplet.sqrt((float)(pt.w / count)), 0, 1, 0.05f, 0.5f); 
                       // PApplet.min((float)pg.width / (float)nx, 
                       //             (float)pg.height / (float)ny));
        pw = pg.width * rad;
        ph = pg.height * rad; 
      } else {
        if (varx.categorical()) {
          long i = (long)(pt.x * (nx-1));
          float dx = (float)pg.width / nx;
          px = dx/2 + dx * i;
        } else {
          px = (float)(pg.width * pt.x);  
        }
        
        if (vary.categorical()) {
          long j = (long)(pt.y * (ny-1));
          float dy = (float)pg.height / ny;
          py = pg.height - (dy/2 + dy * j);          
        } else {
          py = pg.height - (float)(pg.height * pt.y);  
        }      
        pw = pg.width * rad;
        ph = pg.height * rad;        
      }        
      if (USE_ELLIPSES) pg.ellipse(px, py, pw, ph);
      else pg.rect(px - pw/2, py - ph/2, pw, ph);
      
      // Only when hovering...
//      if (pt.label != null) {
//        pg.fill(0);
//        pg.text(pt.label, px + pw, py);  
//      }        
    }              
    pg.endDraw();
  }
  
  public Selection getSelection(double valx, double valy) {
    // TODO: needs some kind of tree representation of the data to search 
    // efficiently when there are many data points.
    return null;
  }  
  
  protected void initPoints(DataSlice2D slice) {
    points = new ArrayList<Value2D>();    
    if (varx.categorical() && vary.categorical()) {
      for (Value2D val: slice.values) {
        Value2D pt = search(val);
        if (pt == null) {
          points.add(new Value2D(val));
        } else {
          pt.w += val.w;
        }
      }
    } else {
      points.addAll(slice.values);  
    }
    count = 0;
    for (Value2D val: slice.values) count += val.w;    
  }
  
  protected Value2D search(Value2D val) {
    for (Value2D pt: points) {
      if (val.equals(pt)) return pt;     
    }
    return null;    
  }
}

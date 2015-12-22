/* COPYRIGHT (C) 2014-16 Fathom Information Design. All Rights Reserved. */

package mirador.views;

import java.util.ArrayList;

import miralib.data.DataSlice2D;
import miralib.data.Value2D;
import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * Scatter plot.
 *
 */

public class Scatter extends View {  
  protected ArrayList<Value2D> points;
  
  public Scatter(DataSlice2D slice) {
    super(slice.varx, slice.vary, slice.ranges);
    initPoints(slice);
  }

  public void draw(PGraphics pg) {
    pg.beginDraw();
    pg.background(WHITE);
    pg.noStroke();
    float rad = 0;      
    int a;
    int count = PApplet.min(500, points.size());
    rad = PApplet.map(count, 0, 500, 0.05f, 0.01f);
    a = (int)PApplet.map(count, 0, 500, 70, 10);

    pg.fill(pg.red(BLUE), pg.green(BLUE), pg.blue(BLUE), a);
    for (Value2D pt: points) {
      float px = pg.width * (float)pt.x;      
      float py = pg.height * (float)(1 - pt.y);
      float pw = pg.width * rad;
      float ph = pg.height * rad;        
      if (50000 < points.size()) pg.rect(px - pw/2, py - ph/2, pw, ph);
      else pg.ellipse(px, py, pw, ph);
    }              
    pg.endDraw();
  }
  
  public Selection getSelection(double valx, double valy, boolean shift) {
    if (points.size() < 500) {
      int count = PApplet.min(500, points.size());
      float rad = PApplet.map(count, 0, 500, 0.05f, 0.01f);
      
      for (Value2D pt: points) {
        float px = (float)pt.x;        
        float py = 1 - (float)(pt.y);        
        if (PApplet.dist((float)valx, (float)valy, px, py) < rad) {
          Selection sel = new Selection(px, py, rad, rad);
          sel.isEllipse = true;
          sel.setLabel(pt.label);
          return sel;            
        }
      }
      
      return null;      
    } else {
      // TODO: needs some kind of tree representation of the data to search 
      // efficiently when there are many data points.      
      return null;      
    }    
  }  
  
  protected void initPoints(DataSlice2D slice) {
    float dx = (float)(1.0d / varx.getCount(ranges));
    float dy = (float)(1.0d / vary.getCount(ranges)); 
    
    points = new ArrayList<Value2D>();
    for (Value2D val: slice.values) {
      Value2D pt = new Value2D(val);
      pt.label = val.label;
      if (varx.categorical()) {
        pt.x += (1 - 2 * Math.random()) * 0.25f * dx;      
        pt.x = PApplet.map((float)pt.x, 0, 1, dx/2, 1 - dx/2);
      }
      if (vary.categorical()) {
        pt.y += (float)(1 - 2 * Math.random()) * 0.25f * dy;
        pt.y = PApplet.map((float)pt.y, 0, 1, dy/2, 1 - dy/2);
      }      
      points.add(pt);
    }    
  }
}

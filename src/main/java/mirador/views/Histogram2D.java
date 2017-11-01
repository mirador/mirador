/* COPYRIGHT (C) 2014-16 Fathom Information Design. All Rights Reserved. */

package mirador.views;

import miralib.data.DataSlice2D;
import miralib.data.Value2D;
import miralib.shannon.BinOptimizer;
import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * Joint histogram.
 *
 */

public class Histogram2D extends View {  
  protected double[][] weightSum;
  protected float[][] density;
  protected boolean[][] selected;
  protected float binSizeX;
  protected float binSizeY;
  protected int binCountX;
  protected int binCountY;
  protected float maxProb; 
  protected int sampleSize;
  
  public Histogram2D(DataSlice2D slice, int algo) {
    super(slice.varx, slice.vary, slice.ranges);
    calcDensity(slice, algo); 
  }
  
  public void draw(PGraphics pg, boolean pdf) {
    pg.beginDraw();
    pg.background(WHITE);
    if (canDraw()) {
      float binw = (float)pg.width / binCountX;
      float binh = (float)pg.height / binCountY;    
      pg.noStroke();
      for (int bx = 0; bx < binCountX; bx++) {
        for (int by = 0; by < binCountY; by++) {
          float p = PApplet.map(density[bx][by], 0, maxProb, 0, 1);
          pg.fill(mixColors(WHITE, BLUE, p));
          pg.rect(binw * bx, pg.height - binh * by, binw, -binh);        
        }
      }
    } else {
      drawCross(pg);
    }
    if (pdf) pg.dispose();
    pg.endDraw();
  }
  
  public Selection getSelection(double valx, double valy, boolean shift) {
    if (canDraw()) {
      float binw = 1.0f / binCountX;
      float binh = 1.0f / binCountY;
      for (int bx = 0; bx < binCountX; bx++) {
        for (int by = 0; by < binCountY; by++) {
          float x0 = binw * bx;
          float y0 = 1 - binh * by - binh;
          float x1 = x0 + binw; 
          float y1 = y0 + binh;
          if (x0 <= valx && valx <= x1 && y0 <= valy && valy <= y1) {
            Selection sel = new Selection(x0, y0, binw, binh);
            float p = density[bx][by];
            float np = PApplet.map(p, 0, maxProb, 0, 1);
            sel.setColor(mixColors(WHITE, BLUE, np));
            if (shift) {
              sel.setLabel(PApplet.round(sampleSize * p) + "/" + sampleSize);
            } else {
              sel.setLabel(PApplet.nfc(100 * p, 2) + "%");
            }
            return sel;
          }
        }
      }
    } else {
      return getUnavailableSelection();
    }
    return null;
  }

  public boolean canDraw() {
    return 1 < binCountX && 1 < binCountY;
  }

  protected void calcDensity(DataSlice2D slice, int algo) {
    // Calculating number of bins ----------------------------------------------
    int[] res = BinOptimizer.calculate(slice, algo);
    binCountX = res[0];
    binCountY = res[1];
    if (0 < binCountX && 0 < binCountY) {
      binSizeX = 1.0f / binCountX;      
      binSizeY = 1.0f / binCountY;
    }
    
    sampleSize = slice.values.size();
    
    // Initializing arrays -----------------------------------------------------    
    weightSum = new double[binCountX][binCountY];
    density = new float[binCountX][binCountY];
    selected = new boolean[binCountX][binCountY];
    for (int bx = 0; bx < binCountX; bx++) {
      for (int by = 0; by < binCountY; by++) {
        weightSum[bx][by] = 0;
        selected[bx][by] = false;
      }
    }   
    
    if (0 < binCountX && 0 < binCountY) {
      // Updating counts -------------------------------------------------------
      double totWeight = 0;
      for (Value2D value: slice.values) {
        int bx = PApplet.constrain((int)(value.x / binSizeX), 0, binCountX - 1);  
        int by = PApplet.constrain((int)(value.y / binSizeY), 0, binCountY - 1);  
        weightSum[bx][by] += value.w;
        totWeight += value.w;
      }
        
      maxProb = 0;
      // Calculating density ---------------------------------------------------      
      for (int bx = 0; bx < binCountX; bx++) {
        for (int by = 0; by < binCountY; by++) {
          float p = (float)weightSum[bx][by] / (float)totWeight;
          density[bx][by] = p;
          if (maxProb < p) maxProb = p;
        }
      }
    } 
  }
}

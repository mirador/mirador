/* COPYRIGHT (C) 2014-16 Fathom Information Design. All Rights Reserved. */

package mirador.views;

import miralib.data.DataSlice1D;
import miralib.data.Value1D;
import miralib.shannon.BinOptimizer;
import processing.core.PApplet;
import processing.core.PGraphics;

public class Histogram1D extends View {
  protected double[] weightSum;
  protected float[] density;
  protected boolean[] selected;
  protected float binSize;
  protected int binCount;
  protected float maxProb;
  protected int sampleSize;
  
  public Histogram1D(DataSlice1D slice, int algo) {
    super(slice.varx, slice.varx, slice.ranges);
    calcDensity(slice, algo);
  }

  public void draw(PGraphics pg, boolean pdf) {
    pg.beginDraw();
    pg.background(WHITE);
    if (canDraw()) {
      float binw = (float)pg.width / binCount;    
      pg.noStroke();
      for (int bx = 0; bx < binCount; bx++) {
        float p = density[bx];
        float h = PApplet.map(p, 0, maxProb, 0, 0.5f);
//        float p = density[bx];
        pg.fill(GREY);
        pg.rect(binw * bx, pg.height * (1 - h), binw, pg.height * h);        
      }
    } else {
      drawCross(pg);
    }
    if (pdf) pg.dispose();
    pg.endDraw();
  }

  public String getLabelY(double valx, double valy) {
    return "";
  }
  
  public Selection getSelection(double valx, double valy, boolean shift) {
    if (canDraw()) {
      float binw = 1.0f / binCount;          
      for (int bx = 0; bx < binCount; bx++) {        
        float p = density[bx];
        float h = PApplet.map(p, 0, maxProb, 0, 0.5f);
        float x0 = binw * bx;
        float x1 = binw * bx + binw;
        if (x0 < valx && valx < x1) {
          Selection sel = new Selection(x0, (1 - h), binw, h);
          if (shift) {
            sel.setLabel(PApplet.round(sampleSize * p) + "/" + sampleSize);
          } else {
            sel.setLabel(PApplet.nfc(100 * p, 2) + "%");  
          }
          
          return sel;
        }        
      }
    } else {
      return getUnavailableSelection();
    }
    return null;
  }

  public boolean canDraw() {
    return 1 < binCount;
  }

  protected void calcDensity(DataSlice1D slice, int algo) {
    // Calculating number of bins ----------------------------------------------
    binCount = BinOptimizer.calculate(slice, algo);
    if (0 < binCount) {
      binSize = 1.0f / binCount;
    }
    
    sampleSize = slice.values.size();
    
    // Initializing arrays -----------------------------------------------------    
    weightSum = new double[binCount];
    density = new float[binCount];
    selected = new boolean[binCount];
    for (int bx = 0; bx < binCount; bx++) {
      weightSum[bx] = 0;
      selected[bx] = false;
    }   
    
    if (0 < binCount) {
      // Updating counts -------------------------------------------------------
      double totWeight = 0;
      for (Value1D value: slice.values) {
        int bx = PApplet.constrain((int)(value.x / binSize), 0, binCount - 1);  
        weightSum[bx] += value.w;
        totWeight += value.w;
      }
        
      maxProb = 0;
      // Calculating density ---------------------------------------------------      
      for (int bx = 0; bx < binCount; bx++) {
        float p = (float)weightSum[bx] / (float)totWeight;
        density[bx] = p;
        if (maxProb < p) maxProb = p;
      }
    } 
  }  
}

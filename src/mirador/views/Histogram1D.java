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
  
  public Histogram1D(DataSlice1D slice) {
    super(slice.varx, slice.varx, slice.ranges);
    calcDensity(slice);
  }

  public void draw(PGraphics pg) {
    pg.beginDraw();
    pg.background(WHITE);
    if (1 < binCount) { 
      float binw = (float)pg.width / binCount;    
      pg.noStroke();
      for (int bx = 0; bx < binCount; bx++) {        
//        float p = PApplet.map(density[bx], 0, maxProb, 0, 1);
        float p = density[bx];
        pg.fill(BLUE);
        pg.rect(binw * bx, pg.height * (1 - p), binw, pg.height * p);        
      }
    }        
    pg.endDraw();
  }

  public String getLabelY(double valx, double valy) {
    return "";
  }
  
  public Selection getSelection(double valx, double valy) {
    if (1 < binCount) { 
      float binw = 1.0f / binCount;          
      for (int bx = 0; bx < binCount; bx++) {        
        float p = density[bx];
        float x0 = binw * bx;
        float x1 = binw * bx + binw;
        if (x0 < valx && valx < x1) {
          Selection sel = new Selection(x0, (1 - p), binw, p);
          sel.setLabel(PApplet.nfc(100 * p, 2) + "%");
          return sel;
        }        
      }
    }    
    return null;
  } 

  protected void calcDensity(DataSlice1D slice) {
    // Calculating number of bins ----------------------------------------------
    binCount = BinOptimizer.calculate(slice);
    if (0 < binCount) {
      binSize = 1.0f / binCount;
    }
    
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

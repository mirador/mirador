/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mira.views;

import java.util.Arrays;

import mira.data.DataSlice2D;
import mira.data.Value2D;
import mira.shannon.Histogram;
import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * Eikosogram plot.
 *
 */

public class Eikosogram extends View {
  protected double[][] binCounts;
  protected double[] marginalBinCounts;
  protected float[][] density;
  protected float[] marginalDensity;
  protected double[] averageY;
  protected double[] stddevY;
  protected double[] onestdFrac;
  protected double[] twostdGFrac;
  protected double[] twostdSFrac;
  protected float binSizeX;
  protected float binSizeY;
  protected int binCountX;
  protected int binCountY; 
  
  public Eikosogram(DataSlice2D slice) {
    super(slice.varx, slice.vary, slice.ranges);
    calcDensity(slice); 
  }
  
  public String getLabelX(double valx, double valy) {
    // We need to find which bin the value valx falls into, with the bin width
    // representing the probability of the bin
    float x0 = 0;
    for (int bx = 0; bx < binCountX; bx++) {
      float dx = marginalDensity[bx];
      if (x0 <= valx && valx <= x0 + dx) {
        float val0 = binSizeX * bx;
        float val1 = binSizeX * (bx + 1);
        float val = PApplet.map((float)(valx - x0) / dx, 0, 1, val0, val1);
        return varx.formatValue(val, ranges);
      }
      x0 += dx;
    }
    return ""; 
  }
  
  public String getLabelY(double valx, double valy) {
    if (vary.categorical()) {
      float x0 = 0;
      for (int bx = 0; bx < binCountX; bx++) {
        float dx = marginalDensity[bx];
        if (x0 <= valx && valx <= x0 + dx) {        
          float y0 = 1;
          for (int by = 0; by < binCountY; by++) {
            float dy = density[bx][by] / dx;
            if (y0 - dy <= valy && valy <= y0) {
              float val0 = binSizeY * by;
              float val1 = binSizeY * (by + 1);
              float val = PApplet.map((float)(valy - (y0 - dy)) / dy, 0, 1, val1, val0);
              return vary.formatValue(val, ranges);            
            }
            y0 -= dy;
          }
        }
        x0 += dx;
      }
      return "";       
    } else {
      return super.getLabelY(valx, valy);
    }
  } 
  
  public void draw(PGraphics pg) {
    pg.beginDraw();
    pg.background(WHITE);
    if (1 < binCountX && 1 < binCountY) {  
      if (vary.categorical()) {
        drawEikosogram(pg);
      } else {
        drawBoxplot(pg);
      }
    }
    pg.endDraw();
  }
  
  public Selection getSelection(double valx, double valy) {
    if (1 < binCountX && 1 < binCountY) {
      if (vary.categorical()) {
        return getEikosogramSelection(valx, valy);
      } else {
        return getBoxplotSelection(valx, valy);
      }
    }
    return null;
  }
  
  protected void drawEikosogram(PGraphics pg) {
    pg.noStroke();
    float x0 = 0;
    for (int bx = 0; bx < binCountX; bx++) {
      float dx = pg.width * marginalDensity[bx];
      float y0 = pg.height;
      for (int by = 0; by < binCountY; by++) {
        float dy = pg.width * pg.height * density[bx][by] / dx;    
        float f = PApplet.map(by, 0, binCountY - 1, 1, 0);
        pg.fill(mixColors(WHITE, BLUE, f));
        pg.rect(x0, y0, dx, -dy);
        y0 -= dy;
      }
      x0 += dx;
    }
  }  
  
  protected void drawBoxplot(PGraphics pg) {
    float x0 = 0;
    for (int bx = 0; bx < binCountX; bx++) {
      float mean = (float)averageY[bx];
      float std = (float)stddevY[bx];
      float dx = pg.width * marginalDensity[bx];
      if (Float.isNaN(mean) || Float.isNaN(std)) continue;
      float y0 = pg.height * (1 - mean - std);
      float dy = pg.height * 2 * std;
      pg.noStroke();
      
      // sigma boxes
      pg.fill(mixColors(WHITE, BLUE, 1));
      pg.rect(x0, y0, dx, dy);
      
      // 2 * sigma boxes
      pg.fill(mixColors(WHITE, BLUE, 0.5f));
      pg.rect(x0, y0 - dy/2, dx, dy/2);
      pg.rect(x0, y0 + dy, dx, dy/2);
      
      x0 += dx;  
    }    
  }
  
  public Selection getEikosogramSelection(double valx, double valy) {   
    float x0 = 0;
    for (int bx = 0; bx < binCountX; bx++) {
      float dx = marginalDensity[bx];
      float y0 = 1;
      for (int by = 0; by < binCountY; by++) {
        float dy = density[bx][by] / dx;
        float x1 = x0 + dx; 
        float y1 = y0 - dy;        
        if (x0 <= valx && valx <= x1 && y1 <= valy && valy <= y0) {
          Selection sel = new Selection(x0, y1, dx, dy);
          float f = PApplet.map(by, 0, binCountY - 1, 1, 0);
          sel.setColor(mixColors(WHITE, BLUE, f));
          sel.setLabel(PApplet.nfc(100 * dy, 2) + "%");
          return sel;
        }        
        y0 -= dy;
      }
      x0 += dx;
    }
    return null;
  }
  
  public Selection getBoxplotSelection(double valx, double valy) {
    float x0 = 0;
    for (int bx = 0; bx < binCountX; bx++) {
      double mean = averageY[bx];
      double std = stddevY[bx];
      float dx = marginalDensity[bx];
      float x1 = x0 + dx;
      if (Double.isNaN(mean) || Double.isNaN(std)) continue;
      
      float y0 = (float)(1 - mean - std);
      float dy = (float)(2 * std);
      float y1 = y0 + dy;
      
      if (x0 <= valx && valx <= x1 && y0 <= valy && valy <= y1) {
        Selection sel = new Selection(x0, y0, dx, dy);
        sel.setColor(mixColors(WHITE, BLUE, 1));
        sel.setLabel(PApplet.nfc(100 * (float)onestdFrac[bx], 2) + "%");
        return sel;
      } else if (x0 <= valx && valx <= x1 && y0 - dy/2 <= valy && valy <= y0) {
        Selection sel = new Selection(x0, y0 - dy/2, dx, dy/2);
        sel.setLabel(PApplet.nfc(100 * (float)twostdGFrac[bx], 2) + "%");
        sel.setColor(mixColors(WHITE, BLUE, 0.5f));
        return sel;
      } else if (x0 <= valx && valx <= x1 && y0 + dy <= valy && valy <= y0 + 3*dy/2) {
        Selection sel = new Selection(x0, y0 + dy, dx, dy/2);
        sel.setLabel(PApplet.nfc(100 * (float)twostdSFrac[bx], 2) + "%");
        sel.setColor(mixColors(WHITE, BLUE, 0.5f));
        return sel;
      }
      
      x0 += dx;  
    }    
    return null;
  }  
  
  protected void calcDensity(DataSlice2D slice) {
    // Calculating number of bins ----------------------------------------------
    int[] res = Histogram.optBinCount(slice);
    binCountX = res[0];
    binCountY = res[1];
    if (0 < binCountX && 0 < binCountY) {
      binSizeX = 1.0f / binCountX;      
      binSizeY = 1.0f / binCountY;
    }
    
    // Initializing arrays -----------------------------------------------------    
    binCounts = new double[binCountX][binCountY];
    density = new float[binCountX][binCountY];
    marginalDensity = new float[binCountX];
    
    for (int bx = 0; bx < binCountX; bx++) {
      for (int by = 0; by < binCountY; by++) {
        binCounts[bx][by] = 0;
      }
    }
    
    for (int bx = 0; bx < binCountX; bx++) marginalDensity[bx] = 0;
    
    if (0 < binCountX && 0 < binCountY) {
      // Updating counts -------------------------------------------------------
      double totCount = 0;
      for (Value2D value: slice.values) {
        int bx = PApplet.constrain((int)(value.x / binSizeX), 0, binCountX - 1);  
        int by = PApplet.constrain((int)(value.y / binSizeY), 0, binCountY - 1);
        binCounts[bx][by] += value.w;
        totCount += value.w;
      }    

      // Calculating density ---------------------------------------------------      
      for (int bx = 0; bx < binCountX; bx++) {
        for (int by = 0; by < binCountY; by++) {
          float p = (float)binCounts[bx][by] / (float)totCount;
          density[bx][by] = p;
          marginalDensity[bx] += p;  
        }
      }
    } 

    if (vary.numerical()) {
      averageY = new double[binCountX];
      stddevY = new double[binCountX];
      onestdFrac = new double[binCountX];
      twostdGFrac = new double[binCountX];
      twostdSFrac = new double[binCountX]; 
      marginalBinCounts = new double[binCountX];
      
      for (Value2D value: slice.values) {
        int bx = PApplet.constrain((int)(value.x / binSizeX), 0, binCountX - 1);
        averageY[bx] += value.y * value.w;
        stddevY[bx] += value.y * value.y * value.w;
      }
      
      for (int bx = 0; bx < binCountX; bx++) {
        double tot = 0;
        for (int by = 0; by < binCountY; by++) {
          tot += binCounts[bx][by];
        }
        marginalBinCounts[bx] = tot;
        if (0 < tot) { 
          double mean = averageY[bx] /= tot;
          double meansq = stddevY[bx] /= tot;
          stddevY[bx] = Math.sqrt(Math.max(0, meansq - mean * mean));
        } else {
          averageY[bx] = Double.NaN;
          stddevY[bx] = Double.NaN;
        }
      }
      
      Arrays.fill(onestdFrac, 0d);
      Arrays.fill(twostdGFrac, 0d);
      Arrays.fill(twostdSFrac, 0d);      
      for (Value2D value: slice.values) {
        int bx = PApplet.constrain((int)(value.x / binSizeX), 0, binCountX - 1);
        double ave = averageY[bx];
        double std = stddevY[bx];
        if (Double.isNaN(ave) || Double.isNaN(std)) continue;
        
        if (ave - std <= value.y && value.y <= ave + std) {
          onestdFrac[bx] += value.w;
        } else if (ave + std < value.y && value.y <= ave + 2 * std) {
          twostdGFrac[bx] += value.w;
        } else if (ave - 2 * std <= value.y && value.y < ave - std) {
          twostdSFrac[bx] += value.w;
        }
      }
      
      for (int bx = 0; bx < binCountX; bx++) {
        double tot = marginalBinCounts[bx];
        onestdFrac[bx] /= tot;
        twostdGFrac[bx] /= tot;
        twostdSFrac[bx] /= tot;
      }      
    }
  }
}

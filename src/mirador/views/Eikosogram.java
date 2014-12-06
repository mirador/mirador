/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mirador.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import miralib.data.DataSlice2D;
import miralib.data.Value2D;
import miralib.math.Numbers;
import miralib.shannon.BinOptimizer;
import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * Eikosogram plot.
 *
 */

public class Eikosogram extends View {
  protected double[][] weightSum;
  protected float[][] density;
  protected float[][] logDensity;
  protected int[] sampleCounts;
  protected float[] marginalDensity;
  
  protected float[]perc09;
  protected float[]perc25;
  protected float[]perc75;
  protected float[]perc91;
  
  protected float binSizeX;
  protected float binSizeY;
  protected int binCountX;
  protected int binCountY; 
  protected boolean logY;
  
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
  
  public Selection getSelection(double valx, double valy, boolean shift) {
    if (1 < binCountX && 1 < binCountY) {
      if (vary.categorical()) {
        return getEikosogramSelection(valx, valy, shift);
      } else {
        return getBoxplotSelection(valx, valy, shift);
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
        float pxy = logY ? logDensity[bx][by] : density[bx][by];
        float dy = pg.width * pg.height * pxy / dx;    
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
      float dx = pg.width * marginalDensity[bx];
      
      pg.noStroke();
      
      float y0 = pg.height * (1 - perc75[bx]);
      float dy = pg.height * (perc75[bx] - perc25[bx]);
      
      // Q1 - Q3 box
      pg.fill(mixColors(WHITE, BLUE, 1));
      pg.rect(x0, y0, dx, dy);
             
      pg.fill(mixColors(WHITE, BLUE, 0.5f));
      
      // Q0 - Q1 box
      y0 = pg.height * (1 - perc25[bx]);
      dy = pg.height * (perc25[bx] - perc09[bx]);
      pg.rect(x0, y0, dx, dy);
      
      // Q3 - Q4 box
      y0 = pg.height * (1 - perc91[bx]);
      dy = pg.height * (perc91[bx] - perc75[bx]);
      pg.rect(x0, y0, dx, dy);
      
      x0 += dx;  
    }    
  }
  
  public Selection getEikosogramSelection(double valx, double valy, boolean shift) {   
    float x0 = 0;
    for (int bx = 0; bx < binCountX; bx++) {
      float dx = marginalDensity[bx];
      float y0 = 1;
      for (int by = 0; by < binCountY; by++) {
        float pxy = logY ? logDensity[bx][by] : density[bx][by];
        float dy = pxy / dx;
        float x1 = x0 + dx; 
        float y1 = y0 - dy;        
        if (x0 <= valx && valx <= x1 && y1 <= valy && valy <= y0) {
          Selection sel = new Selection(x0, y1, dx, dy);
          float f = PApplet.map(by, 0, binCountY - 1, 1, 0);
          sel.setColor(mixColors(WHITE, BLUE, f));
          if (shift) {            
            float count = (density[bx][by] / dx) * sampleCounts[bx];
            sel.setLabel(PApplet.round(count) + "/" + sampleCounts[bx]);           
          } else {
            // For the percentage label we use the original density, not the logarithm
            float perc = 100 * density[bx][by] / dx;
            if (logY) sel.setLabel("log(" + PApplet.nfc(perc, 2) + "%)");
            else sel.setLabel(PApplet.nfc(perc, 2) + "%");            
          }
          return sel;
        }        
        y0 -= dy;
      }
      x0 += dx;
    }
    return null;
  }
  
  public Selection getBoxplotSelection(double valx, double valy, boolean shift) {
    float x0 = 0;
    for (int bx = 0; bx < binCountX; bx++) {
      float dx = marginalDensity[bx];
      float x1 = x0 + dx;
      
      float y0 = 1 - perc75[bx];
      float dy =  perc75[bx] - perc25[bx];          
      float y1 = y0 + dy;             
      if (x0 <= valx && valx <= x1 && y0 <= valy && valy <= y1) { // Q1 - Q3 box
        Selection sel = new Selection(x0, y0, dx, dy);
        sel.setColor(mixColors(WHITE, BLUE, 1));
        if (shift) {
          sel.setLabel(PApplet.round(0.5f * sampleCounts[bx]) + "/" + sampleCounts[bx]);
        } else {
          sel.setLabel("50%");
        }
        return sel;
      } 
      
      y0 = 1 - perc25[bx];
      dy =  perc25[bx] - perc09[bx];          
      y1 = y0 + dy;     
      if (x0 <= valx && valx <= x1 && y0 <= valy && valy <= y1) { // Q0 - Q1 box
        Selection sel = new Selection(x0, y0, dx, dy);
        sel.setColor(mixColors(WHITE, BLUE, 0.5f));
        if (shift) {
          sel.setLabel(PApplet.round(0.16f * sampleCounts[bx]) + "/" + sampleCounts[bx]);
        } else {
          sel.setLabel("16%");
        }
        return sel; 
      }
            
      y0 = 1 - perc91[bx];
      dy =  perc91[bx] - perc75[bx];          
      y1 = y0 + dy;
      if (x0 <= valx && valx <= x1 && y0 <= valy && valy <= y1) { // Q3 - Q4 box
        Selection sel = new Selection(x0, y0, dx, dy);
        sel.setColor(mixColors(WHITE, BLUE, 0.5f));
        if (shift) {
          sel.setLabel(PApplet.round(0.16f * sampleCounts[bx]) + "/" + sampleCounts[bx]);
        } else {
          sel.setLabel("16%");
        }
        return sel; 
      }
      
      x0 = x1;  
    }    
    return null;
  }  
  
  protected void calcDensity(DataSlice2D slice) {
    // Calculating number of bins ----------------------------------------------
    int[] res = BinOptimizer.calculate(slice);
    binCountX = res[0];
    binCountY = res[1];
    if (0 < binCountX && 0 < binCountY) {
      binSizeX = 1.0f / binCountX;      
      binSizeY = 1.0f / binCountY;
    }
    
    // Initializing arrays -----------------------------------------------------    
    weightSum = new double[binCountX][binCountY];
    density = new float[binCountX][binCountY];
    marginalDensity = new float[binCountX];
    sampleCounts = new int[binCountX];
    
    for (int bx = 0; bx < binCountX; bx++) {
      for (int by = 0; by < binCountY; by++) {
        weightSum[bx][by] = 0;
      }
    }
    
    for (int bx = 0; bx < binCountX; bx++) marginalDensity[bx] = 0;
    
    if (0 < binCountX && 0 < binCountY) {
      // Updating counts -------------------------------------------------------
      double totWeight = 0;
      for (Value2D value: slice.values) {
        int bx = PApplet.constrain((int)(value.x / binSizeX), 0, binCountX - 1);  
        int by = PApplet.constrain((int)(value.y / binSizeY), 0, binCountY - 1);
        weightSum[bx][by] += value.w;
        totWeight += value.w;
        sampleCounts[bx]++;
      }

      // Calculating density ---------------------------------------------------      
      for (int bx = 0; bx < binCountX; bx++) {
        for (int by = 0; by < binCountY; by++) {
          float p = (float)weightSum[bx][by] / (float)totWeight;
          density[bx][by] = p;
          marginalDensity[bx] += p;  
        }
      }
      
      // Determine if need logarithmic scaling for Y
      logY = true;
      for (int bx = 0; bx < binCountX; bx++) {        
        float px = marginalDensity[bx];
        if (0 < px) {
          boolean found = false;          
          for (int by = 0; by < binCountY; by++) {
            if (density[bx][by] / px > 0.9) {
              found = true;
              break;
            }
          }
          if (!found) logY = false;
        }
      }
      if (logY) {
        logDensity = new float[binCountX][binCountY];
        for (int bx = 0; bx < binCountX; bx++) {
          float px = marginalDensity[bx];
          if (0 < px) {
            double toty = 0;
            for (int by = 0; by < binCountY; by++) {
              double lpy1 = Math.log(1 + 100 *  density[bx][by] / px);
              toty += lpy1;
            }
            for (int by = 0; by < binCountY; by++) {
              logDensity[bx][by] = (float)(px * Math.log(1 + 100 * density[bx][by] / px) / toty);
            }            
          }
        }
      }
      
      if (vary.numerical()) calcBoxPlots(slice);
    } 
  }
  
  protected void calcBoxPlots(DataSlice2D slice) {
    perc09 = new float[binCountX];
    perc25 = new float[binCountX];
    perc75 = new float[binCountX];
    perc91 = new float[binCountX];
    
    @SuppressWarnings("unchecked")
    ArrayList<Value2D>[] values = new ArrayList[binCountX];
    for (int i = 0; i < binCountX; i++) {
      values[i] = new ArrayList<Value2D>(); 
    }
    
    for (Value2D value: slice.values) {
      int bx = PApplet.constrain((int)(value.x / binSizeX), 0, binCountX - 1);
      values[bx].add(value);
    }
    
    for (int bx = 0; bx < binCountX; bx++) {
      ArrayList<Value2D> vbx = values[bx];
      if (vbx.size() == 0) continue;
      Collections.sort(vbx, new ComparatorY());
      double[] sum = new double[vbx.size()];
      double[] prob = new double[vbx.size()];
      double sum0 = 0;
      for (int i = 0; i < sum.length; i++) {
        sum[i] = sum0 + vbx.get(i).w;
        sum0 = sum[i];
      }
      double F = 100 / sum[sum.length - 1];
      for (int i = 0; i < sum.length; i++) {
        prob[i] = F * (sum[i] - 0.5f * vbx.get(i).w);
      }

      perc09[bx] = findPercentile(vbx, prob, 9);
      perc25[bx] = findPercentile(vbx, prob, 25);
      perc75[bx] = findPercentile(vbx, prob, 75);
      perc91[bx] = findPercentile(vbx, prob, 91);
    }
  }
  
  // Returns the value realizing the weighted percentile P, using the Linear 
  // Interpolation Between Closest Ranks method:
  // http://en.wikipedia.org/wiki/Percentile#The_Linear_Interpolation_Between_Closest_Ranks_method
  // http://en.wikipedia.org/wiki/Percentile#The_Weighted_Percentile_method
  protected static float findPercentile(ArrayList<Value2D> vals, 
                                        double[] prob, double P) {
    int len = prob.length;
    double p0 = 0;
    double v0 = 0;
    for (int i = 0; i < len; i++) {
      double p = prob[i];
      double v = vals.get(i).y;
      if (Numbers.equal(P, p)) return (float)v;
      if (p0 < P && P < p) {
        return (float)(v0 + len * (v - v0) * (P - p0) / 100);
      }      
      p0 = p;
      v0 = v;
    }
    return (float)vals.get(len - 1).y;
  }
  
  // Sorts in ascending order using the y value
  protected static class ComparatorY implements Comparator<Value2D> {
    public int compare(Value2D v1, Value2D v2) {
      if (Numbers.equal(v1.y, v2.y)) return 0;
      else return (int)((v1.y - v2.y) / Math.abs(v1.y - v2.y));
    }
  }  
}

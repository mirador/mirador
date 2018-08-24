package miralib.utils;

import miralib.data.Variable;
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Timer {
  protected Project project;
  protected int maxPlotSliceSizeCC;
  protected int maxPlotSliceSizeNN;
  protected int maxPlotSliceSizeCN;
  protected int clockUpdateCC = 0;
  protected int clockUpdateNN = 0;
  protected int clockUpdateCN = 0;
  protected int targetClockSize = 5;
  protected List plotTimesCC = Collections.synchronizedList(new ArrayList<Integer>());
  protected List plotTimesNN = Collections.synchronizedList(new ArrayList<Integer>());
  protected List plotTimesCN = Collections.synchronizedList(new ArrayList<Integer>());

  public Timer(Project prj) {
    project = prj;
    maxPlotSliceSizeCC = project.initSliceSize;
    maxPlotSliceSizeNN = project.initSliceSize;
    maxPlotSliceSizeCN = project.initSliceSize;
  }

  public int getPlotSliceSize(Variable v1, Variable v2) {
    if (v1.string() || v2.string()) return 0;
    if (v1.categorical() && v2.categorical()) {
      return maxPlotSliceSizeCC;
    } else if (v1.numerical() && v2.numerical()) {
      return maxPlotSliceSizeNN;
    } else {
      return maxPlotSliceSizeCN;
    }
  }

  public void clockPlotTime(int time, Variable v1, Variable v2) {
    if (v1.string() || v2.string()) return;
    if (v1.categorical() && v2.categorical()) {
      calcPlotTimeCC(time);
    } else if (v1.numerical() && v2.numerical()) {
      calcPlotTimeNN(time);
    } else {
      calcPlotTimeCN(time);
    }
  }

  private void calcPlotTimeCC(int time) {
    plotTimesCC.add(time);
    if (targetClockSize < plotTimesCC.size()) {
      plotTimesCC.remove(0);
    }
    clockUpdateCC++;
    if (clockUpdateCC == targetClockSize) {
      float meanTime = 0;
      synchronized (plotTimesCC) {
        Iterator i = plotTimesCC.iterator(); // Must be in synchronized block
        while (i.hasNext()) {
          int value = (Integer)i.next();
          meanTime += value;
        }
      }
      meanTime /= plotTimesCC.size();
      if (meanTime < 0.5 * project.maxPlotTime || project.maxPlotTime < meanTime) {
        float f = PApplet.min(PApplet.sqrt(project.maxPlotTime / meanTime), 2);
        maxPlotSliceSizeCC = PApplet.max((int)(maxPlotSliceSizeCC * f), 1000);
      }
      clockUpdateCC = 0;
    }
  }

  private void calcPlotTimeNN(int time) {
    plotTimesNN.add(time);
    if (targetClockSize < plotTimesNN.size()) {
      plotTimesNN.remove(0);
    }
    clockUpdateNN++;
    if (clockUpdateNN == targetClockSize) {
      float meanTime = 0;
      synchronized (plotTimesNN) {
        Iterator i = plotTimesNN.iterator(); // Must be in synchronized block
        while (i.hasNext()) {
          int value = (Integer)i.next();
          meanTime += value;
        }
      }
      meanTime /= plotTimesNN.size();
      if (meanTime < 0.5 * project.maxPlotTime || project.maxPlotTime < meanTime) {
        float f = PApplet.min(PApplet.sqrt(project.maxPlotTime / meanTime), 2);
        maxPlotSliceSizeNN = PApplet.max((int)(maxPlotSliceSizeNN * f), 1000);
      }
      clockUpdateNN = 0;
    }
  }

  private void calcPlotTimeCN(int time) {
    plotTimesCN.add(time);
    if (targetClockSize < plotTimesCN.size()) {
      plotTimesCN.remove(0);
    }
    clockUpdateCN++;
    if (clockUpdateCN == targetClockSize) {
      float meanTime = 0;
      synchronized (plotTimesCN) {
        Iterator i = plotTimesCN.iterator(); // Must be in synchronized block
        while (i.hasNext()) {
          int value = (Integer)i.next();
          meanTime += value;
        }
      }
      meanTime /= plotTimesCN.size();
      if (meanTime < 0.5 * project.maxPlotTime || project.maxPlotTime < meanTime) {
        float f = PApplet.min(PApplet.sqrt(project.maxPlotTime / meanTime), 2);
        maxPlotSliceSizeCN = PApplet.max((int)(maxPlotSliceSizeCN * f), 1000);
      }
      clockUpdateCN = 0;
    }
  }
}

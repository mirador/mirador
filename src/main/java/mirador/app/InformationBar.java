/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import processing.core.PFont;
import processing.core.PShape;
import mui.Display;
import mui.Interface;
import mui.SoftFloat;
import miralib.utils.Log;

/**
 * Sublcass of ColumnScoller specialized to handle an horizontal scroll of 
 * covariate boxes.
 *
 */

public class InformationBar extends MiraWidget {
  // TODO: Make into CCS size parameters
  int rxpad = Display.scale(5);
  int rxinc = Display.scale(25);
  int iconw = Display.scale(13);
  int iconh = Display.scale(13);
  int iconx = Display.scale(12);
  int icony = Display.scale(7);
  int selh = Display.scale(25);
  int selx = Display.scale(10);
  
  protected int totalCount, currentCount;
  protected CountTask countTask;
  protected int bColor;
  protected PFont pFont;
  protected int pColor;
  protected PShape rIcon;
  protected int rColor;  
  protected SoftFloat rx;
  protected SoftFloat ia;
  protected boolean prevCount;
  protected boolean requestedRangeReset;
  
  public InformationBar(Interface intf, float x, float y, float w, float h) {
    super(intf, x, y, w, h);
  }
  
  public void setup() {
    totalCount = currentCount = data.getRowCount();
    bColor = getStyleColor("InfoBar", "background-color");
    
    pFont = getStyleFont("InfoBar.ResetData.p", "font-family", "font-size");
    pColor = getStyleColor("InfoBar.ResetData.p", "color");
    
    rIcon = getStyleShape("InfoBar.ResetData", "icon-image");
    rColor = getStyleColor("InfoBar.ResetData", "background-color");
    
    rx = new SoftFloat();
    ia = new SoftFloat();
    prevCount = false;
  }
  
  public void update() {
    rx.update();
    
    if (requestedRangeReset) {
      mira.resetRanges();
      requestedRangeReset = false;
    }
    
    if (currentCount < totalCount && !prevCount) {
      ia.set(0);
      ia.setTarget(255);
    }
    ia.update();
    prevCount = currentCount < totalCount;
  }
  
  public void draw() {
    noStroke();
    fill(bColor);
    rect(0, 0, width - padding, height);
        
    textFont(pFont);
    String label = currentCount + " of " + totalCount + " data points selected";
    float tw = textWidth(label);
    
    float w0 = rxpad + tw + rxpad;
    if (currentCount < totalCount) w0 += rxinc;
    if (0 < rx.get()) {
      rx.setTarget(width - w0 - rxpad*2);  
    } else {
      rx.set(width - w0 - rxpad*2);
    }
        
    fill(rColor);
    
    if (currentCount < totalCount) {
      tint(color(255), ia.getCeil());
      shape(rIcon, tw + iconx, height/2 - icony, iconw, iconh);
      tint(color(255));
    }    
    
    fill(pColor);
    float yc = (height - pFont.getSize()) / 2;
    text(label, 10, height - yc);
  }
  
  public void mouseReleased() {
    float rw = width - selx - rx.get();
    if (rx.get() <= mouseX && mouseX <= rx.get() + rw && 
        height/2 - selh <= mouseY && mouseY <= height/2 + selh) {    
      requestedRangeReset = true;      
    }    
  }  
  
  public void dataChanged() {
    if (countTask != null && countTask.isAlive()) {
      Log.message("Suspending count calculation...");
      countTask.interrupt();
//      while (countTask.isAlive()) {
//        Thread.yield();
//      }
      Log.message("Done.");
    }  
    countTask = new CountTask();
    countTask.start();
  }
  
  protected class CountTask extends Thread {    
    @Override
    public void run() {
      currentCount = data.getRowCount(mira.ranges);      
    }
  }
}

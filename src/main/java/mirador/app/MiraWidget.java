/* COPYRIGHT (C) 2014-16 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import mui.Interface;
import mui.Widget;
import miralib.data.DataSet;

/**
 * Mirador widget, which add some extra functionality specific to Mirador, 
 * mainly indexing and timeout, in addition to references to the data and main
 * app object.
 *
 */

public class MiraWidget extends Widget {
  final public static int SNAP_THRESHOLD = 30;
  final public static int SHOW_COL_DELAY = 1000;
  final public static int REMOVE_COL_DELAY = 2000;
  final public static int REMOVE_ROW_DELAY = 2000;
  
  final protected static float padding = 2;
  
  protected MiraApp mira;
  protected DataSet data;
  
  protected int idx;
  protected long t0;
  protected boolean pvisible;
  protected boolean timedOut;
  protected long timeOut;  
  
  public MiraWidget(Interface intf) {
    super(intf);
    init(); 
  }
  
  public MiraWidget(Interface intf, float x, float y, float w, float h) {
    super(intf, x, y, w, h);
    init();   
  }
  
  public MiraWidget(Interface intf, float x, float y, float w, float h, 
                   boolean tw, boolean th) {
    super(intf, x, y, w, h, tw, th);
    init();   
  }

  public MiraWidget(Interface intf, float x0, float y0, float x1, float y1, 
                   float w0, float h0, float w1, float h1) {
    super(intf, x0, y0, x1, y1, w0, h1, w1, h1);
    init();  
  }
  
  public void setIndex(int i) {
    this.idx = i;
  }

  public int getIndex() { return idx; }
  
  public void setTimeOut(long t) {
    timeOut = t;
  }
  
  public boolean timedOut() {
    return timedOut;
  }
  
  public boolean visible() {
    boolean vis = super.visible();
    
    if (0 < timeOut) {
      long t = mira.millis();
      if (vis) {        
        if (!pvisible) {
          t0 = t;
          timedOut = false;
        }
      } else {
        if (pvisible) {
          t0 = t;
        }
        timedOut = t - t0 > timeOut;
      }        
    }
    
    pvisible = vis;      
    return vis;
  }
  
  public void mouseReleased(MiraWidget wt) { }
  public void keyPressed(MiraWidget wt) { }
  public void dragColumns(float dx) { }
  public void dragRows(float dy) { }
  
  protected void init() {
    mira = (MiraApp)intf.app;
    data = mira.dataset;
    
    t0 = mira.millis();
    pvisible = false;
    timedOut = false;
    timeOut = 0;
  }
}

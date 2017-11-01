/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import mui.Display;
import mui.Interface;
import mui.SoftFloat;
import processing.core.PApplet;

public class HorizontalScrollbar extends MiraWidget {
  private int minHandleWidth = Display.scale(50);

  protected ColumnScroller col;
  protected boolean insideHandle;
  protected SoftFloat handlex;
  protected float handlew;
  protected boolean dragging;

  protected int hColor, bColor;

  public HorizontalScrollbar(Interface intf, ColumnScroller col, float x, float y, float w, float h) {
    super(intf, x, y, w, h);
    this.col = col;
    handlex = new SoftFloat();
  }


  public void setup() {
    hColor = getStyleColor("Scrollbar", "color");
    bColor = getStyleColor("Scrollbar", "background");
    initHandle(width);
  }


  public void update() {
    handlex.update();
    initWidth();
  }


  public void draw() {
    noStroke();
    fill(bColor);
    rect(0, 0, width, height);
    fill(hColor);
    rect(handlex.get(), 0, handlew, height);
  }


  public void mousePressed() {
    insideHandle = false;
    if (handlex.get() <= mouseX && mouseX <= handlex.get() + handlew) {
      insideHandle = true;
    }
  }


  public void mouseDragged() {
    float dx = mouseX - pmouseX;
    if (insideHandle) {
      float x0 = PApplet.constrain(handlex.getTarget() + dx, 0, width - handlew);
      int tot = col.getTotItemsCount() - 1;
      int idx = PApplet.round(PApplet.map(x0, 0, width - handlew, 0, tot));
      mira.browser.openColumn(idx);
      handlex.setTarget(x0);
    }
    dragging = true;
  }


  public void mouseReleased() {
    if (dragging) {
      mira.browser.snapColumns();
    } else {
      float x = handlex.get();
      if (x <= mouseX && mouseX <= x + handlew) {
        int idx = col.getFirstItemIndex();
        if (x + 0.5 * handlew < mouseX && idx < col.getTotItemsCount() - 1) {
          // one step to the right
          scrollTo(idx++);
          mira.browser.openColumn(idx);
        } else if (0 < idx) {
          // one step to the left
          scrollTo(idx--);
          mira.browser.openColumn(idx);
        }

      } else if (0 <= mouseX && mouseX <= width) {
        float x1 = PApplet.constrain(mouseX, 0, width - handlew);
        int tot = col.getTotItemsCount() - 1;
        int idx = PApplet.round(PApplet.map(x1, 0, width - handlew, 0, tot));
        mira.browser.openColumn(idx);
        handlex.setTarget(x1);
      }
    }
    dragging = false;
  }


  public void scrollToFirst() {
    int idx = col.getFirstItemIndex();
    scrollTo(idx);
  }

  public void scrollTo(int idx) {
    int tot = col.getTotItemsCount() - 1;
    float x1 = PApplet.map(idx, 0, tot, 0, width - handlew);
    handlex.setTarget(x1);
  }


  protected void handleResize(int newWidth, int newHeight) {
    float w1 = newWidth - mira.optWidth - mira.varWidth - mira.browser.scrollSize - padding;
    bounds.w.set(w1);
    initHandle(w1);
  }


  private void initHandle(float w) {
    initWidth();
    int tot = col.getTotItemsCount() - 1;
    int idx = col.getFirstItemIndex();
    float x0 = PApplet.map(idx, 0, tot, 0, w - handlew);
    handlex.setTarget(x0);
  }


  private void initWidth() {
    float w = (width * width) / col.getTotalWidth();
    handlew = PApplet.constrain(w, minHandleWidth, width/2);
  }
}

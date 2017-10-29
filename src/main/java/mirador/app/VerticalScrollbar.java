/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import mui.Display;
import mui.Interface;
import mui.SoftFloat;
import processing.core.PApplet;

public class VerticalScrollbar extends MiraWidget {
  private int minHandleHeight = Display.scale(50);

  protected RowBrowser row;
  protected boolean insideHandle;
  protected SoftFloat handley;
  protected boolean dragging;
  protected float handleh;
  protected int hColor, bColor;

  public VerticalScrollbar(Interface intf, RowBrowser row, float x, float y, float w, float h) {
    super(intf, x, y, w, h);
    this.row = row;
    handley = new SoftFloat();
  }


  public void setup() {
    hColor = getStyleColor("Scrollbar", "color");
    bColor = getStyleColor("Scrollbar", "background");
    initHandle(height);
  }


  public void update() {
    handley.update();
    initHeight();
  }


  public void draw() {
    noStroke();
    fill(bColor);
    rect(0, 0, width, height);
    fill(hColor);
    rect(0, handley.get(), width, handleh);
  }


  public void mousePressed() {
    insideHandle = false;
    if (handley.get() <= mouseY && mouseY <= handley.get() + handleh) {
      insideHandle = true;
    }
  }


  public void mouseDragged() {
    float dy = mouseY - pmouseY;
    if (insideHandle) {
      float y1 = PApplet.constrain(handley.getTarget() + dy, 0, height - handleh);
      int tot = row.getTotItemsCount() - 1;
      int n = PApplet.round(PApplet.map(y1, 0, height - handleh, 0, tot));
      RowScroller scroller = row.getScroller();
      scroller.jumpTo(n);
      handley.setTarget(y1);
    }
    dragging = true;
  }


  public void mouseReleased() {
    if (dragging) {
      RowScroller scroller = row.getScroller();
      scroller.snap();
    } else {
      float y = handley.get();
      if (y <= mouseY && mouseY <= y + handleh) {
        RowScroller scroller = row.getScroller();
        int idx = scroller.getFirstIndex();
        if (y + 0.5 * handleh < mouseY && idx < row.getTotItemsCount() - 1) {
          // one step down
          scroller.down();
          scrollTo(idx++);
        } else if (0 < idx) {
          // one step up
          scroller.up();
          scrollTo(idx--);
        }
      } else if (0 <= mouseY && mouseY <= height) {
        float y1 = PApplet.constrain(mouseY, 0, height - handleh);
        int tot = row.getTotItemsCount() - 1;
        int n = PApplet.round(PApplet.map(y1, 0, height - handleh, 0, tot - 1));
        RowScroller scroller = row.getScroller();
        scroller.jumpTo(n);
        handley.setTarget(y1);
      }
    }
    dragging = false;
  }


  public void scrollToFirst() {
    int idx = row.getFirstItemIndex();
    scrollTo(idx);
  }


  public void scrollTo(int idx) {
    int tot = row.getTotItemsCount() - 1;
    float y0 = PApplet.map(idx, 0, tot, 0, height - handleh);
    handley.setTarget(y0);
  }


  protected void handleResize(int newWidth, int newHeight) {
    float s = row.showingVariables() ? mira.browser.scrollSize : 0;
    float h1 = newHeight - mira.labelHeightClose - 2 * padding - s;
    bounds.h.set(h1);
    initHandle(h1);
  }


  private void initHandle(float h) {
    initHeight();
    int tot = row.getTotItemsCount() - 1;
    int idx = row.getFirstItemIndex();
    float y0 = PApplet.map(idx, 0, tot, 0, h - handleh);
    handley.setTarget(y0);
  }


  private void initHeight() {
    RowScroller scroller = row.getScroller();
    float h = (height * height) / scroller.getApproxTotalHeight();
    handleh = PApplet.constrain(h, minHandleHeight, height/2);
  }
}

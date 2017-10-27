package mirador.app;

import mui.Interface;
import mui.SoftFloat;
import processing.core.PApplet;

public class HorizontalScrollbar extends MiraWidget {
  protected ColumnScroller col;
  protected boolean insideDragBox;
  protected SoftFloat dragBox0, dragBox1;
  protected boolean dragging;

  public HorizontalScrollbar(Interface intf, ColumnScroller col, float x, float y, float w, float h) {
    super(intf, x, y, w, h);
    this.col = col;

    dragBox0 = new SoftFloat();
    dragBox1 = new SoftFloat();
  }

  public void update() {
    int tot = col.getTotItemsCount();
    int vis = col.getVisItemsCount();
    int first = col.getFirstItemIndex();
    System.out.println(tot + " " + vis + "  " + first);
    float x0 = PApplet.map(first, 0, tot, 0, width);
    float len = PApplet.map(vis, 0, tot, 0, width);
    dragBox0.setTarget(x0);
    dragBox1.setTarget(x0 + len);

    dragBox0.update();
    dragBox1.update();
  }

  public void draw() {
    noStroke();




    fill(color(150, 100));
    rect(0, 0, width, height);
    fill(color(255, 0, 0));
    rect(dragBox0.get(), 0, dragBox1.get() - dragBox0.get(), height);
  }

  public void mousePressed() {
    insideDragBox = false;
    if (dragBox0.get() <= mouseX && mouseX <= dragBox1.get()) {
      insideDragBox = true;
    }
  }


  public void mouseDragged() {
    float dx = pmouseX - mouseX;
    if (insideDragBox) {
      int tot = col.getTotItemsCount();
      int vis = col.getVisItemsCount();
      float f = (float)tot / (float)vis;
      mira.browser.dragColumns(-f * dx);
    } else {

    }
    dragging = true;
    System.out.println("dragging");
  }


  public void mouseReleased() {
    if (dragging) {
      mira.browser.snapColumns();
    } else {
//      float f = mouse() / length();
//      float totLength = lengthSum[open1];
//      int i = getCloserIndex(f * totLength);
//      jumpTo(i);
    }
    dragging = false;
  }


  protected void handleResize(int newWidth, int newHeight) {
    float w0 = bounds.w.get();
    float w1 = newWidth - mira.optWidth - mira.varWidth;
    float dw = w1 - w0;
    bounds.w.set(w1);
  }
}

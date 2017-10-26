package mirador.app;

import mui.Interface;
import mui.SoftFloat;
import processing.core.PApplet;

public class VerticalScrollbar extends MiraWidget {
  protected RowBrowser row;
  protected boolean insideDragBox;
  protected SoftFloat dragBox0, dragBox1;
  protected boolean dragging;

  public VerticalScrollbar(Interface intf, RowBrowser row, float x, float y, float w, float h) {
    super(intf, x, y, w, h);
    this.row = row;

    dragBox0 = new SoftFloat();
    dragBox1 = new SoftFloat();
  }

  public void update() {
    int tot = row.getTotItemsCount();
    int vis = row.getVisItemsCount();
    int first = row.getFirstItemIndex();
//    System.out.println(tot + " " + vis + "  " + first);
    float y0 = PApplet.map(first, 0, tot, 0, height);
    float len = PApplet.map(vis, 0, tot, 0, height);
    dragBox0.setTarget(y0);
    dragBox1.setTarget(y0 + len);

    dragBox0.update();
    dragBox1.update();
  }

  public void draw() {
    noStroke();




    fill(color(150, 100));
    rect(0, 0, width, height);
    fill(color(255, 0, 0));
    rect(0, dragBox0.get(), width, dragBox1.get() - dragBox0.get());
  }

  public void mousePressed() {
    insideDragBox = false;
    if (dragBox0.get() <= mouseY && mouseY <= dragBox1.get()) {
      insideDragBox = true;
    }
  }


  public void mouseDragged() {
    float dy = pmouseY - mouseY;
    if (insideDragBox) {
      int tot = row.getTotItemsCount();
      int vis = row.getVisItemsCount();
      float f = (float)tot / (float)vis;
      RowScroller scroller = row.getScroller();
      scroller.fit(-f * dy);
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
    bounds.h.set(newHeight - mira.labelHeightClose);
  }
}

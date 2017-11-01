/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import mirador.handlers.ScrollbarHandler;
import mui.Display;
import mui.Interface;
import mui.SoftFloat;
import processing.core.PApplet;

public class VerticalScrollbar extends MiraWidget {
  private int minHandleHeight = Display.scale(50);

  protected ScrollbarHandler handler;
  protected boolean insideSlider;
  protected SoftFloat slidery;
  protected float sliderh;
  protected boolean dragging;

  protected int hColor, bColor;


  public VerticalScrollbar(Interface intf, ScrollbarHandler handler, float x, float y, float w, float h) {
    super(intf, x, y, w, h);
    this.handler = handler;
    slidery = new SoftFloat();
  }


  public void setup() {
    hColor = getStyleColor("Scrollbar", "color");
    bColor = getStyleColor("Scrollbar", "background");
    initHandle(height);
  }


  public void update() {
    slidery.update();
    initHeight();
  }


  public void draw() {
    noStroke();
    fill(bColor);
    rect(0, 0, width, height);
    fill(hColor);
    rect(0, slidery.get(), width, sliderh);
  }


  public void mousePressed() {
    insideSlider = false;
    if (slidery.get() <= mouseY && mouseY <= slidery.get() + sliderh) {
      insideSlider = true;
    }
  }


  public void mouseDragged() {
    float dy = mouseY - pmouseY;
    if (insideSlider) {
      float y1 = handler.drag(slidery.getTarget() + dy, height - sliderh);
      slidery.setTarget(y1);
    }
    dragging = true;
  }


  public void mouseReleased() {
    if (dragging) {
      handler.stopDrag();
    } else {
      float y = slidery.get();
      if (y <= mouseY && mouseY <= y + sliderh) {
        int idx = handler.pressSlider(mouseY, sliderh);
        scrollTo(idx);
      } else if (0 <= mouseY && mouseY <= height) {
        float y1 = handler.press(mouseY, height - sliderh);
        slidery.setTarget(y1);
      }
    }
    dragging = false;
  }


  public void scrollToFirst() {
    idx = handler.currentItem();
    scrollTo(idx);
  }


  public void scrollTo(int idx) {
    float y1 = handler.itemPosition(idx, height - sliderh);
    slidery.setTarget(y1);
  }


  protected void handleResize(int newWidth, int newHeight) {
    float h1 = handler.resize(newHeight);
    bounds.h.set(h1);
    initHandle(h1);
  }


  private void initHeight() {
    float h = (height * height) / handler.totalSize();
    sliderh = PApplet.constrain(h, minHandleHeight, height/2);
  }


  private void initHandle(float h) {
    initHeight();
    float y0 = handler.initPosition(h - sliderh);
    slidery.setTarget(y0);
  }
}

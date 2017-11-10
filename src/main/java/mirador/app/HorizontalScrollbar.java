/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import mui.Display;
import mui.Interface;
import mui.SoftFloat;
import mirador.handlers.ScrollbarHandler;
import processing.core.PApplet;

public class HorizontalScrollbar extends MiraWidget {
  private int minSliderWidth = Display.scale(50);

  protected ScrollbarHandler handler;
  protected boolean insideSlider;
  protected SoftFloat sliderx;
  protected float sliderw;
  protected boolean dragging;

  protected int hColor, bColor;


  public HorizontalScrollbar(Interface intf, ScrollbarHandler handler, float x, float y, float w, float h) {
    super(intf, x, y, w, h);
    this.handler = handler;
    sliderx = new SoftFloat();
  }


  public void setup() {
    hColor = getStyleColor("Scrollbar", "color");
    bColor = getStyleColor("Scrollbar", "background");
    initHandle(width);
  }


  public void update() {
    sliderx.update();
    initWidth();
  }


  public void draw() {
    noStroke();
    fill(bColor);
    rect(0, 0, width, height);
    fill(hColor);
    rect(sliderx.get(), 0, sliderw, height);
  }


  public void mousePressed() {
    insideSlider = false;
    if (sliderx.get() <= mouseX && mouseX <= sliderx.get() + sliderw) {
      insideSlider = true;
    }
  }


  public void mouseDragged() {
    float dx = mouseX - pmouseX;
    if (insideSlider) {
      float x1 = handler.drag(sliderx.getTarget() + dx, width - sliderw);
      sliderx.setTarget(x1);
    }
    dragging = true;
  }


  public void mouseReleased() {
    if (dragging) {
      handler.stopDrag();
    } else {
      float x = sliderx.get();
      if (x <= mouseX && mouseX <= x + sliderw) {
        int idx = handler.pressSlider(mouseX, sliderw);
        scrollTo(idx);
      } else if (0 <= mouseX && mouseX <= width) {
        float x1 = handler.press(mouseX, width - sliderw);
        sliderx.setTarget(x1);
      }
    }
    dragging = false;
  }


  public void scrollToFirst() {
    idx = handler.currentItem();
    scrollTo(idx);
  }


  public void scrollTo(int idx) {
    float x1 = handler.itemPosition(idx, width - sliderw);
    sliderx.setTarget(x1);
  }


  protected void handleResize(int newWidth, int newHeight) {
    float w1 = handler.resize(newWidth);
    bounds.w.set(w1);
    initHandle(w1);
  }


  private void initWidth() {
    float w = (width * width) / handler.totalSize();
    sliderw = PApplet.constrain(w, minSliderWidth, width/2);
  }


  private void initHandle(float w) {
    initWidth();
    float x0 = handler.initPosition(w - sliderw);
    sliderx.setTarget(x0);
  }
}

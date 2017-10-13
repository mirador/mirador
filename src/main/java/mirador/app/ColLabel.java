package mirador.app;

import miralib.data.Variable;
import mui.Display;
import mui.Interface;
import mui.SoftFloat;
import processing.core.PApplet;
import processing.core.PFont;

public class ColLabel extends MiraWidget {
  // TODO: Make into CCS size parameters
  int crossw = Display.scale(10);
  int marginx = Display.scale(10);
  int marginy = Display.scale(7);
  int posy = Display.scale(55);
  int numh = Display.scale(40);
  int dismissy = Display.scale(10);
  int labelh = Display.scale(35);
  int breakx = Display.scale(5);
  int breaky = Display.scale(34);
  int breakh = Display.scale(40);

  boolean open;
  long t0;
  boolean ready;
  RangeSelector selector;

  protected boolean axisMode;
  protected SoftFloat axisAlpha;

  protected float labelHeightMax;
  protected int bColor;
  protected PFont hFont;
  protected int hColor;
  protected float hLead;
  protected PFont pFont;
  protected int pColor;

  protected Variable colVar;
  float height0;

  public ColLabel(Interface intf, float x, float y, float w, float h, float ihmax,
                  Variable cvar, boolean anim) {
    super(intf, x, y, w, h);
    height0 = h;
    labelHeightMax = ihmax;
    open = false;

    colVar = cvar;

    if (cvar.numerical()) {
      selector = new NumericalRangeSelector(intf, marginx + x, posy, w - marginx*2, numh, cvar);
    } else if (cvar.categorical()) {
      selector = new CategoricalRangeSelector(intf, marginx + x, posy, w - marginx*2, h - posy, cvar);
    }

//    selector.setOffset(visX0);
    selector.hide(false);
    if (anim) selector.targetX(marginx + x);
    addChild(selector, TOP_LEFT_CORNER);
    t0 = millis();
    ready = false;

    axisMode = false;
    axisAlpha = new SoftFloat(0);
  }

  public void setup() {
    bColor = getStyleColor("ColVarBox", "background-color");
    hFont = getStyleFont("ColVarBox.h2", "font-family", "font-size");
    hColor = getStyleColor("ColVarBox.h2", "color");
    hLead = getStyleSize("ColVarBox.h2", "line-height");
    pFont = getStyleFont("p", "font-family", "font-size");
    pColor = color(255);

    selector.setBackgroundColor(bColor);
  }

  public void update() {
    super.update();

    if (!ready && 500 < millis() - t0) {
      selector.show(true);
      ready = true;
    }

    if (mira.browser.getColAxis() == colVar && !axisMode) {
      axisMode = true;
      axisAlpha.setTarget(230);
    }
    if (mira.browser.getColAxis() != colVar && axisMode) {
      axisMode = false;
      axisAlpha.set(0);
    }
    axisAlpha.update();
  }

  public void draw() {
    noStroke();
    fill(bColor);
    rect(padding, 0, width - 2 * padding, height);
    if (ready) drawDismiss();
  }

  public void postDraw() {
    float x0 = 0;
    float y0 = 0;

    String label;
    float w1 = width - 2 * padding;
    float h1 = height;
    if (axisMode) {
      noStroke();
      fill(color(187, 185, 179), axisAlpha.getCeil());
      rect(x0 + padding, y0, w1, h1);
    }

    fill(hColor);
    textFont(hFont);
    textLeading(hLead);
    label = colVar.getName();
    String alias = colVar.getAlias();
    if (!label.equals(alias)) label += ": " + alias;
    text(label, x0 + marginx, y0 + marginy, width - padding - crossw - marginx*2, labelh);

    if (axisMode) {
      fill(pColor);
      textFont(hFont);
      textLeading(hLead);
      label = mira.browser.getColLabel();
      float vw = textWidth(label);
      if (vw <= w1) {
        float x1 = mouseX - vw/2;
        if (x1 < x0 + padding) x1 = x0 + padding;
        if (x0 + w1 < x1 + vw) x1 = x0 + w1 - vw;
        text(label, x1, y0 + h1 - marginy);
      } else {
        // This value string is too long, breaking it into two lines.
        text(label, x0 + breakx, y0 + h1 - breaky, w1 - breakx, breakh);
      }
    }
  }

  void drawDismiss() {
    float x0 = width - padding - crossw - marginx;
    float y0 = dismissy;

    stroke(hColor, 100);
    strokeWeight(1);
    line(x0, y0, x0 + crossw, y0 + crossw);
    line(x0, y0 + crossw, x0 + crossw, y0);
  }

  boolean insideDismiss(float mx, float my) {
    float x0 = width - padding - crossw - marginx;
    float y0 = dismissy;
    return x0 <= mx && mx <= x0 + crossw && y0 <= my && my <= y0 + crossw;
  }

  public void mouseReleased() {
    if (insideDismiss(mouseX, mouseY)) {
      if (keyPressed(SHIFT)) {
        mira.browser.closeColumnsBut(colVar);
      } else {
        mira.browser.closeColumn(colVar);
      }
    } else {
      float x1 = selector.top() + selector.getFullHeight();
      if (height0 < x1) {
        open = !open;
        if (open) {
          float h1 = PApplet.min(labelHeightMax, x1);
          bounds.h.set(h1);
          selector.targetHeight(h1 - selector.top());
        } else {
          bounds.h.set(height0);
          selector.targetHeight(height0 - posy);
        }
      }
    }
  }

  public Variable getVariable() {
    return colVar;
  }
}

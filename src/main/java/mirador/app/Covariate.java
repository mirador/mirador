package mirador.app;

import miralib.data.Variable;
import mui.Display;
import mui.Interface;
import mui.SoftFloat;
import processing.core.PApplet;
import processing.core.PFont;

public class Covariate extends MiraWidget {
  // TODO: Make into CCS size parameters
  int marginx = Display.scale(10);
  int posy = Display.scale(5);
  float selh = Display.scale(50);
  float crossw = Display.scale(10);
  float htexty = Display.scale(6);
  float ptexty = Display.scale(4);

  protected int bColor;
  protected PFont hFont;
  protected PFont pFont;
  protected int hColor;
  protected int pColor;
  protected int dColor;
  protected int oColor;

  Variable covVar;
  boolean open;
  RangeSelector selector;
  SoftFloat maxAlpha;
  protected float covarHeightMax;

  Covariate(Interface intf, float x, float y, float w, float h, float ihmax, Variable var, boolean anim) {
    super(intf, x, y, w, h);
    maxAlpha = new SoftFloat(255);
    open = false;
    covVar = var;
    covarHeightMax = ihmax;

    if (var.numerical()) {
      selector = new NumericalRangeSelector(intf, marginx + x, h + posy, w - marginx*2, selh, var);
    } else if (var.categorical()) {
      selector = new CategoricalRangeSelector(intf, marginx + x, h + posy, w - marginx*2, selh, var);
    }
    selector.setBackgroundColor(oColor);
//    selector.setOffset(visX0);
    if (anim) selector.targetX(x + marginx*2);
    addChild(selector, TOP_LEFT_CORNER);
  }

  public void setup() {
    bColor = getStyleColor("CovVarBox", "background-color");
    hFont = getStyleFont("CovVarBox.h2", "font-family", "font-size");
    hColor = getStyleColor("CovVarBox.h2", "color");
    pFont = getStyleFont("CovVarBox.p", "font-family", "font-size");
    pColor = getStyleColor("CovVarBox.p", "color");
    dColor = getStyleColor("CovVarBox.Dismiss", "color");
    oColor = getStyleColor("CovVarBox.open", "background-color");
  }

  public void update() {
    super.update();
//    selector.targetX(x.getTarget() + marginx);
    maxAlpha.update();
  }

  public void draw() {
    float x0 = 0;
    float y0 = 0;
    noStroke();
//    if (itemHeight < h.get()) {
      fill(oColor, maxAlpha.getCeil());
      rect(x0 + padding, y0, width - 2 * padding, height);

      fill(bColor, maxAlpha.getCeil());
      rect(x0 + padding, y0, width - 2 * padding, height);
//    } else {
//      fill(bColor, maxAlpha.getCeil());
//      rect(x0 + padding, y0, w - 2 * padding, h.get());
//    }

    textFont(hFont);
    fill(hColor, maxAlpha.getCeil());
    String label = covVar.getName();
    String alias = covVar.getAlias();
    if (!label.equals(alias)) label += ": " + alias;
    label = chopStringRight(label, width - padding - crossw - marginx*2);
    text(label, x0 + marginx, y0 + hFont.getSize() + htexty);

    textFont(pFont);
    String range = covVar.formatRange(mira.ranges.get(covVar));
    range = chopStringRight(range, width - padding - crossw - 20);
    text(range, x0 + marginx, y0 + textLeading() + hFont.getSize() + ptexty);

    drawDismiss();
  }

  public void mouseReleased() {
    if (insideDismiss(mouseX, mouseY)) {
      data.removeCovariate(covVar);
    } else {
      open = !open;
      if (open) {
        float h1 = PApplet.min(covarHeightMax, height + posy + selector.getFullHeight() + 2*posy);

        targetY(-(h1 - height));
        targetHeight(h1);
        selector.targetY(height + posy);
        selector.setHeight(h1 - height - posy);
      } else {
        targetY(0);
        targetHeight(covarHeightMax);
        selector.targetY(covarHeightMax + posy);
        selector.setHeight(selh);
      }
    }
  }

  public void show() {
    maxAlpha.set(0);
    maxAlpha.setTarget(255);
    selector.show();
  }

  protected void drawDismiss() {
    float x0 = width - padding - crossw - marginx;
    float y0 = height/2 - crossw/2;
    stroke(dColor, maxAlpha.getCeil());
    strokeWeight(1);
    line(x0, y0, x0 + crossw, y0 + crossw);
    line(x0, y0 + crossw, x0 + crossw, y0);
  }

  protected boolean insideDismiss(float mx, float my) {
    float x0 = width - padding - crossw - marginx;
    float y0 = height/2 - crossw/2;
    return x0 <= mx && mx <= x0 + crossw && y0 <= my && my <= y0 + crossw;
  }
}

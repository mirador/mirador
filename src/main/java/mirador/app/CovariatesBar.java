/* COPYRIGHT (C) 2014-16 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import processing.core.PApplet;
import processing.core.PFont;
import mui.Display;
import mui.Interface;
import mui.Widget;
import mui.SoftFloat;
import miralib.data.Variable;

/**
 * Subclass of ColumnScoller specialized to handle an horizontal scroll of 
 * covariate boxes.
 *
 */

public class CovariatesBar extends Scroller<Covariate> {
  protected float covarHeightMax;
  protected float covWidth, covHeight;

  public CovariatesBar(Interface intf, float x, float y, float w, float h,
                      float iw, float ih, float ihmax) {
    super(intf, x, y, w, h);
    covWidth = iw;
    covHeight = ih;
    covarHeightMax = ihmax;
    initItems(data.getColumnCount(), iw, ih, HORIZONTAL);

  }

  public void draw() {
//    fill(color(120));
//    rect(0, 0, width, height);

    fill(color(150));
    rect(getDragBoxLeft(), getDragBoxTop(), getDragBoxWidth(), getDragBoxHeight());
  }
  
  public boolean inside(float x, float y) {
    return insideItems(x, y); 
  }    
  
  protected void handleResize(int newWidth, int newHeight) {
    float w0 = bounds.w.get();
    float w1 = newWidth - mira.optWidth;    
    float dw = w1 - w0;
    bounds.w.set(w1);
//    visX1.setTarget(visX1.getTarget() + dw);
  }

  @Override
  protected Scroller<Covariate>.Item createItem(int index) {
//    boolean anim = event == WIDGET_UPDATE;
//    Covariate cov = new Covariate(var, w, h, anim);
//    if (event == ITEMS_UPDATE) cov.show();
//    return cov;


    Variable cvar = data.getColumn(index);
//    float h = item.open() ? heightOpen : heightClose;
    Covariate cov = new Covariate(intf, 0, 0, covWidth, covHeight, covarHeightMax, cvar,false /*defaultAnimation(event)*/);;
    addChild(cov, Widget.TOP_LEFT_CORNER);
    Scroller<Covariate>.Item item = new Item(index, cov);
    return item;

  }
  
  protected int getCount() {
    return data.getCovariateCount();  
  }
  
  protected Variable getVariable(int i) {
    return data.getCovariate(i);
  }  
  
  protected int getIndex(Variable var) {
    return data.getCovariate(var);
  }   

  /*
  protected class Covariate extends Item {
    // TODO: Make into CCS size parameters
    int marginx = Display.scale(10);
    int posy = Display.scale(5);
    float selh = Display.scale(50);
    float crossw = Display.scale(10);
    float htexty = Display.scale(6);
    float ptexty = Display.scale(4);

    boolean open;
    RangeSelector selector;
    SoftFloat maxAlpha;

    Covariate(Variable var, float w, float h, boolean anim) {
      super(var, w, h, anim);
      maxAlpha = new SoftFloat(255);
      open = false;

      if (var.numerical()) {
        selector = new NumericalRangeSelector(intf, marginx + x.get(), h + posy, w - marginx*2, selh, var);
      } else if (var.categorical()) {
        selector = new CategoricalRangeSelector(intf, marginx + x.get(), h + posy, w - marginx*2, selh, var);
      }
      selector.setBackgroundColor(oColor);
      selector.setOffset(visX0);
      if (anim) selector.targetX(x.getTarget() + marginx*2);
      addChild(selector, TOP_LEFT_CORNER);
    }

    void dispose() {
//      Log.message("remove " + selector);
      removeChild(selector);
    }

    void update() {
      super.update();
      selector.targetX(x.getTarget() + marginx);
      maxAlpha.update();
    }

    void draw() {
      float x0 = x.get() - visX0.get();
      float y0 = y.get();
      noStroke();
      if (itemHeight < h.get()) {
        fill(oColor, maxAlpha.getCeil());
        rect(x0 + padding, y0, w - 2 * padding, h.get());

        fill(bColor, maxAlpha.getCeil());
        rect(x0 + padding, y0, w - 2 * padding, itemHeight);
      } else {
        fill(bColor, maxAlpha.getCeil());
        rect(x0 + padding, y0, w - 2 * padding, h.get());
      }

      textFont(hFont);
      fill(hColor, maxAlpha.getCeil());
      String label = var.getName();
      String alias = var.getAlias();
      if (!label.equals(alias)) label += ": " + alias;
      label = chopStringRight(label, w - padding - crossw - marginx*2);
      text(label, x0 + marginx, y0 + hFont.getSize() + htexty);

      textFont(pFont);
      String range = var.formatRange(mira.ranges.get(var));
      range = chopStringRight(range, w - padding - crossw - 20);
      text(range, x0 + marginx, y0 + textLeading() + hFont.getSize() + ptexty);

      drawDismiss();
    }

    void drawDismiss() {
      float x0 = x.get() - visX0.get() + w - padding - crossw - marginx;
      float y0 = y.get() + itemHeight/2 - crossw/2;
      stroke(dColor, maxAlpha.getCeil());
      strokeWeight(1);
      line(x0, y0, x0 + crossw, y0 + crossw);
      line(x0, y0 + crossw, x0 + crossw, y0);
    }

    boolean insideDismiss(float mx, float my) {
      float x0 = x.get() - visX0.get() + w - padding - crossw - marginx;
      float y0 = y.get() + itemHeight/2 - crossw/2;
      return x0 <= mx && mx <= x0 + crossw && y0 <= my && my <= y0 + crossw;
    }

    void mouseReleased() {
      if (!inside(mouseX, mouseY)) return;

      if (insideDismiss(mouseX, mouseY)) {
        data.removeCovariate(var);
        markedForRemoval = true;
      } else {
        open = !open;
        if (open) {
          float h1 = PApplet.min(covarHeightMax, itemHeight + posy + selector.getFullHeight() + 2*posy);

          y.setTarget(-(h1 - itemHeight));
          h.setTarget(h1);
          selector.targetY(y.getTarget() + itemHeight + posy);
          selector.setHeight(h1 - itemHeight - posy);
        } else {
          y.setTarget(0);
          h.setTarget(itemHeight);
          selector.targetY(itemHeight + posy);
          selector.setHeight(selh);
        }
      }
    }

    void show() {
      maxAlpha.set(0);
      maxAlpha.setTarget(255);
      selector.show();
    }
  }
  */
}

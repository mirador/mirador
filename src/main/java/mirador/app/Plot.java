/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import mirador.views.View;
import miralib.data.DataSlice1D;
import miralib.data.DataSlice2D;
import miralib.data.Variable;
import miralib.shannon.Similarity;
import mui.Display;
import mui.Interface;
import mui.SoftFloat;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;

import java.nio.file.Paths;
import java.util.concurrent.FutureTask;

public class Plot extends MiraWidget {
  float triSize = Display.scale(13);

  protected PFont pFont;
  protected int pColor;
  protected int sColor;
  protected int corColor, misColor;

  protected Variable colVar;
  protected Variable rowVar;
  protected View view;
  protected PGraphics pcanvas;
  protected PGraphics canvas;
  protected boolean dirty;
  protected boolean pdirty;
  protected boolean update;
  protected boolean depend;
  protected float missing;
  protected SoftFloat blendf;


  @SuppressWarnings("rawtypes")
  protected FutureTask viewTask, indepTask;

  public Plot(Interface intf, Variable cvar, Variable rvar, float x, float y, float w, float h) {
    super(intf, x, y, w, h);

    pFont = getStyleFont("RowPlots.p", "font-family", "font-size");
    pColor = getStyleColor("RowPlots.p", "color");
    sColor = getStyleColor("RowPlots", "selection-color");
    corColor = getStyleColor("RowPlots.Pvalue", "background-color");
    misColor = getStyleColor("RowPlots.MissingData", "background-color");

    dirty = true;
    pdirty = true;
    update = false;
    depend = false;
    blendf = new SoftFloat();
    showContents = false;

    colVar = cvar;
    rowVar = rvar;

    mira.history.addPair(colVar, rowVar);
  }

  public boolean selected() {
    return mira.browser.getSelectedRow() == rowVar &&
           mira.browser.getSelectedCol() == colVar;
  }

  public void dispose() {
    if (viewTask != null && !viewTask.isDone()) {
      viewTask.cancel(true);
    }
    if (indepTask != null && !indepTask.isDone()) {
      indepTask.cancel(true);
    }

    if (pcanvas != null) {
      mira.g.removeCache(pcanvas);
      pcanvas.dispose();
      pcanvas = null;
    }

    if (canvas != null) {
      mira.g.removeCache(canvas);
      canvas.dispose();
      canvas = null;
    }

    mira.history.removePair(colVar, rowVar);
  }

  void dataChanged() {
    dirty = true;
    pdirty = true;
  }

  void pvalueChanged() {
    pdirty = true;
  }

  Variable getRowVar() {
    return rowVar;
  }
  Variable getColVar() {
    return colVar;
  }

  public void update() {
    if (showContents) {
      if (dirty) {
        dirty = false;
        update = false;
        if (viewTask != null && !viewTask.isDone()) viewTask.cancel(true);
        viewTask = mira.browser.submitTask(new Runnable() {
          public void run() {
            if (colVar == rowVar) {
              DataSlice1D slice = data.getSlice(rowVar, mira.ranges);
              missing = slice.missing;
              view = View.create(slice, mira.project.binAlgorithm);
            } else {
              DataSlice2D slice = data.getSlice(colVar, rowVar, mira.ranges);
              missing = slice.missing;
              view = View.create(slice, mira.getPlotType(), mira.project.binAlgorithm);
            }
            update = true;
          }
        }, true);
      }
      if (pdirty) {
        pdirty = false;
        if (indepTask != null && !indepTask.isDone()) indepTask.cancel(true);
        indepTask = mira.browser.submitTask(new Runnable() {
          public void run() {
            DataSlice2D slice = data.getSlice(colVar, rowVar, mira.ranges);
            float score = 0;
            if (slice.missing < mira.project.missingThreshold()) {
              score = Similarity.calculate(slice, mira.project.pvalue(), mira.project);
            }
            depend = 0 < score;
          }
        }, false);
      }

      if (update) {
        update = false;
        if (pcanvas == null) {
          pcanvas = intf.createCanvas((int) width, PApplet.ceil(height), MiraApplet.RENDERER, MiraApplet.SMOOTH_LEVEL);
        }
        if (canvas == null) {
          canvas = intf.createCanvas((int) width, PApplet.ceil(height), MiraApplet.RENDERER, MiraApplet.SMOOTH_LEVEL);
          pcanvas.beginDraw();
          pcanvas.noStroke();
          pcanvas.fill(color(255));
          pcanvas.rect(0, 0, (int) width, PApplet.ceil(height));
          pcanvas.endDraw();
        } else {
          pcanvas.beginDraw();
          pcanvas.image(canvas, 0, 0);
          pcanvas.endDraw();
        }

        view.draw(canvas, false);
        blendf.set(0);
        blendf.setTarget(255);
      }

      blendf.update();
    }
  }

  public void draw() {
    float x0 = padding;
    float y0 = padding;
    float w0 = width - 2 * padding;
    float h0 = height - 2 * padding;

    if (showContents && canvas != null) {
      int bfa = blendf.getCeil();
      if (bfa < 255 && pcanvas != null) {
        tint(color(255));
        image(pcanvas, x0, y0, w0, h0);
      }
      tint(color(255), bfa);
      image(canvas, x0, y0, w0, h0);

      if (view != null && (viewTask == null || viewTask.isDone()) && hovered &&
              x0 <= mouseX && mouseX <= x0 + w0 &&
              y0 <= mouseY && mouseY <= y0 + h0) {
        double valx = PApplet.constrain((mouseX - x0) / w0, 0, 1);
        double valy = PApplet.constrain((mouseY - y0) / h0, 0, 1);
        view.drawSelection(valx, valy, keyPressed(SHIFT), x0, y0, w0, h0,
                parent, pFont, pColor);
      }

      if (depend && mira.project.pvalue() < 1) {
        // TODO: show some kind of animation while calculating the significance...
        noStroke();
        fill(corColor);
        triangle(x0, y0, x0 + triSize, y0, x0, y0 + triSize);
      }

      if (mira.project.missingThreshold() <= missing) {
        float x1 = x0 + w0;
        noStroke();
        fill(misColor);
        triangle(x1 - triSize, y0, x1, y0, x1, y0 + triSize);
      }
    } else {
      noStroke();
      fill(color(255));
      rect(x0, y0, w0, h0);
    }
    if (selected()) {
      stroke(sColor);
      strokeWeight(3);
      noFill();
      rect(x0, y0, w0, h0);
    }
  }


  protected void draw(PGraphics pg) {
    float x0 = padding;
    float y0 = padding;
    float w0 = width - 2 * padding;
    float h0 = height - 2 * padding;

    pg.beginDraw();
    pg.image(canvas, 0, 0);
    if (view != null && (viewTask == null || viewTask.isDone()) && hovered &&
            x0 <= mouseX && mouseX <= x0 + w0 &&
            y0 <= mouseY && mouseY <= y0 + h0) {
      double valx = PApplet.constrain((mouseX - x0) / w0, 0, 1);
      double valy = PApplet.constrain((mouseY - y0) / h0, 0, 1);
      view.drawSelection(valx, valy, keyPressed(SHIFT), pg, pFont, pColor);
    }

    if (depend && mira.project.pvalue() < 1) {
      // TODO: show some kind of animation while calculating the significance...
      pg.noStroke();
      pg.fill(corColor);
      pg.triangle(0, 0, triSize, 0, 0, triSize);
    }

    if (mira.project.missingThreshold() <= missing) {
      pg.noStroke();
      pg.fill(misColor);
      pg.triangle(pg.width - triSize, 0, pg.width, 0, pg.width, triSize);
    }

    pg.noFill();
    pg.stroke(0);
    pg.strokeWeight(2);
    pg.rect(0, 0, pg.width, pg.height);
    pg.endDraw();
  }

  public void save() {
    if (showContents && canvas != null) {
      String imgName = colVar.getName() + "-" + rowVar.getName() + ".pdf";
      String filename = Paths.get(mira.project.dataFolder, imgName).toString();
      PGraphics pdf = intf.createCanvas((int)width, PApplet.ceil(height), PDF, filename);

      view.draw(pdf, true);
    }
  }


  public String getColLabel() {
    if (view != null && (viewTask == null || viewTask.isDone())) {
      float x0 = padding;
      float y0 = padding;
      float w0 = width - 2 * padding;
      float h0 = height - 2 * padding;

      double valx = PApplet.constrain((mouseX - x0) / w0, 0, 1);
      double valy = PApplet.constrain((mouseY - y0) / h0, 0, 1);

      return view.getLabelX(valx, valy);
    }
    return "";
  }

  public String getRowLabel() {
    if (view != null && (viewTask == null || viewTask.isDone())) {
      float x0 = padding;
      float y0 = padding;
      float w0 = width - 2 * padding;
      float h0 = height - 2 * padding;

      double valx = PApplet.constrain((mouseX - x0) / w0, 0, 1);
      double valy = PApplet.constrain((mouseY - y0) / h0, 0, 1);

      return view.getLabelY(valx, valy);
    }
    return "";
  }

  public void mouseReleased() {
    if (selected()) {
      mira.browser.setSelectedPair(colVar, rowVar);
    }
  }

  public void lostFocus() {
    mira.browser.setSelectedPair(null, null);
  }
}

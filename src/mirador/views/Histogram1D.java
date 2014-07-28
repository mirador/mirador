package mirador.views;

import miralib.data.DataSlice2D;
import processing.core.PGraphics;

public class Histogram1D extends View {

  public Histogram1D(DataSlice2D slice) {
    super(slice.varx, slice.vary, slice.ranges);
  }

  @Override
  public void draw(PGraphics pg) {
    pg.beginDraw();
    pg.background(255, 0, 0);
    pg.endDraw();
  }

  @Override
  public Selection getSelection(double valx, double valy) {
    return null;
  } 

}

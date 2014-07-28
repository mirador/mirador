package mirador.views;

import miralib.data.DataRanges;
import miralib.data.DataSlice2D;
import miralib.data.Variable;
import processing.core.PGraphics;

public class Histogram1D extends View {

  public Histogram1D(DataSlice2D slice) {
    super(slice.varx, slice.vary, slice.ranges);
    // TODO Auto-generated constructor stub
  }

  @Override
  public void draw(PGraphics pg) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Selection getSelection(double valx, double valy) {
    // TODO Auto-generated method stub
    return null;
  } 

}

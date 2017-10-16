/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.data;

import miralib.math.Numbers;
import miralib.shannon.BinOptimizer;
import miralib.utils.Log;

/**
 * Class that stores a RxC contingency table derived from a 2D data slice and
 * a given binning of the data. 
 *
 */

public class ContingencyTable {
  public int rowCount;
  public int colCount;
    
  protected long[][] colData;
  protected long[][] rowData;
    
  public ContingencyTable(DataSlice2D slice, int binAlgo) {
    int[] nbins = BinOptimizer.calculate(slice, binAlgo);
    build(slice, nbins[0], nbins[1]);
  }
    
  public ContingencyTable(DataSlice2D slice, int nbinx, int nbiny) {
    build(slice, nbinx, nbiny);
  }      

  public long[] getColumn(int col) {
    return colData[col];
  }
    
  public long[] getRow(int row) {
    return rowData[row];
  }
    
  public boolean empty() {
    return rowCount == 0 || colCount == 0 || colData == null || rowData == null;
  }
  
  private void build(DataSlice2D slice, int nbinx, int nbiny) {
    if (nbinx == 0 || nbiny == 0) {
      String err = "Cannot build a contingency table with zero rows or columns";
      Log.error(err, new RuntimeException(err));
      return;
    }
    
    float sbinx = 1.0f / nbinx;
    float sbiny = 1.0f / nbiny;
    double[][] counts = new double[nbinx][nbiny];
      
    for (Value2D value: slice.values) {
      int bx = Numbers.constrain((int)(value.x / sbinx), 0, nbinx - 1);  
      int by = Numbers.constrain((int)(value.y / sbiny), 0, nbiny - 1);  
      counts[bx][by] += value.w;
    }
    
    colCount = nbinx;
    rowCount = nbiny;    
    colData = new long[nbinx][nbiny];
    rowData = new long[nbiny][nbinx];
    for (int bx = 0; bx < nbinx; bx++) {
      for (int by = 0; by < nbiny; by++) {
        colData[bx][by] = (long)counts[bx][by];
      }
    }
    for (int by = 0; by < nbiny; by++) {
      for (int bx = 0; bx < nbinx; bx++) {      
        rowData[by][bx] = (long)counts[bx][by];
      }
    } 
  }    
}

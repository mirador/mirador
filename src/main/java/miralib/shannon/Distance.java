package miralib.shannon;

import miralib.data.DataSlice2D;

public class Distance {
  static public float calculate(DataSlice2D slice, float ixy) {
    return calculate(slice, ixy, slice.binx, slice.biny);
  }

  static public float calculate(DataSlice2D slice, float ixy, int binx, int biny) {
    float hxy = JointEntropy.calculate(slice, binx, biny);
    float dxy = Math.max(0, hxy - ixy);
    return dxy;
  }
}

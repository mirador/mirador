package miralib.shannon;

import miralib.data.DataSlice2D;
import miralib.utils.Project;

public class Distance extends Statistics {
  static public float calculate(DataSlice2D slice, Project prefs) {
    int[] res = BinOptimizer.calculate(slice, prefs.binAlgorithm);
    int binx = res[0];
    int biny = res[1];
    float ixy = MutualInformation.calculate(slice, binx, biny);
    return calculate(slice, ixy, binx, biny);
  }

  // This method requires the binning already calculated on the slice itself
  static public float calculate(DataSlice2D slice, float ixy) {
    return calculate(slice, ixy, slice.binx, slice.biny);
  }

  static public float calculate(DataSlice2D slice, float ixy, int binx, int biny) {
    float hxy = JointEntropy.calculate(slice, binx, biny);
    float dxy = Math.max(0, hxy - ixy);
    return dxy;
  }
}

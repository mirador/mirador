/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.data;

import miralib.math.Numbers;

/**
 * A 2-dimensional weighted value.
 *
 */

public class Value2D {
  public double x, y;
  public double w;
  public String label;

  public Value2D(double x, double y) {
    this.x = x;
    this.y = y;
    this.w = 1; 
  }
  
  public Value2D(double x, double y, double w) {
    this.x = x;
    this.y = y;
    this.w = w;    
  }    
  
  public Value2D(Value1D v1, Value1D v2) {
    this.x = v1.x;
    this.y = v2.x;
    this.w = v1.w * v2.w;
  } 
  
  public Value2D(Value2D src) {
    this.x = src.x;
    this.y = src.y;
    this.w = src.w;
  }
  
  public boolean equals(Value2D other) {
    return Numbers.equal(x, other.x) && Numbers.equal(y, other.y);
  }
  
}

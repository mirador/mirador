/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.math;

/**
 * Some handy geometry calculations (e.g.: line segment intersection, point-inside-
 * polygon, etc.)  
 * 
 */

public class Geometry {
  // Returns the interval that results of intersecting the (closed) intervals 
  // [a, b] and [c, d].
  static public IntersectPoint intervalIntersection(float a, float b, 
                                                    float c, float d) {
    if (b < c || d < a) {
      // No intersection.
      return new IntersectPoint(0, 0, false);
    }
    
    float x, y;
    if (a < c) {
      x = c;
      y = b < d ? b : d;
    } else {
      x = a;
      y = b < d ? b : d;
    }    
    return new IntersectPoint(x, y, true);
  }
  
  // How to determine if two line segments intersect. From:
  // http://ptspts.blogspot.com/2010/06/how-to-determine-if-two-line-segments.html
  static public boolean doLineSegmentsIntersect(float x1, float y1, 
                                                float x2, float y2,
                                                float x3, float y3, 
                                                float x4, float y4) {
    int d1 = computeDirection(x3, y3, x4, y4, x1, y1);
    int d2 = computeDirection(x3, y3, x4, y4, x2, y2);
    int d3 = computeDirection(x1, y1, x2, y2, x3, y3);
    int d4 = computeDirection(x1, y1, x2, y2, x4, y4);
    return (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
            ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) ||
           (d1 == 0 && isOnSegment(x3, y3, x4, y4, x1, y1)) ||
           (d2 == 0 && isOnSegment(x3, y3, x4, y4, x2, y2)) ||
           (d3 == 0 && isOnSegment(x1, y1, x2, y2, x3, y3)) ||
           (d4 == 0 && isOnSegment(x1, y1, x2, y2, x4, y4));
    
  } 
    
  static public boolean isOnSegment(float xi, float yi, float xj, float yj,
                                    float xk, float yk) {
    return (xi <= xk || xj <= xk) && (xk <= xi || xk <= xj) &&
           (yi <= yk || yj <= yk) && (yk <= yi || yk <= yj);
  }

  static public int computeDirection(float xi, float yi, float xj, float yj,
                                     float xk, float yk) {
    float a = (xk - xi) * (yj - yi);
    float b = (xj - xi) * (yk - yi);
    return a < b ? -1 : a > b ? 1 : 0;
  }  
  
  // The array of input coordinates must be of the form: 
  // x, y, x0, y0, x1, y1, x2, y2...
  // where (x, y) is the point to determine if is inside the (convex) polygon 
  // defined by the vertices (x0, y0), (x1, y1), (x2, y2), ... 
  // Returns true if the point is on the edge of the polygon.
  static public boolean isPointInsideConvexPolygon(float... coords) {
    int n = coords.length;
    
    if (n < 8 || n % 2 != 0) return false;

    float x = coords[0];
    float y = coords[1];
    
    int sign = 1;
    for (int i = 1; i < n/2 - 1; i++) {
      float x1 = coords[2 * i];
      float y1 = coords[2 * i + 1];
      
      float x0, y0;
      if (i == 1) {
        x0 = coords[n - 2];
        y0 = coords[n - 1];
      } else {
        x0 = coords[2 * (i - 1)];
        y0 = coords[2 * (i - 1) + 1];        
      }
      
      float d = (y - y0) * (x1 - x0) - (x - x0) * (y1 - y0);
      int s = 0 < d ? +1 : -1;
            
      if (1 < i && s != 0 && sign != s) {
        return false;
      }
      sign = s;
    }
    return true;
  }
  
  /*
  // TODO: implement point inside polygon for general poly
  // http://paulbourke.net/geometry/polygonmesh/
int pnpoly(int npol, float *xp, float *yp, float x, float y)
   {
     int i, j, c = 0;
     for (i = 0, j = npol-1; i < npol; j = i++) {
       if ((((yp[i] <= y) && (y < yp[j])) ||
            ((yp[j] <= y) && (y < yp[i]))) &&
           (x < (xp[j] - xp[i]) * (y - yp[i]) / (yp[j] - yp[i]) + xp[i]))
         c = !c;
     }
     return c;
   }


  */
  
  
  // Returns the intersection point between two line segments. From:
  // http://paulbourke.net/geometry/lineline2d/
  static public IntersectPoint lineSegmentIntersection(float x1, float y1, 
                                                       float x2, float y2,
                                                       float x3, float y3, 
                                                       float x4, float y4) {
    float mua,mub;
    float denom,numera,numerb;
    
    float x, y;
    
    denom  = (y4-y3) * (x2-x1) - (x4-x3) * (y2-y1);
    numera = (x4-x3) * (y1-y3) - (y4-y3) * (x1-x3);
    numerb = (x2-x1) * (y1-y3) - (y2-y1) * (x1-x3);
    
    // Are the lines coincident?
    if (Math.abs(numera) < Numbers.FLOAT_EPS && 
        Math.abs(numerb) < Numbers.FLOAT_EPS && 
        Math.abs(denom) < Numbers.FLOAT_EPS) {
       x = (x1 + x2) / 2;
       y = (y1 + y2) / 2;
       return new IntersectPoint(x, y, true);
    }

    // Are the lines parallel
    if (Math.abs(denom) < Numbers.FLOAT_EPS) {
       x = 0;
       y = 0;
       return new IntersectPoint(x, y, false);
    }

    // Is the intersection along the the segments?
    mua = numera / denom;
    mub = numerb / denom;
    if (mua < 0 || mua > 1 || mub < 0 || mub > 1) {
       x = 0;
       y = 0;
       return new IntersectPoint(x, y, false);
    }
    
    x = x1 + mua * (x2 - x1);
    y = y1 + mua * (y2 - y1);
    return new IntersectPoint(x, y, true);
  }

  // Returns the intersection points between a line segment between (x1, y1) and
  // (x2, y2) and the circle (x3, y3) with radius r:
  // http://paulbourke.net/geometry/sphereline/
  static public IntersectPoint[] segmentCircleIntersection(float x1, float y1, 
                                                           float x2, float y2,
                                                           float x3, float y3, 
                                                           float r) {
    IntersectPoint[] res;    
    
    // Let's first determine if there the possibility of an intersection 
    // between the segment and the circle
    float u = ((x3 - x1) * (x2 - x1) + (y3 - y1) * (y2 - y1)) / 
              ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    
    if (u < 0 || 1 < u) {
      // no intersection
      res = new IntersectPoint[1];
      res[0] = new IntersectPoint(0, 0, false); 
      return res;      
    }
    
    // Closest point on the segment to the center of the circle
    float cx = x1 + u * (x2 - x1);
    float cy = y1 + u * (y2 - y1);
    
    // Distance between (cx, cy) and the center of the circle must
    // be smaller than the radius r
    float d = (float)Math.sqrt((cx-x3) * (cx-x3) + (cy-y3) * (cy-y3));
    
    if (r < d) {
      // no intersection
      res = new IntersectPoint[1];
      res[0] = new IntersectPoint(0, 0, false); 
      return res;        
    }
 
    // Now we calculate the actual intersection. There should be at least
    // one intersection point, because now we can be sure that the segment
    // crosses the circle somewhere.
 
    float mu1, mu2;
    float a, b, c;
    float bb4ac;
    float dx, dy;

    dx = x2 - x1;
    dy = y2 - y1;
    
    a = dx * dx + dy * dy;
    b = 2 * (dx * (x1 - x3) + dy * (y1 - y3));
    c = x3 * x3 + y3 * y3;
    c += x1 * x1 + y1 * y1;
    c -= 2 * (x3 * x1 + y2 * y1);
    c -= r * r;
    bb4ac = b * b - 4 * a * c;

    if (Math.abs(bb4ac) < Numbers.FLOAT_EPS) {
      // one intersection
      res = new IntersectPoint[1];
      res[0] = new IntersectPoint(0, 0, true);
      
      mu1 = -b / (2 * a);
      res[0].a = x1 + mu1 * (x2 - x1);
      res[0].b = y1 + mu1 * (y2 - y1);      
    } else {
      // two intersections
      res = new IntersectPoint[2];
      res[0] = new IntersectPoint(0, 0, true);
      res[1] = new IntersectPoint(0, 0, true);

      mu1 = (-b + (float)Math.sqrt(Math.abs(bb4ac))) / (2 * a);
      mu2 = (-b - (float)Math.sqrt(Math.abs(bb4ac))) / (2 * a);
      
      res[0].a = x1 + mu1 * (x2 - x1);
      res[0].b = y1 + mu1 * (y2 - y1);
      
      res[1].a = x1 + mu2 * (x2 - x1);
      res[1].b = y1 + mu2 * (y2 - y1);        
    }
    return res;
  }
  
  // Implementation of the the Liang-Barsky line clipping algorithm:
  // http://www.siggraph.org/education/materials/HyperGraph/scanline/clipping/cliplb.htm
  // to clip a line segment to a given axis-aligned rectangle.
  // http://www.skytopia.com/project/articles/compsci/clipping.html
  // Note: here the top/bottom values follow the convention in Processing, 
  // where the Y axis is inverted.
  static public ClippedSegment lineSegmentClipping(float left, float right, 
                                                   float bottom, float top,
                                                   float x0, float y0, 
                                                   float x1, float y1) {
    float t0 = 0.0f;    
    float t1 = 1.0f;
    float xdelta = x1 - x0;
    float ydelta = y1 - y0;
    float p, q, r;

    p = q = r = 0;
    for (int edge=0; edge < 4; edge++) {   
      // Traverse through left, right, bottom, top edges.
      if (edge == 0) { p = -xdelta; q = -(left - x0);   }
      if (edge == 1) { p = xdelta;  q =  (right - x0);  }
      if (edge == 2) { p = -ydelta; q = -(top - y0);    }
      if (edge == 3) { p = ydelta;  q =  (bottom - y0); }   
      r = q / p;
      
      if (Math.abs(p) < Numbers.FLOAT_EPS && q < 0) {
        return new ClippedSegment(0, 0, 0, 0, false);   // Parallel line outside
      }

      if (p < 0) {
        if (r > t1) return new ClippedSegment(0, 0, 0, 0, false);
        else if (r > t0) t0 = r;   // Line is clipped!
      } else if (p > 0) {
        if (r < t0) return new ClippedSegment(0, 0, 0, 0, false);
        else if (r < t1) t1 = r;   // Line is clipped!
      }
    }
    
    float x0clip = x0 + t0 * xdelta;
    float y0clip = y0 + t0 * ydelta;
    float x1clip = x0 + t1 * xdelta;
    float y1clip = y0 + t1 * ydelta;
    return new ClippedSegment(x0clip, y0clip, x1clip, y1clip, true);
  }
  
  // Class to return the result of segment/rectangle clipping methods
  static public class ClippedSegment {
    public float x0, y0;
    public float x1, y1;
    public boolean inside;
    
    public ClippedSegment() {
      x0 = y0 = 0;
      x1 = y1 = 0;
      inside = false;
    }
    
    public ClippedSegment(float x0, float y0, float x1, float y1, 
                          boolean inside) {
      this.x0 = x0;
      this.y0 = y0;
      this.x1 = x1;
      this.y1 = y1;      
      this.inside = inside;
    }    
  }
  
  // Class to return the result of the segment intersection methods.
  static public class IntersectPoint {
    public float a, b;
    public boolean intersect;
    
    public IntersectPoint() {
      a = b = 0;
      intersect = false;
    }
    
    public IntersectPoint(float a, float b, boolean intersect) {
      this.a = a;
      this.b = b;
      this.intersect = intersect;
    }
  }   
}

/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package lib.ui;

import java.util.ArrayList;

import miralib.math.Geometry;
import miralib.math.Numbers;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PImage;
import processing.core.PMatrix;
import processing.core.PMatrix2D;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.core.PStyle;

/**
 * Base class for a basic UI framework. It implements a generic widget with
 * position and size that can be "softly animated". It incorporates an "anchor" 
 * mechanism that allows to attach widgets to each other so the motion on the  
 * anchor widget affects all the widgets anchored to it. Furthermore, the 
 * internal coordinates of a widget are relative to its parent. 
 *
 */

public class Widget implements PConstants {
  static public int POSITION = 0;
  static public int SIZE     = 1;
  static public int OPACITY  = 2;
  
  static public float[] TOP_LEFT_CORNER     = {0, 0};
  static public float[] BOTTOM_LEFT_CORNER  = {0, 1};
  static public float[] TOP_RIGHT_CORNER    = {1, 0};
  static public float[] BOTTOM_RIGHT_CORNER = {1, 1};
  
  // Tolerance to consider the widget inside a given region by an amount of
  // pixels. This is helpful for example to determine when to start the show
  // animation of a widget given that must be inside its parent, because w/out
  // tolerance it might take too long because of soft float.
//  static public int INSIDE_TOLERANCE = 1;  
  
  protected Interface intf;

  protected Widget parent;  
  protected BoundBox bounds;
  protected SoftFloat opacity;

  float xfactor, yfactor;
  
  protected float left, top;
  protected float width, height;
  
  protected boolean clipBounds;
  protected boolean clipLeft, clipRight;
  protected boolean clipTop, clipBottom;

  protected boolean focused;
  
  protected boolean inner;
  protected boolean clickable;
  protected boolean draggable;
  protected boolean hoverable;
  
  protected boolean captureKeys;
  protected boolean showRequested;
  
  protected ArrayList<Widget> children;  

  protected boolean calledSetup;
  protected boolean markedForDeletion;
  
  protected boolean hovered;
  protected int mouseX, mouseY;
  protected int pmouseX, pmouseY;
  
  protected char key;
  protected int keyCode;
  
  public Widget(Interface intf) {
    initWidget(intf);
    initPosition(0, 0);
    initSize(0, 0);
    initOpacity(1);
  }
  
  public Widget(Interface intf, float x, float y, float w, float h) {
    initWidget(intf);
    initPosition(x, y);    
    initSize(w, h);
    initOpacity(1);
  }  
  
  public Widget(Interface intf, float x, float y, float w, float h, 
                boolean tw, boolean th) {
    initWidget(intf);    
    initPosition(x, y);
    initSize(w, h, tw, th);
    initOpacity(1);
  }  
  
  public Widget(Interface intf, float x0, float y0, float x1, float y1, 
                float w0, float h0, float w1, float h1) {
    initWidget(intf);    
    initPosition(x0, y0);
    setPosition(x1, y1);
    initSize(w0, h0);
    setSize(w1, h1);
    initOpacity(1);
  }  

  protected void initWidget(Interface intf) {
    this.intf = intf;
    
    parent = null;
    
    bounds = new BoundBox();
    xfactor = yfactor = 0;
    
    clipBounds = false;
    clipLeft = false; 
    clipRight = false;
    clipTop = false; 
    clipBottom = false;
    
    inner = false;
    focused = false;   
    clickable = true;
    draggable = true;
    hoverable = true;
    
    calledSetup = false;
    markedForDeletion = false;
    
    hovered = false;
//    dragParentRequested = false;
    showRequested = false;    
    
    children = new ArrayList<Widget>();
  }
  
  public void dispose() { }
  
  public void setup() { }

  //////////////////////////////////////////////////////////////////////////////
  //  
  // Clipping

  public void clipBounds(boolean value) {
    clipBounds = clipLeft = clipRight = clipTop = clipBottom = value;
  }
      
  public void clipBounds(boolean left, boolean right, boolean top, boolean bottom) {
    clipLeft = left; 
    clipRight = right;
    clipTop = top; 
    clipBottom = bottom;
    clipBounds = clipLeft || clipRight || clipTop || clipBottom;
  }

  public void clipBounds(boolean vertical, boolean horizontal) {
    clipLeft = clipRight = vertical;
    clipTop = clipBottom = horizontal;
    clipBounds = vertical || horizontal;
  }
    
  protected boolean insideClip(float x, float y) {
    if (!clipBounds) return true;
    boolean insideLeft = !clipLeft || left() <= x;
    boolean insideRight = !clipRight || x <= right();    
    boolean insideTop = !clipTop || top() <= y;
    boolean insideBottom = !clipBottom || y <= bottom();
    return insideLeft && insideRight && insideTop && insideBottom;
  }
  
  final protected void setClip() {
    intf.pushClip();    
    float leftClip = clipLeft ? left() : intf.left();
    float topClip = clipTop ? top() : intf.top();    
    float widthClip = clipRight ? width() : intf.width();    
    float heightClip = clipBottom ? height() : intf.height();    
    intf.setClip(leftClip, topClip, widthClip, heightClip);
  }
  
  final protected void unsetClip() {
    intf.popClip(); 
  }   
  
  //////////////////////////////////////////////////////////////////////////////
  //  
  // Updating and drawing  
  
  final protected void updateChildren() {
    removeChildren();    
    for (Widget child: children) {
      if (!child.calledSetup) {
        child.setup();
        child.calledSetup = true;
      }      
      if (child.canUpdate()) {
        child.updateMouse(intf.app.mouseX, intf.app.mouseY, 
                          intf.app.pmouseX, intf.app.pmouseY);
        child.updateKey(intf.app.key, intf.app.keyCode);
        
        child.requestShow();
        child.updatePosition();
        child.updateSize();
        child.updateOpacity();
        if (child.clipBounds) child.setClip();
        child.updateChildren();
        child.update();     
        if (child.clipBounds) child.unsetClip();
      }
    }        
  }
  
  final protected void removeChildren() {
    // Remove children widgets marked for deletion.
    for (int i = children.size() - 1; i >= 0; i--) {
      Widget child = children.get(i);
      child.removeChildren();
      if (child.markedForDeletion) {        
        child.parent = null;
        child.dispose();
        children.remove(i);
      }
    }    
  }
  
  public boolean isMarkedForDeletion() {
    return markedForDeletion;
  }
  
  public int getMarkedForDeletionCount() {
    int count = 0;
    for (Widget child: children) {
      if (child.markedForDeletion) count++;
    }
    return count;
  }
  
  final protected void updatePosition() {
    bounds.x.update(); 
    bounds.y.update();
    left = left();
    top = top();
  }
  
  final protected void updateSize() {
    bounds.w.update();
    bounds.h.update();
    width = width();
    height = height();
  } 
    
  final protected void updateOpacity() {
    opacity.update();
  }
  
  final protected void drawChildren() {
    for (Widget child: children) {
      if (child.clipBounds) child.setClip();
      
      intf.app.pushMatrix();
      child.setOrigin();
      
      boolean canDraw = child.visible() && child.canDraw();
      if (canDraw) {
        intf.addDrawn(child);
        child.draw();
      }      
      
      // Even tough the parent is not visible, the children could be
      // outside of it and so still be visible.
      child.drawChildren();
      
      if (canDraw) {
        // So the parent can draw on top of children if needed.
        child.postDraw();
      }

      intf.app.popMatrix();
       
      if (child.clipBounds) child.unsetClip();
    }    
  }
  
  protected boolean canUpdate() {
    return calledSetup;
  }
  
  protected boolean canDraw() {
    return calledSetup;
  }
  
  public void update() { }      
  public void draw() { }
  public void postDraw() { }
  
  //////////////////////////////////////////////////////////////////////////////
  //  
  // Mouse
    
  final protected boolean mouseFocused(int mx, int my) {
    focused = visible() && isFocused(mx, my) && insideClip(mx, my);
    return focused;
  }
  
  final protected void updateMouse(int mx, int my, int pmx, int pmy) {
    mouseX = PApplet.round(mx - left);
    mouseY = PApplet.round(my - top);    
    pmouseX = PApplet.round(pmx - left);
    pmouseY = PApplet.round(pmy - top);
  }
  
  public boolean isDraggable() {
    return draggable;
  }
  
  public void setDraggable(boolean value) {
    draggable = value;
  }

  public void setHoverable(boolean value) {
    hoverable = value;
  } 
  
  public boolean isClickable() {
    return clickable;
  }
  
  public void setClickable(boolean value) {
    clickable = value;
  }
  
  public void hoverIn() { }
  
  public void hoverOut() { }
  
  public void lostFocus() { }
  
  public void mousePressed() { }
  
  public void mouseDragged() { }  
    
  public void mouseReleased() { }
  
  public void mouseMoved() { }  
  
  public void handle() { }
  
  //////////////////////////////////////////////////////////////////////////////
  //  
  // Keyboard
  
  final protected void updateKey(char k, int kcode) {
    key = k;
    keyCode = kcode;
  }
  
  public boolean isCapturingKeys() {
    return captureKeys;
  }
  
  public void setCaptureKeys(boolean value) {
    captureKeys = value;
  }
  
  public void keyPressed() { }
  
  public void keyReleased() { }
  
  public void keyTyped() { }
  
  public void handleKey() { }
  
  //////////////////////////////////////////////////////////////////////////////
  //  
  // Size/position setters
  // TODO: need to cleanup this API...
  

  
  
  // targeting check
  
//  public boolean isPositioning() {
//    return bounds.x.isTargeting() || bounds.y.isTargeting();
//  }
//  
//  public boolean isResizing() {
//    return bounds.w.isTargeting() || bounds.h.isTargeting();
//  }
  
  
  
  
  // init
  
  protected void initPosition(float x0, float y0) {
    bounds.x0 = x0;
    bounds.y0 = y0;
    bounds.x.set(x0);    
    bounds.y.set(y0);
    bounds.x.setThreshold(0.25f);
    bounds.y.setThreshold(0.25f);
  }
  
  protected void initSize(float w0, float h0) {
    initSize(w0, h0, false, false);
  }
  
  protected void initSize(float w0, float h0, 
                          boolean targetW, boolean targetH) {
    bounds.w0 = w0;
    bounds.h0 = h0;
    if (targetW) {      
      bounds.w.setTarget(w0);
    } else {
      bounds.w.set(w0);      
    }    
    if (targetH) {
      bounds.h.setTarget(h0);
    } else  {
      bounds.h.set(h0);
    }
    bounds.w.setThreshold(0.25f);
    bounds.h.setThreshold(0.25f);
    
    width = w0;
    height = h0;
  }

  protected void initOpacity(float value) {
    opacity = new SoftFloat(value);
    opacity.setAttraction(0.04f);
    opacity.setThreshold(0.5f);
  }
  
  final protected void resizeChildren(int newWidth, int newHeight) {
    for (Widget child: children) {
      child.handleResize(newWidth, newHeight);
      child.resizeChildren(newWidth, newHeight);
    }
  }
  
  protected void handleResize(int newWidth, int newHeight) { }
  
  //////////////////////////////////////////////////////////////////////////////
  //  
  // Size/position setters & getters
  // TODO: need to cleanup this API...
  
  
  public float left() {
    if (parent == null) {
      return bounds.x.get();
    } else {
      return parent.left() + childLeft();  
    }
  }
  
  public float right() {
    return left() + width();
  }  

  public float top() {
    if (parent == null) {
      return bounds.y.get();
    } else {
      return parent.top() + childTop();
    }
  }
  
  public float bottom() {
    return top() + height();
  }
  
  public float width() {
    return bounds.w.get();
  }
  
  public float height() {
    return bounds.h.get();
  }
  
  
  public float targetX() {
    return bounds.x.getTarget();
  }

  public float targetY() {
    return bounds.y.getTarget();
  }
  
  public float targetWidth() {
    return bounds.w.getTarget();
  }
  
  public float targetHeight() {
    return bounds.h.getTarget();
  }   
  
  
  // set target
  
  public void targetPosition(float x, float y) {
    bounds.x.setTarget(x);
    bounds.y.setTarget(y);
  }
  
  public void targetX(float x) {
    bounds.x.setTarget(x);
  }

  public void targetY(float y) {
    bounds.y.setTarget(y);
  }  

  
  public void targetSize(float w, float h) {
    bounds.w.setTarget(w);
    bounds.h.setTarget(h);
  }  
  
  public void targetWidth(float w) {
    bounds.w.setTarget(w);
  }
  
  public void targetHeight(float h) {
    bounds.h.setTarget(h);
  }  
  
  
  // Copy (preserves animation state)

  public void copyX(Widget wt, float offset) {
    bounds.x.copy(wt.bounds.x, offset);
  } 
  
  public void copyY(Widget wt, float offset) {
    bounds.y.copy(wt.bounds.y, offset);
  }
  
  
  public void copyWidth(Widget wt) {
    bounds.w.copy(wt.bounds.w, 0);
  } 
  
  public void copyHeight(Widget wt) {
    bounds.h.copy(wt.bounds.h, 0);
  }  
  

  public void copyX(SoftFloat x, float offset) {
    bounds.x.copy(x, offset);
  } 
  
  public void copyY(SoftFloat y, float offset) {
    bounds.y.copy(y, offset);
  }
  
  
  public void copyWidth(SoftFloat w) {
    bounds.w.copy(w, 0);
  } 
  
  public void copyHeight(SoftFloat h) {
    bounds.h.copy(h, 0);
  }  
 
  
  
  // translate (target)
  
  public void translateXY(float tx, float ty) {
    translateX(tx);
    translateY(ty);
  }  
  
  public void translateX(float tx) {
    bounds.x.setTarget(bounds.x.get() + tx);  
  }
  
  public void translateTargetX(float tx) {
    bounds.x.setTarget(bounds.x.getTarget() + tx);
  }

  public void translateY(float ty) {
    bounds.y.setTarget(bounds.y.get() + ty);
  }  
  
  public void translateTargetY(float ty) {
    bounds.y.setTarget(bounds.y.getTarget() + ty);
  }
  

  
  // set current value
  
  public void setPosition(float x, float y) {
    bounds.x.set(x);
    bounds.y.set(y);
  }
  
  public void setX(float x) {
    bounds.x.set(x);
  }

  public void setY(float y) {
    bounds.y.set(y);
  }  
  
  public void setSize(float w, float h) {
    setWidth(w);
    setHeight(h);
  }  
  
  public void setWidth(float w) {
    bounds.w.set(w);
  }
  
  public void setHeight(float h) {
    bounds.h.set(h);
  }
  
  
  
  protected void setOrigin() {
    float x, y;
    if (parent == null) {
      x = bounds.x.get();
      y = bounds.y.get();
    } else {
      x = childLeft();
      y = childTop();
    }
    intf.app.translate(x, y);
  }
  
  
  protected float childLeft() {
    return xfactor * parent.width() + bounds.x.get();
  }
  
  protected float childTop() {
    return yfactor * parent.height() + bounds.y.get();
  }
  
  
//public float centerX() {
//return left() + 0.5f * width();
//}
//
//public float centerY() {
//return top() + 0.5f * height();
//}      
//  public float relativeX() {
//    return bounds.x.get();
//  }
//
//  public float relativeY() {
//    return bounds.y.get();
//  }  
 
  
  
  //////////////////////////////////////////////////////////////////////////////
  //  
  // Visibility/focus
  

  public boolean visible() {
    if (isTransparent() || bounds.zero()) {
      return false;     
    } else {
      Geometry.IntersectPoint ipx = 
          Geometry.intervalIntersection(left(), right(), 
                                        intf.left(), intf.right());    
      Geometry.IntersectPoint ipy = 
          Geometry.intervalIntersection(top(), bottom(), 
                                        intf.top(), intf.bottom());

      return ipx.intersect && Numbers.different(ipx.a, ipx.b) && 
             ipy.intersect && Numbers.different(ipy.a, ipy.b);      
    }
  }
  
  public boolean invisible() {
    return !visible();
  }
  
  public boolean insideX(float x) {
//    return left() - INSIDE_TOLERANCE <= x && x <= right() + INSIDE_TOLERANCE;
    return left() <= x && x <= right();
  }

  public boolean insideY(float y) {
//    return top() - INSIDE_TOLERANCE <= y && y <= bottom() + INSIDE_TOLERANCE;
    return top() <= y && y <= bottom();
  } 
  
  // By default, the focus area is the same as the bounding rectangle
  // of this widget, by re-implementing this method in a subclass it can be 
  // made to be some other region, for example a smaller rectangle contained 
  // inside the bounding rectangle.  
  public boolean inside(float x, float y) {
    return insideX(x) && insideY(y);
  }
  
  public boolean outside(float x, float y) {
    return !inside(x, y);
  }
  
  public boolean isFocused() {
    return focused;  
  }
  
  public boolean isFocused(float x, float y) {
    return inside(x, y) && calledSetup;
  }
  
  public boolean insideParent() {
    if (parent == null) {
      return true; 
    } else {
//      return parent.left() - INSIDE_TOLERANCE <= left() && 
//             right() <= parent.right() + INSIDE_TOLERANCE &&
//             parent.top() - INSIDE_TOLERANCE <= top() && 
//             bottom() <= parent.bottom() + INSIDE_TOLERANCE;
      return parent.left() <= left() && right() <= parent.right() &&
             parent.top() <= top() && bottom() <= parent.bottom();      
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////
  //  
  // Show/hide
    
  public void show() {
    show(false);
  }
  
  public void show(boolean now) {
    if (now) {
      opacity.set(0);
      opacity.setTarget(1);
      for (Widget child: children) {
        child.show(now);
      }
    } else {
      // Simply request show, opacity animation will start once the widget is
      // completely inside its parent (in the case it is an inner widget)
      showRequested = true;
      opacity.set(0);      
    }
  }
  
  final protected void requestShow() {
    if (showRequested && (!inner || insideParent())) {      
      opacity.setTarget(1);
      for (Widget child: children) {
        child.show();
      }
      showRequested = false;
    }
  }
  
  public void hide() {
    hide(true);
  }
  
  public void hide(boolean target) {
    if (target) opacity.setTarget(0);
    else opacity.set(0);
    for (Widget child: children) {
      child.hide(target);
    }
  }  
    
  public boolean isTransparent() {
    return opacity.get() < 0.01f;
  }
  
  public int getAlpha() {
    return getAlpha(255);
  }

  public int getAlpha(int max) {
    if (parent != null && inner) {
      // Constraining by the opacity of the parent in case this is an "inner"
      // widget.
      max = PApplet.min(max, parent.getAlpha(255));
    }
    return PApplet.round(opacity.get() * max);
  }  
  
  public void setAlpha(int value) {
    opacity.setTarget(value / 255f);
  }
  
  //////////////////////////////////////////////////////////////////////////////
  //  
  // Parent/children
  
  public boolean isParentOf(Widget widget) {
    return -1 < children.indexOf(widget);
  }

  public boolean isChildOf(Widget widget) {
    return parent == widget;
  }  

  public void addChild(Widget child) {
    addChildImpl(children.size(), child, 0, 0);
  } 

  public void addChild(int pos, Widget child) {
    addChildImpl(pos, child, 0, 0);
  }   
  
  public void addChild(Widget child, float[] xyfactor) {    
    addChildImpl(children.size(), child, xyfactor[0], xyfactor[1]);
  }  
  
  public void addChild(int pos, Widget child, float[] xyfactor) {    
    addChildImpl(pos, child, xyfactor[0], xyfactor[1]);
  }

  protected void addChildImpl(int pos, Widget child, float xfactor, float yfactor) {
    if (0 <= pos && pos <= children.size()) {
      children.add(pos, child);      
      child.setParent(this);
      child.xfactor = xfactor;
      child.yfactor = yfactor;      
      child.inner |= child.insideParent();
    }
  }  
  
  protected void setParent(Widget parent) {
    this.parent = parent;
  }
  
  public void removeChild(Widget child) {
    if (-1 < children.indexOf(child)) {
      child.markedForDeletion = true;
      for (Widget grandchild: child.children) {
        child.removeChild(grandchild);
      }
    }
  }
  
  public int getChildrenCount() {
    return children.size();
  }
  
  public int getChildIndex(Widget child) {
    return children.indexOf(child);
  }
   
  public void getChildren(Class<? extends Widget> clazz, ArrayList<Widget> widgets) {
    for (Widget child: children) {
      if (child.getClass() == clazz) widgets.add(child);
      child.getChildren(clazz, widgets);
    }
  }
  
  public void setLayout(float[] xyfactor) {
    setLayout(xyfactor[0], xyfactor[1]);
  }
  
  public void setLayout(float xfactor, float yfactor) {
    this.xfactor = xfactor;
    this.yfactor = yfactor;    
  }
  
  public void setInner(boolean value) {
    inner = value;
  }
  
  //////////////////////////////////////////////////////////////////////////////
  //  
  // Resources
  
//  protected PImage loadImage(String filename) {
//    return intf.loadImage(filename);
//  }
//  
//  protected PFont loadFont(String filename, float size) {
//    return intf.loadFont(filename, size);
//  }  
//  
//  protected PShape loadShape (String filename) {
//    return intf.loadShape(filename);
//  }
  
  //////////////////////////////////////////////////////////////////////////////
  //  
  // Style  
  
  protected String getStyle(String clazz, String property) {
    return intf.getStyle(clazz, property);    
  }
  
  protected int getStyleColor(String clazz, String property) {
    return intf.getStyleColor(clazz, property);
  } 
  
  protected int getStyleAlign(String clazz, String property) {
    return intf.getStyleAlign(clazz, property);
  }
  
  protected float getStylePosition(String clazz, String property) {
    return intf.getStylePosition(clazz, property); 
  } 
  
  protected float getStyleSize(String clazz, String property) {
    return intf.getStyleSize(clazz, property); 
  }  
  
  protected PFont getStyleFont(String clazz, String nameProperty, 
                            String sizeProperty) {
    return intf.getStyleFont(clazz, nameProperty, sizeProperty);
  }

  protected PFont getStyleFont(String clazz, String nameProperty, 
                               String sizeProperty, float sizeScale) {
    return intf.getStyleFont(clazz, nameProperty, sizeProperty, sizeScale);
  } 
  
  protected PImage getStyleImage(String clazz, String property) {
    return intf.getStyleImage(clazz, property);
  }      

  protected PShape getStyleShape(String clazz, String property) {
    return intf.getStyleShape(clazz, property);
  }       
  
  //////////////////////////////////////////////////////////////////////////////
  //  
  // Subset of the Processing API that can be directly accessed through widget,
  // should be sufficient for UI drawing.
  
  public void beginShape() {
    intf.app.beginShape();
  }
  
  public void beginShape(int kind) {
    intf.app.beginShape(kind);
  }
  
  public void normal(float nx, float ny, float nz) {
    intf.app.normal(nx, ny, nz);
  }
  
  public void textureMode(int mode) {
    intf.app.textureMode(mode);
  }
  
  public void textureWrap(int wrap) {
    intf.app.textureWrap(wrap);
  }
  
  public void texture(PImage image) {
    intf.app.texture(image);
  }
  
  public void noTexture() {
    intf.app.noTexture();
  }
  
  public void vertex(float x, float y) {
    intf.app.vertex(x, y);
  }

  public void vertex(float x, float y, float z) {
    intf.app.vertex(x, y, z);
  }
  
  public void vertex(float[] v) {
    intf.app.vertex(v);
  }

  public void vertex(float x, float y, float u, float v) {
    intf.app.vertex(x, y, u, v);
  }
  
  public void vertex(float x, float y, float z, float u, float v) {
    intf.app.vertex(x, y, z, u, v);
  }
  
  public void beginContour() {
    intf.app.beginContour();
  }
  
  public void endContour() {
    intf.app.endContour();
  }

  public void endShape() {
    intf.app.endShape();
  }
  
  public void endShape(int mode) {
    intf.app.endShape(mode);
  } 
  
  public PShape createShape() {
    return intf.app.createShape();
  }

  public PShape createShape(PShape source) {
    return intf.app.createShape(source);
  } 
  
  public PShape createShape(int type) {
    return intf.app.createShape(type);
  }
  
  public PShape createShape(int kind, float... p) {
    return intf.app.createShape(kind, p);
  }
  
  public void clip(float a, float b, float c, float d) {
    intf.app.clip(a, b, c, d);
  }
  
  public void noClip() {
    intf.app.noClip();
  }
  
  public void blendMode(int mode) {
    intf.app.blendMode(mode);
  }
  
  public void bezierVertex(float x2, float y2,
                           float x3, float y3,
                           float x4, float y4) {
    intf.app.bezierVertex(x2, y2, x3, y3, x4, y4);
  }  
  
  public void bezierVertex(float x2, float y2, float z2,
      float x3, float y3, float z3,
      float x4, float y4, float z4) {
    intf.app.bezierVertex(x2, y2, z2, x3, y3, z3, x4, y4, z4);
  }
  
  public void quadraticVertex(float cx, float cy,
      float x3, float y3) {
    intf.app.quadraticVertex(cx, cy, x3, y3);
  }
  
  public void quadraticVertex(float cx, float cy, float cz,
      float x3, float y3, float z3) {
    intf.app.quadraticVertex(cx, cy, cz, x3, y3, z3);
  }
  
  public void curveVertex(float x, float y) {
    intf.app.curveVertex(x, y);
  }
  
  public void curveVertex(float x, float y, float z) {
    intf.app.curveVertex(x, y, z);
  }
 
  public void point(float x, float y) {
   
    intf.app.point(x, y);
  }
  
  public void point(float x, float y, float z) {
    intf.app.point(x, y, z);
  }
  
  public void line(float x1, float y1, float x2, float y2) {
    intf.app.line(x1, y1, x2, y2);
  }
  
  public void line(float x1, float y1, float z1,
      float x2, float y2, float z2) {
    intf.app.line(x1, y1, z1, x2, y2, z2);
  }
  
  public void triangle(float x1, float y1, float x2, float y2,
      float x3, float y3) {
    intf.app.triangle(x1, y1, x2, y2, x3, y3);
  }
  
  public void quad(float x1, float y1, float x2, float y2,
      float x3, float y3, float x4, float y4) {
    intf.app.quad(x1, y1, x2, y2, x3, y3, x4, y4);
  }
  
  public void rectMode(int mode) {
    intf.app.rectMode(mode);
  }
  
  public void rect(float a, float b, float c, float d) {
    intf.app.rect(a, b, c, d);
  }
  
  public void rect(float a, float b, float c, float d, float r) {
    intf.app.rect(a, b, c, d, r);
  }
  
  public void rect(float a, float b, float c, float d,
      float tl, float tr, float br, float bl) {
    intf.app.rect(a, b, c, d, tl, tr, br, bl);
  }
  
  public void ellipseMode(int mode) {
    intf.app.ellipseMode(mode);
  }
  
  public void ellipse(float a, float b, float c, float d) {
    intf.app.ellipse(a, b, c, d);
  }
  
  public void arc(float a, float b, float c, float d,
      float start, float stop) {
    intf.app.arc(a, b, c, d, start, stop);
  }
  
  public void arc(float a, float b, float c, float d,
      float start, float stop, int mode) {
    intf.app.arc(a, b, c, d, start, stop, mode);
  }
  
  public void box(float size) {
    intf.app.box(size);
  }
  
  public void box(float w, float h, float d) {
    intf.app.box(w, h, d);
  }
 
  public void sphereDetail(int res) {
    intf.app.sphereDetail(res);
  }
  
  public void sphereDetail(int ures, int vres) {
    intf.app.sphereDetail(ures, vres);
  }
  
  public void sphere(float r) {
    intf.app.sphere(r);
  }
 
  public float bezierPoint(float a, float b, float c, float d, float t) {
    return intf.app.bezierPoint(a, b, c, d, t);
  }
  
  public float bezierTangent(float a, float b, float c, float d, float t) {
    return intf.app.bezierTangent(a, b, c, d, t);
  }
  
  public void bezierDetail(int detail) {
    intf.app.bezierDetail(detail);
  }
  
  public void bezier(float x1, float y1,
      float x2, float y2,
      float x3, float y3,
      float x4, float y4) {
    intf.app.bezier(x1, y1, x2, y2, x3, y3, x4, y4);
  }
  
  public void bezier(float x1, float y1, float z1,
      float x2, float y2, float z2,
      float x3, float y3, float z3,
      float x4, float y4, float z4) {
    intf.app.bezier(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4);
  }
  
  public float curvePoint(float a, float b, float c, float d, float t) {
    return intf.app.curvePoint(a, b, c, d, t);
  }
  
  public float curveTangent(float a, float b, float c, float d, float t) {
    return intf.app.curveTangent(a, b, c, d, t);
  }
  
  public void curveDetail(int detail) {
    intf.app.curveDetail(detail);
  }
 
  public void curveTightness(float tightness) {
    intf.app.curveTightness(tightness);
  }
  
  public void curve(float x1, float y1,
      float x2, float y2,
      float x3, float y3,
      float x4, float y4) {
    intf.app.curve(x1, y1, x2, y2, x3, y3, x4, y4);
  }
  
  public void curve(float x1, float y1, float z1,
      float x2, float y2, float z2,
      float x3, float y3, float z3,
      float x4, float y4, float z4) {
    intf.app.curve(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4);
  }
  
  public void imageMode(int mode) {
    intf.app.imageMode(mode);
  }
  
  public void image(PImage img, float a, float b) {
    intf.app.image(img, a, b);
  }
  
  public void image(PImage img, float a, float b, float c, float d) {
    intf.app.image(img, a, b, c, d);
  }
  
  public void image(PImage img,
      float a, float b, float c, float d,
      int u1, int v1, int u2, int v2) {
    intf.app.image(img, a, b, c, d, u1, v1, u2, v2);
  }
  
  public void shapeMode(int mode) {
    intf.app.shapeMode(mode);
  }
  
  public void shape(PShape shape) {
    intf.app.shape(shape);
  }
  
  public void shape(PShape shape, float x, float y) {
    intf.app.shape(shape, x, y);
  }
  
  public void shape(PShape shape, float a, float b, float c, float d) {
    intf.app.shape(shape, a, b, c, d);
  }
  
  public void textAlign(int alignX) {
    intf.app.textAlign(alignX);
  }
  
  public void textAlign(int alignX, int alignY) {
    intf.app.textAlign(alignX, alignY);
  }
  
  public float textAscent() {
    return intf.app.textAscent();
  }
  
  public float textDescent() {
    return intf.app.textDescent();
  }
  
  public void textFont(PFont which) {
    intf.app.textFont(which);
  }
  
  public void textFont(PFont which, float size) {
    intf.app.textFont(which, size);
  }
  
  public void textLeading(float leading) {
    intf.app.textLeading(leading);
  }
  
  public void textMode(int mode) {
    intf.app.textMode(mode);
  }

  public void textSize(float size) {
    intf.app.textSize(size);
  }

  public float textWidth(char c) {
    return intf.app.textWidth(c);
  }

  public float textWidth(String str) {
    return intf.app.textWidth(str);
  }

  public float textWidth(char[] chars, int start, int length) {
    return intf.app.textWidth(chars, start, length);
  }

  public void text(char c, float x, float y) {
    intf.app.text(c, x, y);
  }

  public void text(char c, float x, float y, float z) {
    intf.app.text(c, x, y, z);
  }

  public void text(String str, float x, float y) {
    intf.app.text(str, x, y);
  }

  public void text(char[] chars, int start, int stop, float x, float y) {
    intf.app.text(chars, start, stop, x, y);
  }

  public void text(String str, float x, float y, float z) {
    intf.app.text(str, x, y, z);
  }

  public void text(char[] chars, int start, int stop,
      float x, float y, float z) {
    intf.app.text(chars, start, stop, x, y, z);
  }

  public void text(String str, float x1, float y1, float x2, float y2) {
    intf.app.text(str, x1, y1, x2, y2);
  }

  public void text(int num, float x, float y) {
    intf.app.text(num, x, y);
  }

  public void text(int num, float x, float y, float z) {
    intf.app.text(num, x, y, z);
  }

  public void text(float num, float x, float y) {
    intf.app.text(num, x, y);
  }

  public void text(float num, float x, float y, float z) {
    intf.app.text(num, x, y, z);
  }

  public void pushMatrix() {
    intf.app.pushMatrix();
  }

  public void popMatrix() {
    intf.app.popMatrix();
  }

  public void translate(float x, float y) {
    intf.app.translate(x, y);
  }

  public void translate(float x, float y, float z) {
    intf.app.translate(x, y, z);
  }

  public void rotate(float angle) {
    intf.app.rotate(angle);
  }

  public void rotateX(float angle) {
    intf.app.rotateX(angle);
  }

  public void rotateY(float angle) {
    intf.app.rotateY(angle);
  }

  public void rotateZ(float angle) {
    intf.app.rotateZ(angle);
  }

  public void rotate(float angle, float x, float y, float z) {
    intf.app.rotate(angle, x, y, z);
  }

  public void scale(float s) {
    intf.app.scale(s);
  }

  public void scale(float x, float y) {
    intf.app.scale(x, y);
  }

  public void scale(float x, float y, float z) {
    intf.app.scale(x, y, z);
  }

  public void shearX(float angle) {
    intf.app.shearX(angle);
  }

  public void shearY(float angle) {
    intf.app.shearY(angle);
  }

  public void resetMatrix() {
    intf.app.resetMatrix();
  }

  public void applyMatrix(PMatrix source) {
    intf.app.applyMatrix(source);
  }

  public void applyMatrix(PMatrix2D source) {
    intf.app.applyMatrix(source);
  }

  public void applyMatrix(float n00, float n01, float n02,
      float n10, float n11, float n12) {
    intf.app.applyMatrix(n00, n01, n02, n10, n11, n12);
  }

  public void applyMatrix(PMatrix3D source) {
    intf.app.applyMatrix(source);
  }

  public void applyMatrix(float n00, float n01, float n02, float n03,
      float n10, float n11, float n12, float n13,
      float n20, float n21, float n22, float n23,
      float n30, float n31, float n32, float n33) {
    intf.app.applyMatrix(n00, n01, n02, n03, n10, n11, n12, n13, n20, n21, n22, n23, n30, n31, n32, n33);
  }


  public PMatrix getMatrix() {
    return intf.app.getMatrix();
  }

  public PMatrix2D getMatrix(PMatrix2D target) {
    return intf.app.getMatrix(target);
  }

  public PMatrix3D getMatrix(PMatrix3D target) {
    return intf.app.getMatrix(target);
  }

  public void setMatrix(PMatrix source) {
    intf.app.setMatrix(source);
  }

  public void setMatrix(PMatrix2D source) {
    intf.app.setMatrix(source);
  }

  public void setMatrix(PMatrix3D source) {
    intf.app.setMatrix(source);
  }

//  public void beginCamera() {
//    intf.canvas.beginCamera();
//  }
//
//  public void endCamera() {
//    intf.canvas.endCamera();
//  }
//
//  public void camera() {
//    intf.canvas.camera();
//  }
//
//  public void camera(float eyeX, float eyeY, float eyeZ,
//      float centerX, float centerY, float centerZ,
//      float upX, float upY, float upZ) {
//    intf.canvas.camera(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
//  }
//
//  public void ortho() {
//    intf.canvas.ortho();
//  }
//
//  public void ortho(float left, float right,
//      float bottom, float top) {
//    intf.canvas.ortho(left, right, bottom, top);
//  }
//
//  public void ortho(float left, float right,
//      float bottom, float top,
//      float near, float far) {
//    intf.canvas.ortho(left, right, bottom, top, near, far);
//  }
//
//  public void perspective() {
//    intf.canvas.perspective();
//  }
//
//  public void perspective(float fovy, float aspect, float zNear, float zFar) {
//    intf.canvas.perspective(fovy, aspect, zNear, zFar);
//  }
//
//  public void frustum(float left, float right,
//      float bottom, float top,
//      float near, float far) {
//    intf.canvas.frustum(left, right, bottom, top, near, far);
//  }
//
//  public float screenX(float x, float y) {
//    return intf.canvas.screenX(x, y);
//  }
//
//  public float screenY(float x, float y) {
//    return intf.canvas.screenY(x, y);
//  }
//
//  public float screenX(float x, float y, float z) {
//    return intf.canvas.screenX(x, y, z);
//  }
//
//  public float screenY(float x, float y, float z) {
//    return intf.canvas.screenY(x, y, z);
//  }
//
//  public float screenZ(float x, float y, float z) {
//    return intf.canvas.screenZ(x, y, z);
//  }
//
//  public float modelX(float x, float y, float z) {
//    return intf.canvas.modelX(x, y, z);
//  }
//
//  public float modelY(float x, float y, float z) {
//    return intf.canvas.modelY(x, y, z);
//  }
//
//  public float modelZ(float x, float y, float z) {
//    return intf.canvas.modelZ(x, y, z);
//  }

  public void pushStyle() {
    intf.app.pushStyle();
  }

  public void popStyle() {
    intf.app.popStyle();
  }

  public void style(PStyle s) {
    intf.app.style(s);
  }

  public void strokeWeight(float weight) {
    intf.app.strokeWeight(weight);
  }

  public void strokeJoin(int join) {
    intf.app.strokeJoin(join);
  }

  public void strokeCap(int cap) {
    intf.app.strokeCap(cap);
  }

  public void noStroke() {
    intf.app.noStroke();
  }

  public void stroke(int argb) {
    int maxAlpha = argb >> 24 & 0xFF;
    intf.app.stroke(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, getAlpha(maxAlpha));    
  }

  public void stroke(int argb, int maxa) {
    int maxAlpha = PApplet.min(argb >> 24 & 0xFF, maxa);
    intf.app.stroke(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, getAlpha(maxAlpha));    
  }  

//  public void stroke(int rgb, float alpha) {
//    intf.canvas.stroke(rgb, alpha);
//  }
//
//  public void stroke(float gray) {
//    intf.canvas.stroke(gray);
//  }
//
//  public void stroke(float gray, float alpha) {
//    intf.canvas.stroke(gray, alpha);
//  }
//
//  public void stroke(float v1, float v2, float v3) {
//    intf.canvas.stroke(v1, v2, v3);
//  }
//
//  public void stroke(float v1, float v2, float v3, float alpha) {
//    intf.canvas.stroke(v1, v2, v3, alpha);
//  }

  public void noTint() {
    intf.app.noTint();
  }

  public void tint(int argb) {
    int maxAlpha = argb >> 24 & 0xFF;
    intf.app.tint(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, getAlpha(maxAlpha));
  }

  public void tint(int argb, int maxa) {
    int maxAlpha = PApplet.min(argb >> 24 & 0xFF, maxa);
    intf.app.tint(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, getAlpha(maxAlpha));
  }  
  
//  public void tint(int rgb, float alpha) {
//    intf.canvas.tint(rgb, alpha);
//  }
//
//  public void tint(float gray) {
//    intf.canvas.tint(gray);
//  }
//
//  public void tint(float gray, float alpha) {
//    intf.canvas.tint(gray, alpha);
//  }
//
//  public void tint(float v1, float v2, float v3) {
//    intf.canvas.tint(v1, v2, v3);
//  }
//
//  public void tint(float v1, float v2, float v3, float alpha) {
//    intf.canvas.tint(v1, v2, v3, alpha);
//  }

  public void noFill() {
    intf.app.noFill();
  }

  public void fill(int argb) {    
    int maxAlpha = argb >> 24 & 0xFF;
    intf.app.fill(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, getAlpha(maxAlpha));
  }
  
  public void fill(int argb, int maxa) {
    int maxAlpha = PApplet.min(argb >> 24 & 0xFF, maxa);
    intf.app.fill(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, getAlpha(maxAlpha));
  }
  
  public int color(int gray) {
    return intf.app.color(gray);
  }

  public int color(float fgray) {
    return intf.app.color(fgray);
  }

  public int color(int gray, int alpha) {
    return intf.app.color(gray, alpha);
  }

  public int color(float fgray, float falpha) {
    return intf.app.color(fgray, falpha);
  }

  public int color(int v1, int v2, int v3) {
    return intf.app.color(v1, v2, v3);
  }

  public int color(int v1, int v2, int v3, int alpha) {
    return intf.app.color(v1, v2, v3, alpha);
  }

  public int color(float v1, float v2, float v3) {
    return intf.app.color(v1, v2, v3);
  }

  public int color(float v1, float v2, float v3, float alpha) {
    return intf.app.color(v1, v2, v3, alpha);
  }

  public int blendColor(int c1, int c2, int mode) {
    return PApplet.blendColor(c1, c2, mode);
  }
  
  public int replaceAlpha(int color, int alpha) {
    return (alpha) << 24 | (color & 0xFFFFFF);    
  }  
  
  public long millis() {
    return intf.app.millis();
  }
  
  
//  public void fill(int rgb, float alpha) {
//    intf.canvas.fill(rgb, alpha);
//  }
//
//  public void fill(float gray) {
//    intf.canvas.fill(gray);
//  }
//
//  public void fill(float gray, float alpha) {
//    intf.canvas.fill(gray, alpha);
//  }
//
//  public void fill(float v1, float v2, float v3) {
//    intf.canvas.fill(v1, v2, v3);
//  }
//
//  public void fill(float v1, float v2, float v3, float alpha) {
//    intf.canvas.fill(v1, v2, v3, alpha);
//  }
//
//  public void ambient(int rgb) {
//    intf.canvas.ambient(rgb);
//  }
//
//  public void ambient(float gray) {
//    intf.canvas.ambient(gray);
//  }
//
//  public void ambient(float v1, float v2, float v3) {
//    intf.canvas.ambient(v1, v2, v3);
//  }
//
//  public void specular(int rgb) {
//    intf.canvas.specular(rgb);
//  }
//
//  public void specular(float gray) {
//    intf.canvas.specular(gray);
//  }
//
//  public void specular(float v1, float v2, float v3) {
//    intf.canvas.specular(v1, v2, v3);
//  }
//
//  public void shininess(float shine) {
//    intf.canvas.shininess(shine);
//  }
//
//  public void emissive(int rgb) {
//    intf.canvas.emissive(rgb);
//  }
//
//  public void emissive(float gray) {
//    intf.canvas.emissive(gray);
//  }
//
//  public void emissive(float v1, float v2, float v3) {
//    intf.canvas.emissive(v1, v2, v3);
//  }
//
//  public void lights() {
//    intf.canvas.lights();
//  }
//
//  public void noLights() {
//    intf.canvas.noLights();
//  }
//
//  public void ambientLight(float v1, float v2, float v3) {
//    intf.canvas.ambientLight(v1, v2, v3);
//  }
//
//  public void ambientLight(float v1, float v2, float v3,
//      float x, float y, float z) {
//    intf.canvas.ambientLight(v1, v2, v3, x, y, z);
//  }
//
//  public void directionalLight(float v1, float v2, float v3,
//      float nx, float ny, float nz) {
//    intf.canvas.directionalLight(v1, v2, v3, nx, ny, nz);
//  }
//
//  public void pointLight(float v1, float v2, float v3,
//      float x, float y, float z) {
//    intf.canvas.pointLight(v1, v2, v3, x, y, z);
//  }
//
//  public void spotLight(float v1, float v2, float v3,
//      float x, float y, float z,
//      float nx, float ny, float nz,
//      float angle, float concentration) {
//    intf.canvas.spotLight(v1, v2, v3, x, y, z, nx, ny, nz, angle, concentration);
//  }
//
//  public void lightFalloff(float constant, float linear, float quadratic) {
//    intf.canvas.lightFalloff(constant, linear, quadratic);
//  }
//
//  public void lightSpecular(float v1, float v2, float v3) {
//    intf.canvas.lightSpecular(v1, v2, v3);
//  }
//
//  public void background(int rgb) {
//    intf.canvas.background(rgb);
//  }
//
//  public void background(int rgb, float alpha) {
//    intf.canvas.background(rgb, alpha);
//  }
//
//  public void background(float gray) {
//    intf.canvas.background(gray);
//  }
//
//  public void background(float gray, float alpha) {
//    intf.canvas.background(gray, alpha);
//  }
//
//  public void background(float v1, float v2, float v3) {
//    intf.canvas.background(v1, v2, v3);
//  }
//
//  public void background(float v1, float v2, float v3, float alpha) {
//    intf.canvas.background(v1, v2, v3, alpha);
//  }
//
//  public void clear() {
//    intf.canvas.clear();
//  }
//
//  public void background(PImage image) {
//    intf.canvas.background(image);
//  }
//
//  public void colorMode(int mode) {
//    intf.canvas.colorMode(mode);
//  }
//
//  public void colorMode(int mode, float max) {
//    intf.canvas.colorMode(mode, max);
//  }
//
//  public void colorMode(int mode, float max1, float max2, float max3) {
//    intf.canvas.colorMode(mode, max1, max2, max3);
//  }
//
//  public void colorMode(int mode,
//      float max1, float max2, float max3, float maxA) {
//    intf.canvas.colorMode(mode, max1, max2, max3, maxA);
//  }
//
//  public final float alpha(int rgb) {
//    return intf.canvas.alpha(rgb);
//  }
//
//  public final float red(int rgb) {
//    return intf.canvas.red(rgb);
//  }
//
//  public final float green(int rgb) {
//    return intf.canvas.green(rgb);
//  }
//
//  public final float blue(int rgb) {
//    return intf.canvas.blue(rgb);
//  }
//
//  public final float hue(int rgb) {
//    return intf.canvas.hue(rgb);
//  }
//
//  public final float saturation(int rgb) {
//    return intf.canvas.saturation(rgb);
//  }
//
//  public final float brightness(int rgb) {
//    return intf.canvas.brightness(rgb);
//  }
//
//  public int lerpColor(int c1, int c2, float amt) {
//    return intf.canvas.lerpColor(c1, c2, amt);
//  }
//
//  static public int lerpColor(int c1, int c2, float amt, int mode) {
//    return PGraphics.lerpColor(c1, c2, amt, mode);
//  }

  
  //////////////////////////////////////////////////////////////////////////////
  //  
  // Some additional utilities
     
  public String chopStringRight(String str, float maxw) {
    String chopped = str;
    float w = intf.app.textWidth(chopped);    
    while (w > maxw - 10) {
      int n = chopped.length() - 4;
      if (-1 < n) {
        chopped = chopped.substring(0, n) + "...";
      } else {
        return "";
      }
      w = intf.app.textWidth(chopped);
    }
    return chopped;    
  }  

//  public float textWidth(String str, PFont font) {
//    intf.canvas.textFont(font, font.getSize());
//    return intf.canvas.textWidth(str);
//  }

  public float textLeading() {
    return intf.app.g.textLeading;
  }

  public PFont textFont() {
    return intf.app.g.textFont;
  }

  public int fontSize() {
    return intf.app.g.textFont.getSize();
  } 
  
  public float[] textTopBottom(float y) {
    return new float[] {y - intf.app.textAscent(), y + intf.app.textDescent()};
  }
  
//  public float[] textTopBottom(float y, PFont font) {
//    intf.canvas.textFont(font);
//    return textTopBottom(y);
//  }  
  
  //////////////////////////////////////////////////////////////////////////////
  //  
  // Bound box class
  
  public class BoundBox {
    public float x0, y0;
    public float w0, h0;
    public SoftFloat x, y;
    public SoftFloat w, h;  
    
    BoundBox() {
      x = new SoftFloat();
      y = new SoftFloat();
      w = new SoftFloat();
      h = new SoftFloat();
    }
    
    boolean zero() {
      return Numbers.equal(w.get(), 0) || Numbers.equal(h.get(), 0);      
    }
  }
}

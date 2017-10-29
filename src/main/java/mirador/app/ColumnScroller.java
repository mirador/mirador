/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import processing.core.PApplet;
import mui.Interface;
import mui.SoftFloat;
import miralib.data.Variable;
import miralib.math.Geometry;
import miralib.math.Geometry.IntersectPoint;

/**
 * Class used to implement horizontal scrollers of large number of general.
 * items. The contents of the list are updated dynamicall depending on what is
 * visible to the user.
 *
 */

abstract public class ColumnScroller extends MiraWidget {  
  final protected static int SETUP         = 0;
  final protected static int WIDGET_UPDATE = 1;
  final protected static int ITEMS_UPDATE  = 2;
  final protected static int JUMP_TO       = 3;
  final protected static int DRAG          = 4;
  
  protected SoftFloat visX0, visX1;
  protected HashMap<Variable, Item> visItems;
  protected float itemWidth, itemHeight;
  protected boolean dragging;
  
  public ColumnScroller(Interface intf, float x, float y, float w, float h, 
      float iw, float ih) {
    this(intf, x, y, w, h, iw, ih, null);
  }
  
  public ColumnScroller(Interface intf, float x, float y, float w, float h, 
                        float iw, float ih, ColumnScroller init) {
    super(intf, x, y, w, h);
    visItems = new HashMap<Variable, Item>();
    if (init == null) {
      visX0 = new SoftFloat(0); 
      visX1 = new SoftFloat(width);      
    } else {
      visX0 = new SoftFloat(init.visX0.getTarget()); 
      visX1 = new SoftFloat(init.visX1.getTarget());      
    }
    itemWidth = iw; 
    itemHeight = ih;
  }
  
  public void setup() {
    for (int i = 0; i < getCount(); i++) {
      float x0 = i * itemWidth;
      float x1 = i * itemWidth + itemWidth;
      IntersectPoint pt = Geometry.intervalIntersection(visX0.get(), visX1.get(), x0, x1);
      if (pt.intersect && visible()) {
        Variable var = getVariable(i);
        Item item = createItem(var, itemWidth, itemHeight, SETUP);
        visItems.put(var, item);        
      }
    }
  }  
  
  public boolean inside(float x, float y) {
    return super.inside(x, y) || insideItems(x, y); 
  }  
  
  public void update() {
    visX0.update();
    visX1.update();
    updateItemsImpl(WIDGET_UPDATE);
  }  
  
  public void draw() {
    for (Item item: visItems.values()) {
      if (item.visible) item.draw();
    }      
  }

  public void postDraw() {
    for (Item item: visItems.values()) {
      if (item.visible) item.postDraw();
    }      
  }

  public float getTotalWidth() {
    return getCount() * itemWidth;
  }
  
  public boolean isUpdating() {
    return (visX0.isTargeting() || visX1.isTargeting());
  }
  
  public void mousePressed() {
    dragging = false;
  }  
  
  public void mouseDragged() {
    drag(pmouseX - mouseX);
    dragging = true;
  }

  public void mouseReleased() {
    if (dragging) {
      snap(); 
    } else {
      for (Item item: visItems.values()) {
        item.mouseReleased();
      }  
    }    
  }
  
  public void jumpTo(int idx) {
    drag(jumpToImpl(idx));    
  }  
  
  public void drag(float dx) {
    if (getCount() == 0) return;
    
    visX0.incTarget(dx);
    visX1.incTarget(dx);
    
    float minX0 = 0;
    float maxX0 = getCount() * itemWidth;
    
    if (visX0.getTarget() < minX0) {
      float dif = minX0 - visX0.getTarget();
      visX0.incTarget(dif);
      visX1.incTarget(dif);      
    }
    
    if (maxX0 < visX1.getTarget()) {
      float dif = maxX0 - visX1.getTarget();
      visX0.incTarget(dif);
      visX1.incTarget(dif);
      
      if (visX0.getTarget() < minX0) {
        dif = visX1.getTarget() - visX0.getTarget();
        visX0.setTarget(minX0);
        visX1.setTarget(minX0 + dif);        
      }
    }
    
    float x0 = visX0.getTarget();
    float x1 = visX1.getTarget();
    int i0 = PApplet.constrain((int)(x0 / itemWidth), 0, getCount() - 1);  
    int i1 = PApplet.constrain((int)(x1 / itemWidth), 0, getCount() - 1);
    for (int i = i0; i <= i1; i++) {
      Variable var = getVariable(i);
      if (!visItems.containsKey(var)) {
        Item item = createItem(var, itemWidth, itemHeight, DRAG);
        visItems.put(var, item);          
      }
    }  
  }  
  
  public void snap() {
    if (getCount() == 0) return;
    
    float x0 = visX0.getTarget();
    float x1 = visX1.getTarget();
    int i0 = PApplet.constrain(PApplet.round(x0 / itemWidth), 0, getCount() - 1);  
    int i1 = PApplet.constrain(PApplet.round(x1 / itemWidth), 0, getCount() - 1);    
    if (PApplet.abs(i0 * itemWidth - x0) < SNAP_THRESHOLD) {
      visX0.setTarget(i0 * itemWidth);
    } else if (PApplet.abs(i1 * itemWidth - x1) < SNAP_THRESHOLD) {
      visX1.setTarget(i1 * itemWidth);
    }
  } 
  
  public void dispose() {
    for (Item item: visItems.values()) {
      item.dispose();
    }      
  }
  
  public void dataChanged() {
    for (Item item: visItems.values()) {
      item.dataChanged();
    }    
  }  
  
  public void pvalueChanged() {
    for (Item item: visItems.values()) {
      item.pvalueChanged();
    }    
  }    
    
  public void updateItems() {
    updateItemsImpl(ITEMS_UPDATE);
  }
  
  public void close(Variable var) {
    Item item = visItems.get(var);
    if (item != null) {
      item.markedForRemoval = true;
    }
  }      
  
  public boolean contains(Variable var) {
    return visItems.containsKey(var);
  }


  public int getTotItemsCount() {
    return getCount();
  }


  public int getVisItemsCount() {
    return (int)(width / itemWidth);
  }


  public int getFirstItemIndex() {
    int i0 = PApplet.constrain((int)(visX0.getTarget() / itemWidth), 0, getCount() - 1);
    return i0;
  }


  protected void updateItemsImpl(int event) {
    if (0 < getCount()) {
      for (Item item: visItems.values()) {        
        item.updatePosition();
      }    
      int i0 = PApplet.constrain((int)(visX0.getTarget() / itemWidth), 0, getCount() - 1);  
      int i1 = PApplet.constrain((int)(visX1.getTarget() / itemWidth), 0, getCount() - 1);
      for (int i = i0; i <= i1; i++) {
        Variable var = getVariable(i);
        if (!visItems.containsKey(var)) {
          float x0 = i * itemWidth;
          float x1 = i * itemWidth + itemWidth;
          IntersectPoint pt = Geometry.intervalIntersection(visX0.get(), visX1.get(), x0, x1);
          if (pt.intersect && visible()) {
            Item item = createItem(var, itemWidth, itemHeight, event);
            visItems.put(var, item);          
          }
        }
      }
    }
    
    for (Item item: visItems.values()) {
      item.update();      
    }
    Set<Variable> keys = new HashSet<Variable>();
    for (Variable var: visItems.keySet()) {
      if (visItems.get(var).markedForRemoval) {
        keys.add(var);
      }
    }
    
    boolean removed = false;
    for (Variable var: keys) {
      Item item = visItems.get(var);
      if (item.markedForRemoval) {
        item.dispose();
        visItems.remove(var);
        removed = true;
//        Log.message("Removing item " + item + " for variable: " +  var.getName());
      }
    }
    if (removed && 0 < visItems.size()) {
      int i0 = PApplet.constrain((int)(visX0.getTarget() / itemWidth), 0, getCount() - 1);  
      int i1 = PApplet.constrain((int)(visX1.getTarget() / itemWidth), 0, getCount() - 1);
      if (itemWidth * i1 - i0 < width) {
        jumpTo(0);
      }
    }    
  }
  
  protected float jumpToImpl(int idx) {
    if (getCount() == 0) return 0;
    
    float x0 = visX0.getTarget();
    float x1 = visX1.getTarget();
    int i0 = PApplet.constrain((int)(x0 / itemWidth), 0, getCount() - 1);  
    int i1 = PApplet.constrain((int)(x1 / itemWidth), 0, getCount() - 1);
    
    float xi = idx * itemWidth;
    float dx = xi - visX0.getTarget();
    
    // Creating some items in between, even if they disappear during the drag 
    // animation, in order to avid an empty scroller while animating.
    int spaceBorder = i1 - i0;
    int spaceMiddle = PApplet.min(10, PApplet.max(1, getCount() / 50));
    if (idx < i0) {
      for (int i = idx; i < i0; i++) {
        if (i < idx + spaceBorder || i % spaceMiddle == 0 || i0 - spaceBorder < i) {
          Variable var = getVariable(i);
          if (!visItems.containsKey(var)) {
            Item item = createItem(var, itemWidth, itemHeight, JUMP_TO);
            visItems.put(var, item);
          }
        }
      }      
    } else if (i1 < idx) {
      for (int i = i1 + 1; i <= idx; i++) {
        if (i < i1 + spaceBorder || i % spaceMiddle == 0 ||  idx - spaceBorder < i) {
          Variable var = getVariable(i);
          if (!visItems.containsKey(var)) {
            Item item = createItem(var, itemWidth, itemHeight, JUMP_TO);
            visItems.put(var, item);          
          }          
        }        
      }      
    }
    
    return dx;
  }
  
  protected boolean insideItems(float x, float y) {
    for (Item item: visItems.values()) {
      if (item.inside(x - left, y - top)) return true;
    }
    return false;
  }
  
  protected boolean defaultAnimation(int event) {
    return event == WIDGET_UPDATE || event == ITEMS_UPDATE;
  }
  
  abstract protected Item createItem(Variable var, float w, float h, int event);
  abstract protected int getCount();
  abstract protected Variable getVariable(int i);
  abstract protected int getIndex(Variable var);
  
  abstract protected class Item {
    boolean visible;
    Variable var;
    SoftFloat x, y;
    SoftFloat h;
    float w;
    long t0;
    boolean showContents;
    boolean markedForRemoval;
    
    Item(Variable var, float w, float h, boolean anim) {
      this.var = var;
      this.w = w;
      this.h = new SoftFloat(h);
      int pos = getIndex(var);      
      if (anim) {
        x = new SoftFloat(var.getIndex() * w);
        x.setTarget(pos * w);        
      } else {
        x = new SoftFloat(pos * w);
      }
      y = new SoftFloat(0);       
      visible = false;
      showContents = false;
      markedForRemoval = false;
      t0 = mira.millis();      
    }
    
    void dispose() { }
    
    void updatePosition() {
      int pos = getIndex(var);
      if (-1 < pos) x.setTarget(pos * w);
    }    
    
    void update() {
      x.update();
      y.update();
      h.update();
      
      float x0 = x.get();
      float x1 = x0 + w;
      IntersectPoint pt = Geometry.intervalIntersection(visX0.get(), visX1.get(), x0, x1);
      
      long t = mira.millis();
      if (pt.intersect && visible()) {        
        if (!visible) {
          t0 = t;
        }
        visible = true;
        showContents |= t - t0 > SHOW_COL_DELAY;
      } else {
        if (visible) {
          t0 = t;
        }
        visible = false;
        markedForRemoval = t - t0 > REMOVE_COL_DELAY;
      }      
    }
    
    abstract void draw();
    void postDraw() { }
    
    boolean inside(float rx, float ry) {
      float x0 = x.get() - visX0.get();
      float y0 = y.get();      
      return x0 <= rx && rx <= x0 + w && y0 <= ry && ry <= y0 + h.get();
    }
    
    float left() {
      return x.get() - visX0.get();
    }
    
    float right() {
      return left() + w;
    }
    
    void mouseReleased() { }
    
    void dataChanged() { }
    
    void pvalueChanged() { }    
  }  
}

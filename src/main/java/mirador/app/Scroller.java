/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Set;
import java.util.TreeMap;

import mui.Interface;
import mui.SoftFloat;
import mui.Widget;
import processing.core.PApplet;

/**
 * Class used to implement horizontal scrollers of large number of general
 * items. The contents of the list are updated dynamically depending on what is
 * visible to the user at each time.
 *
 */

public abstract class Scroller<T extends Widget> extends MiraWidget {
  final public static int HORIZONTAL = 0;
  final public static int VERTICAL   = 1;
  
  final public static int SNAP_THRESHOLD = 50;
  final protected static int SHOW_DELAY  = 1000;
  final public static int REMOVE_DELAY   = 2000;
  
  protected float initItemWidth, initItemHeight;
  protected int orientation;
  protected boolean dragging;  
  protected SoftFloat visPos0, visPos1;
  protected TreeMap<Integer, Item> visItems;
  
  protected boolean insideDragBox;
  protected SoftFloat dragBox0, dragBox1;    
  protected  float[] lengths;
  protected float[] lengthSum;
  protected boolean[] closed;
  protected int open0, open1; 
  
  public Scroller(Interface intf, float x, float y, float w, float h) {
    super(intf, x, y, w, h);
  }
  
  public Scroller(Interface intf, float x, float y, float w, float h, 
                  float iw, float ih, int in) {
    this(intf, x, y, w, h, iw, ih, in, VERTICAL);
  }
  
  public Scroller(Interface intf, float x, float y, float w, float h, 
                  float iw, float ih, int n, int ori) {
    super(intf, x, y, w, h);
    initItems(n, iw, ih, ori);
  }
  
  public void setup() {
    if (visible()) {
      for (int i = 0; i < getItemCount(); i++) {
        if (isItemMissing(i, visPos0.get(), visPos1.get())) {
          Item item = createItem(i);
          visItems.put(i, item);        
        }
      }
    }
  }    
  
  public boolean inside(float x, float y) {
    return super.inside(x, y) || insideItems(x, y); 
  }  
  
  public float getOrientation() {
    return orientation;
  }
  
  public void update() {
    visPos0.update();
    visPos1.update();
    updateItemsImpl();

    Set<Entry<Integer, Scroller<T>.Item>> set = visItems.entrySet();
    Iterator<Entry<Integer, Scroller<T>.Item>> iterator = set.iterator();
    while (iterator.hasNext()) {
      Map.Entry<Integer, Item> entry = (Map.Entry<Integer, Item>)iterator.next();
      Item item = entry.getValue();
      lengths[item.index] = item.getTargetLength();
    }

    Arrays.fill(lengthSum, 0);
    int pi = open0;
    for (int i = open0; i <= open1; i++) {
      if (closed[i]) continue; 
      lengthSum[i] = lengthSum[pi] + lengths[i];
      pi = i;
    }

    float totWidth = lengthSum[open1];
    float f = (visPos1.getTarget() - visPos0.getTarget()) / totWidth;
    float f0 = visPos0.getTarget() / totWidth;
    dragBox0.setTarget(f0 * length());
    dragBox1.setTarget((f0 + f) * length());
    
    dragBox0.update();
    dragBox1.update();

    System.out.println(visPos0.getTarget() + " " + visPos1.getTarget());
  }

  public boolean isUpdating() {
    return visPos0.isTargeting() || visPos1.isTargeting();
  }
  
  public void mousePressed() {
    insideDragBox = false;
    if (dragBox0.get() <= mouse() && mouse() <= dragBox1.get()) {
      insideDragBox = true;
    }        
  }  
  
  public void mouseDragged() {
    float dif = mouseDif();
    if (insideDragBox) {      
      float totLength = lengthSum[open1];      
      float f = totLength / length();
      drag(-f * dif);
    } else {
      drag(dif);
    }
    dragging = true;
  }

  public void mouseReleased() {
    if (dragging) {
      snap(); 
    } else {
      float f = mouse() / length();
      float totLength = lengthSum[open1];
      int i = getCloserIndex(f * totLength);
      jumpTo(i);
    }
    dragging = false;
  }
  
  
  public void jumpTo(int idx) {
    drag(jumpToImpl(idx));      
  }  

  
  public void jumpToFirst() {
    jumpTo(open0);
  }  
  
  
  public void jumpToLast() {
    jumpTo(open1);
  }


  public void jumpToPrev() {
    float x0 = visPos0.getTarget();
    int i0 = getCloserIndex(x0);
    if (open0 < i0) {
      jumpTo(i0 - 1);
    }
  }
  
  
  public void jumpToNext() {
    float x0 = visPos0.getTarget();
    int i0 = getCloserIndex(x0);
    if (i0 < open1) {
      jumpTo(i0 + 1);
    }
  }
  
  
  public void drag(float dpos) {    
    if (getItemCount() == 0) return;
    
    visPos0.incTarget(dpos);
    visPos1.incTarget(dpos);

    float minp = getFirstPos();
    float maxp = getLastPos();
    
    if (visPos0.getTarget() < minp) {
      float dif = minp - visPos0.getTarget();
      visPos0.incTarget(dif);
      visPos1.incTarget(dif);      
    }
    
    if (maxp < visPos1.getTarget()) {
      float dif = maxp - visPos1.getTarget();
      visPos0.incTarget(dif);
      visPos1.incTarget(dif);
      
      if (visPos0.getTarget() < minp) {
        dif = visPos1.getTarget() - visPos0.getTarget();
        visPos0.setTarget(minp);
        visPos1.setTarget(minp + dif);        
      }
    }
    
    float x0 = visPos0.getTarget();
    float x1 = visPos1.getTarget();
    int i0 = getItemIndex(x0);
    int i1 = getItemIndex(x1);
    for (int i = i0; i <= i1; i++) {
      if (isItemMissing(i)) {
        Item item = createItem(i);
        visItems.put(i, item);        
      }
    }
  }
  
  public void snap() {
    if (getItemCount() == 0) return;

    float vis0 = visPos0.getTarget();
    float vis1 = visPos1.getTarget();
    int i0 = getCloserIndex(vis0);  
    int i1 = getCloserIndex(vis1);
    float pos0 = getItemPos(i0);
    float pos1 = getItemPos(i1);
    if (PApplet.abs(pos0 - vis0) < SNAP_THRESHOLD) {
      visPos0.setTarget(pos0);
    } else if (PApplet.abs(pos1 - vis1) < SNAP_THRESHOLD) {
      visPos1.setTarget(pos1);
    }
  }
  
  public void dispose() {
    for (Item item: visItems.values()) {
      item.dispose();
    }      
  }  
  
  public void open(int i) {    
    if (visItems.containsKey(i)) return;
    float pos0 = getItemPos(i);
    float pos1 = pos0 + getItemLength(i);
    float vis0 = visPos0.get(); 
    float vis1 = visPos1.get();
    float[] pt = Widget.intersect(vis0, vis1, pos0, pos1);
    if (pt != null) {
      Item item = createItem(i);
      visItems.put(i, item);
    }    
  }
  
  public void close(int i) {
    Item item = visItems.get(i);
    if (item != null) {
      item.markedForRemoval = true;
      item.markedForClosing = true;
    }
  }      
  
  protected void updateItemsImpl() {
    if (0 < getItemCount()) {
      for (Item item: visItems.values()) {        
        item.updatePosition();
      }

      if (visible()) {
        int i0 = getItemIndex(visPos0.getTarget());
        int i1 = getItemIndex(visPos1.getTarget());
        for (int i = i0; i <= i1; i++) {
          if (isItemMissing(i, visPos0.get(), visPos1.get())) {
            Item item = createItem(i);
            visItems.put(i, item);
          }
        }        
      }
    }
    
    for (Item item: visItems.values()) {
      item.update();      
    }

    ArrayList<Integer> keys = new ArrayList<Integer>();
    Set<Entry<Integer, Scroller<T>.Item>> set = visItems.entrySet();
    Iterator<Entry<Integer, Scroller<T>.Item>> iterator = set.iterator();
    while (iterator.hasNext()) {
       Map.Entry<Integer, Item> entry = (Map.Entry<Integer, Item>)iterator.next();
       int i = entry.getKey();       
       Item item = entry.getValue();       
       if (item.markedForRemoval) {
         keys.add(i);
       }
    }
    
    boolean removed = false;
    for (Integer i: keys) {
      Item item = visItems.get(i);
      if (item.markedForRemoval) {
        if (item.markedForClosing) {
          closed[i] = true;
          if (open0 == i) {
            for (int k = open0; k <= open1; k++)
            if (!closed[k]) { 
              open0 = k;
              break;
            }
          }          
          if (open1 == i) {
            for (int k = open1; k >= open0; k--)
            if (!closed[k]) {
              open1 = k;
              break;
            }
          }
        }
        
        item.dispose();
        visItems.remove(i);
        removed = true;
//        Log.message("Removing item " + i);
      }
    }
    if (removed && 0 < visItems.size()) {
      int i0 = getItemIndex(visPos0.getTarget());  
      int i1 = getItemIndex(visPos1.getTarget());
      if (getItemLength(i1) + getItemPos(i1) - getItemPos(i0) < length()) {
        jumpToFirst();
      }
    } else if (!dragging) {
      float x = getLastPos();
      float pos1 = visPos1.getTarget();
      if (x < pos1) {
        jumpToLast();          
      } else {
        int i1 = getItemIndex(pos1);
        if (i1 == open1 && x > pos1) {
          jumpToLast();  
        }
      }
    }
  }

  
  protected float jumpToImpl(int index) {
    if (getItemCount() == 0) return 0;
    
    float vis0 = visPos0.getTarget();
    float vis1 = visPos1.getTarget();
    int i0 = getItemIndex(vis0);  
    int i1 = getItemIndex(vis1);
    
    float posi = getItemPos(index);
    float dpos = posi - visPos0.getTarget();
    
    // Creating some items in between, even if they disappear during the drag 
    // animation, in order to avid an empty scroller while animating.
    int spaceBorder = i1 - i0;
    int spaceMiddle = PApplet.min(10, PApplet.max(1, getItemCount() / 50));
    if (index < i0) {
      for (int i = index; i < i0; i++) {
        if (i < index + spaceBorder || i % spaceMiddle == 0 || i0 - spaceBorder < i) {          
          if (isItemMissing(i)) {
            Item item = createItem(i);
            visItems.put(i, item);
          }
        }
      }      
    } else if (i1 < index) {
      for (int i = i1 + 1; i <= index; i++) {
        if (i < i1 + spaceBorder || i % spaceMiddle == 0 || index - spaceBorder < i) {
          if (!visItems.containsKey(i)) {
            Item item = createItem(i);
            visItems.put(i, item);
          }
        }        
      }      
    }
    
    return dpos;
  }
  
  protected boolean insideItems(float x, float y) {
    for (Item item: visItems.values()) {
      if (item.inside(x - left, y - top)) {
        return true;
      }
    }
    return false;
  }
  
  protected float length() {
    if (orientation == HORIZONTAL) {
      return width;
    } else {
      return height;
    }
  }
  
  protected float mouse() {
    if (orientation == HORIZONTAL) {
      return mouseX;
    } else {
      return mouseY;
    }
  }
  
  protected float mouseDif() {
    if (orientation == HORIZONTAL) {
      return pmouseX - mouseX;
    } else {
      return pmouseY - mouseY;
    }    
  }
  
  protected float getDragBoxLeft() {
    if (orientation == HORIZONTAL) {
      return dragBox0.get();
    } else {
      return 0;
    }
  }
  
  protected float getDragBoxTop() {
    if (orientation == HORIZONTAL) {
      return 0;
    } else {
      return dragBox0.get();
    }    
  }

  protected float getDragBoxWidth() {
    if (orientation == HORIZONTAL) {
      return dragBox1.get() - dragBox0.get();
    } else {
      return 20;
    }    
  }
  
  protected float getDragBoxHeight() {
    if (orientation == HORIZONTAL) {
      return 20;
    } else {
      return dragBox1.get() - dragBox0.get();
    }
  }
  
  abstract protected Item createItem(int index);
  
  protected int getItemCount() {
    return open1 - open0 + 1;
  }  
  
  protected float getItemWidth(int index) {
    if (orientation == HORIZONTAL) { 
      return lengths[index];
    } else {
      return initItemWidth;
    }
  }
  
  protected float getItemHeight(int index) {
    if (orientation == VERTICAL) { 
      return lengths[index];
    } else {
      return initItemHeight;
    }
  }
  
  protected float getItemLength(int index) {
    return lengths[index];
  }
  
  protected float getItemPos(int index) {
    if (0 < index) {
      return lengthSum[index] - getItemLength(index);
    }
    return 0;
  }

  protected int getItemIndex(float pos) {
    float psum0 = 0;
    float psum1 = 0;
    psum1 = 0;
    for (int i = open0; i <= open1; i++) {
      if (closed[i]) continue; 
      psum0 = psum1;
      psum1 += getItemLength(i);
      if (psum0 <= pos && pos <= psum1) return i; 
    }
    return open1;  
  }  

  protected int getCloserIndex(float pos) {
    float psum0 = 0;
    float psum1 = 0;
    psum1 = 0;
    int pi = open0;
    for (int i = open0; i <= open1; i++) {
      if (closed[i]) continue; 
      psum0 = psum1;
      if (open0 < i) psum1 += getItemLength(pi) / 2;
      psum1 += getItemLength(i) / 2;
      if (psum0 <= pos && pos <= psum1) return i;
      pi = i;
    }
    return open1;    
  }

  protected void initItems(int n, float iw, float ih, int ori) {
    orientation = ori;
    visItems = new TreeMap<Integer, Item>();
    visPos0 = new SoftFloat(0);
    visPos1 = new SoftFloat(length());
    
    initItemWidth = iw;
    initItemHeight = ih;
    
    dragBox0 = new SoftFloat(); 
    dragBox1 = new SoftFloat();
    
    lengths = new float[n];
    lengthSum = new float[n];
    closed = new boolean[n];
    
    float len = orientation == HORIZONTAL ? iw : ih; 
    Arrays.fill(lengths, len);
    for (int i = 0; i < lengthSum.length; i++) lengthSum[i] = (i + 1) * len;
    
    Arrays.fill(closed, false);
    open0 = 0;
    open1 = n - 1;    
  }
  
  protected float getFirstPos() {
    return getItemPos(open0);
  }
  
  protected float getLastPos() {    
    return getItemPos(open1) + getItemLength(open1); 
  }
  
  protected boolean isItemMissing(int index, float vis0, float vis1) {
    if (!isItemMissing(index)) return false;
    float pos0 = getItemPos(index);
    float pos1 = pos0 + getItemLength(index);
    float[] pt = Widget.intersect(vis0, vis1, pos0, pos1);
    return pt != null;
  }
  
  protected boolean isItemMissing(int index) {
    return !closed[index] && !visItems.containsKey(index);
  }
  
  public class Item {
    public int index;
    public T widget;
    protected boolean visible;
    protected SoftFloat x, y;
    protected float w, h;
    protected long t0;
    protected boolean showContents;
    protected boolean markedForRemoval;
    protected boolean markedForClosing;

    public Item(int idx, T wt) {
      this.index = idx;
      this.widget = wt;
      this.w = getItemWidth(index);
      this.h = getItemHeight(index);
      
      if (orientation == HORIZONTAL) {
        x = new SoftFloat(getItemPos(index));
        y = new SoftFloat(20);      
        wt.setOffsetX(-1, visPos0);        
      } else {
        x = new SoftFloat(20);
        y = new SoftFloat(getItemPos(index));      
        wt.setOffsetY(-1, visPos0);        
      }      
      wt.setPosition(x.get(), y.get());
      
      visible = false;
      showContents = false;
      markedForRemoval = false;
      markedForClosing = false;
      t0 = intf.app.millis();
      
      closed[index] = false;
      if (index < open0) open0 = index;
      if (index > open1) open1 = index;      
    }
    
    public void dispose() { 
      removeChild(widget);
    }
    
    public void updatePosition() {
      if (orientation == HORIZONTAL) {
        x.setTarget(getItemPos(index));
        widget.copyX(x, 0);        
      } else {
        y.setTarget(getItemPos(index));
        widget.copyY(y, 0);        
      }
    }
    
    public void update() {
      x.update();
      y.update();
      w = getItemWidth(index);
      h = getItemHeight(index);
      
      float vis0 = visPos0.get(); 
      float vis1 = visPos1.get();
      float pos0 = 0;
      float pos1 = 0;
      if (orientation == HORIZONTAL) {
        pos0 = x.get();
        pos1 = pos0 + w;
      } else {
        pos0 = y.get();
        pos1 = pos0 + h;        
      }      
      float[] pt = Widget.intersect(vis0, vis1, pos0, pos1);
      
      long t = intf.app.millis();
      if (pt != null && visible()) {        
        if (!visible) {
          t0 = t;
        }
        visible = true;
        showContents |= t - t0 > SHOW_DELAY;
      } else {
        if (visible) {
          t0 = t;
        }
        visible = false;
        markedForRemoval = t - t0 > REMOVE_DELAY;
      }
    }

    public boolean inside(float rx, float ry) {
      float x0 = 0;
      float y0 = 0;
      if (orientation == HORIZONTAL) {
        x0 = x.get() - visPos0.get();
        y0 = y.get();        
      } else {
        x0 = x.get();
        y0 = y.get() - visPos0.get();
      }
      return x0 <= rx && rx <= x0 + w && y0 <= ry && ry <= y0 + h;
    }
    
    public float getTargetLength() {
      if (orientation == HORIZONTAL) {
        return widget.targetWidth();
      } else {
        return widget.targetHeight();
      }
    }
  }
}

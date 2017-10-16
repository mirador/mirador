/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Set;
import java.util.TreeMap;

import mui.Display;
import mui.Interface;
import mui.SoftFloat;
import mui.Widget;
import processing.core.PApplet;

/**
 * Abstract class that can be used to implement horizontal or vertical scrollers with a large number of items.
 * Each item is associated with a MiraWidget, when items become visible they automatically create the corresponding
 * MiraWidget, and when become invisible they dispose them. Items also have an open/close state, which is different
 * from its visibility: an open item is one that can be scrolled to, whereas a closed item is temporarily disabled so
 * it will not show up in the scroller.
 *
 */

public abstract class Scroller<T extends MiraWidget> extends MiraWidget {
  int minDragHandlerSize = Display.scale(10);

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
  protected SoftFloat[] lengths;
  protected float[] lengthSum;
  protected boolean[] closed;
  protected int open0, open1; 


  public Scroller(Interface intf, float x, float y, float w, float h) {
    this(intf, x, y, w, h, VERTICAL);
  }


  public Scroller(Interface intf, float x, float y, float w, float h, int or) {
    super(intf, x, y, w, h);
    orientation = or;
  }


  public void setup() {
    initItems();

    if (visible()) {
      for (int i = 0; i < getOpenItemCount(); i++) {
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


  public void update() {
    visPos0.update();
    visPos1.update();
    updateItemsImpl();

    Set<Entry<Integer, Scroller<T>.Item>> set = visItems.entrySet();
    Iterator<Entry<Integer, Scroller<T>.Item>> iterator = set.iterator();
    while (iterator.hasNext()) {
      Map.Entry<Integer, Item> entry = (Map.Entry<Integer, Item>) iterator.next();
      Item item = entry.getValue();
      if (closed[item.index]) {
        // Closed items have their length animating towards zero, so we need to update.ok
        lengths[item.index].update();
      } else {
        lengths[item.index].set(item.getLength());
      }
    }

    Arrays.fill(lengthSum, 0);
    int pi = open0;
    for (int i = open0; i <= open1; i++) {
      lengthSum[i] = lengthSum[pi] + lengths[i].get();
      pi = i;
    }

    float totWidth = lengthSum[open1];
    float f = (visPos1.getTarget() - visPos0.getTarget()) / totWidth;
    float f0 = visPos0.getTarget() / totWidth;
    float p0 = f0 * length();
    float p1 = p0 + PApplet.max(f * length(), minDragHandlerSize);
    dragBox0.setTarget(p0);
    dragBox1.setTarget(p1);
    
    dragBox0.update();
    dragBox1.update();
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
    if (getOpenItemCount() == 0) return;
    
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
    if (getOpenItemCount() == 0) return;

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


  public void attach(MiraWidget scrollWt, MiraWidget attachWt) {
    int i = scrollWt.getIndex();
    Item item = visItems.get(i);
    if (item != null) {
      item.attach(attachWt);
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


  public int getFirstVisible() {
    float x0 = visPos0.getTarget();
    return getItemIndex(x0);
  }


  public int getLastVisible() {
    float x1 = visPos1.getTarget();
    return getItemIndex(x1);
  }


  abstract protected int getTotalItemCount();
  abstract protected Item createItem(int index);
  abstract protected boolean itemIsOpen(int index);

//  abstract protected float getWidth(int index);
//  abstract protected float getHeight(int index);
  abstract protected float getTargetWidth(int index);
  abstract protected float getTargetHeight(int index);


  // Remove?
  protected float getItemWidth(int index) {
    if (orientation == HORIZONTAL) {
      return lengths[index].get();
    } else {
      return getTargetWidth(index);
    }
  }


  // Remove?
  protected float getItemHeight(int index) {
    if (orientation == VERTICAL) {
      return lengths[index].get();
    } else {
      return getTargetHeight(index);
    }
  }


  protected float getItemLength(int index) {
    return lengths[index].get();
  }


  protected void updateItemsImpl() {
    if (0 < getOpenItemCount()) {
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
        lengths[i].setTarget(0);
        visItems.remove(i);
        removed = true;
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
    if (getOpenItemCount() == 0) return 0;
    
    float vis0 = visPos0.getTarget();
    float vis1 = visPos1.getTarget();
    int i0 = getItemIndex(vis0);  
    int i1 = getItemIndex(vis1);
    
    float posi = getItemPos(index);
    float dpos = posi - visPos0.getTarget();
    
    // Creating some items in between, even if they disappear during the drag 
    // animation, in order to avid an empty scroller while animating.
    int spaceBorder = i1 - i0;
    int spaceMiddle = PApplet.min(10, PApplet.max(1, getOpenItemCount() / 50));
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


  protected int getOpenItemCount() {
    return open1 - open0 + 1;
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


  protected void initItems() {
    int n = getTotalItemCount();

    visItems = new TreeMap<Integer, Item>();
    visPos0 = new SoftFloat(0);
    visPos1 = new SoftFloat(length());
    
    dragBox0 = new SoftFloat();
    dragBox1 = new SoftFloat();
    
    lengths = new SoftFloat[n];
    lengthSum = new float[n];
    closed = new boolean[n];
    
    open0 = n - 1;
    open1 = 0;

    float sum0 = 0;
    for (int i = 0; i < lengthSum.length; i++) {
      closed[i] = !itemIsOpen(i);
      float len = 0;
      if (itemIsOpen(i)) {
        len = orientation == HORIZONTAL ? getTargetWidth(i) : getTargetHeight(i);
        closed[i] = false;
        if (i < open0) open0 = i;
        if (open1 < i) open1 = i;
      } else {
        closed[i] = true;
      }
      lengths[i] = new SoftFloat(len);
      lengthSum[i] = sum0 + len;
      sum0 = lengthSum[i];
    }
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
    protected boolean markedForRemoval;
    protected boolean markedForClosing;
    protected ArrayList<MiraWidget> attached = new ArrayList<MiraWidget>();


    public Item(int idx, T wt) {
      this.index = idx;
      this.widget = wt;
      this.w = wt.targetWidth();  // getItemWidth(index);
      this.h = wt.targetHeight(); //getItemHeight(index);
      widget.showContents = false;

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
      wt.setIndex(idx);

      visible = false;
      markedForRemoval = false;
      markedForClosing = false;
      t0 = intf.app.millis();
      
      closed[index] = false;
      if (index < open0) open0 = index;
      if (index > open1) open1 = index;      
    }


    public void attach(MiraWidget wt) {
      attached.add(wt);
      wt.showContents = false;
      if (orientation == HORIZONTAL) {
        wt.setOffsetX(-1, visPos0);
        wt.setX(x.get());
      } else {
        wt.setOffsetY(-1, visPos0);
        wt.setY(y.get());
      }
    }


    public void dispose() {
      widget.removeSelf();
      for (MiraWidget wt: attached) {
        System.out.println("removing attached widget " + wt);
        wt.removeSelf();
      }
    }


    public void updatePosition() {
      if (orientation == HORIZONTAL) {
        x.setTarget(getItemPos(index));
        if (!x.isTargeting()) return;
        widget.copyX(x, 0);
        for (MiraWidget wt: attached) wt.copyX(x, 0);
      } else {
        y.setTarget(getItemPos(index));
        if (!y.isTargeting()) return;
        widget.copyY(y, 0);
        for (MiraWidget wt: attached) wt.copyY(y, 0);
      }
    }


    public void update() {
      x.update();
      y.update();
      w = widget.width();  //getItemWidth(index);
      h = widget.height(); //getItemHeight(index);
      
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
      float[] vpt = Widget.intersect(vis0, vis1, pos0, pos1);

      long t = intf.app.millis();
      if (vpt != null && visible()) {
        if (!visible) {
          t0 = t;
        }
        visible = true;
        widget.showContents |= t - t0 > SHOW_DELAY;
        for (MiraWidget wt: attached) wt.showContents |= t - t0 > SHOW_DELAY;
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

    
    public float getLength() {
      if (orientation == HORIZONTAL) {
        return widget.width();
      } else {
        return widget.height();
      }
    }
  }
}

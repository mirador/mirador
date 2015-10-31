/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mirador.ui;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import miralib.math.Geometry;
import miralib.math.Numbers;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PShape;

/**
 * Root class for a UI, where Widgets can be added to. It handles keyboard and 
 * mouse event handling, clipping, update.
 *
 */

public class Interface implements PConstants {
  public static boolean SHOW_DEBUG_INFO = false;
  
  static public int INIT   = 0;
  static public int UPDATE = 1;
  static public int DRAW   = 2;
  static public int IDLE   = 3;
  
  static public char[] ALL_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
  
  public PApplet app;
  protected Widget root;
  protected Widget selected;
  protected ArrayList<Widget> drawnWidgets;
  
  protected int bckColor;
  
  protected boolean requestedRecord = false;
  protected boolean recording = false;
  protected String recFilename;  
  
  protected int state;
  protected boolean enabled;  
  
  protected boolean usingKeymap;
  protected HashMap<Character, Widget> keyMap;  
  protected HashMap<Integer, Widget> codeMap;
  
  private ClipRect viewport, currentClip;
  private Stack<ClipRect> clipStack;    
  
  private HashMap<String, PImage> imageCache;
  private HashMap<String, PFont> fontCache;
  private HashMap<String, PShape> shapeCache;
  private Style style;

  public Interface(PApplet app) {
    this(app, null);
  }
  
  public Interface(PApplet app, String cssfile) {
    this.app = app;
    
    root = new Widget(this);
    selected = null;
    drawnWidgets = new ArrayList<Widget>();
    state = INIT;
    
    viewport = currentClip = new ClipRect(0, 0, app.width, app.height);
    clipStack = new Stack<ClipRect>();    
    
    imageCache = new HashMap<String, PImage>();
    fontCache = new HashMap<String, PFont>();
    shapeCache = new HashMap<String, PShape>();
    
    if (cssfile != null && !cssfile.equals("")) {
      style = new Style(app.createInput(cssfile));
    }
    
    bckColor = 0xFFFFFFFF;
    enabled = true;
  }  
  
  public boolean isEnabled() {
    return enabled;
  }
  
  public void enable() {
    enabled = true;
  }

  public void disable() {
    enabled = false;
  }
  
  public void setBackground(int color) {
    bckColor = color;
  }
  
  public PGraphics mainCanvas() {
    return app.g;
  }
  
  public PGraphics createCanvas(int w, int h, String renderer, int quality) {
    PGraphics pg = app.createGraphics(w, h, renderer);
    pg.smooth(quality);
    return pg;
  } 
  
  public ArrayList<Widget> getWidgets(Class<? extends Widget> clazz) {
    ArrayList<Widget> res = new ArrayList<Widget>();    
    root.getChildren(clazz, res);    
    return res;
  }
  
  @SuppressWarnings("rawtypes")
  public void invoke(Class<? extends Widget> clazz, String method, Object... args) {
    ArrayList<Widget> widgets = getWidgets(clazz); 
    Class[] types = new Class[args.length];
    for (int i = 0; i < types.length; i++) {
      types[i] = args[i].getClass();
    }   
    for (Widget wt: widgets) {
      try {
        Method meth = wt.getClass().getMethod(method, types);
        meth.invoke(wt, args);        
      } catch (Exception e) { 
        e.printStackTrace();
      }      
    } 
  }
  
  //////////////////////////////////////////////////////////////////////////////
  //  
  // Viewport    
  
  public void setSize(float w, float h) {
    viewport.w = w;
    viewport.h = h;
  }
  
  public float left() {
    return currentClip.left();
  }
  
  public float right() {
    return currentClip.right();
  }
  
  public float top() {
    return currentClip.top();
  }  
  
  public float bottom() {
    return currentClip.bottom();
  }
  
  public float width() {
    return currentClip.width();
  }
  
  public float height() {
    return currentClip.height();
  }  
  
  public void pushClip() {
    clipStack.push(currentClip);
  }
  
  public void setClip(float cx, float cy, float cw, float ch) {    
    if (!currentClip.equalsTo(cx, cy, cw, ch)) {
      
      Geometry.IntersectPoint ipx = 
          Geometry.intervalIntersection(currentClip.left(), currentClip.right(), 
                                        cx, cx + cw);
      Geometry.IntersectPoint ipy = 
        Geometry.intervalIntersection(currentClip.top(), currentClip.bottom(), 
                                      cy, cy + ch);
      
      cx = ipx.a;
      cw = ipx.b - ipx.a;

      cy = ipy.a;
      ch = ipy.b - ipy.a;
        
      currentClip = new ClipRect(cx, cy, cw, ch);
      if (state == DRAW) currentClip.set();
    }    
  }
  
  public void popClip() {
    ClipRect clip = clipStack.pop();
    if (!currentClip.equalsTo(clip)) {
      currentClip = clip;
      if (state == DRAW) currentClip.set();  
    }
  }
  
  protected class ClipRect {
    float x, y;
    float w, h;
    
    ClipRect(float x, float y, float w, float h) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;      
    }
    
    void set() {
      if (recording) return; // TODO: clipping doesn't work with PDF recording.
      if (equalsTo(0, 0, app.width, app.height)) {
        app.noClip();
      } else {  
        app.clip(x, y, w, h);
      }
    }
    
    boolean equalsTo(float x, float y, float w, float h) {
      return Numbers.equal(this.x, x) && Numbers.equal(this.y, y) && 
             Numbers.equal(this.w, w) && Numbers.equal(this.h, h);
    }
    
    boolean equalsTo(ClipRect other) {
      return equalsTo(other.x, other.y, other.w, other.h);      
    }

    float left() {
      return x;
    }    
    
    float right() {
      return x + w;
    }
    
    float top() {
      return y;
    }    
    
    float bottom() {
      return y + h;
    }
    
    float width() {
      return w;
    }
    
    float height() {
      return h;
    }    
  }
   
  //////////////////////////////////////////////////////////////////////////////
  //  
  // Updating and drawing
  
  public void update() {
    state = UPDATE;
    handleResize();
    root.updateChildren();
    state = IDLE;
  }
  
  public void draw() {    
    if (requestedRecord) {
      recording = true;
      app.beginRecord(PDF, recFilename);
    }
    
    app.background(bckColor);
    state = DRAW;
    clearDrawn();    
    root.drawChildren();
    state = IDLE;
    
    if (recording) {
      app.endRecord();
      requestedRecord = false;
    }
            
    if (SHOW_DEBUG_INFO && app.frameCount % 180 == 0) {
      System.out.println("framerate: " + app.frameRate);      
      System.out.println("number of drawn widgets    : " + drawnWidgets.size());
    }
  }
  
  protected void clearDrawn() {
    drawnWidgets.clear();
  }

  protected void addDrawn(Widget widget) {
    drawnWidgets.add(widget);
  }
  
  protected void handleResize() {
    if (Numbers.different(viewport.w, app.width) || 
        Numbers.different(viewport.h, app.height)) {
      setSize(app.width, app.height);
      root.resizeChildren(app.width, app.height);
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  //  
  // PDF output
  
  public void record(String filename) {
    requestedRecord = true;
    recFilename = filename; 
  }
  
  //////////////////////////////////////////////////////////////////////////////
  //  
  // Mouse
  
  public boolean isSelectedWidget(Widget wt) {
    return wt == selected;
  }
  
  public Widget getSelectedWidget() {
    return selected;
  }
  
  public void selectWidget(Widget wt) {    
    for (int i = 0; i < drawnWidgets.size(); i++) {
      drawnWidgets.get(i).focused = false;
    }
    if (selected != wt && selected != null) selected.lostFocus();
    selected = wt;
    if (selected != null) {
      selected.focused = true;
    }    
  }
  
  public void mousePressed() {
    if (!enabled) return;
    
    Widget selected0 = selected;
    selected = null;
    
    for (int i = 0; i < drawnWidgets.size(); i++) {
      drawnWidgets.get(i).focused = false;
    }
    
    // Reverse iterate so the widgets drawn on top are selected first.
    for (int i = drawnWidgets.size() - 1; i >= 0; i--) {
      Widget wt = drawnWidgets.get(i);
      if (wt.isClickable() && wt.mouseFocused(app.mouseX, app.mouseY)) {
        if (selected0 != wt && selected0 != null) {          
          selected0.lostFocus();
        }
        selected = wt;
        if (selected.parent != null && selected.parent.hovered) {
          selected.parent.hovered = false;
          selected.parent.hoverOut();
        }
        break;
      }
    }
    
    if (selected != null) {
      Widget handler = selected;
      handler.updateMouse(app.mouseX, app.mouseY, app.pmouseX, app.pmouseY);
      handler.mousePressed();
    }
  }

  public void mouseDragged() {
    if (!enabled) return;
    
    if (selected != null) {
      Widget handler = selected;
      boolean dragParent = false;
      if (handler.isDraggable()) {
        handler.updateMouse(app.mouseX, app.mouseY, app.pmouseX, app.pmouseY);
        handler.mouseDragged();
      } else {
        dragParent = true;
      }
      if (dragParent) {
        // Passing the drag event up to a focused, draggable parent
        handler.focused = false;
        selected = handler.parent;
        while (handler.parent != null) {
          if (handler.mouseFocused(app.mouseX, app.mouseY) && 
              handler.isDraggable()) {
            handler.updateMouse(app.mouseX, app.mouseY, 
                                app.pmouseX, app.pmouseY);
            handler.mousePressed();
            handler.mouseDragged();
          }
          handler.focused = false;
          handler = handler.parent;
        }        
      }
    }
  }  

  public void mouseReleased() {
    if (!enabled) return;
    
    if (selected != null) {
      Widget handler = selected;      
      handler.updateMouse(app.mouseX, app.mouseY, app.pmouseX, app.pmouseY);
      handler.mouseReleased();
      if (handler.focused) handler.handle();
    }    
    handleHover(false);   
  }
  
  public void mouseMoved() {
    if (!enabled) return;
    
    for (Widget wt: drawnWidgets) {
      wt.updateMouse(app.mouseX, app.mouseY, app.pmouseX, app.pmouseY);
    }    
    handleHover(true);
  }
  
  protected void handleHover(boolean triggerMoved) {
    // Reverse iterate so the widgets drawn on top are selected first.
    boolean found = false;
    for (int i = drawnWidgets.size() - 1; i >= 0; i--) {
      Widget wt = drawnWidgets.get(i);
      if (!found && wt.hoverable && wt.inside(app.mouseX, app.mouseY) && 
                                    wt.insideClip(app.mouseX, app.mouseY)) {
        if (!wt.hovered) {
          wt.hovered = true;
          wt.hoverIn();          
        }        
        if (triggerMoved) drawnWidgets.get(i).mouseMoved();          
        found = true;
      } else if (wt.hovered) {
        wt.hovered = false;
        wt.hoverOut();        
      }
    }    
  }
  
  //////////////////////////////////////////////////////////////////////////////
  //  
  // Keyboard
  
  public void addKeymap(Widget wt, char... keys) {
    if (!usingKeymap) {
      usingKeymap = true;  
      keyMap = new HashMap<Character, Widget>();
      codeMap = new HashMap<Integer, Widget>();
    }    
    for (char key: keys) keyMap.put(key, wt);
  }

  public void addKeymap(Widget wt, int... codes) {
    if (!usingKeymap) {
      usingKeymap = true;  
      keyMap = new HashMap<Character, Widget>();
      codeMap = new HashMap<Integer, Widget>();
    }    
    for (int code: codes) codeMap.put(code, wt);
  }  
  
  public void keyPressed() {
    if (!enabled) return;
    
    Widget handler = null;
    if (usingKeymap && (selected == null || !selected.isCapturingKeys())) {
      handler = app.key == CODED ? codeMap.get(app.keyCode) : keyMap.get(app.key);
    } else {  
      handler = selected;
    }

    if (handler != null) {
      handler.updateKey(app.key, app.keyCode);
      handler.keyPressed();
    }  
    if (app.key == ESC) {
      app.key = 0;
    }     
  }
  
  public void keyReleased() {
    if (!enabled) return;
    
    Widget handler = null;    
    if (usingKeymap && (selected == null || !selected.isCapturingKeys())) {
      handler = app.key == CODED ? codeMap.get(app.keyCode) : keyMap.get(app.key);
    } else {  
      handler = selected;
    }       
    if (handler != null) {
      handler.updateKey(app.key, app.keyCode);     
      handler.keyReleased();
      if (handler.focused) handler.handleKey();
    }    
  }
  
  public void keyTyped() {
    if (!enabled) return;
    
    Widget handler = null;
    if (usingKeymap && (selected == null || !selected.isCapturingKeys())) {
      handler = app.key == CODED ? codeMap.get(app.keyCode) : keyMap.get(app.key);
    } else {  
      handler = selected;
    }    
    if (handler != null) {
      handler.updateKey(app.key, app.keyCode);
      handler.keyTyped();
    }      
  }  
  
  //////////////////////////////////////////////////////////////////////////////
  //  
  // Adding/removing widgets
  
  public void add(Widget widget) {
    root.addChild(widget);
  }
  
  public void remove(Widget widget) {
    root.removeChild(widget);
  } 
  
  //////////////////////////////////////////////////////////////////////////////
  //  
  // Resources cache

  public PImage loadImage(String filename) {
    PImage image = imageCache.get(filename);
    if (image == null) {
      image = app.loadImage(filename);
      imageCache.put(filename, image);
    }
    return image;
  }  
  
  public PFont loadFont(String filename, int size) {
    String id = filename + "@" + size + "pt";
    PFont font = fontCache.get(id);
    if (font == null) {
      font = app.createFont(filename, size);
      fontCache.put(id, font);
    }
    return font;
  }
  
  public PShape loadShape(String filename) {
    PShape shape = shapeCache.get(filename);
    if (shape == null) {
      shape = app.loadShape(filename);
      shapeCache.put(filename, shape);
    }
    return shape;
  }   
  
  //////////////////////////////////////////////////////////////////////////////
  //  
  // Style  
  
  public String getStyle(String clazz, String property) {
    return style.get(clazz, property, "");
  }
  
  public int getStyleColor(String clazz, String property) {
    return style.getColor(clazz, property, 0x0);
  } 
  
  public int getStyleAlign(String clazz, String property) {
    return style.getAlign(clazz, property, 0);
  }
  
  public float getStylePosition(String clazz, String property) {
    return style.getPosition(clazz, property, 0.0f);
  }      
  
  public float getStyleSize(String clazz, String property) {
    return style.getSize(clazz, property, 0.0f);
  }    

  public PFont getStyleFont(String clazz, 
                            String nameProperty, String sizeProperty) {    
    return getStyleFont(clazz, nameProperty, sizeProperty, 1);
  }  
  
  public PFont getStyleFont(String clazz, 
                            String nameProperty, String sizeProperty, 
                            float sizeScale) {    
    String fn = style.getFont(clazz, nameProperty, "");
    int size = Math.round(style.getSize(clazz, sizeProperty, 0.0f) * sizeScale);
    return loadFont(fn, size);
  }  
   
  public PImage getStyleImage(String clazz, String property) {
    String fn = style.getImage(clazz, property, "");
    return loadImage(fn);
  }      
  
  public PShape getStyleShape(String clazz, String property) {
    String fn = style.getShape(clazz, property, "");
    return loadShape(fn);
  }    
}

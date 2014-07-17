/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mira.app;

import processing.core.PApplet;
import processing.core.PConstants;
import lib.ui.Interface;
import lib.ui.Widget;
import mira.data.DataSet;

/**
 * Mirador widget, which add some extra functionality specific to Mirador, 
 * mainly indexing and timeout, in addition to references to the data and main
 * app object.
 *
 * @author Andres Colubri
 */

public class MiraWidget extends Widget {
  final public static int SNAP_THRESHOLD = 30;
  final public static int SHOW_COL_DELAY = 1000;
  final public static int REMOVE_COL_DELAY = 5000;
  final public static int REMOVE_ROW_DELAY = 5000;  
  
  final protected static float padding = 2;
  
  protected MiraApp mira;
  protected DataSet data;
  
  protected int idx;
  protected long t0;
  protected boolean pvisible;
  protected boolean timedOut;
  protected long timeOut;  
  
  public MiraWidget(Interface intf) {
    super(intf);
    init(); 
  }
  
  public MiraWidget(Interface intf, float x, float y, float w, float h) {
    super(intf, x, y, w, h);
    init();   
  }
  
  public MiraWidget(Interface intf, float x, float y, float w, float h, 
                   boolean tw, boolean th) {
    super(intf, x, y, w, h, tw, th);
    init();   
  }

  public MiraWidget(Interface intf, float x0, float y0, float x1, float y1, 
                   float w0, float h0, float w1, float h1) {
    super(intf, x0, y0, x1, y1, w0, h1, w1, h1);
    init();  
  }
  
  public void setIndex(int i) {
    this.idx = i;
  }
  
  public void setTimeOut(long t) {
    timeOut = t;
  }
  
  public boolean timedOut() {
    return timedOut;
  }
  
  public boolean visible() {
    boolean vis = super.visible();
    
    if (0 < timeOut) {
      long t = mira.millis();
      if (vis) {        
        if (!pvisible) {
          t0 = t;
          timedOut = false;
        }
      } else {
        if (pvisible) {
          t0 = t;
        }
        timedOut = t - t0 > timeOut;
      }        
    }
    
    pvisible = vis;      
    return vis;
  }
  
  public void mouseReleased(MiraWidget wt) { }
  public void keyPressed(MiraWidget wt) { }
  public void dragolumns(float dx) { }
  public void dragRows(float dy) { }
  
  protected void init() {
    mira = (MiraApp)intf.app;
    data = mira.dataset;
    
    t0 = mira.millis();
    pvisible = false;
    timedOut = false;
    timeOut = 0;
  }
    
  public void text(EditableText str, float x, float y) {
    str.draw(x,  y);
  }
  
  public float textWidth(EditableText str) {
    return str.width();
  }
  
  // Class to handle a (single) line of editable text
  class EditableText {
    static final int CURSOR_HIDDEN = 0;
    static final int CURSOR_VISIBLE = 1;
    
    StringBuffer text;
//    PFont font;
    boolean focused;
    protected int pos = 0;
    protected int pos0 = 0;    
    boolean bounded;    
    float boundw;
    boolean clearInitial;
    
    int cursorStatus;
    int lastCursorChange;
    
    EditableText(String text) {
//      this.font = font;
      this.text = new StringBuffer(text);
      pos = text.length();
      pos0 = 0;      
      focused = false;
      bounded = false;
      clearInitial = false;
    }
        
    void setBound(float w) {
      bounded = true;      
      boundw = w;
    }
    
    String get() {
      return text.substring(pos0, text.length());
    }

    void set(String text) {
      this.text = new StringBuffer(text);
      pos = text.length();
      pos0 = 0;      
    }
    
    void clearInitial() {
      clearInitial = true;
    } 
    
    float width() {
//      textFont(font);
      return textWidth(get());
    } 
    
    float getFloat() {
      try {
        return new Float(get()).floatValue();
      } catch (NumberFormatException e) { 
        return Float.NaN;
      }      
    }
    
    void setFocused(boolean focus) {
      if (focus != focused) {
        boolean focused0 = focused;
        focused = focus;
        if (focused) {
          cursorStatus = CURSOR_VISIBLE;
          lastCursorChange = intf.app.millis();
          if (!focused0 && clearInitial) {
            set("");
          }
        }
      }
    }
    
    boolean isFocused() {
      return focused;
    }
    
    boolean inside(float x, float y, float mx, float my) {
//      textFont(font);
      float[] tb = textTopBottom(y);
      float ytop = tb[0];
      float ybot = tb[1];
      float w = 0;        
      if (bounded) {
        w = boundw;       
      } else {
        w = width();
      }      
      return x <= mx && mx <= x + w && ytop <= my && my <= ybot;
    }    
    
    void draw(float x, float y) {
//      fill(color);
//      textFont(font);
      
      String visible = text.substring(pos0, text.length());
      text(visible, x, y);
      
      if (focused) {
        // Draw blinking cursor.
        int t = intf.app.millis();
        if (500 < t - lastCursorChange) {
          if (cursorStatus == CURSOR_VISIBLE) {
            cursorStatus = CURSOR_HIDDEN;
          } else {
            cursorStatus = CURSOR_VISIBLE;
          }
          lastCursorChange = t;
        }
        
        if (cursorStatus == CURSOR_VISIBLE) {
          int curPos = pos - pos0;
          String substr = visible.substring(0, curPos);
          float len = textWidth(substr);    
          //text("_", x + len, y);
//          stroke(color);
          line(x + len, y, x + len, y - fontSize());
        }        
      }     
    }
    
    void keyPressed(char key, int code)  {
      if (!focused) return;
      
      if (key != PConstants.CODED) {
        if (key == PConstants.BACKSPACE) {
          if (0 < pos && 0 < text.length()) {
            // Delete character before cursor.
            text.deleteCharAt(pos - 1); 
            pos -= 1;
            if (pos < pos0) {
              pos0 = pos;
            }
          }
        } else if (key == PConstants.DELETE) {
          if (0 < text.length()) {
            // Delete character at cursor.
            text.deleteCharAt(pos); 
          }
        } else if (key != PConstants.TAB && 
                   key != PConstants.ENTER && 
                   key != PConstants.RETURN && 
                   key != PConstants.ESC) {
          // Add character after cursor.
          text.insert(pos, key); 
          pos += 1;      
          fixVisibleText();
        }
      } else {
        if (code == PConstants.LEFT) {
          // Shift text one character to the left.
          pos = PApplet.max(0, pos - 1);
          if (pos < pos0) {
              pos0 = pos;  
          }      
        } else if (code == PConstants.RIGHT) {
          // Shift text one character to the right.
          pos = PApplet.min(text.length(), pos + 1);        
          fixVisibleText();
        }
      }
    }  
    
    void fixVisibleText() {
      if (bounded) {
        String temp = text.substring(pos0, pos);    
        float l = textWidth(temp);      
        while (l >= boundw) {
          pos0++;
          temp = text.substring(pos0, pos);      
          l = textWidth(temp);      
        }    
      }
    }      
  }
}

/* COPYRIGHT (C) 2014-16 Fathom Information Design. All Rights Reserved. */

package mui;

import processing.core.PApplet;
import processing.core.PConstants;

// Class to handle a (single) line of editable text
public class EditableText {
  static final int CURSOR_HIDDEN = 0;
  static final int CURSOR_VISIBLE = 1;
  
  Widget parent;
  String text0;
  StringBuffer text;
  boolean focused;
  protected int pos = 0;
  protected int pos0 = 0;    
  boolean bounded;    
  float boundw;
  boolean clearInitial;
  boolean resetInitial;
  boolean modified;
  
  int cursorStatus;
  int lastCursorChange;
  
  public EditableText(Widget parent, String text) {
    this.parent = parent;
    text0 = text;
    this.text = new StringBuffer(text);
    pos = text.length();
    pos0 = 0;      
    focused = false;
    bounded = false;
    clearInitial = false;
    resetInitial = false;
    modified = false;
  }
      
  public void setBound(float w) {
    bounded = true;      
    boundw = w;
  }
  
  public String get() {
    return getImpl(true);
  }
  
  protected String getImpl(boolean init) {
    if (init && !modified && clearInitial) return "";
    return text.substring(pos0, text.length());    
  }

  public void set(String text) {
    this.text = new StringBuffer(text);
    pos = text.length();
    pos0 = 0;      
  }
  
  public void clearInitial() {
    clearInitial = true;
  } 
  
  public void resetInitial() {
    resetInitial = true;
  }
  
  public float width() {
    return parent.textWidth(getImpl(false));
  } 
  
  public float getFloat() {
    try {
      return new Float(getImpl(false)).floatValue();
    } catch (NumberFormatException e) { 
      return Float.NaN;
    }      
  }
  
  public void setFocused(boolean focus) {
    if (focus != focused) {
      boolean focused0 = focused;
      focused = focus;
      if (focused) {
        cursorStatus = CURSOR_VISIBLE;
        lastCursorChange = parent.intf.app.millis();
        if (!focused0 && clearInitial && getImpl(false).equals(text0)) {
          set("");
        }
      } else if (resetInitial) {
        String last = getImpl(false);
        if (last.equals("")) {
          set(text0);
          modified = false;
        }
      }
    }
  }
  
  public boolean isFocused() {
    return focused;
  }
  
  public boolean inside(float x, float y, float mx, float my) {
    return insideImpl(x, y, mx, my, false);
  }
  
  public boolean insideBounds(float x, float y, float mx, float my) {
    if (bounded) return insideImpl(x, y, mx, my, true);
    else return insideImpl(x, y, mx, my, false);
  }  
  
  protected boolean insideImpl(float x, float y, float mx, float my, boolean useBound) {
    float[] tb = parent.textTopBottom(y);
    float ytop = tb[0];
    float ybot = tb[1];
    float w = 0;        
    if (useBound) {
      w = boundw;       
    } else {
      w = width();
    }      
    return x <= mx && mx <= x + w && ytop <= my && my <= ybot;
  }  
  
  public void draw(float x, float y) {
    String visible = text.substring(pos0, text.length());
    parent.text(visible, x, y);
    
    if (focused) {
      // Draw blinking cursor.
      int t = parent.intf.app.millis();
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
        float len = parent.textWidth(substr);    
        parent.line(x + len, y, x + len, y - parent.fontSize());
      }        
    }     
  }
  
  public void keyPressed(char key, int code)  {
    if (!focused) return;
    
    if (key != PConstants.CODED) {
      if (key == PConstants.BACKSPACE) {
        if (0 < pos && 0 < text.length()) {
          // Delete character before cursor.
          text.deleteCharAt(pos - 1); 
          pos -= 1;
          modified = true;
          if (pos < pos0) {
            pos0 = pos;
          }
        }
      } else if (key == PConstants.DELETE) {
        if (0 < text.length()) {
          // Delete character at cursor.
          text.deleteCharAt(pos);
          modified = true;
        }
      } else if (key != PConstants.TAB && 
                 key != PConstants.ENTER && 
                 key != PConstants.RETURN && 
                 key != PConstants.ESC) {
        // Add character after cursor.
        text.insert(pos, key); 
        pos += 1;
        modified = true;
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
  
  public void fixVisibleText() {
    if (bounded) {
      String temp = text.substring(pos0, pos);    
      float l = parent.textWidth(temp);      
      while (l >= boundw) {
        pos0++;
        temp = text.substring(pos0, pos);      
        l = parent.textWidth(temp);      
      }    
    }
  }      
}

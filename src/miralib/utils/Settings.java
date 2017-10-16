/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.utils;

import java.awt.Color;
import java.awt.Font;
import java.io.*;
import java.util.*;

import miralib.math.Numbers;

/**
 * Generic class to store program preferences.
 *
 */

public class Settings {
  // Table of attributes/values.
  ArrayList<String> attribs = new ArrayList<String>();
  HashMap<String, String> table = new HashMap<String, String>();  
  // Associated file for this settings data.
  File file;

  public Settings(File file) throws IOException {
    this.file = file;
    
    if (file.exists()) {
      load();
    }
  }

  public void load() {
    String[] lines = Fileu.loadStrings(file);
    for (String line : lines) {
      if ((line.length() == 0) || (line.charAt(0) == '#')) continue;

      // this won't properly handle = signs being in the text
      int equals = line.indexOf('=');
      if (equals != -1) {
        String key = line.substring(0, equals).trim();
        String value = line.substring(equals + 1).trim();
        set(key, value);
      }
    }
  }

  public void save() {
    PrintWriter writer = Fileu.createWriter(file);

    cleanup();
    for (String key : attribs) {
      writer.println(key + "=" + table.get(key));
    }

    writer.flush();
    writer.close();
  }
  
  public void cleanup() {
    HashSet<String> emptyKeys = new HashSet<String>();
    for (String key : table.keySet()) {
      if (table.get(key).equals("")) {
        emptyKeys.add(key);     
      }
    }
    
    for (String key : emptyKeys) {
      table.remove(key);
      attribs.remove(key);
    }
  }

  public void cleanup(String pkey) {
    HashSet<String> emptyKeys = new HashSet<String>();
    for (String key : table.keySet()) {
      if (key.indexOf(pkey) == 0) {
        emptyKeys.add(key);     
      }
    }
    
    for (String key : emptyKeys) {
      table.remove(key);
      attribs.remove(key);
    }
  }  
  
  public String get(String attribute, String dflt) {
    String what = table.get(attribute);
    return (what == null) ? dflt : what; 
  }

  public void set(String attribute, String value) {
    if (!table.containsKey(attribute)) {
      attribs.add(attribute);
    }
    table.put(attribute, value);
  }

  public boolean getBoolean(String attribute, boolean dflt) {
    String value = get(attribute, null);
    if (value == null) {
      return dflt;
    }
    return Boolean.parseBoolean(value);
  }

  public void setBoolean(String attribute, boolean value) {
    set(attribute, value ? "true" : "false");
  }

  public int getInteger(String attribute, int dflt) {
    String value = get(attribute, null);
    if (value == null) {
      return dflt;
    }
    return Integer.parseInt(value);
  }

  public void setInteger(String key, int value) {
    set(key, String.valueOf(value));
  }
  
  public float getFloat(String attribute, float dflt) {
    String value = get(attribute, null);
    if (value == null) {
      return dflt;
    }
    return Float.parseFloat(value);
  }

  public void setFloat(String key, float value) {
    set(key, String.valueOf(value));
  }
  
  public Color getColor(String attribute, Color dflt) {
    Color parsed = null;
    String s = get(attribute, null);
    if ((s != null) && (s.indexOf("#") == 0)) {
      try {
        int v = Integer.parseInt(s.substring(1), 16);
        parsed = new Color(v);
      } catch (Exception e) {
      }
    }
    return parsed == null ? dflt : parsed;
  }

  public void setColor(String attr, Color what) {
    set(attr, "#" + hex(what.getRGB() & 0xffffff, 6));
  }

  public Font getFont(String attr, String dflt) {
    boolean replace = false;
    String value = get(attr, null);
    if (value == null) {
      value = dflt;
      replace = true;
    }

    String[] pieces = value.split(",");
    if (pieces.length != 3) {
      value = dflt;
      pieces = value.split(",");
      replace = true;
    }

    String name = pieces[0];
    int style = Font.PLAIN;  // equals zero
    if (pieces[1].indexOf("bold") != -1) {
      style |= Font.BOLD;
    }
    if (pieces[1].indexOf("italic") != -1) {
      style |= Font.ITALIC;
    }
    int size = Numbers.parseInt(pieces[2], 12);
    Font font = new Font(name, style, size);

    // replace bad font with the default
    if (replace) {
      set(attr, value);
    }

    return font;
  }
  
  static final public String hex(int value, int digits) {
    String stuff = Integer.toHexString(value).toUpperCase();
    if (digits > 8) {
      digits = 8;
    }

    int length = stuff.length();
    if (length > digits) {
      return stuff.substring(length - digits);

    } else if (length < digits) {
      return "00000000".substring(8 - (digits-length)) + stuff;
    }
    return stuff;
  }  
}
package mui;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import processing.core.PApplet;
import processing.core.PConstants;

// DPI-detection code for Windows, from processing/app/platform/WindowsPlatform.java
// Copyright (c) 2012-2013 The Processing Foundation
// Copyright (c) 2008-2012 Ben Fry and Casey Reas
public class Display {
  interface ExtUser32 extends StdCallLibrary, com.sun.jna.platform.win32.User32 {
    ExtUser32 INSTANCE = (ExtUser32) Native.loadLibrary("user32", ExtUser32.class, W32APIOptions.DEFAULT_OPTIONS);

    public int GetDpiForSystem();

    public int SetProcessDpiAwareness(int value);

    public final int DPI_AWARENESS_INVALID = -1;
    public final int DPI_AWARENESS_UNAWARE = 0;
    public final int DPI_AWARENESS_SYSTEM_AWARE = 1;
    public final int DPI_AWARENESS_PER_MONITOR_AWARE = 2;

    public Pointer SetThreadDpiAwarenessContext(Pointer dpiContext);

    public final Pointer DPI_AWARENESS_CONTEXT_UNAWARE = new Pointer(-1);
    public final Pointer DPI_AWARENESS_CONTEXT_SYSTEM_AWARE = new Pointer(-2);
    public final Pointer DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE = new Pointer(-3);
  }

  static private float scale = 0;

  public static int detectSystemDPI() {
    try {
      ExtUser32.INSTANCE.SetProcessDpiAwareness(ExtUser32.DPI_AWARENESS_SYSTEM_AWARE);
    } catch (Throwable e) {
      // Ignore error
    }
    try {
      ExtUser32.INSTANCE.SetThreadDpiAwarenessContext(ExtUser32.DPI_AWARENESS_CONTEXT_SYSTEM_AWARE);
    } catch (Throwable e) {
      // Ignore error (call valid only on Windows 10)
    }
    try {
      return ExtUser32.INSTANCE.GetDpiForSystem();
    } catch (Throwable e) {
      // DPI detection failed, fall back with default
      System.out.println("DPI detection failed, fallback to 96 dpi");
      return 96;
    }
  }  
  

  static private void setScale() {
    if (scale == 0) {
      if (PApplet.platform == PConstants.WINDOWS) {
        scale = detectSystemDPI() / 96;
      } else {
        scale = 1;
      }
    }
  }


  static private int nextPOT(int val) {
    int ret = 1;
    while (ret < val) ret <<= 1;
    return ret;
  }


  static public int scale(int pixels) {
    setScale();
    return (int) Math.ceil(scale * pixels);
  }
  
  static public float scalef(float pixels) {
    setScale();
    return scale * pixels;
  }

  static public int scalepot(int pixels) {
    return nextPOT(scale(pixels));
  }
}

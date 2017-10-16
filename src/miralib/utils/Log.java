/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.utils;

import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import javax.swing.JOptionPane;

/**
 * Message logging.
 *
 */

public class Log {
  static protected Messages messages;
  static protected FileOutputStream out;
  static protected PrintStream ps;
  
  static public void init() {
    init(false);
  }
  
  static public void init(boolean save) {
    messages = new Messages();
    
    if (save) {
      File home = new File(System.getProperty("user.home"));
      File path = new File(home, ".mirador");
      if (!path.exists()) {
        if (!path.mkdirs()) {
          String err = "Cannot create a folder to store the log file";
          Log.error(err, new RuntimeException(err));
        }
      }
      File file = new File(path, "session.log");
      out = null;
      try {
        out = new FileOutputStream(file, false);
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      if (out != null) {
        ps = new PrintStream(out);
        System.setOut(ps);
        System.setErr(ps);
      }
    }
  }
  
  static public void message(String msg) {
    messages.push(msg);
  }
  
  static public void warning(String msg) {
    messages.push(msg);
  }

  static public void error(String msg, Throwable e) {
    messages.push(msg);
    JOptionPane.showMessageDialog(new Frame(), msg, "mirador",
                                  JOptionPane.ERROR_MESSAGE);    
    e.printStackTrace();    
    System.exit(0);
  } 
  
  static protected class Messages {
//  protected PApplet parent;
  
    public Messages(/*PApplet parent*/) {
//    this.parent = parent;  
    }
  
    public void push(String msg) {
      System.out.println(msg);  
    }
  }  
}

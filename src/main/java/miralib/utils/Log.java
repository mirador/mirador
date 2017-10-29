/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package miralib.utils;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URI;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import mui.Display;

/**
 * Message logging.
 *
 */

public class Log {
  final static private int FONT_SIZE = Display.scale(12);
  final static private int TEXT_MARGIN = Display.scale(8);
  final static private int TEXT_WIDTH = Display.scale(300);

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
    showMessageDialog(msg,"Mirador Error", JOptionPane.ERROR_MESSAGE);
    e.printStackTrace();    
    System.exit(0);
  } 

  static public void showMessageDialog(String msg, String title, int type) {
    String htmlString = "<html> " +
            "<head> <style type=\"text/css\">" +
            "p { font: " + FONT_SIZE + "pt \"Lucida Grande\"; " +
            "margin: " + TEXT_MARGIN + "px; " +
            "width: " + TEXT_WIDTH + "px }" +
            "</style> </head>" +
            "<body> <p>" + msg + "</p> </body> </html>";
    JEditorPane pane = new JEditorPane("text/html", htmlString);
    pane.addHyperlinkListener(new HyperlinkListener() {
      @Override
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
          try {
            Desktop.getDesktop().browse(new URI(e.getURL().toString()));
          } catch (Exception ex) {
          }
        }
      }
    });
    pane.setEditable(false);
    JLabel label = new JLabel();
    pane.setBackground(label.getBackground());
    JOptionPane.showMessageDialog(null, pane, title, type);
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

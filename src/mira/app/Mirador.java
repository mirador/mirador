/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mira.app;

import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JFileChooser;

import processing.core.PApplet;
import processing.core.PConstants;

/**
 * Mirador launcher class. 
 * 
 * @author Ben Fry
 */

public class Mirador {
  // The Swing chooser is much better on Linux
  static final boolean useNativeSelect = PApplet.platform != PConstants.LINUX;
  
  static void selectPrompt(final String prompt,
                           final File defaultSelection,
                           final Frame parentFrame,
                           final int mode) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        File selectedFile = null;

        if (useNativeSelect) {
          FileDialog dialog = new FileDialog(parentFrame, prompt, mode);
          if (defaultSelection != null) {
            dialog.setDirectory(defaultSelection.getParent());
            dialog.setFile(defaultSelection.getName());
          }
          
          dialog.setFilenameFilter(new FilenameFilter() {
            public boolean accept(File dir, String name) {
              if (name.startsWith(".")) return false;
              String lower = name.toLowerCase();
              return lower.endsWith(".mira") || 
                     lower.endsWith(".tsv") || 
                     lower.endsWith(".csv") || 
                     lower.endsWith(".xml") ||
                     lower.endsWith(".bin") ||
                     lower.endsWith(".ods");
            }
          });

          dialog.setVisible(true);
          String directory = dialog.getDirectory();
          String filename = dialog.getFile();
          if (filename != null) {
            selectedFile = new File(directory, filename);
          }

        } else {
          JFileChooser chooser = new JFileChooser();
          chooser.setDialogTitle(prompt);
          if (defaultSelection != null) {
            chooser.setSelectedFile(defaultSelection);
          }

          int result = -1;
          if (mode == FileDialog.SAVE) {
            result = chooser.showSaveDialog(parentFrame);
          } else if (mode == FileDialog.LOAD) {
            result = chooser.showOpenDialog(parentFrame);
          }
          if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
          }
        }
        selectCallback(selectedFile);
      }
    });
  }
  
  static protected void selectCallback(final File selectedFile) {
    if (selectedFile == null) {
      System.exit(0);
    }
    new Thread(new Runnable() {
      public void run() {
        MiraApp.inputFile = selectedFile.getAbsolutePath();        
        PApplet.main(MiraApp.class.getName());  
      }
    }).start();
  }

//  static protected File startFolder() {
//    String path = Mirador.class.getProtectionDomain().getCodeSource().getLocation().getPath();
//    // Path may have URL encoding, so remove it
//    String decodedPath = PApplet.urlDecode(path);
//    if (decodedPath.toLowerCase().contains("/mirador/bin")) {
//      // Running from Eclipse
//      File file = new File(path);
//      String filePath = file.getAbsolutePath();
//      return new File(filePath, "../examples/.");      
//    } else {
//      if (PApplet.platform == PApplet.MACOSX) {      
//        File file = new File(path);
//        String absolutePath = file.getAbsolutePath();
//        String filePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));      
//        return new File(filePath, "../Resources/examples/.");
//      } else {
//        return new File(System.getProperty("user.dir"), "examples/.");
//      }      
//    }
//  }
    
  static public void main(String[] args) {
    Frame frame = new Frame();
    frame.pack();  // make it legit
    MiraApp.loadPreferences();
    selectPrompt("Select data for analysis:", new File(MiraApp.prefs.projectFolder), frame, FileDialog.LOAD);
  }
}

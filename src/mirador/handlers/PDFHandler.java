/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mirador.handlers;

import java.io.File;

import mirador.app.MiraApp;
import processing.core.PApplet;

/**
 * Handler for PDF export. 
 * 
 */

public class PDFHandler {
  protected MiraApp app;
  
  public PDFHandler(MiraApp app) {
    this.app = app;
  }
  
  public void outputSelected(File selection) {
    if (selection == null) return;
    
    String pdfFilename = selection.getAbsolutePath();    
    String ext = PApplet.checkExtension(pdfFilename);
    if (ext == null || !ext.equals("pdf")) {
      pdfFilename += ".pdf";
    }
    app.intf.record(pdfFilename);
  }   
}
/* COPYRIGHT (C) 2014-16 Fathom Information Design. All Rights Reserved. */

package mirador.handlers;

import java.io.File;
import mirador.app.MiraApplet;

/**
 * Handler for project loading. 
 * 
 */

public class LoadHandler {
  protected MiraApplet app;
  
  public LoadHandler(MiraApplet app) {
    this.app = app;
  }
  
  public void outputSelected(File selection) {
    if (selection != null) {
      app.loadProject(selection.toString());
    }
  }    
}
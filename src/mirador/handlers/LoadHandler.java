/* COPYRIGHT (C) 2014-16 Fathom Information Design. All Rights Reserved. */

package mirador.handlers;

import java.io.File;
import mirador.app.MiraApp;

/**
 * Handler for project loading. 
 * 
 */

public class LoadHandler {
  protected MiraApp app;
  
  public LoadHandler(MiraApp app) {
    this.app = app;
  }
  
  public void outputSelected(File selection) {
    if (selection != null) {
      app.loadProject(selection.toString());
    }
  }    
}
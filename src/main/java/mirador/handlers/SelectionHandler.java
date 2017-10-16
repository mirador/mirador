/* COPYRIGHT (C) 2014-16 Fathom Information Design. All Rights Reserved. */

package mirador.handlers;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import mirador.app.MiraApplet;
import miralib.data.Variable;
import processing.core.PApplet;
import processing.data.Table;

/**
 * Handler for selected pair export. 
 * 
 */

public class SelectionHandler {
  protected MiraApplet app;
  protected ArrayList<Variable> variables;
  
  public SelectionHandler(MiraApplet app, Variable varx, Variable vary, Variable vark) {
    this.app = app;
    variables = new ArrayList<Variable>();
    if (vark != null) variables.add(vark);
    variables.add(varx);
    if (varx != vary) variables.add(vary);
  }
  
  public void outputSelected(File selection) {
    if (selection == null) return;
    
    String filename = selection.getAbsolutePath();    
    String ext = PApplet.checkExtension(filename);
    if (ext == null || (!ext.equals("csv") && !ext.equals("tsv"))) {
      filename += ".tsv";
    }
    Path dataPath = Paths.get(filename);
    String filePath = dataPath.getParent().toAbsolutePath().toString(); 
    File dictFile = new File(filePath, "selected-dictionary.tsv");

    Table[] tabdict = app.dataset.getTable(variables, app.ranges);
    Table data = tabdict[0];
    if (data != null) {
      app.saveTable(data, filename);
    }
    
    Table dict = tabdict[1];
    if (dict != null) {      
      app.saveTable(dict, dictFile.getAbsolutePath());          
    }
  }
}

/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.handlers;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import mirador.app.MiraApp;
import miralib.data.Variable;
import miralib.utils.Project;
import processing.core.PApplet;
import processing.data.Table;

/**
 * Handler for exporting entire dataset as mirador project.
 * 
 */

public class ExportHandler {
  protected MiraApp app;
  protected ArrayList<Variable> variables;
  
  public ExportHandler(MiraApp app) {
    this.app = app;
    variables = app.dataset.getVariables();
  }
  
  public void outputSelected(File selection) {
    if (selection == null) return;
    
    String filename = selection.getAbsolutePath();    
    String ext = PApplet.checkExtension(filename);
    if (ext == null || (!ext.equals("csv") && !ext.equals("tsv"))) {
      filename += ".csv";
    }
    Path dataPath = Paths.get(filename);
    String filePath = dataPath.getParent().toAbsolutePath().toString();
    File dataFile = new File(filename);
    File dictFile = new File(filePath, "dictionary.csv");
    File miraFile = new File(filePath, "config.mira");

    Table[] tabdict = app.dataset.getTable(variables, app.ranges);
    Table data = tabdict[0];
    if (data != null) {
      app.saveTable(data, filename);
    }
    
    Table dict = tabdict[1];
    if (dict != null) {      
      app.saveTable(dict, dictFile.getAbsolutePath());          
    }

    Project prj = new Project(app.project);
    prj.dataFile = dataFile.getName();
    prj.dictFile = "dictionary.csv";
    prj.grpsFile = "";
    prj.binFile = "";
    prj.codeFile = "";
    prj.save(miraFile.getAbsolutePath());
  }
}

/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

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
 * Handler for profile export. 
 * 
 */

public class ProfileHandler {
  protected MiraApp app;
  protected ArrayList<Variable> variables;
  
  public ProfileHandler(MiraApp app, ArrayList<Variable> vars) {
    this.app = app;
    variables = vars;
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
    File dictFile = new File(filePath, "profile-dictionary.tsv");
    File varsFile = new File(filePath, "profile-variables.tsv");
    File projFile = new File(filePath, "profile-config.mira");
    
    Table[] tabdict = app.dataset.getTable(variables, app.ranges);
    Table data = tabdict[0];
    if (data != null) {
      app.saveTable(data, filename);
    }
    
    Table dict = tabdict[1];
    if (dict != null) {      
      app.saveTable(dict, dictFile.getAbsolutePath());          
    }
    
    Table vars = app.dataset.getProfile(variables);      
    if (vars != null) {
      app.saveTable(vars, varsFile.getAbsolutePath());  
    }
    
    Project proj = new Project(app.project);
    proj.dataTitle = "Profile";
    proj.dataURL = "";
    proj.dataFile = dataPath.getFileName().toString();
    proj.dictFile = dictFile.getName();     
    proj.grpsFile = "";
    proj.binFile = "";
    proj.codeFile = "";
    
    proj.save(projFile.toString());
  }
}
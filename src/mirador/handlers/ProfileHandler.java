/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mirador.handlers;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import mirador.app.MiraApp;
import miralib.data.Range;
import miralib.data.Variable;
import processing.core.PApplet;
//import miralib.utils.Project;
//import processing.data.Table;

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
    String varFN = selection.getAbsolutePath();
    String ext = PApplet.checkExtension(varFN);
    if (ext == null || !ext.equals("txt")) {
      varFN += ".txt";
    }

    Path dataPath = Paths.get(varFN);
    String filePath = dataPath.getParent().toAbsolutePath().toString();    
    
    // All variables in the profile
    String[] varLines = new String[variables.size()];
    for (int i = 0; i < variables.size(); i++) {
      Variable var = variables.get(i);
      varLines[i] = var.getName() + " " + Variable.formatType(var.type());
    }    
    File varFile = new File(varFN);
    PApplet.saveStrings(varFile, varLines);
    
    // Ranges file
    Variable[] rvars = app.ranges.keySet().toArray(new Variable[0]);    
    String[] rangeLines = new String[rvars.length];
    for (int i = 0; i < rvars.length; i++) {
      Variable var = rvars[i];
      Range range = app.ranges.get(var); 
      rangeLines[i] = var.getName() + " " + Variable.formatType(var.type()) + " " + range.toString(); 
    }
    File rangeFile = new File(filePath, "ranges.txt");
    PApplet.saveStrings(rangeFile, rangeLines);

    // Alias file
    String[] aliasLines = new String[variables.size()];    
    for (int i = 0; i < variables.size(); i++) {
      Variable var = variables.get(i);
      aliasLines[i] = var.getName() + " " + var.getAlias();
    }    
    File aliasFile = new File(filePath, "alias.txt");
    PApplet.saveStrings(aliasFile, aliasLines);
        
    // Units file
    String[] unitLines = new String[variables.size()];    
    for (int i = 0; i < variables.size(); i++) {
      Variable var = variables.get(i);
      unitLines[i] = var.getName();
    }    
    File unitsFile = new File(filePath, "units.txt");
    PApplet.saveStrings(unitsFile, unitLines);
    
    // Outcome file
    Variable ovar = variables.get(0);
    Range orange = ovar.range();
    ArrayList<String> values = orange.getValues();
    String[] outLines = new String[values.size()];
    for (int i = 0; i < values.size(); i++) {
      String value = values.get(i);
      String label = ovar.formatValue(value);
      outLines[i] = value + "," + label;
     
    }
    File outFile = new File(filePath, "outcome.txt");
    PApplet.saveStrings(outFile, outLines);    
    
    /*

    // TODO: need to test this code anyways, it was not working before, maybe a bug in
    miralib?
    
    String filename = selection.getAbsolutePath();    

    String prefix = "";
    if (-1 < filename.indexOf("profile-")) {
      prefix = "profile-";
    }
    
    Path dataPath = Paths.get(filename);
    String filePath = dataPath.getParent().toAbsolutePath().toString(); 
    File dictFile = new File(filePath, prefix + "dictionary.tsv");
    File varsFile = new File(filePath, prefix + "variables.tsv");
    File projFile = new File(filePath, prefix + "config.mira");
    
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
    */
  }
}
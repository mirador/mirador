/* COPYRIGHT (C) 2014-16 Fathom Information Design. All Rights Reserved. */

package mirador.handlers;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import mirador.app.MiraApplet;
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
  protected MiraApplet app;
  protected ArrayList<Variable> variables;
  
  public ProfileHandler(MiraApplet app, ArrayList<Variable> vars) {
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
    
    String[] pvalLines = new String[variables.size() - 1];
    for (int i = 1; i < variables.size(); i++) {
      Variable var = variables.get(i);
      float score = app.dataset.getScore(var);
      double pvalue = Math.pow(10, -score);
      pvalLines[i - 1] = var.getName() + " " + pvalue;
    }    
    File pvalFile = new File(filePath, "pvalues.txt");
    PApplet.saveStrings(pvalFile, pvalLines);
    
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
      String alias = var.getAlias();
      String ustr = "";
      int n0 = alias.indexOf('(');
      int n1 = alias.indexOf(')');
      if (-1 < n0 && n0 < n1) {
        ustr = alias.substring(n0 + 1, n1);
      } else {
        n0 = alias.indexOf('[');
        n1 = alias.indexOf(']');
        if (-1 < n0 && n0 < n1) ustr = alias.substring(n0 + 1, n1);
      }
      unitLines[i] = var.getName() + " " + ustr;
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
  }
}
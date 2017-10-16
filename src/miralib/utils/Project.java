/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import miralib.shannon.BinOptimizer;
import miralib.shannon.DependencyTest;

/**
 * Mirador project information.
 *
 */

public class Project {
  // List of accepted P-values  
  final static public int P0_001   = 0;
  final static public int P0_005   = 1;
  final static public int P0_01    = 2;
  final static public int P0_05    = 3;
  final static public int P0_1     = 4;
  final static public int P_IGNORE = 5;
  
  // List of missing data thresholds  
  final static public int MISS_10     = 0;
  final static public int MISS_20     = 1;
  final static public int MISS_60     = 2;
  final static public int MISS_80     = 3;
  final static public int MISS_IGNORE = 4; 
  
  // List of sorting scores
  final static public int SIMILARITY = 0;
  final static public int PVALUE     = 1;  
  
  protected static final Set<String> dataExtensions = 
      new HashSet<String>(Arrays.asList(new String[] { "csv", "tsv", "ods" }));
  
  public String dataTitle;
  public String dataURL;
  
  public String dataFolder;
  public String dataFile;  
  public String dictFile;
  public String grpsFile;
  public String codeFile;
  public String binFile;
  public String keyVar;
  
  public int pValue;
  public int missThreshold;  
  public String missString;
  
  public int binAlgorithm;
  public int depTest;
  public int sortMethod;
  public int surrCount; 
  public float threshold;
  
  public String dateParsePattern;
  public String datePrintPattern;  
  
  public File cfgFile;
  
  public Project(String filename, Preferences prefs) throws IOException {
    File inFile = new File(filename);
    if (!inFile.exists()) {
      String err = "Input file " + filename + " does not exist";
      Log.error(err, new RuntimeException(err));
    }

    Path p = Paths.get(filename);
    Path filePath = p.toAbsolutePath().getParent().toAbsolutePath();    
    dataFolder = filePath.toString(); 
    File[] prjFiles = filePath.toFile().listFiles();
    
    for (File f: prjFiles) {
      String ext = Fileu.checkExtension(f.toString());
      if (ext != null && ext.equals("mira")) {
        cfgFile = f;
        break;
      }
    }
    
    if (cfgFile != null) {
      // We have a Mirador project folder, so we read settings from its  
      // configuration file
      Settings settings = new Settings(cfgFile);
      dataTitle = settings.get("project.title", "unnamed dataset");
      dataURL = settings.get("project.url", "");
            
      dataFile = settings.get("data.source", "");
      dictFile = settings.get("data.dictionary", "");
      grpsFile = settings.get("data.groups", "");
      codeFile = settings.get("data.codebook", "");
      binFile = settings.get("data.binary", "");
      keyVar = settings.get("data.key", "");

      missString = settings.get("missing.string", prefs.missingString);      
      missThreshold = Project.stringToMissing(settings.get("missing.threshold", 
                         Project.missingToString(prefs.missingThreshold)));
     
      binAlgorithm = BinOptimizer.stringToAlgorithm(settings.get("binning.algorithm", 
                BinOptimizer.algorithmToString(prefs.binAlgorithm)));
      
      pValue = Project.stringToPValue(settings.get("correlation.pvalue", 
               Project.pvalueToString(prefs.pValue))); 
      depTest = DependencyTest.stringToAlgorithm(settings.get("correlation.algorithm", 
                DependencyTest.algorithmToString(prefs.depTest)));      
      sortMethod = Project.stringToSorting(settings.get("correlation.sorting", 
                   Project.sortingToString(prefs.sortMethod)));      
      surrCount = settings.getInteger("correlation.surrogates", prefs.surrCount);
      threshold = settings.getFloat("correlation.threshold", prefs.threshold);
      
      dateParsePattern = settings.get("dates.parse", prefs.dateParsePattern);
      datePrintPattern = settings.get("dates.print", prefs.datePrintPattern);
    } else {
      // We don't have a configuration file, so trying to guess the files from  
      // the folder's contents
      dataFile = "";
      dictFile = "";
      grpsFile = "";
      codeFile = "";
      binFile = "";
      keyVar = "";
      
      for (File f: prjFiles) {
        String name = f.getName();
        String ext = Fileu.checkExtension(name);
        if (ext == null) continue;
        if (ext.equals("bin")) {
          binFile = name;
        } else if (ext.equals("xml")) {
          grpsFile = f.getName();
        } else if (dataExtensions.contains(ext)) {
          String lname = name.toLowerCase();          
          if (lname.equals("codebook." + ext) ||
              -1 < lname.indexOf("-codebook." + ext) ||
              lname.equals("codes." + ext) ||
              -1 < lname.indexOf("-codes." + ext)) {
            codeFile = name;
          } else if (lname.equals("dictionary." + ext) ||
                     -1 < lname.indexOf("-dictionary." + ext) ||
                     lname.equals("dict." + ext) ||
                     -1 < lname.indexOf("-dict." + ext)) {
            dictFile = name;
          } else {
            dataFile = name;
          }
        }
      }
      
      if (dictFile.equals("") && codeFile.equals("") && !dataFile.equals(inFile.getName())) {
        // The input folder contains several csv files, but none of them it is         
        // neither a dictionary nor a codebook, so let's just use the original
        // file the user selected
        dataFile = inFile.getName();
      }
      
      dataTitle = dataFile; 
      dataURL = "";
      // Using the defaults for the parameters
      pValue = prefs.pValue;
      missString = prefs.missingString;
      missThreshold = prefs.missingThreshold;
      binAlgorithm = prefs.binAlgorithm;
      depTest = prefs.depTest;
      sortMethod = prefs.sortMethod;
      surrCount = prefs.surrCount;
      threshold = prefs.threshold;
      dateParsePattern = prefs.dateParsePattern;
      datePrintPattern = prefs.datePrintPattern;
    }
  }  
  
  public Project(Project that) {
    this.dataTitle = that.dataTitle;
    this.dataURL = that.dataURL;
    
    this.dataFolder = that.dataFolder;
    this.dataFile = that.dataFile;
    this.dictFile = that.dictFile;
    this.grpsFile = that.grpsFile;
    this.codeFile = that.codeFile;
    this.binFile = that.binFile;
    this.keyVar = that.keyVar;
        
    this.missString = that.missString;  
    this.missThreshold = that.missThreshold;
    
    this.pValue = that.pValue;
    this.binAlgorithm = that.binAlgorithm;
    this.depTest = that.depTest;
    this.sortMethod = that.sortMethod;
    this.surrCount = that.surrCount; 
    this.threshold = that.threshold;
  }  
  
  public boolean hasSource() {
    return !dataFile.equals("");
  }
  
  public boolean hasDictionary() {
    return !dictFile.equals("");
  } 

  public boolean hasGroups() {
    return !grpsFile.equals("");
  }   
  
  public boolean hasBinary() {
    return !binFile.equals("");
  }

  public boolean hasKey() {
    return !keyVar.equals("");
  } 
  
  public boolean hasCodebook() {
    return !codeFile.equals("");
  }   

  public String getSourcePath() {
    return Paths.get(dataFolder, dataFile).toString();
  }
  
  public String getDictionaryPath() {
    return Paths.get(dataFolder, dictFile).toString();
  }
  
  public String getGroupsPath() {
    return Paths.get(dataFolder, grpsFile).toString();
  }
  
  public String getBinaryPath() {
    return Paths.get(dataFolder, binFile).toString();
  }

  public String getCodebookPath() {
    return Paths.get(dataFolder, codeFile).toString();
  }
  
  public void save(String filename) {
    cfgFile = new File(filename);
    save();
  }  
  
  public void save() {
    if (cfgFile != null) {
      try {      
        Settings settings = new Settings(cfgFile);
        settings.set("project.title", dataTitle);
        settings.set("project.url", dataURL);
        settings.set("data.source", dataFile);
        settings.set("data.dictionary", dictFile);
        settings.set("data.groups", grpsFile);
        settings.set("data.codebook", codeFile);      
        settings.set("data.binary", binFile);
        settings.set("data.key", keyVar);
                
        settings.set("missing.string", missString);            
        settings.set("missing.threshold", missingToString(missThreshold));        
        
        settings.set("binning.algorithm", BinOptimizer.algorithmToString(binAlgorithm));
        
        settings.set("correlation.pvalue", pvalueToString(pValue));
        settings.set("correlation.algorithm", DependencyTest.algorithmToString(depTest));
        settings.set("correlation.sorting", sortingToString(sortMethod));
        settings.setInteger("correlation.surrogates", surrCount);
        settings.setFloat("correlation.threshold", threshold);   
        
        settings.set("dates.parse", dateParsePattern);
        settings.set("dates.print", datePrintPattern);
        
        settings.save();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }      
    }
  }
  
  
  static public String pvalueToString(int pval) {
    if (pval == P0_001) {        
      return "P0_001";
    } else if (pval == P0_005) {
      return "P0_005";
    } else if (pval == P0_01) {
      return "P0_01";
    } else if (pval == P0_05) {
      return "P0_05";
    } else if (pval == P0_1) {
      return "P0_1";
    } else if (pval == P_IGNORE) {
      return "P_IGNORE";
    }
    String err = "Unsupported P-value constant: " + pval;
    Log.error(err, new RuntimeException(err));
    return "unsupported";    
  }

  
  static public int stringToPValue(String name) {
    name = name.toUpperCase();
    if (name.equals("P0_001")) {
      return P0_001;
    } else if (name.equals("P0_005")) {
      return P0_005;
    } else if (name.equals("P0_01")) {
      return P0_01;
    } else if (name.equals("P0_05")) {
      return P0_05;
    } else if (name.equals("P0_1")) {
      return P0_1;
    } else if (name.equals("P_IGNORE")) {
      return P_IGNORE;
    } 
    String err = "Unsupported P-value constant: " + name;
    Log.error(err, new RuntimeException(err));
    return -1;
  }
  
  static public String missingToString(int miss) {
    if (miss == MISS_10) {        
      return "MISS_10";
    } else if (miss == MISS_20) {
      return "MISS_20";
    } else if (miss == MISS_60) {
      return "MISS_60";
    } else if (miss == MISS_80) {
      return "MISS_80";
    } else if (miss == MISS_IGNORE) {
      return "MISS_IGNORE";
    }
    String err = "Unsupported missing threshold constant: " + miss;
    Log.error(err, new RuntimeException(err));
    return "unsupported";    
  }
  
  static public int stringToMissing(String name) {
    name = name.toUpperCase();
    if (name.equals("MISS_10")) {
      return MISS_10;
    } else if (name.equals("MISS_20")) {
      return MISS_20;
    } else if (name.equals("MISS_60")) {
      return MISS_60;
    } else if (name.equals("MISS_80")) {
      return MISS_80;
    } else if (name.equals("MISS_IGNORE")) {
      return MISS_IGNORE;
    }
    String err = "Unsupported missing threshold constant: " + name;
    Log.error(err, new RuntimeException(err));
    return -1;
  }
  
  static public int stringToSorting(String name) {
    name = name.toUpperCase();
    if (name.equals("SIMILARITY")) {
      return SIMILARITY;
    } else if (name.equals("PVALUE")) {
      return PVALUE;
    }
    String err = "Unsupported sorting constant: " + name;
    Log.error(err, new RuntimeException(err));
    return -1;
  }
  
  static public String sortingToString(int pval) {
    if (pval == SIMILARITY) {        
      return "SIMILARITY";
    } else if (pval == PVALUE) {
      return "PVALUE";
    }
    String err = "Unsupported sorting constant: " + pval;
    Log.error(err, new RuntimeException(err));
    return "unsupported";    
  }  
    
  public float pvalue() {
    if (pValue == P0_001) return 0.001f;
    else if (pValue == P0_005) return 0.005f;
    else if (pValue == P0_01) return 0.01f;
    else if (pValue == P0_05) return 0.05f;
    else if (pValue == P0_1) return 0.1f;
    else return 1;
  }  
  
  public float missingThreshold() {
    if (missThreshold == MISS_10) return 0.1f;
    else if (missThreshold == MISS_20) return 0.2f;
    else if (missThreshold == MISS_60) return 0.6f;
    else if (missThreshold == MISS_80) return 0.8f;
    else return 1;    
  }  
}

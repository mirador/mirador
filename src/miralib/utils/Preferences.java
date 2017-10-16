/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.utils;

import java.io.File;
import java.io.IOException;

import miralib.shannon.BinOptimizer;
import miralib.shannon.DependencyTest;

/**
 * Mirador preferences, that are used when no project file is provided.
 *
 */

public class Preferences {
  static final protected int defPValue = Project.P0_05;
  static final protected String defMissingString = "?";
  static final protected int defMissThreshold = Project.MISS_80;
  static final protected int defBinAlgo = BinOptimizer.POISSON;
  static final protected int defDepTest = DependencyTest.GAMMA_TEST;
  static final protected int defSortMethod = Project.PVALUE;
  static final protected int defSurrCount = 100;
  static final protected float defThreshold = 1E-3f;
  
  static final protected String defDateParsePattern = "yyyy-MM-dd";
  static final protected String defDatePrintPattern = "d MMM, yyyy";
  
  public String projectFolder;
  
  public int pValue;
  public String missingString; 
  public int missingThreshold;
  public int binAlgorithm;
  public int depTest;
  public int sortMethod;
  public int surrCount; 
  public float threshold;
  public String dateParsePattern;
  public String datePrintPattern;  
  
  protected Settings settings;
  
  public Preferences() throws IOException {
    this("");
  }  
  
  public Preferences(String defFolder) throws IOException {
    File home = new File(System.getProperty("user.home"));
    File path = new File(home, ".mirador");
    if (!path.exists()) {
      if (!path.mkdirs()) {
        String err = "Cannot create a folder to store the preferences";
        Log.error(err, new RuntimeException(err));
      }
    }
    
    File file = new File(path, "preferences.cfg");
    settings = new Settings(file);
    if (file.exists()) {      
      projectFolder = settings.get("data.folder", defFolder);      
      missingString = settings.get("missing.string", defMissingString);
      missingThreshold = Project.stringToMissing(settings.get("missing.threshold", 
                         Project.missingToString(defMissThreshold)));

      binAlgorithm = BinOptimizer.stringToAlgorithm(settings.get("binning.algorithm", 
                BinOptimizer.algorithmToString(defBinAlgo)));
      
      pValue = Project.stringToPValue(settings.get("correlation.pvalue", 
               Project.pvalueToString(defPValue)));
      depTest = DependencyTest.stringToAlgorithm(settings.get("correlation.algorithm", 
                DependencyTest.algorithmToString(defDepTest)));      
      sortMethod = Project.stringToSorting(settings.get("correlation.sorting", 
                   Project.sortingToString(defSortMethod)));
      surrCount = settings.getInteger("correlation.surrogates", defSurrCount);
      threshold = settings.getFloat("correlation.threshold", defThreshold);
      
      dateParsePattern = settings.get("dates.parse", defDateParsePattern);
      datePrintPattern = settings.get("dates.print", defDatePrintPattern);      
    } else {
      projectFolder = defFolder;
      pValue = defPValue;             
      missingString = defMissingString;
      missingThreshold = defMissThreshold;
      binAlgorithm = defBinAlgo;
      depTest = defDepTest;
      sortMethod = defSortMethod;
      surrCount = defSurrCount;
      threshold = defThreshold;
      dateParsePattern = defDateParsePattern;
      datePrintPattern = defDatePrintPattern;
      
      save();
    }
  }
  
  public void save() {
    settings.set("data.folder", projectFolder);
    settings.set("missing.string", missingString);
    settings.set("missing.threshold", Project.missingToString(missingThreshold));    
    settings.set("binning.algorithm", BinOptimizer.algorithmToString(binAlgorithm));    
    settings.set("correlation.pvalue", Project.pvalueToString(pValue));
    settings.set("correlation.algorithm", DependencyTest.algorithmToString(depTest));
    settings.set("correlation.sorting", Project.sortingToString(sortMethod));    
    settings.setInteger("correlation.surrogates", surrCount);
    settings.setFloat("correlation.threshold", threshold);
    settings.set("dates.parse", dateParsePattern);
    settings.set("dates.print", datePrintPattern);    
    settings.save();    
  }
}

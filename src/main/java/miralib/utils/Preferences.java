/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package miralib.utils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import miralib.shannon.BinOptimizer;
import miralib.shannon.DependencyTest;
import processing.core.PApplet;

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

  static final protected int defInitSliceSize = 1000000;
  static final protected int defMaxPlotTime = 500;

  static final protected String defDateParsePattern = "yyyy-MM-dd";
  static final protected String defDatePrintPattern = "d MMM, yyyy";

  static final protected boolean defCopyExamples = true;
  static final public int defPlotWidth = 200;
  static final public int defPlotHeight = 200;
  static final public int defPlotColor = 0xFF278DD2;

  public String[] projectHistory = new String[15];
  public String projectFolder;
  
  public int pValue;
  public String missingString; 
  public int missingThreshold;
  public int binAlgorithm;
  public int depTest;
  public int sortMethod;
  public int surrCount; 
  public float threshold;
  public int initSliceSize;
  public int maxPlotTime;
  public String dateParsePattern;
  public String datePrintPattern;
  public boolean copyExamples;
  public int plotWidth;
  public int plotHeight;
  public int plotColor;
  
  protected Settings settings;
  
  public Preferences() throws IOException {
    this("");
  }  
  
  public Preferences(String defFolder) throws IOException {
    File path = new File(defFolder);
    if (!path.exists()) {
      if (!path.mkdirs()) {
        String err = "Cannot create a folder to store the preferences";
        Log.error(err, new RuntimeException(err));
      }
    }

    File file = new File(path, "preferences.cfg");
    settings = new Settings(file);

    for (int i = 0; i < projectHistory.length; i++) projectHistory[i] = "";

    if (file.exists()) {
      String[] history = settings.get("data.history", "").split("::");
      for (int i = 0; i < PApplet.min(history.length, projectHistory.length); i++) {
        projectHistory[i] = history[i];
      }
      removeMissingHistoryFiles();

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

      initSliceSize = settings.getInteger("performance.samplesize", defInitSliceSize);
      maxPlotTime = settings.getInteger("performance.plottime", defMaxPlotTime);

      dateParsePattern = settings.get("dates.parse", defDateParsePattern);
      datePrintPattern = settings.get("dates.print", defDatePrintPattern);

      copyExamples = settings.getBoolean("examples.copy", defCopyExamples);

      plotWidth = settings.getInteger("plot.width", defPlotWidth);
      plotHeight = settings.getInteger("plot.height", defPlotHeight);
      plotColor = settings.getColor("plot.color", defPlotColor);
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
      initSliceSize = defInitSliceSize;
      maxPlotTime = defMaxPlotTime;
      dateParsePattern = defDateParsePattern;
      datePrintPattern = defDatePrintPattern;
      copyExamples = defCopyExamples;
      plotWidth = defPlotWidth;
      plotHeight = defPlotHeight;
      plotColor = defPlotColor;
      save();
    }
  }
  
  public void save() {
    removeMissingHistoryFiles();
    settings.set("data.history", PApplet.join(projectHistory, "::"));

    settings.set("data.folder", projectFolder);
    settings.set("missing.string", missingString);
    settings.set("missing.threshold", Project.missingToString(missingThreshold));    
    settings.set("binning.algorithm", BinOptimizer.algorithmToString(binAlgorithm));    
    settings.set("correlation.pvalue", Project.pvalueToString(pValue));
    settings.set("correlation.algorithm", DependencyTest.algorithmToString(depTest));
    settings.set("correlation.sorting", Project.sortingToString(sortMethod));    
    settings.setInteger("correlation.surrogates", surrCount);
    settings.setFloat("correlation.threshold", threshold);
    settings.setInteger("performance.samplesize", initSliceSize);
    settings.setInteger("performance.plottime", maxPlotTime);
    settings.set("dates.parse", dateParsePattern);
    settings.set("dates.print", datePrintPattern);
    settings.setBoolean("examples.copy", copyExamples);
    settings.setInteger("plot.width", plotWidth);
    settings.setInteger("plot.height", plotHeight);
    settings.setColor("plot.color", plotColor);
    settings.save();    
  }

  public void setProjectFolder(String path, String name) {
    projectFolder = path;

    String fn = Paths.get(path, name).toString();

    // Searching this file in the history.
    int known = -1;
    for (int i = 0; i < projectHistory.length; i++) {
      if (projectHistory[i].equals(fn)) {
        known = i;
      }
    }

    if (known == 0) {
      // Don't need to add it as it is already in the first position in the list, meaning it was the last open file
      return;
    }

    // If was open at some moment in the past, so it should now be first, and removed from it previous position to
    // avoid duplication. Otherwise, first time opening the file, so adding to the top of the list.
    int start = 0 < known ? known :  projectHistory.length - 1;

    for (int i = start; i > 0; i--) {
      projectHistory[i] = projectHistory[i - 1];
    }
    projectHistory[0] = fn;
  }

  public Object[][] getProjectHistory() {
    int count = 0;
    for (int i = projectHistory.length - 1; i >= 0; i--) {
      if (!projectHistory[i].equals("")) {
        count = i + 1;
        break;
      }
    }

    Object[][] res = new Object[count][1];
    for (int i = 0; i < count; i++) {
      res[i][0] = projectHistory[i];
    }
    return res;
  }

  private void removeMissingHistoryFiles() {
    for (int i = 0; i < projectHistory.length; i++) {
      if (!new File(projectHistory[i]).exists()) {
        for (int j = i; j < projectHistory.length - 1; j++) {
          projectHistory[j] = projectHistory[j + 1];
        }
      }
    }
  }
}

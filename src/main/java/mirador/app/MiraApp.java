/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.net.URL;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;

import javax.swing.JOptionPane;

import mirador.handlers.LoadHandler;
import mirador.handlers.ProfileHandler;
import mirador.handlers.ExportHandler;
//import mirador.handlers.UploadHandler;
import miralib.data.*;
import mui.Display;
import mui.Interface;
import mui.SoftFloat;
import mirador.views.View;
import miralib.utils.Log;
import miralib.utils.Preferences;
import miralib.utils.Project;
import miralib.utils.Fileu;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.opengl.PJOGL;

/**
 * Mirador main class. 
 * 
 */

public class MiraApp extends PApplet {
  static public String RENDERER = P2D;
  static public int PIXEL_DENSITY = 1;
  static public int SMOOTH_LEVEL = 4;
  static public final String APP_NAME = "mirador";
  static public final String APP_VERSION = "1.5.1";
  
  static public String inputFile = "default.mira";
  static public File miraFolder;
  static protected Preferences prefs;
  
  // TODO: Make into CCS size parameters
  int optWidth = Display.scale(120);  
  int varWidth = Display.scale(300);
  int varHeight = Display.scale(85);
  int labelHeightClose = Display.scale(115);
  int labelHeightMax = Display.scale(300);
  int covarHeightClose = Display.scale(58);
  int covarHeightMax = Display.scale(300);
  int plotWidth = Display.scale(Preferences.defPlotWidth);
  int plotHeight = Display.scale(Preferences.defPlotHeight);
  /////////////////////////////////////////////////////////////////////////////

  public Project project;
  public DataSet dataset;
  public DataRanges ranges;
  public History history;
//  public UploadHandler uploader;
  
  public Interface intf;  
  public OptionsPanel options;  
  public VariableBrowser browser;
  public Profile profile;
    
  protected int plotType;
  
  protected boolean loaded;
  protected boolean loadingError;
  protected String errorMessage;
  protected LoadThread loadThread;
  protected boolean animating;
  protected float animTime;
  protected SoftFloat animAlpha;

  /////////////////////////////////////////////////////////////////////////////
  protected int maxPlotSliceSizeCC;
  protected int maxPlotSliceSizeNN;
  protected int maxPlotSliceSizeCN;
  protected int clockUpdateCC = 0;
  protected int clockUpdateNN = 0;
  protected int clockUpdateCN = 0;
  protected int targetClockSize = 5;
  protected List plotTimesCC = Collections.synchronizedList(new ArrayList<Integer>());
  protected List plotTimesNN = Collections.synchronizedList(new ArrayList<Integer>());
  protected List plotTimesCN = Collections.synchronizedList(new ArrayList<Integer>());

  public void settings() {
    plotWidth = Display.scale(prefs.plotWidth);
    plotHeight = Display.scale(prefs.plotHeight);
    View.COLOR = prefs.plotColor;
    int pw = Display.scale(Preferences.defPlotWidth);
    int ph = Display.scale(Preferences.defPlotHeight);
    size(optWidth + varWidth + 4 * pw, labelHeightClose + 3 * ph, RENDERER);
    pixelDensity(PIXEL_DENSITY);
    smooth(SMOOTH_LEVEL);
  
    if (RENDERER == P2D || RENDERER == P3D) {
      final int[] sizes = { 16, 32, 48, 64, 128, 256, 512 };
      String[] icons = new String[sizes.length];
      for (int i = 0; i < sizes.length; i++) {
        icons[i] = "data/icons/icon-" + sizes[i] + ".png";
      }
      PJOGL.setIcon(icons);
    }
  }  
  
  public void setup() {
    Log.init(false);
//    loadPreferences();

    intf = new Interface(this, "style.css");
    intf.setBackground(color(247));
    initPanel();
    
//    uploader = new UploadHandler(this);
    
    surface.setTitle(APP_NAME + " is loading...");

    loadSession();    
    loadProject(inputFile);
  }
  
  public void draw() {        
    if (loaded) {
      history.update();
      intf.update();
      intf.draw();
    }
    if (animating) {
      drawLoadAnimation();
    }
    if (loadingError) {
      JOptionPane.showMessageDialog(new Frame(),
              "The following error ocurred while loading the dataset:\n\n" +
                      errorMessage + "\n\nFix this error and try opening again, or pick another dataset.\n" +
                      "Mirador will now quit.", "Data loading error!",
          JOptionPane.ERROR_MESSAGE);     
      exit();      
    }
  }  
  
  public void mousePressed() {
    if (loaded) intf.mousePressed();
  }
  
  public void mouseDragged() {
    if (loaded) intf.mouseDragged();
  }

  public void mouseReleased() {
    if (loaded) intf.mouseReleased();
  }
  
  public void mouseMoved() {
    if (loaded) intf.mouseMoved();
  }
  
  public void keyPressed() {
    if (loaded) intf.keyPressed();
  }
    
  public void keyReleased() {
    if (loaded) intf.keyReleased();
  }  

  public void keyTyped() {
    if (loaded) intf.keyTyped();
  } 
  
  //////////////////////////////////////////////////////////////////////////////  
  
  public int getPlotType() {
    return plotType;  
  }

  public void setPlotType(int type) {
    if (plotType != type) {
      plotType = type; 
      browser.dataChanged();
      history.setPlotType(type);
    }      
  }  
  
  public int getPValue() {
    return project.pValue;
  }
  
  public void setPValue(int val) {
    if (project.pValue != val) {
      project.pValue = val;
      project.save();
      browser.pvalueChanged();
      dataset.resort(project.pvalue(), project.missingThreshold());
      history.setPValue(project.pvalue());
    }    
  }
  
  public int getMissingThreshold() {
    return project.missThreshold;
  }
  
  public void setMissingThreshold(int threshold) {
    if (project.missThreshold != threshold) {
      project.missThreshold = threshold;
      project.save();
      dataset.resort(project.pvalue(), project.missingThreshold());
      history.setMissingThreshold(project.missingThreshold());
    }
  }
  
  public void updateRanges(RangeSelector selector, boolean resort) {
    Variable var = selector.getVariable();
    Range range = selector.getRange();
    int result = ranges.update(var, range);
    if (resort) dataset.resort(ranges);
    if (result != DataRanges.NO_CHANGE) {      
      browser.dataChanged();
      if (result == DataRanges.ADDED_RANGE) {
        history.addRange(var, range);
      } else if (result == DataRanges.MODIFIED_RANGE) {
        history.replaceRange(var, range);
      } else if (result == DataRanges.REMOVED_RANGE) {
        history.removeRange(var);
      }      
    }
  }
  
  public void resetRanges() {
    if (0 < ranges.size()) {
      browser.resetSelectors();
      ranges.clear();
      dataset.resort(ranges);
      browser.dataChanged();
      history.clearRanges();
    }    
  }
  
  public void loadProject(String filename) {
    try {
      project = new Project(filename, prefs);
      Path p = Paths.get(filename);
      Path filePath = p.toAbsolutePath().getParent().toAbsolutePath();      
      prefs.setProjectFolder(filePath.toString(), project.dataFile);
      prefs.save();
      if (history != null) history.dispose();
      history = new History(this, project, plotType);      
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(new Frame(), ex.getMessage(), "Loading error!", 
                                    JOptionPane.ERROR_MESSAGE);     
      exit();
    }
    
    if (project != null) {
      loaded = false;
      animating = true;
      animTime = 0;
      ranges.clear();
      animAlpha = new SoftFloat(255);
      loadThread = new LoadThread();
      loadThread.start();

      maxPlotSliceSizeCC = project.initSliceSize;
      maxPlotSliceSizeNN = project.initSliceSize;
      maxPlotSliceSizeCN = project.initSliceSize;

      if (options != null) {
        options.projectUpdate();
      }
    }    
  }
  
  public void loadDataset() {
    selectInput("Select data for analysis:",
                "outputSelected", new File(prefs.projectFolder), new LoadHandler(this));    
  }
  
  public void reloadDataset() {
    if (new File(project.dataFolder).exists()) {
      loadProject(project.dataFolder);
    }
  }
  
  public void exportProfile(ArrayList<Variable> vars) {    
    File file = new File(project.dataFolder, "variables.txt");
    selectOutput("Select a csv or tsv file to save the selection to:", 
                 "outputSelected", file, new ProfileHandler(this, vars));
  }
  
  public void exportSelection() {
    File file = new File(project.dataFolder, "data.csv");
    selectOutput("Select a csv or tsv file to export the data to:",
                 "outputSelected", file, new ExportHandler(this));
  }
  
//  public void uploadSession() {
//    if (!uploader.isAuthenticated()) {
//      UserLogin login = new UserLogin(this);
//      login.setVisible(true);
//    } else {
//      uploader.upload();
//    }
//  }
  
//  public void savePDF() {
//    File file = new File(project.dataFolder, "capture.pdf");
//    selectOutput("Enter the name of the PDF file to save the screen to",
//                 "outputSelected", file, new PDFHandler(this));
//  }

  public void saveSelectedPlot() {
    browser.saveSelectedPlot();
  }

  //////////////////////////////////////////////////////////////////////////////


  public int getPlotSliceSize(Variable v1, Variable v2) {
    if (v1.string() || v2.string()) return 0;
    if (v1.categorical() && v2.categorical()) {
      return maxPlotSliceSizeCC;
    } else if (v1.numerical() && v2.numerical()) {
      return maxPlotSliceSizeNN;
    } else {
      return maxPlotSliceSizeCN;
    }
  }

  public void clockPlotTime(int time, Variable v1, Variable v2) {
    if (v1.string() || v2.string()) return;
    if (v1.categorical() && v2.categorical()) {
      calcPlotTimeCC(time);
    } else if (v1.numerical() && v2.numerical()) {
      calcPlotTimeNN(time);
    } else {
      calcPlotTimeCN(time);
    }
  }

  private void calcPlotTimeCC(int time) {
    plotTimesCC.add(time);
    if (targetClockSize < plotTimesCC.size()) {
      plotTimesCC.remove(0);
    }
    clockUpdateCC++;
    if (clockUpdateCC == targetClockSize) {
      float meanTime = 0;
      synchronized (plotTimesCC) {
        Iterator i = plotTimesCC.iterator(); // Must be in synchronized block
        while (i.hasNext()) {
          int value = (Integer)i.next();
          meanTime += value;
        }
      }
      meanTime /= plotTimesCC.size();
      if (meanTime < 0.5 * project.maxPlotTime || project.maxPlotTime < meanTime) {
        float f = PApplet.min(PApplet.sqrt(project.maxPlotTime / meanTime), 2);
        maxPlotSliceSizeCC = PApplet.max((int)(maxPlotSliceSizeCC * f), 1000);
      }
      clockUpdateCC = 0;
    }
  }

  private void calcPlotTimeNN(int time) {
    plotTimesNN.add(time);
    if (targetClockSize < plotTimesNN.size()) {
      plotTimesNN.remove(0);
    }
    clockUpdateNN++;
    if (clockUpdateNN == targetClockSize) {
      float meanTime = 0;
      synchronized (plotTimesNN) {
        Iterator i = plotTimesNN.iterator(); // Must be in synchronized block
        while (i.hasNext()) {
          int value = (Integer)i.next();
          meanTime += value;
        }
      }
      meanTime /= plotTimesNN.size();
      if (meanTime < 0.5 * project.maxPlotTime || project.maxPlotTime < meanTime) {
        float f = PApplet.min(PApplet.sqrt(project.maxPlotTime / meanTime), 2);
        maxPlotSliceSizeNN = PApplet.max((int)(maxPlotSliceSizeNN * f), 1000);
      }
      clockUpdateNN = 0;
    }
  }

  private void calcPlotTimeCN(int time) {
    plotTimesCN.add(time);
    if (targetClockSize < plotTimesCN.size()) {
      plotTimesCN.remove(0);
    }
    clockUpdateCN++;
    if (clockUpdateCN == targetClockSize) {
      float meanTime = 0;
      synchronized (plotTimesCN) {
        Iterator i = plotTimesCN.iterator(); // Must be in synchronized block
        while (i.hasNext()) {
          int value = (Integer)i.next();
          meanTime += value;
        }
      }
      meanTime /= plotTimesCN.size();
      if (meanTime < 0.5 * project.maxPlotTime || project.maxPlotTime < meanTime) {
        float f = PApplet.min(PApplet.sqrt(project.maxPlotTime / meanTime), 2);
        maxPlotSliceSizeCN = PApplet.max((int)(maxPlotSliceSizeCN * f), 1000);
      }
      clockUpdateCN = 0;
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  
  protected void drawLoadAnimation() {
    animAlpha.update();
    int alpha = animAlpha.getFloor();
    if (alpha == 0) {
      animating = false;
      MiraApp.this.surface.setResizable(true);
    }    
    float x = 0.5f * width;
    float y = 0.5f * height;
    float r = 75;
    fill(color(247), alpha);
    rect(0, 0, width, height);
    noStroke();
    pushMatrix();
    translate(0.5f * width, 0.5f * height);
    rotate(animTime);
    float da = PConstants.TWO_PI / 8;
    for (int i = 0; i < 8; i++) {
      fill(106, 179, 219, PApplet.map(i, 0, 9, 0, alpha));
      arc(0, 0, r, r, i * da, (i + 1) * da);
    }
    popMatrix();
    fill(250, alpha);
    ellipse(x, y, r - 5, r - 5);
    animTime += 0.1f;
  }
  
  protected void loadSession() {
    plotType = View.EIKOSOGRAM;
    ranges = new DataRanges();    
  }  

  protected void initPanel() {
    options = new OptionsPanel(intf, 0, 0, optWidth, height);
    intf.add(options);
  }
  
  protected void initInterface() {
    intf.remove(browser);
    intf.remove(profile);
    
    browser = new VariableBrowser(intf, optWidth, 0, width - optWidth, height);          
    intf.add(browser);
    
    profile = new Profile(intf, optWidth, 0, width - optWidth, height);
    profile.hide(false);
    intf.add(profile);   
  }
  
  protected class LoadThread extends Thread { 
    @Override
    public void run() {
      loadingError = false;
      try {
        dataset = new DataSet(project);
        initInterface();
        loaded = true;
        animAlpha.setTarget(0);
        surface.setTitle(project.dataTitle);
      } catch (Exception ex) {
        loadingError = true;
        errorMessage = ex.getMessage();
      }
    }
  }  
  
  //////////////////////////////////////////////////////////////////////////////  
  
  static public void loadPreferences() {
    miraFolder = defaultFolder();
    try {
      prefs = new Preferences(miraFolder.toString());
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  static public void copyExamples() {
    if (prefs.copyExamples) {
      try {
        URL url = MiraLauncher.class.getProtectionDomain().getCodeSource().getLocation();
        File jarFile = Paths.get(url.toURI()).toFile();
        File builtinExamplesFolder = new File(jarFile.getParent(), "examples");
        File defExamplesFolder = new File(miraFolder, "examples");
        if (builtinExamplesFolder.exists() && !defExamplesFolder.exists()) {
          // Copy the built-in examples to the default folder
          Fileu.copyFolder(builtinExamplesFolder, defExamplesFolder);
          prefs.copyExamples = false;
          prefs.save();
        }
      } catch (Exception ex) {
        System.err.println("Error tyring to copy the built-in examples:");
        ex.printStackTrace();
      }
    }
  }


  static public File defaultFolder() {
    File homeFolder = new File(System.getProperty("user.home"));
    File cfgFolder = new File(homeFolder, ".mirador");
    File cfgFile = new File(cfgFolder, "config.txt");
    File miraFolder = homeFolder;
    boolean save = true;
    if (!cfgFolder.exists() || !cfgFile.exists()) {
      if (!cfgFolder.exists()) {
        boolean success = cfgFolder.mkdirs();
        if (!success) {
          System.err.println("Cannot create .mirador inside the home folder");
          save = false;
        }
      }
      // Set default locations
      miraFolder = new File(homeFolder, "Documents");
      if (miraFolder.exists()) {
        miraFolder = new File(miraFolder, "Mirador");
      } else {
        miraFolder = new File(homeFolder, "Mirador");
      }
    } else {
      String[] lines = PApplet.loadStrings(cfgFile);
      if (0 < lines.length) {
        miraFolder = new File(lines[0]);
        if (miraFolder.exists()) {
          save = false;
        } else {
          // Folder in config does not exist, try default locations
          miraFolder = new File(homeFolder, "Documents");
          if (miraFolder.exists()) {
            miraFolder = new File(miraFolder, "Mirador");
          } else {
            miraFolder = new File(homeFolder, "Mirador");
          }
        }
      }
    }

    if (save) {
      PApplet.saveStrings(cfgFile, new String[] {miraFolder.getAbsolutePath()});
    }

    return miraFolder;
  }
  
  public static void main(String args[]) {
    if (0 < args.length) inputFile = args[0];
    if (!(new File(inputFile)).isAbsolute()) {
      String appPath = System.getProperty("user.dir");
      inputFile = (new File(appPath, inputFile)).getAbsolutePath();      
    }    
    PApplet.main(new String[] { MiraApp.class.getName() });
  }  
}

/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import mirador.handlers.LoadHandler;
import mirador.handlers.PDFHandler;
import mirador.handlers.ProfileHandler;
import mirador.handlers.SelectionHandler;
import mirador.handlers.UploadHandler;
import mirador.ui.Interface;
import mirador.ui.SoftFloat;
import mirador.views.View;
import miralib.data.DataRanges;
import miralib.data.DataSet;
import miralib.data.Range;
import miralib.data.Variable;
import miralib.utils.Log;
import miralib.utils.Preferences;
import miralib.utils.Project;
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
  static public final String APP_VERSION = "1.4beta";
  
  static public String inputFile = "default.mira";
  static protected Preferences prefs;
  
  // TODO: move these somewhere else, make into parameters?
  int optWidth = 120;  
  int varWidth = 300;
  int varHeight = 85;
  int labelHeightClose = 115;
  int labelHeightMax = 300;
  int covarHeightClose = 50;
  int covarHeightMax = 300;
  int plotWidth = 200;
  int plotHeight = 200;
  /////////////////////////////////////////////////////////////////////////////

  public Project project;
  public DataSet dataset;
  public DataRanges ranges;
  public History history;
  public UploadHandler uploader;
  
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
  
  public void settings() {
    size(optWidth + varWidth + 4 * plotWidth, labelHeightClose + 3 * plotHeight, RENDERER);
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
    Log.init(true);
    loadPreferences();
    
    intf = new Interface(this, "style.css");
    intf.setBackground(color(247));
    initPanel();
    
    uploader = new UploadHandler(this);
    
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
      JOptionPane.showMessageDialog(new Frame(), errorMessage, "Loading error!", 
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
    history.read();
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
      prefs.projectFolder = filePath.toString();
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
    }    
  }
  
  public void loadDataset() {
    selectInput("Select a csv or tsv file to save the selection to:", 
                "outputSelected", new File(prefs.projectFolder), new LoadHandler(this));    
  }
  
  public void reloadDataset() {
    if (project.cfgFile != null) {
      loadProject(project.cfgFile.getPath());  
    } else {
      loadProject(project.dataFile);
    }    
  }
  
  public void exportProfile(ArrayList<Variable> vars) {    
    File file = new File(project.dataFolder, "variables.txt");
    selectOutput("Select a csv or tsv file to save the selection to:", 
                 "outputSelected", file, new ProfileHandler(this, vars));
  }
  
  public void exportSelection() {
    if (browser.getSelectedRow() != null && browser.getSelectedCol() != null) {
      File file = new File(project.dataFolder, "selected-data.tsv");
      selectOutput("Select a csv or tsv file to save the selection to:", 
                   "outputSelected", file, new SelectionHandler(this, browser.getSelectedCol(), 
                                                                      browser.getSelectedRow()));      
    }
  }
  
  public void uploadSession() {    
    if (!uploader.isAuthenticated()) {
      UserLogin login = new UserLogin(this);
      login.setVisible(true);
    } else {
      uploader.upload();
    }
  }
  
  public void savePDF() {
    File file = new File(project.dataFolder, "capture.pdf");
    selectOutput("Enter the name of the PDF file to save the screen to", 
                 "outputSelected", file, new PDFHandler(this));    
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
    try {
      prefs = new Preferences(defaultFolder().toString());
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }        
  }
  
  static public File defaultFolder() {
    String path = Mirador.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    // Path may have URL encoding, so remove it
    String decodedPath = PApplet.urlDecode(path);
    if (decodedPath.toLowerCase().contains("/mirador/bin")) {
      // Running from Eclipse
      File file = new File(path);
      String filePath = file.getAbsolutePath();
      return new File(filePath, "../examples/.");      
    } else {
      if (PApplet.platform == PApplet.MACOSX) {      
        File file = new File(path);
        String absolutePath = file.getAbsolutePath();
        String filePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));      
        return new File(filePath, "../Resources/examples/.");
      } else {
        return new File(System.getProperty("user.dir"), "examples/.");
      }      
    }
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

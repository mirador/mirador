/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mira.app;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import lib.math.SoftFloat;
import lib.ui.Interface;
import mira.data.DataRanges;
import mira.data.DataSet;
import mira.data.Variable;
import mira.utils.Log;
import mira.utils.Preferences;
import mira.utils.Project;
import mira.views.View;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.data.Table;

/**
 * Mirador main class. 
 * 
 */

@SuppressWarnings("serial")
public class MiraApp extends PApplet {
  static public String RENDERER = P2D;
  static public int SMOOTH_LEVEL = 4;
  static public final String APP_NAME = "mirador";
  static public final String APP_VERSION = "1.1";
  
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

  public Project project;
  public DataSet dataset;
  public DataRanges ranges;
  
  protected Interface intf;  
  protected OptionsPanel options;  
  public VariableBrowser browser;
  protected Profile profile;
    
  protected int plotType;
  
  protected boolean loaded;  
  protected LoadThread loadThread;
  protected boolean animating;
  protected float animTime;
  protected SoftFloat animAlpha;  
 
  public boolean savingToPdf = false;
  protected String pdfFilename;
  
  protected String username;
  protected String password;
  protected boolean user_authenticated = false;
  
  public int sketchQuality() {
    return SMOOTH_LEVEL;
  }

  public int sketchWidth() {
    return optWidth + varWidth + 4 * plotWidth;
  }

  public int sketchHeight() {
    return labelHeightClose + 3 * plotHeight;
  }

  public String sketchRenderer() {
    return RENDERER;
  }

  public boolean sketchFullScreen() {
    return false;
  }  
    
  public void setup() {
    size(optWidth + varWidth + 4 * plotWidth, labelHeightClose + 3 * plotHeight, RENDERER);
    smooth(SMOOTH_LEVEL);
    
    Log.init();
    loadPreferences();
    
    intf = new Interface(this, "style.css");
    intf.setBackground(color(247));
    initPanel();
    
    frame.setTitle(APP_NAME + " is loading...");
    frame.setAutoRequestFocus(true);
    
    loadSession();
    
    try {
      project = new Project(inputFile, prefs);
      Path p = Paths.get(inputFile);
      Path filePath = p.toAbsolutePath().getParent().toAbsolutePath();
      prefs.projectFolder = filePath.toString();
      prefs.save();
//      System.err.println(prefs.projectFolder);
      
    } catch (IOException e) {
      e.printStackTrace();
      exit();
    }
     
    if (project != null) {
      loaded = false;
      animating = true;
      animTime = 0;
      animAlpha = new SoftFloat(255);
      loadThread = new LoadThread();
      loadThread.start();
    }
  }
  
  public void draw() {        
    if (loaded) {
      intf.update();
      intf.draw();
    }
    if (animating) {
      drawLoadAnimation();
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
    }    
  }
  
  public int getMissingThreshold() {
    return project.missingThreshold;
  }
  
  public void setMissingThreshold(int threshold) {
    if (project.missingThreshold != threshold) {
      project.missingThreshold = threshold;
      project.save();
      dataset.resort(project.pvalue(), project.missingThreshold());      
    }
  }
  
  public void updateRanges(RangeSelector selector, boolean resort) {
    boolean change = ranges.update(selector.getVariable(), selector.getRange());
    if (resort) dataset.resort(ranges);
    if (change) browser.dataChanged();
  }
  
  public void resetRanges() {
    if (0 < ranges.size()) {
      browser.resetSelectors();
      ranges.clear();
      dataset.resort(ranges);
      browser.dataChanged();
    }    
  }
  
  public void loadDataset() {
    selectInput("Select a csv or tsv file to save the selection to:", 
                "outputSelected", new File(prefs.projectFolder), new LoadHandler());    
  }
  
  public void exportProfile(ArrayList<Variable> vars) {    
    File file = new File(project.dataFolder, "profile-data.tsv");
    selectOutput("Select a csv or tsv file to save the selection to:", 
                 "outputSelected", file, new ProfileHandler(vars));
  }
  
  public void exportSelection() {
    if (browser.getSelectedRow() != null && browser.getSelectedCol() != null) {
      File file = new File(project.dataFolder, "selected-data.tsv");
      selectOutput("Select a csv or tsv file to save the selection to:", 
                   "outputSelected", file, new SelectionHandler(browser.getSelectedCol(), 
                                                                browser.getSelectedRow()));      
    }
  }
  
  public void uploadSession() {
    JFrame frame = new JFrame("SEErador - Login");
    frame.setSize(300, 150);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel panel = new JPanel();
    frame.add(panel);
    placeComponents(panel);

    frame.setVisible(true);    
  }
  
  public void savePDF() {
    File file = new File(project.dataFolder, "capture.pdf");
    selectOutput("Enter the name of the PDF file to save the screen to", 
                 "outputSelected", file, new PDFHandler());
    
  }
  
  protected void placeComponents(JPanel panel) {
    panel.setLayout(null);

    JLabel userLabel = new JLabel("User");
    userLabel.setBounds(10, 10, 80, 25);
    panel.add(userLabel);

    final JTextField userText = new JTextField(20);
    userText.setBounds(100, 10, 160, 25);
    panel.add(userText);

    JLabel passwordLabel = new JLabel("Password");
    passwordLabel.setBounds(10, 40, 80, 25);
    panel.add(passwordLabel);

    final JPasswordField passwordText = new JPasswordField(20);
    passwordText.setBounds(100, 40, 160, 25);
    panel.add(passwordText);

    JButton loginButton = new JButton("Login");
    loginButton.setBounds(5, 80, 80, 25);
    panel.add(loginButton);
    
    JButton registerButton = new JButton("What's this?");
    registerButton.setBounds(160, 80, 100, 25);
    panel.add(registerButton);
    
    loginButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent event){
          
          username = userText.getText();
          password = passwordText.getText();
          
//          HttpClientExample client;
          try {
          user_authenticated = HttpClientExample.authenticate(username,password);
          System.out.println(user_authenticated);
        } catch (Exception e) {
          e.printStackTrace();
        }
          
          //(new Thread(mirac)).start();
        
        }
        
      });
    registerButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent event){
          
          try {

             String url ="http://localhost/classes/access_user/register.php";

             Desktop dt = Desktop.getDesktop();
             URI uri = new URI(url);
             dt.browse(uri.resolve(uri));


         } catch (URISyntaxException ex) {
         } catch (IOException ex) {
         }
        
        }
        
      });
    
    
  } 
  
  //////////////////////////////////////////////////////////////////////////////
  
  protected void drawLoadAnimation() {
    animAlpha.update();
    int alpha = animAlpha.getFloor();
    if (alpha == 0) {
      animating = false;
      MiraApp.this.frame.setResizable(true);
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
      dataset = new DataSet(project);      
      initInterface();      
      loaded = true;
      animAlpha.setTarget(0);
      frame.setTitle(project.dataTitle);
    }
  }  
  
  protected class LoadHandler {
    public void outputSelected(File selection) {
      if (selection != null) {
        try {
          project = new Project(selection.toString(), prefs);          
          Path p = Paths.get(selection.toString());
          Path filePath = p.toAbsolutePath().getParent().toAbsolutePath();          
          prefs.projectFolder = filePath.toString();
          prefs.save();
          
          loaded = false;
          animating = true;
          animTime = 0;
          animAlpha = new SoftFloat(255);
          loadThread = new LoadThread();
          loadThread.start();
        } catch (IOException e) {
          e.printStackTrace();
          exit();
        }        
      }
    }    
  }
  
  protected class ProfileHandler {
    ArrayList<Variable> variables;
    
    ProfileHandler(ArrayList<Variable> vars) {
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
      
      Table[] tabdict = dataset.getTable(variables, ranges);
      Table data = tabdict[0];
      if (data != null) {
        saveTable(data, filename);
      }
      
      Table dict = tabdict[1];
      if (dict != null) {      
        saveTable(dict, dictFile.getAbsolutePath());          
      }
      
      Table vars = dataset.getProfile(variables);      
      if (vars != null) {
        saveTable(vars, varsFile.getAbsolutePath());  
      }
      
      Project proj = new Project(project);
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
  
  protected class SelectionHandler {
    ArrayList<Variable> variables;
    
    SelectionHandler(Variable varx, Variable vary) {
      variables = new ArrayList<Variable>();
      variables.add(varx);
      variables.add(vary);
    }
    
    public void outputSelected(File selection) {
      String filename = selection.getAbsolutePath();    
      String ext = PApplet.checkExtension(filename);
      if (ext == null || (!ext.equals("csv") && !ext.equals("tsv"))) {
        filename += ".tsv";
      }
      Path dataPath = Paths.get(filename);
      String filePath = dataPath.getParent().toAbsolutePath().toString(); 
      File dictFile = new File(filePath, "selected-dictionary.tsv");

      Table[] tabdict = dataset.getTable(variables, ranges);
      Table data = tabdict[0];
      if (data != null) {
        saveTable(data, filename);
      }
      
      Table dict = tabdict[1];
      if (dict != null) {      
        saveTable(dict, dictFile.getAbsolutePath());          
      }
    }
  }
  
  protected class PDFHandler {
    public void outputSelected(File selection) {
      pdfFilename = selection.getAbsolutePath();    
      String ext = PApplet.checkExtension(pdfFilename);
      if (ext == null || !ext.equals("pdf")) {
        pdfFilename += ".pdf";
      }
      intf.record(pdfFilename);
      savingToPdf = true;
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

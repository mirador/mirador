/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import java.io.File;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;
import miralib.utils.Log;
import processing.core.PApplet;
import mirador.views.View;
import miralib.data.Range;
import miralib.data.Variable;
import miralib.math.Numbers;
import miralib.utils.Project;

/**
 * Stores a session of data exploration: set of visible plots, current ranges,
 * sorting variable, p-value, missing threshold, plot type, and selected 
 * variables, and any other operations done through Mirador's interface.
 *
 */

public class Session {
  static protected int FLUSH_INTERVAL = 10000; // in millis
  
  protected MiraApp app;
  protected Project prj;
  protected HashSet<VariablePair> pairs;
  protected HashSet<VariableRange> ranges;

  protected float pvalue;
  protected float misst;
  protected int plotType;

  protected VariablePair selPair;
  protected Variable sortVar;
  protected boolean openProfile;
    
  protected File file;
  protected PrintWriter writer;
  protected int lastFlush = 0;
  protected boolean changed = false;

  protected boolean enabled = true;


  protected OSCPortOut sender;

  static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd.HHmm");
  
  public Session(MiraApp app, Project prj, String filename, int ptype) {
    this.app = app;
    this.prj = prj;

    if (!prj.saveSessions) {
      enabled = false;
      return;
    }

    File folder = new File(prj.dataFolder, "sessions");
    if (!folder.exists()) {
      boolean success = folder.mkdirs();
      if (!success) {
        Log.warning("Cannot create session folder inside the data folder. Session recording will be disabled.");
        enabled = false;
        return;
      }
    }

    String stamp = getDateStamp();
    file = new File(folder, "session-" + stamp);
    writer = PApplet.createWriter(file);
    
    pairs = new HashSet<VariablePair>();
    ranges = new HashSet<VariableRange>();
    
    pvalue = -1;
    misst = -1;
    plotType = -1;
    
    selPair = null;
    sortVar = null;
    openProfile = false;

    if (prj.oscOutput) oscInit();
    loadProject(filename);
    setPValue(prj.pvalue());
    setMissingThreshold(prj.missingThreshold());
    setPlotType(ptype);
  }

  public void loadProject(String filename) {
    if (!enabled) return;
    int m = app.millis();
    write("LOAD\t" + m + "\t" + filename);
    if (prj.oscOutput) oscSend("LOAD", m, filename);
  }

  public void addPair(Variable varx, Variable vary) {
    if (!enabled) return;
    VariablePair pair = new VariablePair(varx, vary);
    if (pairs.add(pair)) {
      int m = app.millis();
      write("+PAIR\t" + m + "\t" + varx.getName() + "\t" + varx.getAlias() + "\t" + vary.getName() + "\t" + vary.getAlias());
      if (prj.oscOutput) oscSend("+PAIR", m, varx.getName(), varx.getAlias(), vary.getName(), vary.getAlias());
    }
  }
  
  public void removePair(Variable varx, Variable vary) {
    if (!enabled) return;
    VariablePair pair = new VariablePair(varx, vary);
    if (pairs.remove(pair)) {
      int m = app.millis();
      write("-PAIR\t" + m + "\t" + varx.getName() + "\t" + varx.getAlias() + "\t" + vary.getName() + "\t" + vary.getAlias());
      if (prj.oscOutput) oscSend("-PAIR", m, varx.getName(), varx.getAlias(), vary.getName(), vary.getAlias());
    }
  }
  
  public void addRange(Variable var, Range range) {
    if (!enabled) return;
    VariableRange vrange = new VariableRange(var, range);
    if (ranges.add(vrange)) {
      String frange = var.formatRange(range, false);
      int m = app.millis();
      write("+RANGE\t" + m + "\t" + var.getName() + "\t" + var.getAlias() + "\t" + frange);
      if (prj.oscOutput) oscSend("+RANGE", m, var.getName(), var.getAlias(), frange);
    }
  }
  
  public void removeRange(Variable var) {
    if (!enabled) return;
    VariableRange vrange = null;
    for (VariableRange r: ranges) {
      if (r.var == var) {
        vrange = r;
        break;
      }
    }
    if (vrange != null) {
      Range range = vrange.range;
      ranges.remove(vrange);
      String frange = var.formatRange(range, false);
      int m = app.millis();
      write("-RANGE\t" + m + "\t" + var.getName() + "\t" + var.getAlias() + "\t" + frange);
      if (prj.oscOutput) oscSend("-RANGE", m, var.getName(), var.getAlias(), frange);
    }
  }
  
  public void replaceRange(Variable var, Range range) {
    if (!enabled) return;
    VariableRange vrange = null;
    for (VariableRange r: ranges) {
      if (r.var == var) {
        vrange = r;
        break;
      }
    }
    if (vrange != null) {
      ranges.remove(vrange);
      vrange = new VariableRange(var, range);
      if (ranges.add(vrange)) {
        String frange = var.formatRange(range, false);
        int m = app.millis();
        write("~RANGE\t" + m + "\t" + var.getName() + "\t" + var.getAlias() + "\t" + frange);
        if (prj.oscOutput) oscSend("~RANGE", m, var.getName(), var.getAlias(), frange);
      }      
    }
  }
  
  public void clearRanges() {
    if (!enabled) return;
    Object[] array = ranges.toArray();
    for (Object obj: array) {
      if (ranges.remove(obj)) {
        VariableRange vrange = (VariableRange)obj;
        Variable var = vrange.var;
        Range range = vrange.range;
        String frange = var.formatRange(range, false);
        int m = app.millis();
        write("-RANGE\t" + m + "\t" + var.getName() + "\t" + var.getAlias() + "\t" + frange);
        if (prj.oscOutput) oscSend("-RANGE", m, var.getName(), var.getAlias(), frange);
      }
    }
  }
  
  public void setPValue(float pvalue) {
    if (!enabled) return;
    if (Numbers.different(this.pvalue, pvalue)) {
      this.pvalue = pvalue;
      int m = app.millis();
      write("PVALUE\t" + m + "\t" + pvalue);
      if (prj.oscOutput) oscSend("PVALUE", m, String.valueOf(pvalue));
    }
  }
  
  public void setMissingThreshold(float misst) {
    if (!enabled) return;
    if (Numbers.different(this.misst, misst)) {
      this.misst = misst;
      int m = app.millis();
      write("MISSING\t" + m + "\t" + misst);
      if (prj.oscOutput) oscSend("MISSING", m, String.valueOf(misst));
    }
  }

  public void setPlotType(int plotType) {
    if (!enabled) return;
    if (this.plotType != plotType) {
      this.plotType = plotType;
      String stype = View.typeToString(plotType);
      int m = app.millis();
      write("PLOT\t" + m + "\t" + stype);
      if (prj.oscOutput) oscSend("PLOT", m, stype);
    }
  }
  
  public void setSelectedPair(Variable varx, Variable vary) {
    if (!enabled) return;
    if (varx != null && vary != null) {
      VariablePair pair = new VariablePair(varx, vary);
      if (!pair.equals(selPair)) {
        selPair = pair;
        int m = app.millis();
        write("SELECT\t" + m + "\t" + varx.getName() + "\t" + varx.getAlias() + "\t" + vary.getName() + "\t" + vary.getAlias());
        if (prj.oscOutput) oscSend("SELECT", m, varx.getName(), varx.getAlias(), vary.getName(), vary.getAlias());
      }      
    } else if (selPair != null) {
      selPair = null;
      int m = app.millis();
      write("SELECT\t" + m + "\tNONE");
      if (prj.oscOutput) oscSend("SELECT", m, "NONE");
    }
  }
  
  public void sort(Variable var) {
    if (!enabled) return;
    if (sortVar != var) {
      sortVar = var;
      int m = app.millis();
      write("SORT\t" + m + "\t" + var.getName() + "\t" + var.getAlias());
      if (prj.oscOutput) oscSend("SORT", m, var.getName(), var.getAlias());
    }
  }
  
  public void unsort() {
    if (!enabled) return;
    if (sortVar != null) {
      sortVar = null;
      int m = app.millis();
      write("SORT\t" + m + "\tNONE");
      if (prj.oscOutput) oscSend("SORT", m, "NONE");
    }
  }
  
  public void openProfile() {
    if (!enabled) return;
    if (!openProfile) {
      openProfile = true;
      int m = app.millis();
      write("+PROFILE\t" + m);
      if (prj.oscOutput) oscSend("+PROFILE", m, "");
    }
  }
  
  public void closeProfile() {
    if (!enabled) return;
    if (openProfile) {
      openProfile = false;
      int m = app.millis();
      write("-PROFILE\t" + m);
      if (prj.oscOutput) oscSend("-PROFILE", m, "");
    }    
  }  
  
  public void update() {
    if (!enabled) return;
    int t = app.millis();
    if (FLUSH_INTERVAL < t - lastFlush && changed) {
      writer.flush();
      lastFlush = t;      
      changed = false;
    }
  }
  
  public void dispose() {
    if (!enabled) return;
    writer.flush();
    writer.close();
  }
  
  public String read() {
    if (!enabled) return "";
    writer.flush();
    String[] lines = PApplet.loadStrings(file);
    String concat = "";
    for (String line: lines) {
      concat += line + "\n";
    }
    return concat;
  }

  protected void oscInit() {
    try {
      sender = new OSCPortOut(InetAddress.getByName(prj.hostName), prj.portNumber);
    } catch (Exception ex) {
      // this is just a demo program, so this is acceptable behavior
      ex.printStackTrace();
    }
  }

  protected void oscSend(String command, int timestamp, float...data) {
    if (sender != null) {
      List<Object> args = new ArrayList<Object>(6);
      args.add(timestamp);
      for (int i = 0; i < data.length; i++) {
        args.add(data[i]);
      }
      OSCMessage msg = new OSCMessage("/" + command, args);
      try {
        sender.send(msg);
      } catch (Exception ex) {
        Log.error("Error sending OSC message", ex);
      }
    }
  }

  protected void oscSend(String command, int timestamp, int...data) {
    if (sender != null) {
      List<Object> args = new ArrayList<Object>(6);
      args.add(timestamp);
      for (int i = 0; i < data.length; i++) {
        args.add(data[i]);
      }
      OSCMessage msg = new OSCMessage("/" + command, args);
      try {
        sender.send(msg);
      } catch (Exception ex) {
        Log.error("Error sending OSC message", ex);
      }
    }
  }

  protected void oscSend(String command, int timestamp, String...data) {
    if (sender != null) {
      List<Object> args = new ArrayList<Object>(6);
      args.add(timestamp);
      for (int i = 0; i < data.length; i++) {
        args.add(data[i]);
      }
      OSCMessage msg = new OSCMessage("/" + command, args);
      try {
        sender.send(msg);
      } catch (Exception ex) {
        Log.error("Error sending OSC message", ex);
      }
    }
  }

  protected void write(String line) {
    writer.println(line);
    changed = true;
  }

  static public String getDateStamp() {
    return dateFormat.format(new Date());
  }

  class VariablePair {
    Variable varx, vary;
    
    public VariablePair(Variable varx, Variable vary) {
      this.varx = varx;
      this.vary = vary;
    }

    public boolean equals(Object obj) {
      if (obj instanceof VariablePair) {
        VariablePair that = (VariablePair)obj;
        return this.varx == that.varx && this.vary == that.vary;
      } else {
        return false;  
      }
    }
    
    public int hashCode() {
      return 31 + 7 * varx.hashCode() + 11 * vary.hashCode();
    } 
  }
  
  class VariableRange {
    Variable var;
    Range range;
    
    public VariableRange(Variable var, Range range) {
      this.var = var;
      this.range = range;
    }
    
    public boolean equals(Object obj) {
      if (obj instanceof VariablePair) {
        VariableRange that = (VariableRange)obj;
        return this.var == that.var && this.range == that.range;
      } else {
        return false;  
      }
    }
    
    public int hashCode() {
      return 31 + 7 * var.hashCode() + 11 * range.hashCode();
    }     
  }
}

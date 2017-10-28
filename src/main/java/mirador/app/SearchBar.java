/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import mui.Display;
import mui.EditableText;
import mui.Interface;
import mui.SoftFloat;
import miralib.data.Variable;

/**
 * Widget containing the search bar.
 *
 */

public class SearchBar extends MiraWidget {
  int bColor;
  PFont h1Font;
  int h1Color; 
  
  ArrayList<SearchTab> searchTabs; 
  SearchBox searchBox;
  SearchResults searchRes;
  
  float tabsY = Display.scale(10);  
  float tabsX = Display.scale(10);  
  float tabsH = Display.scale(30);  
  float pad1 = Display.scale(1); 
  float pad2 = Display.scale(2);
  float pad3 = Display.scale(3); 
  float pad5 = Display.scale(5);
  float pad10 = Display.scale(10);
  
  public SearchBar(Interface intf, float x, float y, float w, float h) {
    super(intf, x, y, w, h);
    captureKeys = true;
  }

  public void setup() {
    bColor = getStyleColor("InfoBar", "background-color");
    h1Font = getStyleFont("h1", "font-family", "font-size");
    h1Color = getStyleColor("h1", "color");
    
    float sw = getStyleSize("InfoBar.SearchAdd.SearchBar.SearchResult", "width");
    float tw = sw/3;
//    float sh = getStyleSize("InfoBar.SearchAdd", "height");
    
    searchTabs = new ArrayList<SearchTab>();
    searchTabs.add(new SearchTab(tabsX, tabsY, tw - pad1, tabsH, "Covariate"));     
    searchTabs.add(new SearchTab(tabsX + tw + pad1, tabsY, tw - pad2, tabsH, "Row")); 
    searchTabs.add(new SearchTab(tabsX + 2*tw + pad1, tabsY, tw - pad1, tabsH, "Column"));
    searchTabs.get(0).selected = true;
    
    searchBox = new SearchBox(tabsX, tabsY + tabsH, sw, tabsH);    
    searchRes = new SearchResults(tabsX, tabsY + tabsH + tabsH + pad2, 10);
  }
  
  public void update() {
    searchBox.update();
    searchRes.update();
  }
  
  public void draw() {
    noStroke();
    fill(bColor);
    rect(0, 0, width - padding, height);
    
//    fill(h1Color);
//    textFont(h1Font);
//    text("Search & Add", 10, 50); 
    
    for (SearchTab tab: searchTabs) tab.draw();
    searchBox.draw();
    searchRes.draw();
  }  
  
  public boolean inside(float x, float y) {
    return super.inside(x, y) || searchRes.inside(x - left, y - top);
  }
  
  public void lostFocus() {
    searchBox.clear();
  }
  
  public void mousePressed() {    
    for (SearchTab tab: searchTabs) {
      if (tab.mousePressed(mouseX, mouseY)) return;
    }    
    searchBox.mousePressed(mouseX, mouseY);    
    searchRes.mousePressed(mouseX, mouseY);
  }
  
  public void mouseMoved() {
    searchRes.hover(mouseX, mouseY);
  }
  
  public void keyPressed() {        
    if (key == ESC) {
      searchBox.clear();
      if (intf.isSelectedWidget(this)) intf.selectWidget(null);
    } else {
      intf.selectWidget(this);
      if (key == CODED && keyCode == DOWN) {
        searchRes.selectNext();
      } else if (key == CODED && keyCode == UP) {
        searchRes.selectPrev();
      } else if (key == TAB) {
        selectNextTab();
      } else if (key == ENTER || key == RETURN) {
        searchRes.enterPressed();
        if (intf.isSelectedWidget(this)) intf.selectWidget(null);
      } else {
        searchBox.keyPressed(key, keyCode);
      }
    }
  }
  
  protected void selectNextTab() {
    int n = searchTabs.size();
    for (int i = 0; i < n; i++) {
      SearchTab tab0 = searchTabs.get(i); 
      if (tab0.selected) {
        SearchTab tab1 = i < n - 1 ? searchTabs.get(i + 1) : searchTabs.get(0);
        tab0.selected = false;
        tab1.selected = true;
        break;
      }
    }
  }
  
  protected void selectVar(Variable var) {
    if ((searchTabs.get(0).selected)) {        // covariate
      mira.browser.openCovariate(var);
    } else if ((searchTabs.get(1).selected)) { // row 
      mira.browser.openRow(var);
    } else if ((searchTabs.get(2).selected)) { // column
      mira.browser.openColumn(var);
    }
  }
  
  class SearchTab {
    boolean selected;
    float x, y, w, h;
    String label;
    int bColor;
    int sColor;
    PFont pFont;
    int pColor;
    int pAlign;
    
    SearchTab(float x, float y, float w, float h, String label) {
      this.x = x;      
      this.y = y;
      this.w = w;
      this.h = h;
      this.label = label;
      
      pFont = getStyleFont("InfoBar.SearchAdd.SearchTab.p", "font-family", "font-size");
      pColor = getStyleColor("InfoBar.SearchAdd.SearchTab.p", "color"); 
      pAlign = getStyleAlign("InfoBar.SearchAdd.SearchTab.p", "text-align");
      
      bColor = getStyleColor("InfoBar.SearchAdd.SearchTab", "background-color");
      sColor = getStyleColor("InfoBar.SearchAdd.SearchTab.Selected", "background-color"); 
      
      selected = false;
    } 
    
    void draw() {
      if (selected) {
        fill(sColor);
      } else {
        fill(bColor);
      }
      rect(x, y, w, h);
            
      float yc = (h - pFont.getSize()) / 2;
      
      fill(pColor);
      textFont(pFont);
      textAlign(pAlign);
      text(label, x, y + yc - pad3, w, h);      
      textAlign(PConstants.LEFT);
    }
    
    boolean mousePressed(float mx, float my) {
      if (x <= mx && mx <= x + w && y <= my && my <= y + h) {
        for (SearchTab tab: searchTabs) tab.selected = false;
        selected = true;
        return true;
      }
      return false;
    }
  }
  
  class SearchBox {
    float x, y, w, h;
    PFont pFont;
    int bColor, pColor, sColor;
    EditableText searchStr;
        
    String lastQuery = "";
    int timeLastQuery;    
    boolean skipQuery = false;
    
    SearchBox(float x, float y, float w, float h) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
      
      pFont = getStyleFont("InfoBar.SearchAdd.SearchBar.p", "font-family", "font-size");
      pColor = getStyleColor("InfoBar.SearchAdd.SearchBar.p", "color");
      sColor = getStyleColor("InfoBar.SearchAdd.SearchBar.SearchResult.p.seachedText", "color");
      
      bColor = getStyleColor("InfoBar.SearchAdd.SearchBar", "background-color");
      
      searchStr = new EditableText(SearchBar.this, "Search for a variable");
      searchStr.clearInitial();
      searchStr.setBound(w - pad5 - pFont.getSize());
    }
    
    void update() {
      int time = mira.millis();
      if (skipQuery && 250 < time - timeLastQuery) {
        if (searchRes.search(lastQuery)) {
          timeLastQuery = time;
          skipQuery = false;
        } else {
          skipQuery = true;  
        }
      }      
    }
    
    void draw() {
      noStroke();
      fill(bColor);
      rect(x, y, w, h);

      // shadows
      beginShape(QUAD);
      fill(color(0), 20);
      vertex(x, y);
      vertex(x + w, y);
      fill(color(0), 0);
      vertex(x + w, y - pad10);
      vertex(x, y - pad10);      
      endShape();
      
      textFont(pFont);
      if (searchStr.isFocused()) {
        fill(sColor);
        stroke(sColor);        
      } else {
        fill(pColor);
        stroke(pColor);
      }      
      float yc = (h - pFont.getSize()) / 2;
      text(searchStr, x + pad10, y + h - yc);
    }
    
    void select() {
      searchStr.setFocused(true);
    }
    
    void clear() {
      searchStr.setFocused(false);
      searchStr.set("Search for a variable");
      searchRes.clear();
    }
    
    void mousePressed(float mx, float my) {
      textFont(pFont);
      if (searchStr.insideBounds(x + pad5, y + h - pad5, mx, my)) {
        searchStr.setFocused(true);
      } else {
        searchStr.setFocused(false);
        for (SearchTab tab: searchTabs) { 
          if (tab.selected) return;      
        }
        searchStr.set("Search for a variable");
      }
    }
    
    void keyPressed(char key, int code) {
      int time = mira.millis();
      searchStr.setFocused(true);
      searchStr.keyPressed(key, code);
      String query = searchStr.get();
      skipQuery = false;
      if (!lastQuery.equals(query)) {
        if (0 < query.length()) {
          if (250 < time - timeLastQuery) {
            if (searchRes.search(query)) {
              timeLastQuery = time;  
            } else {
              skipQuery = true;  
            }
          } else {
            skipQuery = true;
          }
        } else {          
          searchRes.clear();
        }        
      }
      lastQuery = query;
    }
  }
  
  class SearchResults {
    float x0, y0; 
    float rw, rh;
    float sep;
    int max;
    SearchTask searchTask;

    HashMap<Variable, SearchResult> results;
    ArrayList<SearchResult> ordered;
    
    SearchResults(float x, float y, int max) {
      this.x0 = x;
      this.y0 = y;
      this.max = max;
      results = new HashMap<Variable, SearchResult>();
      ordered = new ArrayList<SearchResult>();
      
      rw = getStyleSize("InfoBar.SearchAdd.SearchBar.SearchResult", "width");
      rh = getStyleSize("InfoBar.SearchAdd.SearchBar.SearchResult", "height");     
      sep = getStyleSize("InfoBar.SearchAdd.SearchBar.SearchResult", "margin-bottom");
      
      searchTask = null;
    }
    
    void update() {
      if (searching()) return;  
      for (SearchResult res: results.values()) res.update();
    }
    
    void draw() {
      if (searching()) return;      
      for (SearchResult res: results.values()) res.draw();
    }    
    
    boolean search(String query) {
      if (searchTask != null && searchTask.isAlive()) {
        return false;
//        searchTask.interrupt();
//        while (searchTask.isAlive()) {
//          Thread.yield();
//        }
      }
      
      searchTask = new SearchTask(query);
      searchTask.start();
      return true;
    }
    
    boolean searching() {
      return searchTask != null && searchTask.isAlive();
    }
    
    void clear() {
      if (searching()) return;
      results.clear();
      ordered.clear();
    }
    
    boolean inside(float x, float y) {
      if (searching()) return false;
      for (SearchResult res: results.values()) {
        if (res.inside(x, y)) return true;
      }
      return false;
    }    
    
    void mousePressed(float mx, float my) {
      if (searching()) return;
      SearchResult sel = null;
      for (SearchResult res: results.values()) { 
        if (res.select(mx, my)) {
          sel = res;
          break;
        }
      }
      if (sel != null) {
        selectVar(sel.var);
        clear();
        searchBox.clear();
        if (intf.isSelectedWidget(SearchBar.this)) intf.selectWidget(null);
      }      
    }
    
    void enterPressed() {
      if (searching()) return;
      SearchResult sel = null;
      for (SearchResult res: results.values()) { 
        if (res.selected) {
          sel = res;
          break;
        }
      }
      if (sel != null) {
        selectVar(sel.var);
        clear();
        searchBox.clear();
      }       
    }
    
    void selectPrev() {
      if (searching()) return;
      boolean noneSel = true;
      int n = ordered.size();
      for (int i = 0; i < n; i++) {
        SearchResult res0 = ordered.get(i);
        if (res0.selected) {          
          SearchResult res1 = 0 < i ? ordered.get(i - 1) : ordered.get(n - 1);
          res0.selected = false;
          res1.selected = true;
          noneSel = false;
          break;
        }
      }
      if (noneSel && 0 < n) {
        SearchResult res0 = ordered.get(0);
        res0.selected = true;
      }
    }
    
    void selectNext() {
      if (searching()) return;
      boolean noneSel = true;
      int n = ordered.size();
      for (int i = 0; i < n; i++) {
        SearchResult res0 = ordered.get(i);
        if (res0.selected) {          
          SearchResult res1 = i < n - 1 ? ordered.get(i + 1) : ordered.get(0);
          res0.selected = false;
          res1.selected = true;
          noneSel = false;
          break;
        }
      }
      if (noneSel && 0 < n) {
        SearchResult res0 = ordered.get(0);
        res0.selected = true;
      }
    }  
    
    void hover(float mx, float my) {
      if (searching()) return;
      for (SearchResult res: results.values()) { 
        res.select(mx, my);
      }      
    }
    
    protected class SearchTask extends Thread {
      String query;
      
      SearchTask(String query) {
        this.query = query;
      }
      
      @Override
      public void run() {
        HashSet<Variable> old = new HashSet<Variable>(results.keySet());     
        
        ordered.clear();
        Variable[] matches = data.getVariables(query);
        for (int i = 0; i < PApplet.min(max, matches.length); i++) {
          float ry = i * (rh + sep);
          Variable var = matches[i];
          SearchResult res;
          if (results.keySet().contains(var)) {
            old.remove(var);
            res = results.get(var);
            res.setQuery(query);
            res.targetY(y0 +ry);
          } else {
            res = new SearchResult(x0, y0, y0 + ry, rw, rh, var, query);
            results.put(var, res);
          }
          ordered.add(res);
          res.selected = false;
        }
        if (0 < ordered.size()) ordered.get(0).selected = true;
        for (Variable var: old) {
          results.remove(var);
        }
      }
    } 
  }
  
  class SearchResult {
    Variable var;
    String query;
    SoftFloat y, w;
    float x, h, w0;
    PFont pFont, sFont;
    int pColor, sColor; 
    int bColor, hColor;
    boolean selected;
    
    SearchResult(float x, float y0, float y1, float w, float h, Variable var, String query) {
      this.var = var;
      this.query = query;      

      this.x = x;
      this.y = new SoftFloat(y0);
      this.y.setTarget(y1);
      this.w = new SoftFloat(w);
      this.w0 = w;
      this.h = h; 
      
      selected = false;
      
      pFont = getStyleFont("InfoBar.SearchAdd.SearchBar.SearchResult.p", "font-family", "font-size");
      pColor = getStyleColor("InfoBar.SearchAdd.SearchBar.SearchResult.p", "color");         
      sColor = getStyleColor("InfoBar.SearchAdd.SearchBar.SearchResult.p.seachedText", "color");
      
      bColor = getStyleColor("InfoBar.SearchAdd.SearchBar.SearchResult.p", "background-color");
      hColor = getStyleColor("InfoBar.SearchAdd.SearchBar.SearchResult.hover", "background-color");
    }
    
    void setQuery(String qry) {
      query = qry;
    }
    
    void targetY(float y1) {
      y.setTarget(y1);
    }
    
    void update() {
      y.update();
      w.update();
    }
    
    void draw() {
      noStroke();
      
      String label = var.getName() + ": " + var.getAlias();
      
      textFont(pFont);
      if (selected) {
        fill(hColor);
        float w1 = textWidth(label) + 20;
        if (w0 < w1) w.setTarget(w1);
      } else {
        fill(bColor);
        w.setTarget(w0);        
      }
      if (w.isTargeting()) {
        label = chopStringRight(label, w.get() - pad10);
      }
      
      rect(x, y.get(), w.get(), h);
      
      // Draw the label, splitting into separate chunks so the query substring
      // is highlighted with the search color.
      float center = (h - pFont.getSize()) / 2;      
      float x0 = x + 10;
      String lquery = query.toLowerCase();
      String llabel = label.toLowerCase();
      int qlen = query.length();
      int pos0 = 0;
      int pos1 = llabel.indexOf(lquery, pos0);
      while (pos0 < label.length()) { 
        String piece = "";
        if (pos0 == pos1) {
          // Found query string
          fill(sColor);
          pos1 += qlen;
          piece = label.substring(pos0, pos1);
          pos0 = pos1;
        } else {
          fill(pColor);
          if (-1 < pos1) {
            piece = label.substring(pos0, pos1);
            pos0 = pos1;
          } else {
            pos1 = label.length();
            piece = label.substring(pos0, pos1);
            pos0 = label.length();
          }
        }        
        text(piece, x0, y.get() + h - center);
        x0 += textWidth(piece);        
        pos1 = llabel.indexOf(lquery, pos0);
      }
    }
    
    boolean inside(float x, float y) {
      return this.x <= x && x <= this.x + this.w.get() &&
             this.y.get() <= y && y <= this.y.get() + this.h;
    }    
    
    boolean select(float mx, float my) {
      selected = inside(mx, my);
      return selected;
    }
  }
}

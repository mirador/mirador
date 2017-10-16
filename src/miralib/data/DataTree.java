/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.data;

import java.util.ArrayList;
import java.util.HashMap;

import miralib.utils.Log;
import processing.data.Table;

/**
 * A hierarchical data tree defined by groups, tables, and variables. 
 *
 */

public class DataTree {
  final static public int NONE = 0;
  final static public int SOME = 1;
  final static public int ALL  = 2;
  
  final public static int GROUP_ITEM    = 0;
  final public static int TABLE_ITEM    = 1;
  final public static int VARIABLE_ITEM = 2;
  
  public ArrayList<Item> groups;
  public ArrayList<Item> tables;
  public ArrayList<Item> variables;  
  
  public HashMap<String, VariableContainer> grpmap;
  public HashMap<String, VariableContainer> tabmap;
  public HashMap<String, Variable> varmap;
  
  public DataTree() {
    groups = new ArrayList<Item>();
    tables = new ArrayList<Item>();
    variables = new ArrayList<Item>();
    
    grpmap = new HashMap<String, VariableContainer>();
    tabmap = new HashMap<String, VariableContainer>();
    varmap = new HashMap<String, Variable>();
  }
  
  public DataTree(ArrayList<Variable> allvars) {
    variables = new ArrayList<Item>();
    for (Variable var: allvars) {
      if (var.type() == Table.STRING) {
        Log.message("Variable "+ var.getName() + " is a string variable, " + 
                    "which is not supported for analysis");
        continue;              
      }    
      variables.add(var);
    }

    tables = new ArrayList<Item>();
    tables.add(new VariableContainer("All variables", 0, variables.size() - 1, TABLE_ITEM)); 
    
    groups = new ArrayList<Item>();
    groups.add(new VariableContainer("All tables", 0, 0, GROUP_ITEM));
    
    initMaps();
  }
  
  public int getGroupCount() {
    return groups.size();
  }

  public int getTableCount() {
    return tables.size();
  }  

  public int getVariableCount() {
    return variables.size();
  }    
  
  public void addGroup(String name, int table0, int table1) {
    VariableContainer grp = new VariableContainer(name, table0, table1, GROUP_ITEM);
    groups.add(grp);
    grpmap.put(name, grp);
  }
  
  public void addTable(String name, int var0, int var1) {
    VariableContainer tab = new VariableContainer(name, var0, var1, TABLE_ITEM);
    tables.add(tab);
    tabmap.put(name, tab);
  }
  
  public void addVariable(Variable var) {
    variables.add(var);
    varmap.put(var.name, var);
  }
    
  public VariableContainer getGroup(int i) {
    return (VariableContainer)groups.get(i);
  }

  public VariableContainer getGroup(String name) {
    return (VariableContainer)grpmap.get(name);
  }
   
  public VariableContainer getTable(int i) {
    return (VariableContainer)tables.get(i);
  }
  
  public VariableContainer getTable(String name) {
    return (VariableContainer)tabmap.get(name);
  }
  
  public Variable getVariable(int i) {
    return (Variable)variables.get(i);
  }
  
  public Variable getVariable(String name) {
    return (Variable)varmap.get(name);
  }  
  
  public ArrayList<Variable> getGroupVariables(VariableContainer group) {
    ArrayList<Variable> sel = new ArrayList<Variable>();
    for (int t = group.getFirstChild(); t <= group.getLastChild(); t++) {      
      Item table = tables.get(t);
      for (int v = table.getFirstChild(); v <= table.getLastChild(); v++) {
        Variable var = (Variable)variables.get(v);
        sel.add(var);
      }
    }
    return sel;
  }
  
  public ArrayList<Variable> getTableVariables(VariableContainer table) {
    ArrayList<Variable> sel = new ArrayList<Variable>();
    for (int v = table.getFirstChild(); v <= table.getLastChild(); v++) {
      Variable var = (Variable)variables.get(v);
      sel.add(var);
    }    
    return sel;
  }    
  
  public void updateColumns() {
    for (Item table: tables) updateTableColumns(table);
    for (Item group: groups) updateGroupColumns(group);
  }
  
  protected void updateGroupColumns(Item group) {
    boolean all = true;
    boolean some = false;    
    for (int i = group.getFirstChild(); i <= group.getLastChild(); i++) {
      int state = tables.get(i).getColumnSelection();
      if (state != ALL) all = false;
      if (state != NONE) some = true;
    }
    if (all) group.setColumnSelection(ALL);
    else if (some) group.setColumnSelection(SOME);
    else group.setColumnSelection(NONE);   
  }
  
  protected void updateTableColumns(Item table) {
    boolean all = true;
    boolean some = false;
    for (int i = table.getFirstChild(); i <= table.getLastChild(); i++) {
      Variable var = (Variable)variables.get(i);
      if (!var.column()) all = false;
      if (var.column()) some = true;
    }    
    if (all) table.setColumnSelection(ALL);
    else if (some) table.setColumnSelection(SOME);
    else table.setColumnSelection(NONE); 
  }
  
  protected void initMaps() {
    grpmap = new HashMap<String, VariableContainer>();
    for (Item grp: groups) {
      grpmap.put(grp.getName(), (VariableContainer)grp);
    }
        
    tabmap = new HashMap<String, VariableContainer>();
    for (Item tab: tables) {
      tabmap.put(tab.getName(), (VariableContainer)tab);
    }
    
    varmap = new HashMap<String, Variable>();
    for (Item var: variables) {
      varmap.put(var.getName(), (Variable)var);
    }
  }
  
  public interface Item {
    public int getItemType();
    public String getName();
    public int getFirstChild();
    public int getLastChild();
    public boolean canOpen();
    public boolean open();
    public void setOpen();
    public void setClose();
    public int getColumnSelection();
    public void setColumnSelection(int sel);
  }
}

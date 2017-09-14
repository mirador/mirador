/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.data;

/**
 * Element to store an item in the data tree (either group, table or variable).
 *
 */

public class VariableContainer implements DataTree.Item {
  protected int type;
  protected String name;
  protected int child0; 
  protected int child1;
  protected int colsel;
  
  public VariableContainer(String name, int idx0, int idx1, int type) {
    this.name = name;
    this.child0 = idx0;
    this.child1 = idx1;
    this.type = type;
    colsel = DataTree.ALL;
  }
  
  public int getItemType() {
    return type;
  }
  
  public String getName() {
    return name;
  }
  
  public int getFirstChild() { 
    return child0;    
  }
  
  public int getLastChild() {
    return child1;
  }
  
  public boolean canOpen() {
    return false;
  }
  
  public boolean open() {
    return false;
  }
  
  public void setOpen() { }
  
  public void setClose() { }
  
  public int getColumnSelection() {
    return colsel;
  }
  
  public void setColumnSelection(int sel) { 
    colsel = sel;
  }
}

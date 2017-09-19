package mirador.app;

import miralib.data.DataTree;
import miralib.data.Variable;
import miralib.data.VariableContainer;
import mui.Interface;
import mui.Widget;
import mui.Scroller;
import mui.SoftFloat;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.ArrayList;

public class SingleScroller2 extends Scroller<RowWidget> {

  protected ArrayList<DataTree.Item> items;
  protected float heightOpen;
  protected float heightClose;


  public SingleScroller2(Interface intf, float x, float y, float w, float h) {
    super(intf, x, y, w, h);
    orientation = VERTICAL;
  }


  public void setItems(ArrayList<DataTree.Item> items, float hopen, float hclose) {
    this.items = items;
    heightOpen = hopen;
    heightClose = hclose;
    initItems();
  }


  public void draw() {
    fill(color(120));
    rect(0, 0, width, height);

    fill(color(255));
    rect(getDragBoxLeft(), getDragBoxTop(), getDragBoxWidth(), getDragBoxHeight());
  }


  public boolean plotsReady() {
    boolean ready = true;
    for (Widget child: children) {
      if (child instanceof RowVariable) {
        if (!((RowVariable)child).plotsReady()) {
          ready = false;
          break;
        }
      }
    }
    return ready;
  }


  public void dragColumns(float dx) {
    for (Widget child: children) {
      if (child instanceof RowVariable) {
        ((RowVariable)child).drag(dx);
      }
    }
  }


  public void snapColumns() {
    for (Widget child: children) {
      if (child instanceof RowVariable) {
        ((RowVariable)child).snap();
      }
    }
  }


  public void dataChanged() {
    for (Widget child: children) {
      if (child instanceof RowVariable) {
        ((RowVariable)child).dataChanged();
      }
    }
  }


  public void pvalueChanged() {
    for (Widget child: children) {
      if (child instanceof RowVariable) {
        ((RowVariable)child).pvalueChanged();
      }
    }
  }


  public void enter() {
    for (Widget child: children) {
      if (child instanceof RowVariable) {
        ((RowVariable)child).enterPressed();
      }
    }
  }


  public String getRowLabel(Variable varx, Variable vary) {
    for (Widget child: children) {
      if (child instanceof RowVariable) {
        RowVariable rvar = (RowVariable)child;
        if (rvar.getVariable() == vary) return rvar.getRowLabel(varx);
      }
    }
    return "";
  }


  public String getColLabel(Variable varx, Variable vary) {
    for (Widget child: children) {
      if (child instanceof RowVariable) {
        RowVariable rvar = (RowVariable)child;
        if (rvar.getVariable() == vary) return rvar.getColLabel(varx);
      }
    }
    return "";
  }


  public void closeColumn(Variable var) {
    for (Widget child: children) {
      if (child instanceof RowVariable) {
        ((RowVariable)child).close(var);
      }
    }
  }

  public void closeAllBut(MiraWidget wt) {
    /*
    Variable var = null;
    if (wt instanceof RowVariable) {
      var = ((RowVariable)wt).rowVar;
    }

    for (int i = 0; i < children.size(); i++) {
      MiraWidget wti = (MiraWidget)children.get(i);
      if (wti == wt) continue;
      if (canOpen(wti.idx)) {
        if (isOpen(wti.idx)) {
          close(wti.idx);
          wti.targetHeight(heightClose);
        }
        updatePositions(wti);
      }
    }
    jumpTo(wt.idx);

    for (int i = 0; i < items.size(); i++) {
      DataTree.Item itm = items.get(i);
      if (itm != var) itm.setClose();
    }
    */
  }


  public void setActive(boolean active) {
    setActive(active, true);
  }


  public void setActive(boolean active, boolean changeAlpha) {
    /*
    this.active = active;
    if (!active) {
      savedIdx = -1;
      for (Widget child: children) {
        if (changeAlpha) child.hide();
        if (savedIdx == -1 && !child.isMarkedForDeletion()) {
          MiraWidget wt = (MiraWidget)child;
          savedIdx = wt.idx;
        }
      }
    } else {
      needShow = changeAlpha;
    }
    */
  }


  protected void initItems() {
    int n = items.size();

    visItems = new TreeMap<Integer, Item>();
    visPos0 = new SoftFloat(0);
    visPos1 = new SoftFloat(length());

    initItemWidth = width;
    initItemHeight = heightOpen;

    dragBox0 = new SoftFloat();
    dragBox1 = new SoftFloat();

    lengths = new float[n];
    lengthSum = new float[n];
    closed = new boolean[n];

//    float len = ih;
//    Arrays.fill(lengths, len);
    float sum0 = 0;
    for (int i = 0; i < lengthSum.length; i++) {
      float h = items.get(i).open() ? heightOpen : heightClose;
      lengths[i] = h;
      lengthSum[i] = sum0 + h;
      sum0 = lengthSum[i];
    }

    Arrays.fill(closed, false);
    open0 = 0;
    open1 = n - 1;
  }


  @Override
  protected Scroller<RowWidget>.Item createItem(int index) {
    DataTree.Item item = items.get(index);
    int type = item.getItemType();
    float h = item.open() ? heightOpen : heightClose;
    RowWidget row = null;
    if (type == DataTree.GROUP_ITEM) {
      row = new RowGroup(intf, 0, 0, width, h, (VariableContainer)item);
    } else if (type == DataTree.TABLE_ITEM) {
      row = new RowTable(intf, 0, 0, width, h, (VariableContainer)item);
    } else if (type == DataTree.VARIABLE_ITEM) {
      row = new RowVariable(intf, 0, 0, width, h, (Variable)item);
    }
    addChild(row, Widget.TOP_LEFT_CORNER);
    return new Item(index, row);
  }

}

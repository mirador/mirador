package mirador.app;

import miralib.data.DataTree;
import miralib.data.Variable;
import miralib.data.VariableContainer;
import mui.Interface;
import mui.Widget;
import mui.SoftFloat;

import java.util.TreeMap;
import java.util.ArrayList;

public class SingleScroller2 extends Scroller<RowWidget> {

  protected TreeMap<Integer, Item> pvisItems;
  protected ArrayList<DataTree.Item> items;

  protected boolean active;
  protected int savedIdx;
  protected boolean needShow;
  protected float heightOpen;
  protected float heightClose;
  protected RowScroller root;


  public SingleScroller2(Interface intf, RowScroller root, float x, float y, float w, float h) {
    super(intf, x, y, w, h);
    this.root = root;
    active = false;
    savedIdx = -1;
    needShow = false;
    orientation = VERTICAL;
  }


  public void setItems(ArrayList<DataTree.Item> items, float hopen, float hclose) {
    this.items = items;
    heightOpen = hopen;
    heightClose = hclose;
    initItems();
  }


  public void draw() {
//    fill(color(120));
//    rect(0, 0, width, height);

    if (active) {
      fill(color(150));
      rect(getDragBoxLeft(), getDragBoxTop(), getDragBoxWidth(), getDragBoxHeight());
    }
  }


  public void update() {
    super.update();
    if (active) {
      if (needShow) {
        for (Widget child: children) child.show(true);
        needShow = false;
      }
    }
  }


  protected void handleResize(int newWidth, int newHeight) {
    float h = newHeight - mira.labelHeightClose;
    bounds.h.set(h);
    visPos1.setTarget(visPos0.getTarget() + h);
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


//  public void dragColumns(float dx) {
//    for (Widget child: children) {
//      if (child instanceof RowVariable) {
//        ((RowVariable)child).drag(dx);
//      }
//    }
//  }
//
//
//  public void snapColumns() {
//    for (Widget child: children) {
//      if (child instanceof RowVariable) {
//        ((RowVariable)child).snap();
//      }
//    }
//  }


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


  public void up() {
    jumpToPrev();
  }

  public void down() {
    jumpToNext();
  }

  public void prev() {
    root.prev();
  }

  public void next() {
    root.next();
  }

  public void next(int i) {
    root.next(i);
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
//        initItems();
      }
    }
    jumpTo(wt.idx);

    for (int i = 0; i < items.size(); i++) {
      DataTree.Item itm = items.get(i);
      if (itm != var) itm.setClose();
    }
  }


  public boolean canOpen(int i) {
    if (0 <= i && i < items.size()) {
      return items.get(i).canOpen();
    } else {
      return false;
    }
  }


  public boolean isOpen(int i) {
    if (0 <= i && i < items.size()) {
      return items.get(i).open();
    } else {
      return false;
    }
  }


  public void setActive(boolean active) {
    setActive(active, true);
  }


  public void setActive(boolean active, boolean changeAlpha) {
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
  }


  @Override
  public void mouseReleased(MiraWidget  wt) {
    if (active) {
      if (canOpen(wt.idx)) {
        if (isOpen(wt.idx)) {
          close(wt.idx);
          if (0 <= wt.idx && wt.idx < items.size()) {
            items.get(wt.idx).setClose();
          }
          wt.targetHeight(heightClose);
        } else {
          open(wt.idx);
          if (0 <= wt.idx && wt.idx < items.size()) {
            items.get(wt.idx).setOpen();
          }
          wt.targetHeight(heightOpen);
        }
//        updatePositions(wt);
//        initItems();
      } else {
        next(wt.idx);
      }
    }
  }

  public void keyPressed(MiraWidget  wt) {
    if (active) {
      if (key == CODED) {
        if (keyCode == LEFT) {
          prev();
        } else if (keyCode == RIGHT) {
          next();
        } else if (keyCode == UP) {
          up();
        } else if (keyCode == DOWN) {
          down();
        }
      }
    }
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
      closed[i] = !items.get(i).open();
      float h = items.get(i).open() ? heightOpen : heightClose;
      lengths[i] = h;
      lengthSum[i] = sum0 + h;
      sum0 = lengthSum[i];
    }

//    Arrays.fill(closed, false);
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
    if (!active) {
      row.hide(false);
    }
    return new Item(index, row);
  }

}

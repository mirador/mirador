/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.app;

import miralib.data.DataTree;
import miralib.data.Variable;
import miralib.data.VariableContainer;
import mui.Display;
import mui.Interface;
import mui.Widget;
import processing.core.PApplet;

import java.util.ArrayList;

public class RowScroller extends MiraWidget {
  protected float snapThreshold = Display.scale(30);

  protected RowBrowser row;
  protected ArrayList<DataTree.Item> items;
  protected boolean active;
  protected boolean dragx;
  protected int pmouseX0, dragx1;
  protected int savedIdx;
  protected boolean needShow;
  protected float heightOpen;
  protected float heightClose;

  public RowScroller(Interface intf, RowBrowser row, float x, float y, float w, float h,
                     float openh, float closeh) {
    super(intf, x, y, w, h);
    this.row = row;
    active = false;
    savedIdx = -1;
    heightOpen = openh;
    heightClose = closeh;
  }

  protected void handleResize(int newWidth, int newHeight) {
    bounds.h.set(newHeight - mira.labelHeightClose);
  }

  public void setItems(ArrayList<DataTree.Item> items) {
    this.items = items;
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
        if (savedIdx == -1 && child.visible() && !child.isMarkedForDeletion()) {
          MiraWidget wt = (MiraWidget)child;
          savedIdx = wt.idx;
        }
      }
    } else {
      needShow = changeAlpha;
    }
  }

  public int getTotalCount() {
    return items.size();
  }


  public int getFirstIndex() {
    for (Widget child: children) {
      if (child.visible()) return ((MiraWidget)child).idx;
    }
    return 0;
  }

  public boolean isActive() {
    return active;
  }

  public void up() {
    fit(-heightClose);
  }

  public void down() {
    fit(+heightClose);
  }

  public void enter() {
    for (Widget child: children) {
      if (child instanceof RowVariable) {
        ((RowVariable)child).enterPressed();
      }
    }
  }

  public void prev() {
    row.prev();
  }

  public void next() {
    row.next();
  }

  public void next(int i) {
    row.next(i);
  }

  public void dragRows(float dy) {
    if (active) {
      fit(dy);
      row.updateVertScrollbar();
    }
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

  public void closeColumn(Variable var) {
    for (Widget child: children) {
      if (child instanceof RowVariable) {
        ((RowVariable)child).close(var);
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

  public void update() {
    cleanScroll();
    if (active) {
      if (children.size() == 0) {
        initScroll();
      } else if (0 < children.size() && getMarkedForDeletionCount() == 0) {
        updateScroll();
      }
      if (-1 < savedIdx) {
        jumpTo(savedIdx, false);
        row.updateVertScrollbar(savedIdx);
        savedIdx = -1;
      }
      if (needShow) {
        for (Widget child: children) child.show(true);
        needShow = false;
      }
    }
    fit();
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
        updatePositions(wti);
      }
    }
    jumpTo(wt.idx);

    for (int i = 0; i < items.size(); i++) {
      DataTree.Item itm = items.get(i);
      if (itm != var) itm.setClose();
    }
  }

  public void mousePressed() {
    if (active) {
      dragx = false;
      pmouseX0 = mouseX;
    }
  }

  public void mouseDragged() {
    if (active) {
      int dx = pmouseX - mouseX;
      int dy = pmouseY - mouseY;
      if (dy != 0) {
        fit(dy);
        row.updateVertScrollbar();
        dragx = false;
      } else if (dx != 0) {
        dragx = true;
      }
    }
  }

  public void mouseReleased() {
    if (active) {
      if (dragx) {
        int dx = pmouseX0 - mouseX;
        if (20 < dx) {
          row.next(false);
        } else if (dx < 20) {
          row.prev(false);
        }
      } else {
        snap();
      }
      row.updateVertScrollbar();
    }
  }

  public void mouseReleased(MiraWidget  wt) {
    if (active) {
      if (canOpen(wt.idx)) {
        if (isOpen(wt.idx)) {
          close(wt.idx);
          wt.targetHeight(heightClose);
        } else {
          open(wt.idx);
          wt.targetHeight(heightOpen);
        }
        updatePositions(wt);
      } else {
        next(wt.idx);
        row.updateVertScrollbar();
      }
    }
  }

  public void keyPressed(MiraWidget  wt) {
    if (active) {
      if (key == CODED) {
        boolean move = false;
        if (keyCode == LEFT) {
          prev();
          move = true;
        } else if (keyCode == RIGHT) {
          next();
          move = true;
        } else if (keyCode == UP) {
          up();
          move = true;
        } else if (keyCode == DOWN) {
          down();
          move = true;
        }
        if (move) row.updateVertScrollbar();
      }
    }
  }

  public void jumpTo(int i) {
    jumpTo(i, true);
  }

  public void jumpTo(int i, boolean target) {
    if (0 <= i && i < items.size()) {
      if (children.size() == 0) initScroll();
      MiraWidget first = (MiraWidget)children.get(0);
      float h0 = first.targetY();
      float hdif = getHeight(first.idx, i);
      fit(h0 + hdif, target);
    }
  }

  public String getName(int i) {
    if (0 <= i && i < items.size()) {
      return items.get(i).getName();
    } else {
      return "";
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

  public void open(int i) {
    if (0 <= i && i < items.size()) {
      items.get(i).setOpen();
    }
  }

  public void close(int i) {
    if (0 <= i && i < items.size()) {
      items.get(i).setClose();
    }
  }

  public float getHeight() {
    return getHeight(0, items.size());
  }

  public float getHeight(int i0, int i1) {
    boolean inverted = false;
    if (i1 < i0) {
      int tmp = i0;
      i0 = i1;
      i1 = tmp;
      inverted = true;
    }
    float h = 0;
    for (int i = i0; i < i1; i++) {
      DataTree.Item itm = items.get(i);
      h += itm.open() ? heightOpen : heightClose;
    }

    return inverted ? -h : h;
  }

  public int getWidgetCountTop(MiraWidget first) {
    int n = 0;
    float h = 0;
    float maxh = first.top() - top();
    while (h <= maxh) {
      n++;
      int i = first.idx - n;
      if (i < 0) break;
      h += items.get(i).open() ? heightOpen : heightClose;
    }
    return n;
  }

  public int getWidgetCountBottom(MiraWidget last) {
    int n = 0;
    float h = 0;
    float maxh = bottom() - last.bottom();
    while (h <= maxh) {
      n++;
      int i = last.idx + n;
      if (i == items.size()) break;
      h += items.get(i).open() ? heightOpen : heightClose;
    }
    return n;
  }

  public int getWidgetCountInit() {
    int n = 0;
    float h = 0;
    while (h <= height) {
      n++;
      if (n == items.size()) break;
      h += items.get(n).open() ? heightOpen : heightClose;
    }
    return n;
  }

  public void updatePositions(MiraWidget prev) {
    int idx = children.indexOf(prev);
    for (int i = idx + 1; i < children.size(); i++) {
      MiraWidget wt = (MiraWidget)children.get(i);
      float y = prev.targetY() + prev.targetHeight();
      wt.targetY(y);
      prev = wt;
    }
  }

  public void fit() {
    fit(0);
  }

  public void fit(float dy) {
    fit(dy, true);
  }

  public void fit(float dy, boolean target) {
    if (0 < children.size()) {
      float toffset = 0;
      float boffset = 0;
      MiraWidget first = (MiraWidget)children.get(0);
      MiraWidget last = (MiraWidget)children.get(children.size() - 1);
      float h = PApplet.min(height, getHeight());

      if (first.idx == 0 && 0 < first.targetY() - dy) {
        toffset = first.targetY() - dy;
      }
      if (last.idx == items.size() - 1 && last.targetY() + last.height() - dy < h) {
        boffset = h - last.targetY() - last.height() + dy;
      }

      if (dy != 0 || 0 < toffset || 0 < boffset) {
        for (Widget child: children) {
          float y = child.targetY() - dy;
          y -= toffset;
          y += boffset;
          if (target) {
            child.targetY(y);
          } else {
            child.setY(y);
          }
        }
      }
    }
  }

  public void snap() {
    if (0 < children.size()) {
      float offset = 0;
      MiraWidget first = (MiraWidget)children.get(0);
      MiraWidget last = (MiraWidget)children.get(children.size() - 1);
      float h = PApplet.min(height, getHeight());

      if (PApplet.abs(first.targetY()) < 30) {
        offset = -first.targetY();
      } else if (PApplet.abs(first.targetY() + first.targetHeight()) < snapThreshold) {
        offset = -(first.targetY() + first.targetHeight());
      } else if (PApplet.abs(last.targetY() - h) < 30) {
        offset = h - last.targetY();
      } else if (PApplet.abs(last.targetY() + last.targetHeight() - h) < snapThreshold) {
        offset = h - (last.targetY() + last.targetHeight());
      }

      if (0 != offset) {
        for (Widget child: children) {
          float y = child.targetY();
          y += offset;
          child.targetY(y);
        }
      }
    }
  }

  protected void initScroll() {
    int count = getWidgetCountInit();
    float y = 0;
    for (int i = 0; i < count; i++) {
      float h = items.get(i).open() ? heightOpen : heightClose;
      MiraWidget wt = createScrollWidget(items.get(i), y, h);
      wt.setIndex(i);
      wt.setTimeOut(REMOVE_ROW_DELAY);
      wt.setDraggable(false);
      addChild(wt);
      y += h;
    }
  }

  protected void updateScroll() {
    // Adding widgets at the top
    MiraWidget first = (MiraWidget)children.get(0);
    if (0 < first.idx && top() < first.top()) {
      int n = getWidgetCountTop(first);
      int idx0 = PApplet.max(first.idx - n, 0);
      for (int i = first.idx - 1; i >= idx0; i--) {
        float h = items.get(i).open() ? heightOpen : heightClose;
        MiraWidget wt = createScrollWidget(items.get(i), 0, h);
        wt.setIndex(i);
        wt.setTimeOut(REMOVE_ROW_DELAY);
        wt.copyY(first, -wt.targetHeight());
        wt.setDraggable(false);
        addChild(0, wt);
        first = wt;
      }
    }

    // Adding widgets at the bottom
    MiraWidget last = (MiraWidget)children.get(children.size() - 1);
    if (last.idx < items.size() - 1 && last.bottom() < bottom() - 1) {
      int n = getWidgetCountBottom(last);
      int idx1 = PApplet.min(last.idx + n, items.size() - 1);
      for (int i = last.idx + 1; i <= idx1; i++) {
        float h = items.get(i).open() ? heightOpen : heightClose;
        MiraWidget wt = createScrollWidget(items.get(i), 0, h);
        wt.setIndex(i);
        wt.setTimeOut(REMOVE_ROW_DELAY);
        wt.copyY(last, +last.targetHeight());
        wt.setDraggable(false);
        addChild(wt);
        last = wt;
      }
    }
  }

  protected void cleanScroll() {
    for (Widget child: children) {
      MiraWidget wt = (MiraWidget)child;
      if (wt.timedOut()) {
        removeChild(wt);
//          Log.message("Removing scroll item " + wt.idx);
      }
    }
  }

  protected MiraWidget createScrollWidget(DataTree.Item item, float y, float h) {
    int type = item.getItemType();
    if (type == DataTree.GROUP_ITEM) {
      return new RowGroup(intf, 0, y, width, h, (VariableContainer)item);
    } else if (type == DataTree.TABLE_ITEM) {
      return new RowTable(intf, 0, y, width, h, (VariableContainer)item);
    } else if (type == DataTree.VARIABLE_ITEM) {
      return new RowVariable(intf, 0, y, width, h, (Variable)item);
    }
    return null;
  }
}

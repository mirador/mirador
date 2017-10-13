package mirador.app;

import miralib.data.DataTree;
import miralib.data.Variable;
import miralib.data.VariableContainer;
import mui.Interface;
import mui.Widget;
import java.util.ArrayList;

public class ColScroller extends Scroller<ColLabel> {
  protected RowScroller rowScroller;
  protected float labelWidth, labelHeight;
  protected float labelHeightMax;

  public ColScroller(Interface intf, RowScroller scroller, float x, float y, float w, float h, float iw,
                     float ih, float ihmax) {
    super(intf, x, y, w, h, HORIZONTAL);
    rowScroller = scroller;
    labelWidth = iw;
    labelHeight = ih;
    labelHeightMax = ihmax;
  }

  public void draw() {
//    fill(color(120));
//    rect(0, 0, width, height);

    fill(color(150));
    rect(getDragBoxLeft(), getDragBoxTop(), getDragBoxWidth(), getDragBoxHeight());
  }

  public void close(Variable var) {
    Item item = visItems.get(var.getIndex());
    if (item != null) {
      item.markedForRemoval = true;
    }
  }

  public boolean contains(Variable var) {
    return visItems.containsKey(var.getIndex());
  }

  public boolean ready() {
    return calledSetup;
  }

  public ArrayList<ColLabel> getColLabels() {
    ArrayList<ColLabel> list = new ArrayList<ColLabel>();
    for (int i = 0; i < getChildrenCount(); i++) {
      ColLabel clab = (ColLabel)getChild(i);
      list.add(clab);
    }
    return list;
  }

  public void mouseDragged() {
    super.mouseDragged();
    mira.browser.dragColumns(pmouseX - mouseX);
  }

  protected void handleResize(int newWidth, int newHeight) {
    float w0 = bounds.w.get();
    float w1 = newWidth - mira.optWidth - mira.varWidth;
    float dw = w1 - w0;
    bounds.w.set(w1);
    visPos1.setTarget(visPos1.getTarget() + dw);
  }


  @Override
  protected int getTotalItemCount() {
    // This is problematic, as columns can be added and removed...
    return data.getVariableCount();
  }

  @Override
  protected Scroller<ColLabel>.Item createItem(int index) {
    Variable cvar = data.getColumn(index);
//    float h = item.open() ? heightOpen : heightClose;
    ColLabel col = new ColLabel(intf, 0, 0, labelWidth, labelHeight, labelHeightMax, cvar,false /*defaultAnimation(event)*/);;
    addChild(col, Widget.TOP_LEFT_CORNER);
    Scroller<ColLabel>.Item item = new Item(index, col);

    // Create new plot and attach to the scroller item, which will handle positioning and disposal automatically :-)
    ArrayList<RowVariable> rows = rowScroller.getRowVariables();
    for (RowVariable row: rows) {
      Plot plot = row.createPlot(cvar);
      System.out.println("Creating plot for " + cvar.getName());
      item.attach(plot);
    }

    return item;
  }

  @Override
  protected boolean itemIsOpen(int index) {
    return data.getVariable(index).column();
  }

  @Override
  protected float getTargetWidth(int index) {
    return labelWidth;
  }

  @Override
  protected float getTargetHeight(int index) {
    return labelHeight;
    // What happens when the label changes height?

//    labelHeight = ih;
//    labelHeightMax = ihmax
  }
}

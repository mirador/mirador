package mirador.app;

import miralib.data.DataTree;
import miralib.data.Variable;
import miralib.data.VariableContainer;
import mui.Interface;
import mui.Widget;

public class ColScroller extends Scroller<ColLabel> {
  float labelWidth, labelHeight;
  protected float labelHeightMax;

  public ColScroller(Interface intf, float x, float y, float w, float h, float iw, float ih, float ihmax) {
    super(intf, x, y, w, h);
    labelWidth = iw;
    labelHeight = ih;
    labelHeightMax = ihmax;
    initItems(data.getColumnCount(), iw, ih, HORIZONTAL);
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

  @Override
  protected Scroller<ColLabel>.Item createItem(int index) {
    Variable var = data.getColumn(index);
//    float h = item.open() ? heightOpen : heightClose;
    ColLabel col = new ColLabel(intf, 0, 0, labelWidth, labelHeight, labelHeightMax, var,false /*defaultAnimation(event)*/);;
    addChild(col, Widget.TOP_LEFT_CORNER);
    return new Item(index, col);
  }
}

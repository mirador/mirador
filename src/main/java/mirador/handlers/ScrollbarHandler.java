/* COPYRIGHT (C) 2014-17 Fathom Information Design. All Rights Reserved. */

package mirador.handlers;

public interface ScrollbarHandler {
  public float drag(float newp, float maxd);
  public void stopDrag();
  public float press(float pos, float maxd);
  public int pressSlider(float pos, float size);
  public int currentItem();
  public float itemPosition(int idx, float maxd);
  public float resize(float news);
  public float totalSize();
  public float initPosition(float maxd);
}

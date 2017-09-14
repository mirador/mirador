/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package miralib.data;

import java.util.ArrayList;

import processing.data.TableRow;

/**
 * Dummy ranger for variables not included in display/calculations (for example
 * string variables).
 *
 */

public class DummyRange extends Range {

  public DummyRange(Variable var) {
    super(var);
  }

  public DummyRange(Range another) {
    super(another);
  }
  
  public void set(double min, double max, boolean normalized) {}
  public void set(ArrayList<String> values) {}
  public void set(String... values) {}
  public void reset() {}
  public void update(TableRow row) {}

  public boolean inside(TableRow row) {
    return false;
  }
  
  public double getMin() {
    return 0;
  }

  public double getMax() {
    return 0;
  }

  public long getCount() {
    return 0;
  }

  public int getRank(String value) {
    return 0;
  }

  public int getRank(String value, Range supr) {
    return 0;
  }

  public ArrayList<String> getValues() {
    return new ArrayList<String>();
  }

  public double snap(double value) {
    return 0;
  }

  public double normalize(int value) {
    return 0;
  }

  public double normalize(long value) {
    return 0;
  }

  public double normalize(float value) {
    return 0;
  }

  public double normalize(double value) {
    return 0;
  }

  double denormalize(double value) {
    return 0;
  }

  public int constrain(int value) {
    return 0;
  }

  public long constrain(long value) {
    return 0;
  }

  public float constrain(float value) {
    return 0;
  }

  public double constrain(double value) {
    return 0;
  }
}

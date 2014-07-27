/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package lib.ui;

import processing.core.PApplet;

/**
 * Simple soft float class to implement smooth animations
 * 
 */

public class SoftFloat {
  private float ATTRACTION = 0.1f;
  private float DAMPING = 0.5f;
  private float THRESHOLD = 0.0f;

  private float value;
  public float velocity;
  private float acceleration;
  
  private boolean enabled;  
  private boolean targeting;
  private float source;
  private float target;

  public SoftFloat(float v, boolean targeting) {
    if (targeting) {
      value = source = 0; 
      target = v;
      this.targeting = true;
    } else {
      value = source = target = v;
      this.targeting = false;      
    }
    enabled = true;
  }
  
  public SoftFloat(SoftFloat that) {
    this(that, 0);
  }
  
  public SoftFloat(SoftFloat that, float offset) {
    copy(that, offset);   
  }

  public void copy(SoftFloat that, float offset) {
    this.ATTRACTION = that.ATTRACTION;
    this.DAMPING = that.DAMPING;
    this.THRESHOLD = that.THRESHOLD;

    this.value = that.value + offset;
    this.velocity = that.velocity;
    this.acceleration = that.acceleration;
    
    this.enabled = that.enabled;  
    this.targeting = that.targeting;
    this.source = that.source + offset;
    this.target = that.target + offset;     
  }
  
  public SoftFloat(float v) {
    this(v, false);
  }

  public SoftFloat() {
    this(0);
  }

  public void setAttraction(float value) {
    ATTRACTION = value;
  }

  public void setDamping(float value) {
    DAMPING = value;
  }  
  
  public void setThreshold(float value) {
    THRESHOLD = value;
  }
  
  public void inc(float dt) {
    value = source = value + dt;
    targeting = false;
  }
  
  public void set(float v) {
    value = source = target = v;
    targeting = false;
  }  

  public float get() {
    return value;
  }

  public int getFloor() {
    return (int)value;
  }

  public int getCeil() {
    return PApplet.ceil(value);
  }
  
  public void enable() {
    enabled = true;
  }

  public void disable() {
    enabled = false;
  }

  public boolean update() {
    if (!enabled) return false;

    if (targeting) {
      acceleration += ATTRACTION * (target - value);
      velocity = (velocity + acceleration) * DAMPING;
      value += velocity;
      acceleration = 0;
      if (Math.abs(velocity) > 0.0001 && Math.abs(target - value) >= THRESHOLD) {
        return true;
      }
      
      // arrived, set it to the target value to prevent rounding error
      value = target;
      targeting = false;
    }
    return false;
  }

  public void setTarget(float t) {
    targeting = true;
    target = t;
    source = value;
  }
  
  public void incTarget(float dt) {
    targeting = true;
    target += dt;
    source = value;    
  }

  public float getTarget() {
    return targeting ? target : value;
  }

  public float getSource() {
    return targeting ? source : value;
  }
  
  public boolean isTargeting() {
    return targeting;
  }
}

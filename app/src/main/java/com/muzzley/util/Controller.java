package com.muzzley.util;

public class Controller<T> {
  private T t;

  public synchronized T getControlled() {
    return t;
  }

  public synchronized void onResume(T t) {
    this.t = t;
  }

  public synchronized void onPause() {
    this.t = null;
  }
}

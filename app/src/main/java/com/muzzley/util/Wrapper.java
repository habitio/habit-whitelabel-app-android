package com.muzzley.util;

public final class Wrapper<T> {
  public final T value;

  public Wrapper() {
    value = null;
  }

  public Wrapper(T value) {
    this.value = value;
  }
}

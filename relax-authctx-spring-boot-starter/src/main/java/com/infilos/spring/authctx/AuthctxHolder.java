package com.infilos.spring.authctx;

import java.util.*;

public class AuthctxHolder {
  private final Map<String, String> items = new HashMap<>();
  private final Map<String, String> mocks = new HashMap<>();

  void setValue(String name, String value) {
    if (name != null && value != null) {
      items.put(name, value);
    }
  }

  void setMock(String name, String value) {
    if (name != null && value != null) {
      mocks.put(name, value);
    }
  }
  
  public Map<String,String> allItems() {
    return Collections.unmodifiableMap(items);
  }
  
  public Map<String,String> allMocks() {
    return Collections.unmodifiableMap(mocks);
  }

  public Optional<String> getValue(String name) {
    return Optional.ofNullable(items.get(name));
  }

  public Optional<String> getValueOrMock(String name) {
    if (items.containsKey(name)) {
      return Optional.ofNullable(items.get(name));
    } else {
      return Optional.ofNullable(mocks.get(name));
    }
  }

  public Optional<String> getMock(String name) {
    return Optional.ofNullable(mocks.get(name));
  }
  
  public boolean isDefined() {
    return !items.isEmpty();
  }

  public boolean isUndefined() {
    return items.isEmpty();
  }

  public boolean isMockProvided() {
    return !mocks.isEmpty();
  }
}

package com.infilos.spring.track.aop;

import java.util.*;

public abstract class AuditContext {
  protected final String method;
  protected final Map<String, Object> params;
  protected final Object result;
  protected final Throwable failure;

  protected AuditContext(
      String method,
      List<String> paramNames,
      List<Object> paramValues,
      Object result,
      Throwable failure) {
    this.method = method;
    this.result = result;
    this.failure = failure;

    Map<String, Object> params = new HashMap<>();
    for (int idx = 0; idx < paramNames.size(); idx++) {
      params.put(paramNames.get(idx), paramValues.get(idx));
    }
    this.params = Collections.unmodifiableMap(params);
  }

  public String reqMethod() {
    return method;
  }

  public Optional<Object> reqParam(String name) {
    return Optional.ofNullable(params.get(name));
  }

  public Map<String, Object> reqParams() {
    return params;
  }

  public Optional<Object> resValue() {
    return Optional.ofNullable(result);
  }

  public Optional<Throwable> resCause() {
    return Optional.ofNullable(failure);
  }

  public Boolean succed() {
    return !resCause().isPresent();
  }

  public AuditMethodContext asMethod() {
    if (this instanceof AuditMethodContext) {
      return (AuditMethodContext) this;
    }

    throw new ClassCastException("Audit context cannot be cast to method.");
  }

  public AuditRestContext asRest() {
    if (this instanceof AuditRestContext) {
      return (AuditRestContext) this;
    }

    throw new ClassCastException("Audit context cannot be cast to rest.");
  }
}

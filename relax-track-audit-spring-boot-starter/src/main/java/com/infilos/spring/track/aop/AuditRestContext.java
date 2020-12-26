package com.infilos.spring.track.aop;

import java.util.List;
import java.util.Optional;

public abstract class AuditRestContext extends AuditMethodContext {

  protected AuditRestContext(
      String method,
      List<String> paramNames,
      List<Object> paramValues,
      Object result,
      Throwable failure) {
    super(method, paramNames, paramValues, result, failure);
  }

  public abstract String reqPath();

  public abstract Optional<String> reqQuery(String name);

  public abstract Optional<String> reqHeader(String name);

  public abstract Optional<String> reqCookie(String name);

  public abstract Optional<String> reqSession(String name);

  public abstract Optional<String> reqBody();

  public abstract Integer resStatus();

  public abstract Optional<String> resHeader(String name);
}

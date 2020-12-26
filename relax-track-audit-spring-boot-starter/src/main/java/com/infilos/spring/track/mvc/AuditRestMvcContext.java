package com.infilos.spring.track.mvc;

import com.infilos.spring.track.aop.AuditRestContext;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class AuditRestMvcContext extends AuditRestContext {
  private final HttpServletRequest request;
  private final Map<String, String> queries;
  private final Map<String, String> cookies;
  private final HttpServletResponse respond;

  public AuditRestMvcContext(
      String method,
      List<String> paramNames,
      List<Object> paramValues,
      Object result,
      Throwable failure,
      HttpServletRequest request,
      HttpServletResponse respond) {
    super(method, paramNames, paramValues, result, failure);
    this.request = request;
    this.respond = respond;

    Map<String, String> queries = new HashMap<>();
    if (Objects.nonNull(request.getQueryString())) {
      Arrays.stream(request.getQueryString().split("&"))
          .map(q -> Arrays.asList(q.split("=")))
          .filter(q -> q.size() == 2)
          .collect(Collectors.toList())
          .forEach(q -> queries.put(q.get(0), q.get(1)));
    }
    this.queries = Collections.unmodifiableMap(queries);

    Map<String, String> cookies = new HashMap<>();
    if (Objects.nonNull(request.getCookies())) {
      Arrays.stream(request.getCookies()).forEach(c -> cookies.put(c.getName(), c.getValue()));
    }
    this.cookies = Collections.unmodifiableMap(cookies);
  }

  @Override
  public String reqPath() {
    return request.getRequestURI();
  }

  @Override
  public Optional<String> reqQuery(String name) {
    return Optional.ofNullable(queries.get(name));
  }

  @Override
  public Optional<String> reqHeader(String name) {
    return Optional.ofNullable(request.getHeader(name));
  }

  @Override
  public Optional<String> reqCookie(String name) {
    return Optional.ofNullable(cookies.get(name));
  }

  @Override
  public Optional<String> reqSession(String name) {
    return Optional.ofNullable(request.getSession().getAttribute(name).toString());
  }

  @Override
  public Optional<String> reqBody() {
    try {
      return Optional.of(
          IOUtils.toString(request.getInputStream(), request.getCharacterEncoding()));
    } catch (IOException ex) {
      throw new IllegalStateException("Read request body failed.", ex);
    }
  }

  @Override
  public Integer resStatus() {
    return respond.getStatus();
  }

  @Override
  public Optional<String> resHeader(String name) {
    return Optional.ofNullable(respond.getHeader(name));
  }
}

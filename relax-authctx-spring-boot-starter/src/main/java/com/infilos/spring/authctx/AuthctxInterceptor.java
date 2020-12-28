package com.infilos.spring.authctx;

import com.infilos.spring.UserAuthentic;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public class AuthctxInterceptor implements HandlerInterceptor {
  private final AuthctxHolder holder;
  private final AuthctxOptions options;

  public AuthctxInterceptor(AuthctxOptions options, AuthctxHolder holder) {
    this.holder = holder;
    this.options = options;
  }

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    for (int idx = 0; idx < options.getHeaderNames().length; idx++) {
      String headerName = options.getHeaderNames()[idx];
      String headerValue = request.getHeader(headerName);
      
      if (headerValue != null && headerValue.trim().length() > 0) {
        holder.setValue(headerName, headerValue);
      }
    }
    
    if(!holder.isDefined()) {
      for (int idx = 0; idx < options.getHeaderNames().length; idx++) {
        String headerName = options.getHeaderNames()[idx];
        String mockValue = options.getMockedValues()[idx];

        if (mockValue != null && mockValue.trim().length() > 0) {
          holder.setMock(headerName, mockValue);
        }
      }
    }

    HandlerMethod handlerMethod;
    try {
      handlerMethod = (HandlerMethod) handler;
    } catch (ClassCastException e) {
      return true;
    }

    // if mock header values provided, then skip user authentic
    if (options.getMockedValues().length > 0) {
      return true;
    }

    // auto check authentic headers depends on controller's annotation
    Method method = handlerMethod.getMethod();
    if (method.isAnnotationPresent(UserAuthentic.class)
        || method.getDeclaringClass().isAnnotationPresent(UserAuthentic.class)) {
      if (holder.isUndefined()) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            String.format(
                "Request require http header for user authentic: '%s'", options.allHeaderNames()));
      }
    }

    return true;
  }
}

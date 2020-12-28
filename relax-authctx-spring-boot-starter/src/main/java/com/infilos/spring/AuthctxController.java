package com.infilos.spring;

import com.infilos.spring.authctx.AuthctxHolder;
import com.infilos.spring.authctx.AuthctxOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AuthctxController<T> {

  @Autowired private AuthctxOptions options;

  @Autowired private AuthctxHolder authctx;

  @Autowired private AuthctxService<T> service;

  protected T authUser() {
    if (authctx.isDefined()) {
      Optional<T> user = service.findUser(authctx.allItems());
      if (user.isPresent()) {
        return user.get();
      }
      
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          String.format(
              "Request provided http header cannot find valid user: '%s'",
              formatAuthctx(authctx.allItems())));
    }

    if (authctx.isMockProvided()) {
      Optional<T> user = service.buildUser(authctx.allMocks());
      if (user.isPresent()) {
        return user.get();
      }

      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          String.format(
              "Request provided mock http header cannot build valid user: '%s'",
              formatAuthctx(authctx.allMocks())));
    }

    throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        String.format(
            "Request require http header for user authentic: '%s'", options.allHeaderNames()));
  }

  private String formatAuthctx(Map<String, String> items) {
    return items.entrySet().stream()
        .map(entry -> entry.getKey() + "=" + entry.getValue())
        .collect(Collectors.joining(", "));
  }
}

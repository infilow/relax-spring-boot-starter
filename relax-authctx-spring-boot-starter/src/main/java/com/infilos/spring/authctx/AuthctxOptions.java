package com.infilos.spring.authctx;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("track.rest.authctx")
public class AuthctxOptions {
  private String[] headerNames;
  private String[] mockedValues;

  public void setHeaderNames(String[] headerNames) {
    this.headerNames = headerNames;
  }

  public void setMockedValues(String[] mockedValues) {
    this.mockedValues = mockedValues;
  }

  public String[] getHeaderNames() {
    return headerNames != null ? headerNames : new String[0];
  }

  public String[] getMockedValues() {
    return mockedValues != null ? mockedValues : new String[0];
  }

  public String allHeaderNames() {
    return String.join(",", getHeaderNames());
  }
}

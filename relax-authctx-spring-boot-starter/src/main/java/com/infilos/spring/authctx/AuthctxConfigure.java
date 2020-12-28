package com.infilos.spring.authctx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AuthctxConfigure implements WebMvcConfigurer {

  @Autowired private AuthctxOptions options;

  @Bean("authctxHolder")
  @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
  public AuthctxHolder scopedAuthctx() {
    return new AuthctxHolder();
  }

  @Bean
  public AuthctxInterceptor authctxInterceptor() {
    if(options.getHeaderNames().length == 0) {
      throw new IllegalStateException("Authctx options required: track.rest.authctx.header-names");
    }
    if(options.getMockedValues().length != 0 && options.getMockedValues().length != options.getHeaderNames().length) {
      throw new IllegalStateException("Authctx options invalid: track.rest.authctx.mocked-values not enough");
    }
    return new AuthctxInterceptor(options, scopedAuthctx());
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(authctxInterceptor());
  }
}

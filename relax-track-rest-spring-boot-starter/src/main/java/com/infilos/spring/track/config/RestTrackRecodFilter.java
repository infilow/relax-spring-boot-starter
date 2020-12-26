package com.infilos.spring.track.config;

import com.infilos.spring.track.api.Consts;
import com.infilos.spring.track.utils.UuidGenerator;
import com.infilos.spring.track.wrapper.SpringRequestWrapper;
import com.infilos.spring.track.wrapper.SpringRespondWrapper;
import com.infilos.utils.Loggable;
import org.apache.commons.io.IOUtils;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RestTrackRecodFilter extends OncePerRequestFilter implements Loggable {

  private final ApplicationContext context;
  private final UuidGenerator generator;
  private final String ignorePatterns;
  private final Boolean includeHeaders;

  public RestTrackRecodFilter(
      ApplicationContext context, RestTrackOptions options, UuidGenerator generator) {
    this.context = context;
    this.generator = generator;
    this.ignorePatterns = options.getIgnorePatterns();
    this.includeHeaders = options.isIncludeHeaders();
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if (ignorePatterns != null && request.getRequestURI().matches(ignorePatterns)) {
      chain.doFilter(request, response);
      return;
    }

    generator.fillMdc(request);
    fillHandlerMethod(request);

    final long start = System.currentTimeMillis();
    final SpringRequestWrapper wrappedRequest = new SpringRequestWrapper(request);
    final SpringRespondWrapper wrappedRespond = new SpringRespondWrapper(response);
    wrappedRespond.setHeader(Consts.ReqidHeader, MDC.get(Consts.ReqidHeader));
    wrappedRespond.setHeader(Consts.CoridHeader, MDC.get(Consts.CoridHeader));

    try {
      loggingRequest(wrappedRequest);
      chain.doFilter(wrappedRequest, wrappedRespond);
      loggingResponse(start, wrappedRespond);
    } catch (Exception ex) {
      loggingResponse(start, wrappedRespond, 500);
      throw ex;
    }
  }

  private void loggingRequest(SpringRequestWrapper request) {
    try {
      if (includeHeaders) {
        log()
            .info(
                "-->Request: method={}, uri={}, body={}, headers={}",
                request.getMethod(),
                request.getRequestURI(),
                IOUtils.toString(request.getInputStream(), request.getCharacterEncoding()),
                request.getAllHeaders());
      } else {
        log()
            .info(
                "-->Request: method={}, uri={}, body={}",
                request.getMethod(),
                request.getRequestURI(),
                IOUtils.toString(request.getInputStream(), request.getCharacterEncoding()));
      }
    } catch (IOException ignore) {
    }
  }

  private void loggingResponse(long start, SpringRespondWrapper respond) {
    loggingResponse(start, respond, respond.getStatus());
  }

  private void loggingResponse(long start, SpringRespondWrapper respond, int status) {
    try {
      final long cost = System.currentTimeMillis() - start;
      respond.setCharacterEncoding("UTF-8");
      if (includeHeaders) {
        log()
            .info(
                "<--Respond: cost={}ms, status={}, body={}, headers={}",
                cost,
                status,
                IOUtils.toString(respond.getContentAsByteArray(), respond.getCharacterEncoding()),
                respond.getAllHeaders());
      } else {
        log()
            .info(
                "<--Respond: cost={}ms, status={}, body={}",
                cost,
                status,
                IOUtils.toString(respond.getContentAsByteArray(), respond.getCharacterEncoding()));
      }
    } catch (IOException ignore) {
    }
  }

  private void fillHandlerMethod(HttpServletRequest request) {
    try {
      RequestMappingHandlerMapping mappings =
          (RequestMappingHandlerMapping) context.getBean("requestMappingHandlerMapping");
      HandlerExecutionChain handlerChain = mappings.getHandler(request);

      if (handlerChain != null) {
        HandlerMethod handler = (HandlerMethod) handlerChain.getHandler();
        MDC.put(
            Consts.OperaHeader,
            handler.getBeanType().getSimpleName() + "." + handler.getMethod().getName());
      }
    } catch (Exception ex) {
      log().trace("Cannot extract handler method", ex);
    }
  }
}

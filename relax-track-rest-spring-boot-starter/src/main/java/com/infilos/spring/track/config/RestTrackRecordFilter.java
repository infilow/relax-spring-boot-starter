package com.infilos.spring.track.config;

import com.infilos.spring.track.TrackSupport;
import com.infilos.spring.track.api.Consts;
import com.infilos.spring.track.utils.UuidGenerator;
import com.infilos.spring.track.wrapper.SpringRequestWrapper;
import com.infilos.spring.track.wrapper.SpringRespondWrapper;
import com.infilos.utils.Loggable;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RestTrackRecordFilter extends OncePerRequestFilter implements Loggable {

    private final ApplicationContext context;
    private final UuidGenerator generator;
    private final List<Pattern> excludePatterns;
    private final Boolean includeHeaders;
    private final Boolean includeBodies;

    public RestTrackRecordFilter(
        ApplicationContext context, RestTrackOptions options, UuidGenerator generator) {
        this.context = context;
        this.generator = generator;
        this.includeHeaders = options.getIncludeHeaders();
        this.includeBodies = options.getIncludeBodies();
        this.excludePatterns = options.getExcludePatterns().stream().filter(StringUtils::hasText).map(Pattern::compile).collect(Collectors.toList());
        this.excludePatterns.add(Pattern.compile("^/swagger-ui.*"));
        this.excludePatterns.add(Pattern.compile("^/v3/api-docs.*"));
        this.excludePatterns.add(Pattern.compile("^/_stcore.*"));
        this.excludePatterns.add(Pattern.compile("^/actuator.*"));
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        for (Pattern excludePattern: excludePatterns) {
            if (excludePattern.matcher(request.getRequestURI()).matches()) {
                chain.doFilter(request, response);
                return;
            }
        }

        TrackSupport.fillMDCContext(request);
        fillMDCContextHandlerMethod(request);
        TrackSupport.fillThreadMDCContext();

        final long start = System.currentTimeMillis();
        final SpringRequestWrapper wrappedRequest = SpringRequestWrapper.create(request);
        final SpringRespondWrapper wrappedRespond = SpringRespondWrapper.create(response);
        wrappedRespond.setHeader(Consts.ReqidHeader, MDC.get(Consts.ReqidHeader));
        wrappedRespond.setHeader(Consts.CoridHeader, MDC.get(Consts.CoridHeader));

        try {
            loggingRequest(wrappedRequest);
            chain.doFilter(wrappedRequest, wrappedRespond);
            loggingResponse(start, wrappedRespond);
        } catch (Exception ex) {
            loggingResponse(start, wrappedRespond, 500);
            throw ex;
        } finally {
            TrackSupport.clearMDCContext();
            TrackSupport.clearThreadMDCContext();
        }
    }

    private void loggingRequest(SpringRequestWrapper request) {
        try {
            if (includeHeaders && includeBodies) {
                log().info(
                    "==>Request: method={}, uri={}, body={}, headers={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getBodyString(),
                    request.getAllHeaders());
            } else if (includeHeaders) {
                log().info(
                    "==>Request: method={}, uri={}, headers={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getAllHeaders());
            } else if (includeBodies) {
                log().info(
                    "==>Request: method={}, uri={}, bodies={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getBodyString());
            } else {
                log().info(
                    "==>Request: method={}, uri={}",
                    request.getMethod(),
                    request.getRequestURI());
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
            if (includeHeaders && includeBodies) {
                log().info(
                    "<==Respond: cost={}ms, status={}, body={}, headers={}",
                    cost,
                    status,
                    respond.getBodyString(),
                    respond.getAllHeaders());
            } else if (includeHeaders) {
                log().info(
                    "<==Respond: cost={}ms, status={}, headers={}",
                    cost,
                    status,
                    respond.getAllHeaders());
            } else if (includeBodies) {
                log().info(
                    "<==Respond: cost={}ms, status={}, body={}",
                    cost,
                    status,
                    respond.getBodyString());
            } else {
                log().info(
                    "<==Respond: cost={}ms, status={}",
                    cost,
                    status);
            }
        } catch (IOException ignore) {
        }
    }

    private void fillMDCContextHandlerMethod(HttpServletRequest request) {
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

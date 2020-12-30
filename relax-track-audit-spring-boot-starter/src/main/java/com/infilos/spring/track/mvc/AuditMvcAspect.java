package com.infilos.spring.track.mvc;

import com.infilos.spring.track.aop.AuditAspect;
import com.infilos.spring.track.aop.AuditMethodContext;
import com.infilos.spring.track.aop.AuditOptions;
import com.infilos.spring.track.aop.AuditRestContext;
import com.infilos.spring.track.api.AuditRecord;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Aspect
@Component
public class AuditMvcAspect extends AuditAspect {
    private static final Logger log = LoggerFactory.getLogger(AuditMvcAspect.class);

    @Around("isAuditAction() && !isRestController()")
    public Object aroundMethodInvocation(ProceedingJoinPoint point) throws Throwable {
        Optional<AuditOptions> options = buildAuditOptions(point);
        Object result = null;
        Throwable failure = null;

        try {
            result = point.proceed();
        } catch (Throwable cause) {
            failure = cause;
            throw cause;
        } finally {
            Optional<AuditMethodContext> context = buildMethodContext(point, result, failure);
            if (options.isPresent() && context.isPresent()) {
                Optional<AuditRecord> record = buildActionRecord(options.get(), context.get());
                record.ifPresent(
                    r -> {
                        if (options.get().loggingEnabled() || log.isDebugEnabled()) {
                            log.info(r.toString());
                        }
                        collectActionRecord(r);
                    });
            }
        }

        return result;
    }

    @Around("isAuditAction() && isRestController()")
    public Object aroundRestController(ProceedingJoinPoint point) throws Throwable {
        Optional<AuditOptions> options = buildAuditOptions(point);
        Object result = null;
        Throwable failure = null;

        try {
            result = point.proceed();
        } catch (Throwable cause) {
            failure = cause;
            throw cause;
        } finally {
            Optional<AuditRestContext> context = buildRestContext(point, result, failure);
            if (options.isPresent() && context.isPresent()) {
                Optional<AuditRecord> record = buildActionRecord(options.get(), context.get());
                record.ifPresent(
                    r -> {
                        if (options.get().loggingEnabled() || log.isDebugEnabled()) {
                            log.info(r.toString());
                        }
                        collectActionRecord(r);
                    });
            }
        }

        return result;
    }

    @Override
    protected Optional<AuditRestContext> buildRestContext(
        ProceedingJoinPoint point, Object result, Throwable failure) {
        try {
            String method = point.getSignature().getName();
            List<String> paramNames =
                Arrays.asList(((CodeSignature) point.getSignature()).getParameterNames());
            List<Object> paramValues = Arrays.asList(point.getArgs());

            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            HttpServletResponse response = attributes.getResponse();

            return Optional.of(
                new AuditRestMvcContext(
                    method, paramNames, paramValues, result, failure, request, response));
        } catch (Throwable ex) {
            log.error("Construct audit rest mvc context failed.", ex);
            return Optional.empty();
        }
    }
}

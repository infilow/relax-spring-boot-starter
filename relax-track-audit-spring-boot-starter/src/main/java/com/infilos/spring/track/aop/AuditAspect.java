package com.infilos.spring.track.aop;

import com.infilos.spring.track.AuditExtractors;
import com.infilos.spring.track.AuditTransfers;
import com.infilos.spring.track.api.*;
import com.infilos.spring.track.config.SpringContextConfigure;
import com.infilos.spring.track.service.AuditRecordJdbcService;
import com.infilos.spring.track.service.AuditRecordMemService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class AuditAspect {
    protected static final Logger log = LoggerFactory.getLogger(AuditAspect.class);
    protected static final ConcurrentHashMap<Class<?>, AuditOption> ClassOptions =
        new ConcurrentHashMap<>();
    protected static final ConcurrentHashMap<String, AuditOptions> ActionOptions =
        new ConcurrentHashMap<>();

    @Autowired(required = false)
    protected Optional<AuditRecordMemService> memService;

    @Autowired(required = false)
    protected Optional<AuditRecordJdbcService> jdbcService;

    @Autowired
    protected List<AuditService> customServices;

    @Pointcut("@annotation(com.infilos.spring.track.api.AuditAction)")
    public void isAuditAction() {
    }

    @Pointcut(
        "@annotation(org.springframework.web.bind.annotation.RequestMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.GetMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.PostMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.PutMapping) || "
            + "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void isRestController() {
    }

    protected Optional<AuditOptions> buildAuditOptions(ProceedingJoinPoint point) {
        try {
            // class level options
            Signature signature = point.getSignature();
            Class<?> classType = signature.getDeclaringType();
            String className = signature.getDeclaringTypeName();
            if (classType.isAnnotationPresent(AuditOption.class)) {
                ClassOptions.computeIfAbsent(classType, c -> classType.getAnnotation(AuditOption.class));
            }

            // method level options
            String pointName = signature.getName();
            String pointActionKey = className + "." + pointName;
            ActionOptions.computeIfAbsent(
                pointActionKey,
                k -> {
                    Method method = ((MethodSignature) signature).getMethod();
                    AuditAction action = method.getAnnotation(AuditAction.class);
                    return new AuditOptions(action, ClassOptions.get(classType));
                });

            return Optional.ofNullable(ActionOptions.get(pointActionKey));
        } catch (Throwable ex) {
            log.error("Construct audit action options failed.", ex);
            return Optional.empty();
        }
    }

    protected Optional<AuditMethodContext> buildMethodContext(
        ProceedingJoinPoint point, Object result, Throwable failure) {
        try {
            String method = point.getSignature().getName();
            List<String> paramNames =
                Arrays.asList(((CodeSignature) point.getSignature()).getParameterNames());
            List<Object> paramValues = Arrays.asList(point.getArgs());
            return Optional.of(new AuditMethodContext(method, paramNames, paramValues, result, failure));
        } catch (Throwable ex) {
            log.error("Construct audit action method context failed.", ex);
            return Optional.empty();
        }
    }

    /**
     * Implement by mvc and flux.
     */
    protected abstract Optional<AuditRestContext> buildRestContext(
        ProceedingJoinPoint point, Object result, Throwable failure);

    protected <CTX extends AuditContext> Optional<AuditRecord> buildActionRecord(
        AuditOptions options, CTX context) {
        try {
            Optional<AuditAttribute> org = options.org().map(attri -> extract(context, attri, "org"));
            Optional<AuditAttribute> role = options.role().map(attri -> extract(context, attri, "role"));
            Optional<AuditAttribute> user = options.user().map(attri -> extract(context, attri, "user"));
            Optional<AuditAttribute> act =
                options.action().map(attri -> extract(context, attri, "acttion"));
            List<AuditAttribute> tags =
                options.tags().stream()
                    .map(attri -> AuditExtractors.of(attri.from()).extract(attri, context))
                    .collect(Collectors.toList());

            AuditRecord record =
                new AuditRecord(
                    SpringContextConfigure.artifactOrName(),
                    org.map(AuditAttribute::value).orElse(null),
                    role.map(AuditAttribute::value).orElse(null),
                    user.map(AuditAttribute::value).orElse(null),
                    act.map(AuditAttribute::value).orElse(null),
                    tags.stream()
                        .collect(
                            Collectors.toMap(AuditAttribute::key, AuditAttribute::value, (k1, k2) -> k2)),
                    context.succed());

            if (options.transfer().isPresent()) {
                Optional<AuditTransfer> transfer = AuditTransfers.of(options.transfer().get());
                if (transfer.isPresent()) {
                    try {
                        record = transfer.get().transform(record);
                    } catch (Throwable ex) {
                        log.error(
                            "Audit transform failed with transfer {} on {}.",
                            options.transfer().get().getName(),
                            record,
                            ex);
                    }
                } else {
                    log.error("Audit transfer not found for class {}.", options.transfer().get().getName());
                }
            }

            if (record.isValid()) {
                return Optional.of(record);
            } else {
                log.error("Audit extracted invalid record: {}", record);
                return Optional.empty();
            }
        } catch (Throwable ex) {
            log.error("Construct audit record failed.", ex);
            return Optional.empty();
        }
    }

    protected <CTX extends AuditContext> AuditAttribute extract(
        CTX context, AuditAttri attri, String patchAlias) {
        return extract(
            context,
            attri,
            new HashMap<String, String>() {
                {
                    put("alias", patchAlias);
                }
            });
    }

    protected <CTX extends AuditContext> AuditAttribute extract(
        CTX context, AuditAttri attri, Map<String, String> attriPatches) {
        try {
            return AuditExtractors.of(attri.from())
                .extract(AuditSpec.patch(attri, attriPatches), context);
        } catch (Throwable ex) {
            return AuditAttribute.of(attri, ex);
        }
    }

    protected void collectActionRecord(AuditRecord record) {
        if (!customServices.isEmpty()) {
            collect(customServices.get(0), record);
        } else if (jdbcService.isPresent()) {
            collect(jdbcService.get(), record);
        } else if (memService.isPresent()) {
            collect(memService.get(), record);
        }
    }

    protected void collect(AuditService service, AuditRecord record) {
        try {
            if (service != null) {
                service.collect(Collections.singletonList(record));
                log.debug("Audit record collected: {}", record);
            }
        } catch (Throwable ex) {
            log.error("Audit record collected failed: {}", record, ex);
        }
    }
}

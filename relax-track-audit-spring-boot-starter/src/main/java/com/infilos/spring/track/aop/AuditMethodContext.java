package com.infilos.spring.track.aop;

import java.util.List;

public class AuditMethodContext extends AuditContext {

    public AuditMethodContext(
        String method,
        List<String> paramNames,
        List<Object> paramValues,
        Object result,
        Throwable failure) {
        super(method, paramNames, paramValues, result, failure);
    }
}

package com.infilos.spring.track.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditOption {
    AuditAttri org() default @AuditAttri();

    AuditAttri role() default @AuditAttri();

    AuditAttri user() default @AuditAttri();

    AuditAttri action() default @AuditAttri();

    AuditAttri[] tags() default @AuditAttri();

    boolean log() default false;

    Class<? extends AuditTransfer> trans() default AuditTransfer.NoopTransfer.class;
}

package com.infilos.spring.track.api;

import java.lang.annotation.*;

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

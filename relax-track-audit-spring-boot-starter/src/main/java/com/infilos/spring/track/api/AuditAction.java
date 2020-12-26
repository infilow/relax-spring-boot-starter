package com.infilos.spring.track.api;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditAction {
  /** The user's organization which execute this action. */
  AuditAttri org() default @AuditAttri();

  /** The user's authorization role which execute this action. */
  AuditAttri role() default @AuditAttri();

  /** The user's name which execute this action. */
  AuditAttri user() default @AuditAttri();

  /** The name of this action. */
  AuditAttri action() default @AuditAttri();

  /** Additional key-value pairs to describe this action. */
  AuditAttri[] tags() default @AuditAttri();

  /** Enable automatic logging when this action executed. */
  boolean log() default false;

  /** Define the transfer to transform the extracted record to another record. */
  Class<? extends AuditTransfer> trans() default AuditTransfer.NoopTransfer.class;
}

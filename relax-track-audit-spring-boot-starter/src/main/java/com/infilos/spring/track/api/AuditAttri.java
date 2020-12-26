package com.infilos.spring.track.api;

import java.lang.annotation.*;

@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditAttri {
  /** Define where to extract this attribute's value. */
  Audit from() default Audit.Nowhere;

  /** Define attribute's source name. */
  String name() default "";

  /** Define attribute's source json-path, used when from() is json structure. */
  String locate() default "";

  /**
   * Define attribute's constant value, used when from() is Audit.Constant. Define attribute's
   * default value, used when extract failed by name/locate.
   */
  String value() default "";

  /**
   * Define attribute's target name when the value used for tags or transfer. Or use name() as
   * default.
   */
  String alias() default "";
}

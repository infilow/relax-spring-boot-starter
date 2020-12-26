package com.infilos.spring.track.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infilos.spring.track.api.Audit;
import com.infilos.spring.track.api.AuditAttri;
import com.infilos.spring.track.config.SpringContextConfigure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

public class AuditAttribute {
  private static final Logger log = LoggerFactory.getLogger(AuditAttribute.class);
  private static final ObjectMapper json = SpringContextConfigure.inject(ObjectMapper.class);
  private final String key;
  private final String value;
  private final Boolean valid;

  private AuditAttribute(String key, String value, Boolean valid) {
    this.key = key;
    this.value = value;
    this.valid = valid;
  }

  public static AuditAttribute of(AuditAttri attri, Object object) {
    if (attri.from() == Audit.Nowhere) {
      return ofNowhere(attri);
    }

    Object inner = object;

    // un-nested
    if (Objects.isNull(inner)) {
      return ofNull(attri);
    }
    if (inner instanceof Throwable) {
      return ofThrowable(attri, (Throwable) inner);
    }

    if (inner instanceof Optional<?>) {
      inner = ((Optional<?>) inner).orElse(null);
    }

    // nested
    if (Objects.isNull(inner)) {
      return ofNull(attri);
    }
    if (inner instanceof Throwable) {
      return ofThrowable(attri, (Throwable) inner);
    }

    // string
    if (inner instanceof String && inner.toString().replaceAll(" ", "").length() == 0) {
      return ofBlank(attri);
    }
    if (inner instanceof String) {
      return new AuditAttribute(attri.alias(), (String) inner, true);
    }

    // object
    try {
      String value = json.writeValueAsString(inner);
      return new AuditAttribute(attri.alias(), value, true);
    } catch (Throwable ex) {
      log.error("Serialize audit attribute value failed.", ex);
      return ofThrowable(attri, ex);
    }
  }

  public static AuditAttribute ofNowhere(AuditAttri attri) {
    return new AuditAttribute(attri.alias(), "Nowhere", false);
  }

  public static AuditAttribute ofNull(AuditAttri attri) {
    return new AuditAttribute(
        attri.alias(), String.format("Null(%s-%s)", attri.name(), attri.locate()), false);
  }

  public static AuditAttribute ofBlank(AuditAttri attri) {
    return new AuditAttribute(
        attri.alias(), String.format("Blank(%s-%s)", attri.name(), attri.locate()), false);
  }

  public static AuditAttribute ofMissng(AuditAttri attri) {
    return new AuditAttribute(
        attri.alias(), String.format("Missing(%s-%s)", attri.name(), attri.locate()), false);
  }

  public static AuditAttribute ofThrowable(AuditAttri attri, Throwable cause) {
    return new AuditAttribute(
        attri.alias(),
        String.format(
            "Throw(%s-%s-%s-%s)",
            attri.name(), attri.locate(), cause.getClass().getName(), cause.getMessage()),
        false);
  }

  public String key() {
    return key;
  }

  public String value() {
    return value;
  }

  /** Check if is a valid value or a failure description. */
  public Boolean isValid() {
    return valid;
  }
}

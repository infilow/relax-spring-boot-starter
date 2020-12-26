package com.infilos.spring.track.aop;

import com.infilos.spring.track.api.Audit;
import com.infilos.spring.track.api.AuditAttri;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AuditAttributeTest {

  @Test
  public void test() {

    AuditAttri attri =
        AuditSpec.create(
            Audit.ReqBody,
            new HashMap<String, String>() {
              {
                put("name", "name");
                put("locate", "locate");
                put("value", "value");
                put("alias", "alias");
              }
            });

    assertEquals("alias", AuditAttribute.of(attri, "value").key());
    assertTrue(
        AuditAttribute.of(
                AuditSpec.create(
                    Audit.Nowhere,
                    new HashMap<String, String>() {
                      {
                        put("alias", "alias");
                      }
                    }),
                null)
            .value()
            .startsWith("Nowhere"));
    assertTrue(AuditAttribute.of(attri, null).value().startsWith("Null"));
    assertTrue(AuditAttribute.of(attri, Optional.of("value")).isValid());
    assertTrue(AuditAttribute.of(attri, Optional.of("")).value().startsWith("Blank"));
    assertTrue(AuditAttribute.of(attri, Optional.ofNullable(null)).value().startsWith("Null"));
    assertTrue(AuditAttribute.of(attri, "").value().startsWith("Blank"));
    assertTrue(AuditAttribute.of(attri, "value").isValid());
    assertTrue(AuditAttribute.of(attri, new RuntimeException()).value().startsWith("Throw"));
    assertTrue(AuditAttribute.ofMissng(attri).value().startsWith("Missing"));
    assertTrue(AuditAttribute.of(attri, Arrays.asList(1, 2, 3)).isValid());
    assertTrue(AuditAttribute.of(attri, new FailOnGetter()).value().startsWith("Throw"));
    assertTrue(AuditAttribute.of(attri, 10).isValid());
  }

  private static class FailOnGetter {
    public String getValue() {
      throw new RuntimeException();
    }
  }
}

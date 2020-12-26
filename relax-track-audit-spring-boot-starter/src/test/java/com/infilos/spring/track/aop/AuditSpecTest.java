package com.infilos.spring.track.aop;

import com.infilos.spring.track.api.Audit;
import com.infilos.spring.track.api.AuditAttri;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuditSpecTest {

  @Test
  public void createAttri() {
    AuditAttri attri =
        AuditSpec.create(
            Audit.ReqBody,
            new HashMap<String, String>() {
              {
                put("name", "name");
                put("locate", "locate");
                put("alias", "alias");
              }
            });

    assertEquals(Audit.ReqBody, attri.from());
    assertEquals("name", attri.name());
    assertEquals("locate", attri.locate());
    assertEquals("", attri.value());
    assertEquals("alias", attri.alias());
  }

  @Test
  public void patchAttri() {
    AuditAttri origin =
        AuditSpec.create(
            Audit.ReqBody,
            new HashMap<String, String>() {
              {
                put("name", "origin");
                put("locate", "origin");
                put("value", "origin");
                put("alias", "origin");
              }
            });

    AuditAttri patched =
        AuditSpec.patch(
            origin,
            new HashMap<String, String>() {
              {
                put("name", "patched");
                put("locate", "patched");
                put("value", "patched");
                put("alias", "patched");
              }
            });

    assertEquals(Audit.ReqBody, patched.from());
    assertEquals("patched", patched.name());
    assertEquals("patched", patched.locate());
    assertEquals("patched", patched.value());
    assertEquals("patched", patched.alias());
  }
}

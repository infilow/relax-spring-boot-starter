package com.infilos.spring.track;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infilos.relax.Json;
import com.infilos.spring.track.aop.AuditAttribute;
import com.infilos.spring.track.aop.AuditSpec;
import com.infilos.spring.track.api.Audit;
import com.infilos.spring.track.mvc.AuditRestMvcContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AuditExtractorsTest {

  @Bean
  public ObjectMapper objectMapper() {
    return Json.underMapper();
  }

  @Test
  public void test() {
    AuditRestMvcContext context = Mockito.mock(AuditRestMvcContext.class);
    Mockito.when(context.asRest()).thenReturn(context);

    Map<String, String> attributes =
        new HashMap<String, String>() {
          {
            put("name", "name");
            put("alias", "alias");
            put("value", "default");
          }
        };

    AuditAttribute attribute0 =
        AuditExtractors.NowhereExtractor()
            .extract(AuditSpec.create(Audit.Nowhere, attributes), context);
    assertEquals(attribute0.value(), "Nowhere");

    AuditAttribute attribute1 =
        AuditExtractors.ConstantExtractor()
            .extract(AuditSpec.create(Audit.Constant, attributes), context);
    assertEquals(attribute1.value(), "default");

    AuditAttribute attribute2 =
        AuditExtractors.ReqParamExtractor()
            .extract(AuditSpec.create(Audit.ReqParam, attributes), context);
    assertEquals(attribute2.value(), "default");

    attributes.put("name", "param");
    attributes.put("locate", "$.key");
    Mockito.when(context.reqParam("param"))
        .thenReturn(
            Optional.of(
                new HashMap<String, String>() {
                  {
                    put("key", "json-value");
                  }
                }));

    AuditAttribute attribute3 =
        AuditExtractors.ReqParamExtractor()
            .extract(AuditSpec.create(Audit.ReqParam, attributes), context);
    assertEquals(attribute3.value(), "json-value");

    Mockito.when(context.reqParam("param")).thenReturn(Optional.of(new HashMap<String, String>()));

    AuditAttribute attribute4 =
        AuditExtractors.ReqParamExtractor()
            .extract(AuditSpec.create(Audit.ReqParam, attributes), context);
    assertEquals(attribute4.value(), "default");

    Mockito.when(context.reqPath()).thenReturn("/api/list");
    AuditAttribute attribute5 =
        AuditExtractors.ReqPathExtractor()
            .extract(AuditSpec.create(Audit.ReqPath, attributes), context);
    assertEquals(attribute5.value(), "/api/list");

    Mockito.when(context.reqPath()).thenReturn(null);
    AuditAttribute attribute6 =
        AuditExtractors.ReqPathExtractor()
            .extract(AuditSpec.create(Audit.ReqPath, attributes), context);
    assertEquals(attribute6.value(), "default");

    attributes.put("name", "name");
    Mockito.when(context.reqQuery("name")).thenReturn(Optional.of("query-value"));
    AuditAttribute attribute7 =
        AuditExtractors.ReqQueryExtractor()
            .extract(AuditSpec.create(Audit.ReqQuery, attributes), context);
    assertEquals(attribute7.value(), "query-value");

    Mockito.when(context.reqHeader("name")).thenReturn(Optional.of("header-value"));
    AuditAttribute attribute8 =
        AuditExtractors.ReqHeaderExtractor()
            .extract(AuditSpec.create(Audit.ReqHeader, attributes), context);
    assertEquals(attribute8.value(), "header-value");

    attributes.put("locate", "$.key");
    Mockito.when(context.asRest().reqBody()).thenReturn(Optional.of("{\"key\":\"json-value\"}"));
    AuditAttribute attribute9 =
        AuditExtractors.ReqBodyExtractor()
            .extract(AuditSpec.create(Audit.ReqBody, attributes), context);
    assertEquals(attribute9.value(), "json-value");

    Mockito.when(context.asRest().reqBody()).thenReturn(Optional.of("{\"key\":\"form-value\"}"));
    AuditAttribute attribute10 =
        AuditExtractors.ReqBodyExtractor()
            .extract(AuditSpec.create(Audit.ReqBody, attributes), context);
    assertEquals(attribute10.value(), "form-value");

    Mockito.when(context.reqMethod()).thenReturn("method");
    AuditAttribute attribute11 =
        AuditExtractors.ReqMethodExtractor()
            .extract(AuditSpec.create(Audit.ReqMethod, attributes), context);
    assertEquals(attribute11.value(), "method");

    Mockito.when(context.resValue()).thenReturn(Optional.of("{\"key\":\"result-value\"}"));
    AuditAttribute attribute12 =
        AuditExtractors.ResValueExtractor()
            .extract(AuditSpec.create(Audit.ResValue, attributes), context);
    assertEquals(attribute12.value(), "result-value");

    attributes.remove("locate");
    Mockito.when(context.resValue()).thenReturn(Optional.of("{\"key\":\"result-value}"));
    AuditAttribute attribute13 =
        AuditExtractors.ResValueExtractor()
            .extract(AuditSpec.create(Audit.ResValue, attributes), context);

    assertEquals(attribute13.value(), "{\"key\":\"result-value}");

    attributes.put("locate", "$.key");
    Mockito.when(context.resValue())
        .thenReturn(
            Optional.of(
                new HashMap<String, String>() {
                  {
                    put("key", "result-value");
                  }
                }));
    AuditAttribute attribute14 =
        AuditExtractors.ResValueExtractor()
            .extract(AuditSpec.create(Audit.ResValue, attributes), context);
    assertEquals(attribute14.value(), "result-value");

    Mockito.when(context.resValue()).thenReturn(Optional.empty());
    AuditAttribute attribute15 =
        AuditExtractors.ResValueExtractor()
            .extract(AuditSpec.create(Audit.ResValue, attributes), context);
    assertTrue(attribute15.value().startsWith("Blank"));

    Mockito.when(context.resCause()).thenReturn(Optional.of(new RuntimeException("error")));
    AuditAttribute attribute16 =
        AuditExtractors.ResCauseExtractor()
            .extract(AuditSpec.create(Audit.ResCause, attributes), context);

    assertEquals(attribute16.value(), "java.lang.RuntimeException(error)");

    Mockito.when(context.asRest().reqCookie("name")).thenReturn(Optional.of("cookie-value"));
    AuditAttribute attribute17 =
        AuditExtractors.ReqCookieExtractor()
            .extract(AuditSpec.create(Audit.ReqCookie, attributes), context);
    assertEquals(attribute17.value(), "cookie-value");

    Mockito.when(context.asRest().reqSession("name")).thenReturn(Optional.of("session-value"));
    AuditAttribute attribute18 =
        AuditExtractors.ReqSessionExtractor()
            .extract(AuditSpec.create(Audit.ReqSession, attributes), context);
    assertEquals(attribute18.value(), "session-value");
  }
}

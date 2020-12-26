package com.infilos.spring.track.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class SpringContextConfigureTest {

  @Test
  public void test() {
    assertEquals("auditor", SpringContextConfigure.name());
    assertFalse(SpringContextConfigure.artifact().isPresent());
    assertEquals("auditor", SpringContextConfigure.artifactOrName());
  }
}

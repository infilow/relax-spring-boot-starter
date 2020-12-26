package com.infilos.spring.track.service;

import com.infilos.spring.track.MockRecords;
import com.infilos.spring.track.api.AuditCriteria;
import com.infilos.spring.track.api.AuditRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AuditRecordJdbcServiceTest {

  @Autowired(required = false)
  private Optional<JdbcTemplate> jdbc;

  @Autowired(required = false)
  private Optional<AuditRecordJdbcService> service;

  @Test
  public void test() {
    assertTrue(jdbc.isPresent());
    assertTrue(service.isPresent());

    int collected = service.get().collect(MockRecords.Mock);
    assertEquals(9, collected);

    List<AuditRecord> records =
        service
            .get()
            .resolve(
                AuditCriteria.builder()
                    .app("app-3")
                    .org("org-3")
                    .role("role-3")
                    .user("user-3")
                    .action("act-3")
                    .succed(true)
                    .tag("tag-3", "value-3")
                    .after(MockRecords.HeadDay)
                    .before(MockRecords.MiddleDay)
                    .orderByTimeDesc()
                    .take(10)
                    .offset(1)
                    .build());

    assertEquals(1, records.size());
    assertEquals(MockRecords.Mock.get(6).toString(), records.get(0).toString());
  }
}

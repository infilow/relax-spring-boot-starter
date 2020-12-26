package com.infilos.spring.track.service;

import com.infilos.spring.track.MockRecords;
import com.infilos.spring.track.api.AuditCriteria;
import com.infilos.spring.track.api.AuditRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AuditRecordMemServiceTest {

  @Autowired(required = false)
  private Optional<AuditRecordMemService> serviceOptional;

  @Test
  public void test() {
    assertTrue(serviceOptional.isPresent());
    AuditRecordMemService service = serviceOptional.get();

    assertEquals(0, service.collect(Collections.emptyList()));

    int recorded = service.collect(MockRecords.Mock);
    assertEquals(9, recorded);
    assertEquals(9, service.records.size());

    service.collect(MockRecords.Mock);
    assertEquals(9, service.records.size());

    // String app, String org, String role, String user, String act, Map<String, Object>
    // tags,LocalDateTime time, Boolean succed
    assertEquals(3, service.resolve(AuditCriteria.builder().app("app-1").build()).size());
    assertEquals(3, service.resolve(AuditCriteria.builder().org("org-1").build()).size());
    assertEquals(3, service.resolve(AuditCriteria.builder().role("role-1").build()).size());
    assertEquals(3, service.resolve(AuditCriteria.builder().user("user-1").build()).size());
    assertEquals(3, service.resolve(AuditCriteria.builder().action("act-1").build()).size());

    assertEquals(6, service.resolve(AuditCriteria.builder().succed(true).build()).size());
    assertEquals(3, service.resolve(AuditCriteria.builder().succed(false).build()).size());

    assertEquals(
        3, service.resolve(AuditCriteria.builder().tag("tag-1", "value-1").build()).size());

    assertEquals(
        9, service.resolve(AuditCriteria.builder().after(MockRecords.HeadDay).build()).size());
    assertEquals(
        6, service.resolve(AuditCriteria.builder().after(MockRecords.MiddleDay).build()).size());
    assertEquals(
        3, service.resolve(AuditCriteria.builder().after(MockRecords.LastDay).build()).size());
    assertEquals(
        6,
        service
            .resolve(
                AuditCriteria.builder()
                    .after(MockRecords.MiddleDay)
                    .before(MockRecords.LastDay)
                    .build())
            .size());
    assertEquals(
        6,
        service
            .resolve(
                AuditCriteria.builder().between(MockRecords.MiddleDay, MockRecords.LastDay).build())
            .size());

    assertEquals(3, service.resolve(AuditCriteria.builder().take(3).build()).size());
    assertEquals(3, service.resolve(AuditCriteria.builder().take(3).offset(3).build()).size());
    assertEquals(3, service.resolve(AuditCriteria.builder().take(3).offset(6).build()).size());
    assertEquals(1, service.resolve(AuditCriteria.builder().take(3).offset(8).build()).size());
    assertEquals(0, service.resolve(AuditCriteria.builder().take(3).offset(9).build()).size());
    assertEquals(0, service.resolve(AuditCriteria.builder().take(3).offset(10).build()).size());

    List<AuditRecord> records1 =
        service.resolve(AuditCriteria.builder().app("app-1").orderByTimeAsc().build());
    assertEquals(MockRecords.HeadDay, records1.get(0).time());
    assertEquals(MockRecords.MiddleDay, records1.get(1).time());
    assertEquals(MockRecords.LastDay, records1.get(2).time());

    List<AuditRecord> records2 =
        service.resolve(AuditCriteria.builder().app("app-1").orderByTimeDesc().build());
    assertEquals(MockRecords.HeadDay, records2.get(2).time());
    assertEquals(MockRecords.MiddleDay, records2.get(1).time());
    assertEquals(MockRecords.LastDay, records2.get(0).time());

    List<AuditRecord> records3 =
        service.resolve(
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
                .take(1)
                .offset(1)
                .build());

    assertEquals(MockRecords.Mock.get(6), records3.get(0));
  }
}

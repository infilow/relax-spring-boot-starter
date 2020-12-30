package com.infilos.spring.track.api;

import java.util.List;

public interface AuditService {

    int collect(List<AuditRecord> records);

    List<AuditRecord> resolve(AuditCriteria criteria);
}

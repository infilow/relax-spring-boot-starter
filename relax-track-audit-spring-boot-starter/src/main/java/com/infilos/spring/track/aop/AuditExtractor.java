package com.infilos.spring.track.aop;

import com.infilos.spring.track.api.Audit;
import com.infilos.spring.track.api.AuditAttri;

public interface AuditExtractor<CTX extends AuditContext> {

  Audit from();

  AuditAttribute extract(AuditAttri attri, CTX context);
}

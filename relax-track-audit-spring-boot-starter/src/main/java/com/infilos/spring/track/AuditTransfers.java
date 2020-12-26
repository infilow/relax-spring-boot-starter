package com.infilos.spring.track;

import com.infilos.spring.track.api.AuditTransfer;
import com.infilos.spring.track.config.SpringContextConfigure;

import java.util.Optional;

public final class AuditTransfers {
  private AuditTransfers() {}

  @SuppressWarnings("unchecked")
  public static <T extends AuditTransfer> Optional<AuditTransfer> of(Class<T> transferClass) {
    return (Optional<AuditTransfer>) SpringContextConfigure.tryInject(transferClass);
  }
}

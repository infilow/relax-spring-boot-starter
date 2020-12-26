package com.infilos.spring.track.api;

public interface AuditTransfer {

  AuditRecord transform(AuditRecord record);

  /** Defualt value placeholder. */
  class NoopTransfer implements AuditTransfer {

    @Override
    public AuditRecord transform(AuditRecord record) {
      return record;
    }
  }
}

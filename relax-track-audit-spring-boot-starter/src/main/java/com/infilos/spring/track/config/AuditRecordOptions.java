package com.infilos.spring.track.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("track.record.audit")
public class AuditRecordOptions {
  private boolean enableMem = false;
  private int maxMemSize = 5000;
  private boolean enableJdbc = false;

  public boolean isEnableMem() {
    return enableMem;
  }

  public void setEnableMem(boolean enableMem) {
    this.enableMem = enableMem;
  }

  public int getMaxMemSize() {
    return maxMemSize;
  }

  public void setMaxMemSize(int size) {
    this.maxMemSize = size;
  }

  public boolean isEnableJdbc() {
    return enableJdbc;
  }

  public void setEnableJdbc(boolean enableJdbc) {
    this.enableJdbc = enableJdbc;
  }
}

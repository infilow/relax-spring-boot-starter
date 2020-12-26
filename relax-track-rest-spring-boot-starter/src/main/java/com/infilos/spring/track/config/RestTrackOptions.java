package com.infilos.spring.track.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("track.record.rest")
public class RestTrackOptions {
  private String ignorePatterns = null;
  private Boolean includeHeaders = false;

  public String getIgnorePatterns() {
    return ignorePatterns;
  }

  public void setIgnorePatterns(String ignorePatterns) {
    this.ignorePatterns = ignorePatterns;
  }

  public boolean isIncludeHeaders() {
    return includeHeaders;
  }

  public void setIncludeHeaders(boolean includeHeaders) {
    this.includeHeaders = includeHeaders;
  }
}

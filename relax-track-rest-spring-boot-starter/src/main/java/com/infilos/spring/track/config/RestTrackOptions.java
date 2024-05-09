package com.infilos.spring.track.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties("track.record.rest")
public class RestTrackOptions {
    private List<String> excludePatterns = new ArrayList<>();
    private Boolean includeHeaders = false;
    private Boolean includeBodies = false;

    public List<String> getExcludePatterns() {
        return excludePatterns;
    }

    public void setExcludePatterns(List<String> excludePatterns) {
        this.excludePatterns = excludePatterns;
    }

    public boolean getIncludeHeaders() {
        return includeHeaders;
    }

    public void setIncludeHeaders(boolean includeHeaders) {
        this.includeHeaders = includeHeaders;
    }

    public void setIncludeBodies(Boolean includeBodies) {
        this.includeBodies = includeBodies;
    }

    public Boolean getIncludeBodies() {
        return includeBodies;
    }
}

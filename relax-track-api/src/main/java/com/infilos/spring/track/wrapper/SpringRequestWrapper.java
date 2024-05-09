package com.infilos.spring.track.wrapper;

import org.apache.commons.io.IOUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Json body will be read from bytes, form body will be read from params.
 */
public class SpringRequestWrapper extends ContentCachingRequestWrapper {
    private byte[] cachedBytes;

    public SpringRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        super.getParameterMap();
        // form
        this.cachedBytes = super.getContentAsByteArray();
        // json
        if (cachedBytes.length == 0) {
            cachedBytes = IOUtils.toByteArray(super.getInputStream());
        }
    }

    public static SpringRequestWrapper create(HttpServletRequest request) throws IOException {
        if (request instanceof SpringRequestWrapper) {
            return (SpringRequestWrapper) request;
        }
        
        return new SpringRequestWrapper(request);
    }

    @Override
    public ServletInputStream getInputStream() {
        return new InputStreamWrapper(cachedBytes);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    public Map<String, String> getAllHeaders() {
        final Map<String, String> headers = new HashMap<>();
        Collections.list(getHeaderNames()).forEach(it -> headers.put(it, getHeader(it)));
        return headers;
    }

    public String getBodyString() throws IOException {
        return IOUtils.toString(getInputStream(), getCharacterEncoding());
    }
}

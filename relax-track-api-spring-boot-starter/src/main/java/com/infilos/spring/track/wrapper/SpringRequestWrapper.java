package com.infilos.spring.track.wrapper;

import org.apache.commons.io.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

public class SpringRequestWrapper extends HttpServletRequestWrapper {
  private byte[] body;

  public SpringRequestWrapper(HttpServletRequest request) {
    super(request);
    try {
      body = IOUtils.toByteArray(request.getInputStream());
    } catch (IOException ex) {
      body = new byte[0];
    }
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    return new ServletInputStream() {
      final ByteArrayInputStream bytes = new ByteArrayInputStream(body);

      @Override
      public boolean isFinished() {
        return false;
      }

      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setReadListener(ReadListener listener) {}

      @Override
      public int read() throws IOException {
        return bytes.read();
      }
    };
  }

  public Map<String, String> getAllHeaders() {
    final Map<String, String> headers = new HashMap<>();
    Collections.list(getHeaderNames()).forEach(it -> headers.put(it, getHeader(it)));
    return headers;
  }
}

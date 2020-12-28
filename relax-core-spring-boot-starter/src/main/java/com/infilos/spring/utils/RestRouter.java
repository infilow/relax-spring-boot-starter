package com.infilos.spring.utils;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.util.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.util.*;

public final class RestRouter {

  private HttpServletRequest request;
  private HttpServletResponse respond;
  private String patchHost;
  private String patchPath;
  private final Map<String, String> patchHeaders = new HashMap<>();
  private Logger logger;

  private RestRouter() {}

  public static RestRouter of(HttpServletRequest request, HttpServletResponse respond) {
    RestRouter router = new RestRouter();
    router.request = request;
    router.respond = respond;
    return router;
  }

  public RestRouter routingTo(String host) {
    this.patchHost = host;
    return this;
  }

  public RestRouter withPath(String path) {
    this.patchPath = path;
    return this;
  }

  public RestRouter withHeader(String header, String value) {
    this.patchHeaders.put(header, value);
    return this;
  }
  
  public RestRouter withLogger(Logger logger) {
    this.logger = logger;
    return this;
  }

  public void send() throws IOException {
    if (request == null || respond == null || patchHost == null) {
      throw new IllegalArgumentException("Route request/respond/host must not null.");
    }

    URI uri = buildUri(request, patchHost, patchPath);
    HttpHeaders headers = buildHeaders(request, patchHeaders);
    HttpMethod method = HttpMethod.valueOf(request.getMethod());
    byte[] body = buildHttpBody(method, request);
    HttpEntity<?> httpEntity = buildHttpEntity(body, request, headers);

    sendRoutingRequest(method, respond, uri, httpEntity);
  }

  private URI buildUri(HttpServletRequest request, String targetHost, String targetPath) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(targetHost);
    
    if(targetPath != null) {
      builder.path(targetPath);
    } else {
      builder.path(request.getRequestURI());
    }
    
    return builder.query(request.getQueryString())
        .build(true)
        .toUri();
  }

  private byte[] buildHttpBody(HttpMethod method, HttpServletRequest request) throws IOException {
    if (HttpMethod.POST == method && !(request instanceof MultipartHttpServletRequest)) {
      return StreamUtils.copyToByteArray(request.getInputStream());
    }

    return null;
  }

  private HttpHeaders buildHeaders(HttpServletRequest request, Map<String, String> patchHeaders) {
    HttpHeaders headers = new HttpHeaders();
    Enumeration<String> headerNames = request.getHeaderNames();

    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      headers.set(headerName, request.getHeader(headerName));
    }
    
    for(Map.Entry<String,String> entry : patchHeaders.entrySet()) {
      headers.set(entry.getKey(), entry.getValue());
    }

    return headers;
  }

  private HttpEntity<?> buildHttpEntity(
      @RequestBody(required = false) byte[] body, HttpServletRequest request, HttpHeaders headers)
      throws IOException {
    HttpEntity<?> httpEntity;

    if (request instanceof MultipartHttpServletRequest) {
      httpEntity = buildHttpEntityFromMultipartRequest(request, headers);
    } else {
      httpEntity = new HttpEntity<>(body, headers);
    }

    return httpEntity;
  }

  private HttpEntity<MultiValueMap<String, Object>> buildHttpEntityFromMultipartRequest(
      HttpServletRequest request, HttpHeaders headers) throws IOException {
    MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
    Iterator<String> itr = multipartRequest.getFileNames();
    String fileName = itr.next();
    MultipartFile file = multipartRequest.getFile(fileName);

    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
    parts.add(
        fileName,
        new MultipartInputStreamFileResource(
            new ByteArrayInputStream(file.getBytes()), file.getOriginalFilename()));

    ArrayList<String> requestParameters = Collections.list(multipartRequest.getParameterNames());
    for (String requestParameter : requestParameters) {
      parts.add(requestParameter, multipartRequest.getParameter(requestParameter));
    }

    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    return new HttpEntity<>(parts, headers);
  }

  private void sendRoutingRequest(
      HttpMethod method,
      HttpServletResponse servletResponse,
      URI uri,
      HttpEntity<?> httpEntity)
      throws IOException {
    RestTemplate restTemplate = new RestTemplate();
    try {
      if(logger != null) {
        logger.info("Routing dispatch: " + uri.toString());
      }

      ResponseEntity<byte[]> exchange =
          restTemplate.exchange(uri, method, httpEntity, byte[].class);

      int responseStatusCode = exchange.getStatusCodeValue();
      byte[] responseBody = exchange.getBody();
      HttpHeaders responseHeaders = exchange.getHeaders();

      if(logger != null) {
        logger.info("Routing succed: {}, {}", uri.toURL(), responseStatusCode);
      }

      buildRoutingResponse(servletResponse, responseBody, responseHeaders, responseStatusCode);

    } catch (HttpStatusCodeException e) {
      int responseStatusCode = e.getRawStatusCode();
      byte[] responseBody = e.getResponseBodyAsByteArray();
      HttpHeaders resonseHeaders = e.getResponseHeaders();

      if(logger != null) {
        logger.info("Routing failed: {}, {}", uri.toURL(), responseStatusCode);
      }

      buildRoutingResponse(servletResponse, responseBody, resonseHeaders, responseStatusCode);
    }
  }

  private void buildRoutingResponse(
      HttpServletResponse response, byte[] bodyString, HttpHeaders headers, int statusCode)
      throws IOException {
    if (headers != null) {
      headers.forEach(
          (headerName, value) -> {
            if (shouldCopyResponseHeader(headerName)) {
              response.addHeader(headerName, String.join(",", value));
            }
          });
    }

    response.setStatus(statusCode);
    if (bodyString != null) {
      IOUtils.copy(new ByteArrayInputStream(bodyString), response.getOutputStream());
    }
  }

  private boolean shouldCopyResponseHeader(String headerName) {
    return headerName != null && !"Transfer-Encoding".equals(headerName);
  }

  private static class MultipartInputStreamFileResource extends InputStreamResource {

    private final String filename;

    MultipartInputStreamFileResource(InputStream inputStream, String filename) {
      super(inputStream);
      this.filename = filename;
    }

    @Override
    public String getFilename() {
      return this.filename;
    }

    @Override
    public long contentLength() throws IOException {
      return -1;
    }
  }
}

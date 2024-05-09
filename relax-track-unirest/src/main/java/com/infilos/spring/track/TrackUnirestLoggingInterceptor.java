package com.infilos.spring.track;

import com.infilos.relax.Json;
import kong.unirest.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
public class TrackUnirestLoggingInterceptor implements Interceptor {
    private final int maxBodySize;

    public TrackUnirestLoggingInterceptor(int maxBodySize) {
        this.maxBodySize = maxBodySize;
    }

    public TrackUnirestLoggingInterceptor() {
        this.maxBodySize = 500;
    }

    @Override
    public void onResponse(HttpResponse<?> response, HttpRequestSummary request, Config config) {
        StringBuilder builder = new StringBuilder();
        builder.append("HTTPInvokeLogging: \n");
        builder.append("ExecuteRequest==>: ");
        buildRequest(builder, request);
        builder.append("\n");
        builder.append("AcceptResponse<==: ");
        buildResponse(builder, response);

        log.info(builder.toString());
    }

    @Override
    public HttpResponse<?> onFail(Exception e, HttpRequestSummary request, Config config) throws UnirestException {
        return Interceptor.super.onFail(e, request, config);
    }

    private void buildRequest(StringBuilder builder, HttpRequestSummary request) {        
        // remove verbos headers
        List<String> requestLines = Arrays.asList(request.asString().split(System.lineSeparator()));
        int bodyIdx = -1;
        for (int idx = 0; idx < requestLines.size(); idx++) {
            if ("===================================".equals(requestLines.get(idx))) {
                bodyIdx = idx + 1;
            }
        }
        // add method/url
        builder.append(requestLines.get(0)).append(" ");
        if (bodyIdx > 0 && bodyIdx < requestLines.size()) {
            // add body if present
            builder.append(cutoffString(requestLines.get(bodyIdx), maxBodySize));
        }
    }

    private void buildResponse(StringBuilder builder, HttpResponse<?> response) {
        builder.append(response.getStatus());
        if (response.isSuccess() && Objects.nonNull(response.getBody())) {
            if (Objects.isNull(response.getBody())) {
                builder.append(", Succed, Body is null");
            } else {
                builder.append(", Succed, ").append(formatBody(response.getBody()));
            }
        } else {
            builder.append(", Failed");
            response.getParsingError()
                .ifPresent(e -> builder.append(String.format(", ResponeBodeParseFailed(%s)", messageOf(e))));
        }
    }

    private String formatBody(Object body) {
        try {
            return cutoffString(Json.from(body).asString(), maxBodySize);
        } catch (Exception e) {
            return String.format("(FormatResponseBodyFailed: %s)", messageOf(e));
        }
    }

    private String cutoffString(String string, int maxlength) {
        if (!StringUtils.hasText(string)) {
            return string;
        }

        if (string.length() > maxlength) {
            return string.substring(0, maxlength) + "...";
        }

        return string;
    }

    private String messageOf(Throwable e) {
        if (Objects.isNull(e)) {
            return "";
        }
        if (!StringUtils.hasText(e.getMessage())) {
            return e.getClass().getName();
        } else {
            return String.format("%s(%s)", e.getClass().getSimpleName(), e.getMessage());
        }
    }
}

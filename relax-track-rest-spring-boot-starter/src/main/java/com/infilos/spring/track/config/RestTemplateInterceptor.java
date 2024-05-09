package com.infilos.spring.track.config;

import com.infilos.spring.track.api.Consts;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String reqid = MDC.get(Consts.ReqidHeader);
        String corrid = MDC.get(Consts.CoridHeader);

        if (reqid != null) {
            request.getHeaders().add(Consts.ReqidHeader, reqid);
        }
        if (corrid != null) {
            request.getHeaders().add(Consts.CoridHeader, corrid);
        }

        return execution.execute(request, body);
    }
}

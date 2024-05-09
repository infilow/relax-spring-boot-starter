package com.infilos.spring.config;

import com.infilos.relax.Json;
import com.infilos.spring.track.TrackUnirestLoggingInterceptor;
import com.infilos.spring.track.TrackUnirestMDCInterceptor;
import com.infilos.spring.track.TrackUnirestMetricLogger;
import kong.unirest.*;
import kong.unirest.apache.ApacheClient;
import kong.unirest.jackson.JacksonObjectMapper;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;

@Configuration
public class GlobalUnirestConfigure implements InitializingBean {

    private static final int TIMEOUT = 30 * 1000;

    @Override
    public void afterPropertiesSet() throws Exception {
        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy() {
            public boolean isTrusted(X509Certificate[] chain, String authType) {
                return true;
            }
        }).build();

        HttpClient customHttpClient = HttpClients.custom()
            .setMaxConnTotal(30)
            .setMaxConnPerRoute(30)
            .setSSLContext(sslContext)
            .setSSLHostnameVerifier(new NoopHostnameVerifier())
            .build();
        Unirest.config()
            .verifySsl(false)
            .httpClient(ApacheClient.builder(customHttpClient))
            .socketTimeout(TIMEOUT)
            .connectTimeout(TIMEOUT)
            .followRedirects(false)
            .setDefaultHeader("Content-Type", "application/json")
            .setDefaultHeader("Accept", "application/json")
            .setObjectMapper(new JacksonObjectMapper(Json.underMapper()))
            .interceptor(new TrackUnirestMDCInterceptor())
            .interceptor(new TrackUnirestLoggingInterceptor())
            .instrumentWith(new TrackUnirestMetricLogger());
    }
}

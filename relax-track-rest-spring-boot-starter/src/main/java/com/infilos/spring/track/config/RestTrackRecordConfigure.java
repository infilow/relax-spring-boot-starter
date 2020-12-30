package com.infilos.spring.track.config;

import com.infilos.spring.track.utils.UuidGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
public class RestTrackRecordConfigure {

    @Autowired
    private RestTrackOptions options;

    @Autowired
    private ApplicationContext context;

    @Autowired(required = false)
    private Optional<RestTemplate> template;

    @Bean
    public UuidGenerator generator() {
        return new UuidGenerator();
    }

    @Bean
    public RestTrackRecodFilter filter() {
        return new RestTrackRecodFilter(context, options, generator());
    }

    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate restTemplate() {
        RestTemplate template = new RestTemplate();
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new RestTemplateInterceptor());
        template.setInterceptors(interceptors);
        return template;
    }

    @PostConstruct
    public void interceptRestTemplate() {
        template.ifPresent(
            rest -> {
                List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
                interceptors.add(new RestTemplateInterceptor());
                rest.setInterceptors(interceptors);
            });
    }
}

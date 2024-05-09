package com.infilos.spring.service;

import com.infilos.relax.Json;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.Future;

@Slf4j
@Service
public class DemoAsyncService {
    
    @Async
    public Future<Object> processAsync(Object object) {
        log.info("Process async: {}", object);
        
        return new AsyncResult<>(object);
    }
    
    public void invokeHttpApi() {
        HttpResponse<String> response = Unirest.get("https://httpbin.org/headers").asString();
        if(response.isSuccess() && StringUtils.isNotBlank(response.getBody())) {
            log.info("invoke http api: {}", response.getBody());
        }
    }
}

package com.infilos.spring.rest;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class URIMatchingTest {

    @Test
    public void test() {
        List<String> uris = Arrays.asList("/enum/person", "/error/entity/notfound", "/error/param/invalid", "/swagger-ui/index.html", "/v3/api-docs/swagger-config", "/v3/api-docs", "/_stcore/host-config", "/_stcore/health");
        List<String> patterns = Arrays.asList("^/swagger-ui.*", "^/v3/api-docs.*", "^/_stcore.*", "^/enum.*", "^/error/param.*");
        
        for(String uri : uris) {
            if(patterns.stream().anyMatch(uri::matches)) {
                System.out.println("MATCH: uri: "+uri);
            } else {
                System.out.println("fail: uri: "+uri);
            }
        }
    }
}

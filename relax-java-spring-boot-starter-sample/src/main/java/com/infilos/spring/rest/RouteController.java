package com.infilos.spring.rest;

import com.infilos.spring.RestRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class RouteController {

    private static final Logger logger = LoggerFactory.getLogger(RouteController.class);

    @GetMapping("/route/source")
    public void routingSource(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        RestRouter.of(request, response)
            .withHost("http://localhost:8080")
            .withHeader("key", "value")
            .withPath("/route/target")
            .withLogger(logger)
            .execute();
    }

    @GetMapping("/route/target")
    public String routingTarget(@RequestHeader("key") String key) {
        return "Respond from route target, with header " + key + ".";
    }
}

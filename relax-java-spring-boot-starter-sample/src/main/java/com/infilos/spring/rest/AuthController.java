package com.infilos.spring.rest;

import com.infilos.spring.AuthctxController;
import com.infilos.spring.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController extends AuthctxController<User> {

    @GetMapping("/hello/authed")
    public String authed() {
        return String.format("Hello, %s!", authUser().getName());
    }
}

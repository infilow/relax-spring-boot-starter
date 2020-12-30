package com.infilos.spring.rest;

import com.infilos.spring.UserAuthentic;
import com.infilos.spring.authctx.AuthctxHolder;
import com.infilos.spring.track.api.Audit;
import com.infilos.spring.track.api.AuditAction;
import com.infilos.spring.track.api.AuditAttri;
import com.infilos.spring.track.api.AuditOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@AuditOption(log = true)
public class HelloController {

    @Autowired
    private AuthctxHolder authctx;

    @AuditAction(
        user = @AuditAttri(from = Audit.Constant, value = "user"),
        action = @AuditAttri(from = Audit.ReqPath))
    @GetMapping("/hello/{user}")
    public String hello(@PathVariable String user) {
        return String.format("Hello, %s!", user);
    }

    @UserAuthentic
    @GetMapping("/hello/man")
    public String authen() {
        Optional<String> realName = authctx.getValue("user-name");
        Optional<String> mockName = authctx.getMock("user-name");
        if (realName.isPresent()) {
            return String.format("Hello man, are you %s?", realName.get());
        }
        if (mockName.isPresent()) {
            return String.format("Hello man, I will treat you as %s.", mockName.get());
        }

        return "Hello man, I donnot known who you are.";
    }
}

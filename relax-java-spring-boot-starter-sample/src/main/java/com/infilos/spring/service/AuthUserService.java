package com.infilos.spring.service;

import com.infilos.spring.AuthctxService;
import com.infilos.spring.model.User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthUserService implements AuthctxService<User> {

    @Override
    public Optional<User> findUser(Map<String, String> realAttributes) {
        if (realAttributes.containsKey("user-id") && realAttributes.get("user-id").equals("2233")) {
            return Optional.of(new User("2233", "bernard"));
        }
        if (realAttributes.containsKey("user-name") && realAttributes.get("user-name").equals("bernard")) {
            return Optional.of(new User("2233", "bernard"));
        }

        return Optional.empty();
    }

    @Override
    public Optional<User> buildUser(Map<String, String> mockAttributes) {
        if (mockAttributes.containsKey("user-id") && mockAttributes.containsKey("user-name")) {
            return Optional.of(new User(mockAttributes.get("user-id"), mockAttributes.get("user-name")));
        }

        return Optional.empty();
    }
}

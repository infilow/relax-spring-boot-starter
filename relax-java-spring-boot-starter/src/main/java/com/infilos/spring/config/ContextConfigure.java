package com.infilos.spring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Objects;

@Component
public class ContextConfigure {

    private static ApplicationContext staticContext;

    @Autowired
    private ApplicationContext thisContext;

    public static boolean isSpringRunning() {
        return Objects.nonNull(staticContext);
    }

    public static ApplicationContext context() {
        if (!isSpringRunning()) {
            throw new UnsupportedOperationException("Spring context is unavailable!");
        }

        return staticContext;
    }

    @SuppressWarnings("unchecked")
    public static <T> T inject(String beanName) {
        return (T) staticContext.getBean(beanName);
    }

    public static <T> T inject(Class<T> beanClass) {
        return staticContext.getBean(beanClass);
    }

    @PostConstruct
    public void construct() {
        staticContext = thisContext;
    }
}

package com.infilos.spring.track.config;

import org.springframework.beans.BeansException;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
public class SpringContextConfigure implements ApplicationContextAware {
    private static ApplicationContext context;

    public static ApplicationContext context() {
        if (context == null) {
            throw new IllegalStateException("Spring context is unavailable.");
        }

        return context;
    }

    public static String name() {
        return context().getId();
    }

    public static Optional<String> artifact() {
        try {
            return Optional.ofNullable(inject(BuildProperties.class).getArtifact());
        } catch (Exception ignore) {
            return Optional.empty();
        }
    }

    public static String artifactOrName() {
        return artifact().orElse(name());
    }

    public static <T> T inject(Class<T> clazz) {
        return context().getBean(clazz);
    }

    public static <T> List<T> injectAll(Class<T> clazz) {
        return new ArrayList<>(context().getBeansOfType(clazz).values());
    }

    @SuppressWarnings("unchecked")
    public static <T> T inject(String name) {
        return (T) context().getBean(name);
    }

    public static <T> Optional<T> tryInject(Class<T> clazz) {
        try {
            return Optional.of(context().getBean(clazz));
        } catch (Throwable ex) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> tryInject(String name) {
        try {
            return Optional.of((T) context().getBean(name));
        } catch (Throwable ex) {
            return Optional.empty();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}

package com.infilos.spring.utils;

import com.fasterxml.jackson.annotation.JsonValue;

public interface NameBasedEnum<E extends Enum<E>> {

    @JsonValue
    String name();

    static <T extends NameBasedEnum<E>, E extends Enum<E>> T fromName(Class<T> type, String name) {
        for (T t : type.getEnumConstants()) {
            if (t.name().equals(name)) {
                return t;
            }
        }

        throw new IllegalArgumentException(String.format("%s cannot match the name: %s", type.getSimpleName(), name));
    }
}

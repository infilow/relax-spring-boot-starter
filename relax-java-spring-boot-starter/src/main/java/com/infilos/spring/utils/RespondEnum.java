package com.infilos.spring.utils;

import com.fasterxml.jackson.annotation.JsonValue;

public interface RespondEnum<E extends Enum<E>> extends CodeBasedEnum<E> {

    @JsonValue
    String getMessage();
}

package com.infilos.spring.model;

import com.infilos.spring.utils.CodeBasedEnum;

public enum Gender implements CodeBasedEnum<Gender> {
    MALE(1),
    FEMALE(0);

    private final int code;

    Gender(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

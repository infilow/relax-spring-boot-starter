package com.infilos.spring.utils;

import org.apache.commons.lang3.StringUtils;

public final class Throws {
    private Throws() {
    }

    public static String asError(Throwable e) {
        if (e == null) {
            return null;
        }

        String clazz = e.getClass().getName();
        String error = StringUtils.isBlank(e.getMessage()) ? "" : e.getMessage();

        return String.format("%s(%s)", clazz, error);
    }
}

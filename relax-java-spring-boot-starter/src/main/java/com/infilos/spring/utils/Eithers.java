package com.infilos.spring.utils;

import io.vavr.control.Either;

import java.util.Optional;

public final class Eithers {
    private Eithers() {
    }

    public static <L, R> Either<L, R> anyVoid() {
        return Either.right(null);
    }

    /**
     * Dirty use optional as parameter to avoid verbose result construction.
     */
    public static <L, R> Either<L, R> orElse(Optional<R> right, L left) {
        return right.<Either<L, R>>map(Either::right).orElseGet(() -> Either.left(left));
    }
}

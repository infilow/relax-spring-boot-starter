package com.infilos.spring.utils;

import io.vavr.Tuple2;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class Tuples {
    private Tuples() {
    }

    public static <K, V, T extends Tuple2<K, V>> Collector<T, ?, Map<K, V>> toMap() {
        return Collectors.toMap(T::_1, T::_2);
    }
}

package com.infilos.spring.utils;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Optionals<T> {
  private final Optional<T> optional;

  private Optionals(Optional<T> optional) {
    this.optional = optional;
  }

  public static <T> Optionals<T> of(Optional<T> optional) {
    return new Optionals<>(optional);
  }

  public Optionals<T> ifPresent(Consumer<T> c) {
    optional.ifPresent(c);
    return this;
  }

  public Optionals<T> ifNotPresent(Runnable r) {
    if (!optional.isPresent()) {
      r.run();
    }
    return this;
  }

  public Optional<T> orElse(Supplier<Optional<T>> provider) {
    if (optional.isPresent()) {
      return optional;
    } else {
      return provider.get();
    }
  }
}

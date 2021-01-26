package com.github.dfauth.function;

import java.util.function.Function;

public class Identity<T> implements Functor<T, Identity<?>> {

    private final T value;

    public Identity(T value) {
        this.value = value;
    }

    @Override
    public <R> Identity<?> map(Function<T, R> f) {
        final R result = f.apply(value);
        return new Identity<>(result);
    }
}
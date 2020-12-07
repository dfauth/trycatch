package com.github.dfauth.trycatch;

import java.util.Optional;
import java.util.function.Function;

public class Failure<T> implements Try<T> {
    private final Throwable t;

    public Failure(Throwable t) {
        this.t = t;
    }

    @Override
    public final <V> V despatch(DespatchHandler<T, V> handler) {
        return handler.despatch(this);
    }

    @Override
    public <R> Try<R> map(Function<T, R> f) {
        return new com.github.dfauth.trycatch.Failure<>(t);
    }

    @Override
    public <R> Try<R> flatMap(Function<T, Try<R>> f) {
        return map(f).toOptional().orElse(new com.github.dfauth.trycatch.Failure<>(t));
    }

    @Override
    public Optional<T> toOptional() {
        return Optional.empty();
    }

    @Override
    public boolean isFailure() {
        return true;
    }

    @Override
    public boolean isSuccess() {
        return !isFailure();
    }

    public Throwable exception() {
        return this.t;
    }

    public void throwException() throws Throwable {
        throw this.t;
    }
}

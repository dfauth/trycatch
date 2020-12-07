package com.github.dfauth.trycatch;

import java.util.Optional;
import java.util.function.Function;

public class Success<T> implements Try<T> {

    private final T result;

    public Success(T t) {
        result = t;
    }

    @Override
    public final <V> V despatch(DespatchHandler<T,V> handler) {
        return handler.despatch(this);
    }

    @Override
    public <R> Try<R> map(Function<T, R> f) {
        return Try.tryWith(() -> f.apply(result));
    }

    @Override
    public <R> Try<R> flatMap(Function<T, Try<R>> f) {
        return map(f).toOptional().get();
    }

    @Override
    public Optional<T> toOptional() {
        return Optional.ofNullable(result);
    }

    @Override
    public boolean isFailure() {
        return !isSuccess();
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    public T result() {
        return result;
    }
}

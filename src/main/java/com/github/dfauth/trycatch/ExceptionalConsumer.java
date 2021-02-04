package com.github.dfauth.trycatch;

import com.github.dfauth.partial.Unit;

import java.util.function.Consumer;

import static com.github.dfauth.trycatch.TryCatch.tryCatch;

public interface ExceptionalConsumer<T> extends CallableFunction<T, Unit>, Consumer<T> {

    default Unit _apply(T t) throws RuntimeException {
        return Unit.run(() -> accept(t));
    }

    default void accept(T t) {
        tryCatch(() -> _accept(t)); // converts checked exception to runtime
    }

    void _accept(T t) throws Exception;

    static <T,R> ExceptionalConsumer<T> of(CallableFunction<T,R> f) {
        return t -> f._apply(t);
    }
}

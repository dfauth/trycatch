package com.github.dfauth.trycatch;

import java.util.function.Consumer;

import static com.github.dfauth.trycatch.TryCatch.tryCatchIgnore;

public interface ExceptionalConsumer<T> extends CallableFunction<T,Void>, Consumer<T> {

    default Void _apply(T t) {
        tryCatchIgnore(() ->accept(t));
        return null;
    }

    default void accept(T t) {
        tryCatchIgnore(() -> _accept(t));
    }

    void _accept(T t) throws Exception;

    static <T> Consumer<T> toConsumer(ExceptionalConsumer<T> c) {
        return c;
    }
}

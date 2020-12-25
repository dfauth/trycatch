package com.github.dfauth.trycatch;

import java.util.concurrent.Callable;
import java.util.function.Function;

public interface CallableFunction<T,R> extends Function<T, Callable<R>> {

    @Override
    default Callable<R> apply(T t) {
        return () -> _apply(t);
    }

    R _apply(T t) throws Exception;
}

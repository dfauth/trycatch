package com.github.dfauth.trycatch;

import java.util.concurrent.Callable;
import java.util.function.Function;

import static com.github.dfauth.trycatch.TryCatch.tryCatch;

public interface CallableFunction<T,R> extends Function<T, Callable<R>> {

    @Override
    default Callable<R> apply(T t) {
        return () -> _apply(t);
    }

    R _apply(T t) throws Exception;

    static <T,R> Function<T,R> toFunction(CallableFunction<T,R> f, Function<Throwable,R> g) {
        return _t -> tryCatch(f.apply(_t), g);
    }
}

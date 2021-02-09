package com.github.dfauth.trycatch;

import com.github.dfauth.partial.Unit;

import java.util.concurrent.Callable;
import java.util.function.Function;

import static com.github.dfauth.trycatch.TryCatch.tryCatch;

public interface CallableFunction<T,R> extends Function<T, Callable<R>> {

    @Override
    default Callable<R> apply(T t) {
        return () -> _apply(t);
    }

    R _apply(T t) throws Exception;

    static <T,R> java.util.function.Function<T,R> toFunction(CallableFunction<T,R> f) {
        return t -> tryCatch(f.apply(t));
    }

    interface Consumer<T> extends java.util.function.Consumer<T> {

        default void accept(T t) {
            tryCatch(() -> _accept(t)); // converts checked exception to runtime
        }

        void _accept(T t) throws Exception;

        static <T,R> java.util.function.Consumer<T> toConsumer(CallableFunction<T,R> f) {
            return Unit.Function.of(f);
        }
    }
}

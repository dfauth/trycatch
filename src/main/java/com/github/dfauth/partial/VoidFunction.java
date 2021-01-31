package com.github.dfauth.partial;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public interface VoidFunction<T> extends Function<T,Void>, Consumer<T> {
    default Void apply(T t) {
        accept(t);
        return null;
    }

    default <R> Consumer<R> map(Function<R,T> f) {
        return r -> accept(f.apply(r));
    }

    default <R> Consumer<R> mapConcat(Function<R, Optional<T>> f) {
        return r -> f.apply(r).ifPresent(_t -> accept(_t));
    }

    static <T,R> VoidFunction<T> toVoidFunction(Function<T,R> f) {
        return t -> f.apply(t);
    }

    static <T> VoidFunction<T> toFunction(Consumer<T> c) {
        return t -> c.accept(t);
    }

    static <T> Consumer<T> toConsumer(Function<T,?> f) {
        return t -> f.apply(t);
    }

    static <T> Function<T,Void> toFunction(Runnable r) {
        return toFunction(i -> r.run());
    }

    static <T> UnaryOperator<T> peek(Consumer<T> c) {
        return t -> {
            c.accept(t);
            return t;
        };
    }

    static Void supplyVoid(Runnable r) {
        r.run();
        return null;
    }
}

package com.github.dfauth.partial;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public interface VoidFunction<T> extends Function<T,Void>, Consumer<T> {
    default Void apply(T t) {
        accept(t);
        return null;
    }

    static <T> VoidFunction<T> toFunction(Consumer<T> c) {
        return t -> c.accept(t);
    }

    static <T> UnaryOperator<T> peek(Consumer<T> c) {
        return t -> {
            c.accept(t);
            return t;
        };
    }
}

package com.github.dfauth.function;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Function2<T,U,V> extends BiFunction<T,U,V> {

    default Function<T, Function<U,V>> curried() {
        return t -> u -> apply(t,u);
    }

    default Function<T, Function<U,V>> curriedLeft() {
        return curried();
    }

    default Function<U, Function<T,V>> curriedRight() {
        return flip().curried();
    }

    default Function2<U,T,V> flip() {
        return (u,t) -> apply(t,u);
    }

    static <T,U,V> Function2<T,U,V> asFunction2(BiFunction<T,U,V> f) {
        return (t,u) -> f.apply(t,u);
    }
}

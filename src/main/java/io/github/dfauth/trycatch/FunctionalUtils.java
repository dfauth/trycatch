package io.github.dfauth.trycatch;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.function.*;

import static io.github.dfauth.trycatch.ExceptionalRunnable.tryCatch;

@Slf4j
public class FunctionalUtils {

    public static <T> Function<T,T> peek(Consumer<T> c) {
        return t -> {
            c.accept(t);
            return t;
        };
    }

    public static <T> Function<T,T> peek(Supplier<T> s) {
        return ignored -> s.get();
    }

    public static <T> Function<T,T> peek(Runnable runnable) {
        return ignored -> {
            runnable.run();
            return ignored;
        };
    }

    public static <T,R> Function<T,R> supply(Supplier<R> supplier) {
        return t -> supplier.get();
    }

    public static <T,R> Function<T,Function<R,Boolean>> curry(BiPredicate<T,R> p2) {
        return curry((BiFunction<T, R, Boolean>) p2::test);
    }

    public static <T,R,S> Function<T,Function<R,S>> curry(BiFunction<T,R,S> f2) {
        return t -> r -> f2.apply(t,r);
    }
    public static <T,R,S> Function<T,Function<R,S>> leftCurry(BiFunction<T,R,S> f2) {
        return curry(f2);
    }
    public static <T,R,S> Function<R,Function<T,S>> rightCurry(BiFunction<T,R,S> f2) {
        return r -> t -> f2.apply(t,r);
    }

    public static <T> Function<T,Void> adapt(Consumer<T> c) {
        return t -> {
            c.accept(t);
            return null;
        };
    }

    public static <T,R> Consumer<T> adapt(Function<T,R> f) {
        return f::apply;
    }

    public static <T,R> Function<T,R> adapt(R r) {
        return t -> r;
    }

    public static <T> Supplier<T> adapt(Callable<T> callable) {
        return () -> tryCatch(callable);
    }
}

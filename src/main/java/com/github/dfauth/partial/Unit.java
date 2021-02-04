package com.github.dfauth.partial;

import com.github.dfauth.trycatch.ExceptionalRunnable;

import java.util.function.Consumer;

public enum Unit {
    UNIT;

    public static Unit run(ExceptionalRunnable r) throws RuntimeException {
        r.run();
        return UNIT;
    }

    public interface Function<I> extends java.util.function.Function<I,Unit>, Consumer<I> {
        @Override
        default Unit apply(I i) {
            accept(i);
            return UNIT;
        }

        static <I> Function<I> of(Runnable r) {
            return i -> r.run();
        }

        static <I,O> Function<I> of(java.util.function.Function<I, O> f) {
            return i -> f.apply(i);
        }

        static <I> Function<I> of(Consumer<I> c) {
            return i -> c.accept(i);
        }

    }
}

package com.github.dfauth.trycatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.dfauth.trycatch.ThrowableHandler.noOp;

public class TryCatch {

    private static final Logger logger = LoggerFactory.getLogger(TryCatch.class);

    public static <T> ThrowableHandler<T> propagationHandler() {
        return t -> {
            throw new RuntimeException(t);
        };
    }
    private static Runnable noOpFinalRunnable = () -> {};

    public static void tryCatch(ExceptionalRunnable r) {
        tryCatch(r, propagationHandler(), noOpFinalRunnable);
    }

    public static <T> T tryCatch(Callable<T> c) {
        return tryCatch(c, propagationHandler(), noOpFinalRunnable);
    }

    public static <T> T tryCatch(Callable<T> c, ThrowableHandler<T> handler) {
        return tryCatch(c, handler, noOpFinalRunnable);
    }

    public static <T> T tryCatch(Callable<T> c, ThrowableHandler<T> handler, Runnable finalRunnable) {
        try {
            return c.call();
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            return handler.apply(t);
        } finally {
            finalRunnable.run();
        }
    }

    public static void tryCatchIgnore(ExceptionalRunnable r) {
        tryCatch(r, noOp(), noOpFinalRunnable);
    }

    public static <T> T tryCatchIgnore(Callable<T> c, T defaultValueOfT) {
        return tryCatch(c, t -> defaultValueOfT, noOpFinalRunnable);
    }

    public static <I> Consumer<I> withExceptionLogging(Consumer<I> c) {
        return i -> tryCatch(() -> c.accept(i));
    }

    public static <T> Callable<T> ignorantCallable(Callable<T> c, T defaultValue) {
        return () -> tryCatchIgnore(() -> c.call(), defaultValue);
    }

    public static <I> Consumer<I> ignorantConsumer(Consumer<I> c) {
        return i -> tryCatchIgnore(() -> c.accept(i));
    }

    public static Runnable ignorantRunnable(ExceptionalRunnable r) {
        return () -> tryCatchIgnore(r);
    }

    public static <I,O> Function<I,O> ignorantCallableFunction(CallableFunction<I,O> f, O defaultValue) {
        return i -> tryCatchIgnore(f.apply(i), defaultValue);
    }

    public static <T> Consumer<T> withExceptionLogging(ExceptionalConsumer<T> c) {
        return i -> tryCatch(() -> c.accept(i));
    }

    public static <I,O> Function<I,O> withExceptionLogging(CallableFunction<I,O> f) {
        return i -> tryCatch(f.apply(i));
    }

    public static Runnable withExceptionLogging(ExceptionalRunnable r) {
        return () -> tryCatch(r);
    }

    public static <T> Callable<T> withExceptionLogging(Callable<T> c) {
        return () -> tryCatch(c);
    }
}

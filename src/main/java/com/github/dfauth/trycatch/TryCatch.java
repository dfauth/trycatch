package com.github.dfauth.trycatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static com.github.dfauth.function.Function2.asPredicate;
import static com.github.dfauth.partial.Unit.Function.peek;
import static com.github.dfauth.trycatch.ThrowableHandlers.consume;
import static com.github.dfauth.trycatch.ThrowableHandlers.noOp;

public class TryCatch {

    private static final Logger logger = LoggerFactory.getLogger(TryCatch.class);

    public static UnaryOperator<Throwable> loggingOperator = peek(t -> logger.error(t.getMessage(), t));

    public static Function<Throwable,Void> propagationHandler = t -> {
            throw new RuntimeException(t);
    };

    public static Function<Throwable,Boolean> alwaysTrue = defaultValueOf(true);

    public static Function<Throwable,Boolean> alwaysFalse = defaultValueOf(false);

    public static <T> Function<Throwable,T> defaultValueOf(T t) {
        return always(t);
    }

    public static <T> Predicate<T> alwaysTrue() {
        return asPredicate(always(true));
    }

    public static <T> Predicate<T> alwaysFalse() {
        return asPredicate(always(false));
    }

    public static <T,R> Function<T,R> always(R r) {
        return ignored -> r;
    }

    public static <T> Function<Throwable,T> propagationHandler() {
        return  t -> {
            throw new RuntimeException(t);
        };
    }

    public static Runnable noOpFinalRunnable = () -> {};

    public static void tryCatch(ExceptionalRunnable r) {
        tryCatch(r, loggingOperator.andThen(propagationHandler), noOpFinalRunnable);
    }

    public static <T> T tryCatch(Callable<T> c) {
        return tryCatch(c, loggingOperator.andThen(propagationHandler()), noOpFinalRunnable);
    }

    public static <T> T tryCatch(Callable<T> c, Function<Throwable,T> handler) {
        return tryCatch(c, loggingOperator.andThen(handler), noOpFinalRunnable);
    }

    public static <T> T tryCatch(Callable<T> c, Function<Throwable,T> handler, Runnable finalRunnable) {
        try {
            return c.call();
        } catch (Throwable t) {
            return handler.apply(t);
        } finally {
            finalRunnable.run();
        }
    }

    public static void tryCatchIgnore(ExceptionalRunnable r) {
        tryCatch(r, loggingOperator.andThen(noOp()), noOpFinalRunnable);
    }

    public static void tryCatchIgnore(ExceptionalRunnable r, Consumer<Throwable> c) {
        tryCatch(r, loggingOperator.andThen(consume(c)), noOpFinalRunnable);
    }

    public static <T> T tryCatchIgnore(Callable<T> c, T defaultValueOfT) {
        return tryCatch(c, loggingOperator.andThen(t -> defaultValueOfT), noOpFinalRunnable);
    }

    public static void tryCatchSilentlyIgnore(ExceptionalRunnable r) {
        tryCatch(r, noOp(), noOpFinalRunnable);
    }

    public static void tryCatchSilentlyIgnore(ExceptionalRunnable r, Consumer<Throwable> c) {
        tryCatch(r, consume(c), noOpFinalRunnable);
    }

    public static <T> T tryCatchSilentlyIgnore(Callable<T> c, T defaultValueOfT) {
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

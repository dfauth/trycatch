package com.github.dfauth.trycatch;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.dfauth.trycatch.FunctionalUtils.adapt;
import static com.github.dfauth.trycatch.FunctionalUtils.peek;
import static com.github.dfauth.trycatch.Try.tryWith;

public interface ExceptionalRunnable extends Callable<Void>, Runnable {


    static Runnable toRunnable(ExceptionalRunnable runnable) {
        return () -> {
            tryCatchRunnable(() -> runnable._run(),log.andThen(adapt(propagate())));
        };
    }

    Executor runInCallingThread = Runnable::run;
    Consumer<Throwable> ignore = t -> {};
    Consumer<Throwable> log = t -> Logger.log.error(t.getMessage(),t);

    @Slf4j enum Logger {}

    static <T> Function<Throwable, T> propagate() {
        return e -> {
            Logger.log.error(e.getMessage(), e);
            if(e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        };
    }

    static Runnable noOp() {
        return () -> {};
    }

    static void tryCatchRunnable(ExceptionalRunnable callable) {
        tryCatch(callable);
    }

    static void tryCatchIgnore(ExceptionalRunnable callable) {
        tryCatch(callable, peek(log).andThen(adapt(ignore)));
    }

    static void tryCatchRunnable(ExceptionalRunnable callable, Consumer<Throwable> handler) {
        tryCatch(callable, adapt(handler));
    }

    static <T> T tryCatchSupplier(Supplier<T> supplier) {
        return tryCatch(supplier::get);
    }

    static <T> T tryCatchIgnore(Callable<T> callable, T defaultValue) {
        return tryCatch(callable, adapt(defaultValue));
    }

    static <T> T tryCatch(Callable<T> callable) {
        return tryCatch(callable, propagate());
    }

    static <T> T tryCatch(Callable<T> callable, Function<Throwable, T> exceptionHandler) {
        return tryCatch(callable, exceptionHandler, noOp());
    }

    static <T> T tryCatch(Callable<T> callable, Function<Throwable, T> exceptionHandler, Runnable finallyRunnable) {
        return tryWith(callable).recover(exceptionHandler).map(peek(finallyRunnable)).getValue();
    }

    static <T> T tryFinally(Supplier<T> supplier, Runnable runnable) {
        return tryWith(supplier::get).map(peek(runnable)).getValue();
    }

    @Override
    default Void call() throws Exception {
        _run();
        return null;
    }

    default void run() {
        tryCatchRunnable(this);
    }

    void _run() throws Exception;
}

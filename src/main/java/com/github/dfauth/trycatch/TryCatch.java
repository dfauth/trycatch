package com.github.dfauth.trycatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class TryCatch {

    private static final Logger logger = LoggerFactory.getLogger(TryCatch.class);

    public static <T> ThrowableHandler<T> propagationHandler() {
        return t -> {
            throw new RuntimeException(t);
        };
    }
    private static Runnable noOpFinalRunnable = () -> {};

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

    public static void tryCatch(ExceptionalRunnable runnable) {
        tryCatch(runnable, t -> {
            throw new RuntimeException(t);
        }, noOpFinalRunnable);
    }

    public static void tryCatchIgnore(ExceptionalRunnable runnable) {
        tryCatch(runnable, t -> {}, noOpFinalRunnable);
    }

    public static void tryCatch(ExceptionalRunnable runnable, Consumer<Throwable> handler) {
        tryCatch(runnable, handler, noOpFinalRunnable);
    }

    public static void tryCatch(ExceptionalRunnable runnable, Consumer<Throwable> handler, Runnable finalRunnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            handler.accept(t);
        } finally {
            finalRunnable.run();
        }
    }

    ;
}

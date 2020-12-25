package com.github.dfauth.trycatch;

import java.util.concurrent.Callable;

import static com.github.dfauth.trycatch.TryCatch.tryCatch;

@FunctionalInterface
public interface ExceptionalRunnable extends Callable<Void>, Runnable {

    default Void call() throws Exception {
        _run();
        return null;
    }

    default void run() {
        tryCatch(() -> _run());
    }

    void _run() throws Exception;

    static Runnable toRunnable(ExceptionalRunnable r) {
        return r;
    }
}

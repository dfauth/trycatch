package com.github.dfauth.trycatch;

import com.github.dfauth.partial.Unit;

import java.util.concurrent.Callable;

import static com.github.dfauth.partial.Unit.UNIT;
import static com.github.dfauth.trycatch.TryCatch.tryCatch;

@FunctionalInterface
public interface ExceptionalRunnable extends Callable<Unit>, Runnable {

    default Unit call() throws Exception {
        _run();
        return UNIT;
    }

    default void run() {
        tryCatch(() -> _run());
    }

    void _run() throws Exception;
}

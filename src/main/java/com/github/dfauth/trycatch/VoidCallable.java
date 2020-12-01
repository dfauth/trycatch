package com.github.dfauth.trycatch;

import java.util.concurrent.Callable;

@FunctionalInterface
public interface VoidCallable extends Callable<Void>, Runnable {

    default Void call() {
        run();
        return null;
    }

    void run();
}

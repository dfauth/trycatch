package com.github.dfauth.trycatch;

@FunctionalInterface
public interface ExceptionalRunnable {
    void run() throws Exception;
}

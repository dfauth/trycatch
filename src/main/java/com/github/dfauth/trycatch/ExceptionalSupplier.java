package com.github.dfauth.trycatch;

@FunctionalInterface
public interface ExceptionalSupplier<T> {
    T get() throws Throwable;
}

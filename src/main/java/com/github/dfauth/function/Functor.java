package com.github.dfauth.function;

import java.util.function.Function;

@FunctionalInterface
public interface Functor<T, F extends Functor<?,?>> {

    <R> F map(Function<T,R> f);
}
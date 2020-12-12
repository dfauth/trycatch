package com.github.dfauth.partial;

import java.util.function.Function;

import static com.github.dfauth.trycatch.TryCatch.tryCatch;

public class PartialFunctions {

    public static <T,R extends T> PartialFunction<T,R> downcast(Function<T,R> f) {
        return PartialFunction.fromPredicateAndFunction(i -> tryCatch(() -> {
            f.apply(i);
            return true;
        }, t -> false), f);
    }
}

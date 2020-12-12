package com.github.dfauth.partial;

import com.github.dfauth.trycatch.Failure;
import com.github.dfauth.trycatch.Success;
import com.github.dfauth.trycatch.Try;

import java.util.function.Function;

import static com.github.dfauth.trycatch.TryCatch.tryCatch;

public class PartialFunctions {

    public static <T,R extends T> PartialFunction<T,R> downcast(Function<T,R> f) {
        return PartialFunction.fromPredicateAndFunction(i -> tryCatch(() -> {
            f.apply(i);
            return true;
        }, t -> false), f);
    }

    public static <T,R extends T> PartialFunction<Try<T>, Success<T>> isSuccessOf(Class<T> classOfT) {
        return PartialFunction.fromPredicateAndFunction((Try<T> t) -> t.isSuccess(), (Try<T> t) -> t.toSuccess());
    }

    public static <T,R extends T> PartialFunction<Try<T>, Failure<T>> isFailureOf(Class<T> classOfT) {
        return PartialFunction.fromPredicateAndFunction((Try<T> t) -> t.isFailure(), (Try<T> t) -> t.toFailure());
    }
}

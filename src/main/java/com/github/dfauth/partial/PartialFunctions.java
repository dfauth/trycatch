package com.github.dfauth.partial;

import com.github.dfauth.trycatch.Failure;
import com.github.dfauth.trycatch.Success;
import com.github.dfauth.trycatch.Try;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dfauth.partial.PartialConsumer.fromPredicateAndConsumer;
import static com.github.dfauth.trycatch.TryCatch.tryCatch;

public class PartialFunctions {

    static <T,R> PartialFunction<T,T> fromPredicate(Predicate<T> p) {
        return fromPredicateAndFunction(p, Function.identity());
    }

    static <T,R> PartialFunction<T,R> fromFunction(Function<T,R> f) {
        return fromPredicateAndFunction(x -> true, f);
    }

    static <T,R> PartialFunction<T,R> fromPredicateAndFunction(Predicate<T> p, Function<T,R> f) {
        return new PartialFunction<>() {
            @Override
            public boolean isDefinedAt(T t) {
                return p.test(t);
            }

            @Override
            public R _apply(T t) {
                return f.apply(t);
            }
        };
    }

    static <T,R> Function<T, Optional<R>> asFunction(Predicate<T> p, Function<T,R> f) {
        return fromPredicateAndFunction(p,f).lift();
    }

    public static <I,O> PartialFunction<I,O> _case(PartialFunction<I, O> pf) {
        return pf;
    }

    public static <I,O> PartialFunction<I,I> _case(Predicate<I> p) {
        return fromPredicate(p);
    }

    public static <I,O> PartialFunction<I,O> _case(Predicate<I> p, Function<I,O> f) {
        return fromPredicateAndFunction(p,f);
    }

    public static <I,O> PartialFunction<I, Unit> _case(Predicate<I> p, Consumer<I> c) {
        return fromPredicateAndConsumer(p,c);
    }

    public static <T,R extends T> PartialFunction<T,R> downcast(Function<T,R> f) {
        return fromPredicateAndFunction(i -> tryCatch(() -> {
            f.apply(i);
            return true;
        }, t -> false), f);
    }

    public static <T,R extends T> PartialFunction<Try<T>, Success<T>> isSuccessOf(Class<T> classOfT) {
        return fromPredicateAndFunction((Try<T> t) -> t.isSuccess(), (Try<T> t) -> t.toSuccess());
    }

    public static <T,R extends T> PartialFunction<Try<T>, Failure<T>> isFailureOf(Class<T> classOfT) {
        return fromPredicateAndFunction((Try<T> t) -> t.isFailure(), (Try<T> t) -> t.toFailure());
    }
}

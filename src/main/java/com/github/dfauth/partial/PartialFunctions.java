package com.github.dfauth.partial;

import com.github.dfauth.trycatch.Failure;
import com.github.dfauth.trycatch.Success;
import com.github.dfauth.trycatch.Try;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dfauth.partial.PartialConsumer.fromPredicateAndConsumer;
import static com.github.dfauth.trycatch.TryCatch.*;
import static java.util.function.Function.identity;

public class PartialFunctions {

    static <T,R> PartialFunction<T,T> fromPredicate(Predicate<T> p) {
        return fromPredicateAndFunction(p, identity());
    }

    static <T,R> PartialFunction<T,R> fromFunction(Function<T,R> f) {
        return fromPredicateAndFunction(alwaysTrue(), f);
    }

    static <T,R> PartialFunction<T,R> fromPredicateAndFunction(Predicate<T> p, Function<T,R> f) {
        return new PartialFunction<>() {
            @Override
            public boolean isDefinedAt(T t) {
                return tryCatch(() -> p.test(t), alwaysFalse);
            }

            @Override
            public R _apply(T t) {
                return f.apply(t);
            }
        };
    }

    static <T,R> Function<T, Optional<R>> lift(Predicate<T> p, Function<T,R> f) {
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

    public static <T,R extends T> PartialFunction<T,R> narrow(Function<T,R> f) {
        return narrow(f, alwaysTrue());
    }

    public static <T,R extends T> PartialFunction<T,R> narrow(Function<T,R> f, Predicate<R> p) {
        return fromPredicateAndFunction(t -> tryCatch(() ->
            p.test(f.apply(t)),
            alwaysFalse), f);
    }

    public static <T> PartialFunction<Try<T>, Success<T>> isSuccessOf(Class<T> classOfT) {
        return isSuccessOf(classOfT, alwaysTrue());
    }

    public static <T,R> PartialFunction<Try<T>, Success<T>> isSuccessOf(Class<T> classOfT, Predicate<T> p) {
        return fromPredicateAndFunction((Try<T> t) -> t.isSuccess() && p.test(t.toSuccess().result()), (Try<T> t) -> t.toSuccess());
    }

    public static <T> PartialFunction<Try<T>, Failure<T>> isFailureOf(Class<T> classOfT) {
        return isFailureOf(classOfT, alwaysTrue());
    }

    public static <T> PartialFunction<Try<T>, Failure<T>> isFailureOf(Class<T> classOfT, Predicate<Throwable>  p) {
        return fromPredicateAndFunction((Try<T> t) -> t.isFailure() && p.test(t.toFailure().exception()), (Try<T> t) -> t.toFailure());
    }
}

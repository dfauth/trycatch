package com.github.dfauth.trycatch;

import com.github.dfauth.partial.Unit;
import com.github.dfauth.partial.PartialFunction;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dfauth.trycatch.TryCatch.*;

public interface Try<T> {

    default <R> R onComplete(PartialFunction<Try<T>, R>... partials) {
        return Stream.of(partials).filter(p -> p.isDefinedAt(this)).map(p -> p._apply(this)).findFirst().orElseThrow(() -> new IllegalArgumentException("Not matched"));
    }

    default void onComplete(Function<Try<T>, Unit> f) {
        f.apply(this);
    }

    default Try<T> recover(Function<Throwable,T> f) {
        return this;
    }

    default Try<T> recover(Consumer<Throwable> c) {
        return this;
    }

    <V> V despatch(DespatchHandler<T,V> handler);

    <R> Try<R> map(Function<T,R> f);

    default Try<T> accept(Consumer<T> c) {
        return this;
    }

    <R> Try<R> flatMap(Function<T,Try<R>> f);

    Optional<T> toOptional();

    static <T> Try<T> tryWithCallable(Callable<T> c) {
        return tryCatch(() -> new Success<>(c.call()), loggingOperator.andThen(Failure::new), noOpFinalRunnable);
    }

    static <T> Try<T> trySilentlyWithCallable(Callable<T> c) {
        return tryCatch(() -> new Success<>(c.call()), Failure::new, noOpFinalRunnable);
    }

    static Try<Void> tryWith(ExceptionalRunnable r) {
        return tryCatch(() -> {
            r.run();
            return new Success<>(null);
        }, Failure::new);
    }

    static <T> BiFunction<T,Throwable,Try<T>> tryWith() {
        return (t,e) ->
                t != null ?
                        new Success<>(t) :
                        e != null ?
                                new Failure<>(e) :
                                new Failure<>(new UnsupportedOperationException());
    }

    boolean isFailure();

    boolean isSuccess();

    default Failure<T> toFailure() throws ClassCastException {
        return toFailure(this);
    }

    default Success<T> toSuccess()  throws ClassCastException {
        return toSuccess(this);
    }

    static <T> Failure<T> failure() {
        return failure(new IllegalArgumentException());
    }

    static <T> Failure<T> failure(Throwable t) {
        return new Failure<>(t);
    }

    static <T> Success<T> success(T t) {
        return new Success<>(t);
    }

    @SuppressWarnings("unchecked")
    static <T> Failure<T> toFailure(Try<T> t) {
        return t.despatch(new DespatchHandler<>() {
            @Override
            public Failure<T> despatch(Failure<T> f) {
                return f;
            }

            @Override
            public Failure<T> despatch(Success<T> s) {
                throw new UnsupportedOperationException();
            }
        });
    }

    @SuppressWarnings("unchecked")
    static <T> Success<T> toSuccess(Try<T> t) {
        return t.despatch(new DespatchHandler<>() {
            @Override
            public Success<T> despatch(Failure<T> f) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Success<T> despatch(Success<T> s) {
                return s;
            }
        });
    }
}

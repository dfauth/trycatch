package com.github.dfauth.trycatch;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.dfauth.trycatch.TryCatch.tryCatch;

public interface Try<T> {

    default void onComplete(BiConsumer<T,Throwable> fn) {
        despatch(new DespatchHandler<T, Void>() {
            @Override
            public Void despatch(Failure<T> f) {
                fn.accept(null, f.exception());
                return null;
            }

            @Override
            public Void despatch(Success<T> s) {
                fn.accept(s.result(), null);
                return null;
            }
        });
    }

    <V> V despatch(DespatchHandler<T,V> handler);

    default void onSuccess(Consumer<T> c) {
        toOptional().ifPresent(c);
    }

    default void onFailure(Consumer<Throwable> c) {
        onComplete((t,e) -> Optional.ofNullable(e).ifPresent(c));
    }

    <R> Try<R> map(Function<T,R> f);

    <R> Try<R> flatMap(Function<T,Try<R>> f);

    Optional<T> toOptional();

    static <T> Try<T> tryWith(Callable<T> c) {
        return tryCatch(() -> new Success<>(c.call()), Failure::new);
    }

    boolean isFailure();

    boolean isSuccess();

    default Failure<T> toFailure() throws ClassCastException {
        return toFailure(this);
    }

    default Success<T> toSuccess()  throws ClassCastException {
        return toSuccess(this);
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
                throw new IllegalStateException("Oops");
            }
        });
    }

    @SuppressWarnings("unchecked")
    static <T> Success<T> toSuccess(Try<T> t) {
        return t.despatch(new DespatchHandler<>() {
            @Override
            public Success<T> despatch(Failure<T> f) {
                throw new IllegalStateException("Oops");
            }

            @Override
            public Success<T> despatch(Success<T> s) {
                return s;
            }
        });
    }
}

package com.github.dfauth.trycatch;

import com.github.dfauth.partial.PartialConsumer;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dfauth.trycatch.TryCatch.tryCatch;

public interface Try<T> {

    default void onComplete(PartialConsumer<Try<T>>... partials) {
        Stream.of(partials).filter(p -> p.isDefinedAt(this)).map(p -> {
            p.accept(this);
            return p;
        }).findFirst().orElseThrow(() -> new IllegalArgumentException("Not matched"));
    }

    void recover(Consumer<Throwable> consumer);

    <V> V despatch(DespatchHandler<T,V> handler);

    <R> Try<R> map(Function<T,R> f);

    <R> Try<R> flatMap(Function<T,Try<R>> f);

    Optional<T> toOptional();

    static <T> Try<T> tryWithCallable(Callable<T> c) {
        return tryCatch(() -> new Success<>(c.call()), Failure::new);
    }

    static Try<Void> tryWith(ExceptionalRunnable r) {
        return tryCatch(() -> {
            r.run();
            return new Success<>(null);
        }, Failure::new);
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

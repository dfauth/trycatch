package com.github.dfauth.trycatch;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.dfauth.trycatch.TryCatch.tryCatch;

public interface Try<T> {

    void onComplete(BiConsumer<T,Throwable> handler);

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
        return tryCatch(() -> {
            return new Success<>(c.call());
        }, t -> new Failure<>(t));
    }

    static Try<Void> tryWith(ExceptionalRunnable r) {
        return tryCatch(() -> {
            r.run();
            return new Success<Void>(null);
        }, t -> new Failure<Void>(t));
    }

    boolean isFailure();

    boolean isSuccess();

    class Success<T> implements Try<T> {

        private final T result;

        public Success(T t) {
            result = t;
        }

        @Override
        public void onComplete(BiConsumer<T, Throwable> handler) {
            handler.accept(result, null);
        }

        @Override
        public <R> Try<R> map(Function<T, R> f) {
            return Try.tryWith(() -> f.apply(result));
        }

        @Override
        public <R> Try<R> flatMap(Function<T, Try<R>> f) {
            return map(f).toOptional().get();
        }

        @Override
        public Optional<T> toOptional() {
            return Optional.ofNullable(result);
        }

        @Override
        public boolean isFailure() {
            return !isSuccess();
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        public T result() {
            return result;
        }
    }

    class Failure<T> implements Try<T> {
        private final Throwable t;

        public Failure(Throwable t) {
            this.t = t;
        }

        @Override
        public void onComplete(BiConsumer<T, Throwable> handler) {
            handler.accept(null, t);
        }

        @Override
        public <R> Try<R> map(Function<T, R> f) {
            return new Failure<>(t);
        }

        @Override
        public <R> Try<R> flatMap(Function<T, Try<R>> f) {
            return map(f).toOptional().orElse(new Failure<>(t));
        }

        @Override
        public Optional<T> toOptional() {
            return Optional.empty();
        }

        @Override
        public boolean isFailure() {
            return true;
        }

        @Override
        public boolean isSuccess() {
            return !isFailure();
        }

        public Throwable exception() {
            return this.t;
        }
    }
}

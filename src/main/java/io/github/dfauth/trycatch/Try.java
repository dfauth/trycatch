package io.github.dfauth.trycatch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;

public interface Try<T> {

    static <T> Try<T> tryWith(Callable<T> callable) {
        try {
            return new Success<>(callable.call());
        } catch (Exception e) {
            return new Failure<>(e);
        }
    }

    static Try<Void> tryWithRunnable(ExceptionalRunnable runnable) {
        return tryWith(runnable);
    }

    static <T> Success<T> success(T t) {
        return new Success<>(t);
    }

    static <T> Failure<T> failure(Throwable throwable) {
        return new Failure<>(throwable);
    }

    default Optional<T> toOptional() {
        return recover(ex -> null).map(Optional::ofNullable).getValue();
    }

    T getValue();

    Try<T> recover(Function<Throwable, T> f);

    <R> Try<R> map(Function<T,R> f);

    <R> Try<R> flatMap(Function<T,Try<R>> f);

    default boolean isSuccess() {
        return false;
    }

    default boolean isFailure() {
        return !isSuccess();
    }

    Success<T> toSuccess();

    Failure<T> toFailure();

    @Data
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    class Success<T> implements Try<T> {

        private final T value;

        @Override
        public Optional<T> toOptional() {
            return Optional.ofNullable(value);
        }

        @Override
        public Try<T> recover(Function<Throwable, T> f) {
            return this;
        }

        @Override
        public <R> Try<R> map(Function<T, R> f) {
            return tryWith(() -> f.apply(value));
        }

        @Override
        public <R> Try<R> flatMap(Function<T, Try<R>> f) {
            return f.apply(value);
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public Success<T> toSuccess() {
            return this;
        }

        @Override
        public Failure<T> toFailure() {
            throw new IllegalStateException("Cannot convert an instance of "+this.getClass()+" to a Failure<T>");
        }
    }

    @Data
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    class Failure<T> implements Try<T> {

        private final Throwable throwable;

        @Override
        public T getValue() {
            return ExceptionalRunnable.<T>propagate().apply(throwable);
        }

        @Override
        public Try<T> recover(Function<Throwable, T> f) {
            return new Success<>(f.apply(throwable));
        }

        @Override
        public <R> Try<R> map(Function<T, R> f) {
            return new Failure<>(throwable);
        }

        @Override
        public <R> Try<R> flatMap(Function<T, Try<R>> f) {
            return new Failure<>(throwable);
        }

        @Override
        public Success<T> toSuccess() {
            throw new IllegalStateException("Cannot convert an instance of "+this.getClass()+" to a Success<T> use recover instead");
        }

        @Override
        public Failure<T> toFailure() {
            return this;
        }
    }
}

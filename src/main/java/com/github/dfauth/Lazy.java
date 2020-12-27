package com.github.dfauth;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class Lazy<T> implements Supplier<T> {

    private transient Supplier<T> supplier;
    private volatile T t;

    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy(supplier);
    }

    public Lazy(Supplier<T> supplier) {
        this.supplier = requireNonNull(supplier);
    }

    public T get() {
        if(t == null) {
            synchronized (this) {
                if(t == null) {
                    t = requireNonNull(supplier.get());
                    supplier = null;
                }
            }
        }
        return t;
    }

    public <R> Lazy<R> map(Function<T, R> f) {
        return Lazy.of(() -> f.apply(this.get()));
    }

    public <R> Lazy<R> flatMap(Function<T, Lazy<R>> f) {
        return Lazy.of(() -> f.apply(this.get()).get());
    }

    public Lazy<Optional<T>> filter(Predicate<T> p) {
        return Lazy.of(() -> Optional.of(get()).filter(p));
    }}

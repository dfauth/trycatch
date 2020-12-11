package com.github.dfauth.partial;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface PartialFunction<I,O> extends Function<I,O>, Predicate<I> {

    default <T extends I> boolean isDefinedAt(T i) {
        return test(i);
    }

    default <U> PartialFunction<I, U> thenMap(Function<O, U> f) {
        return and(fromFunction(f));
    }

    default <U> PartialFunction<I, U> thenMap(PartialFunction<O,U> pf) {
        return and(pf);
    }

    default PartialFunction<I, O> andIf(Predicate<O> p) {
        return and(fromPredicate(p));
    }

    default <U> PartialFunction<I, U> andIf(PartialFunction<O,U> pf) {
        return and(pf);
    }

    default <V> PartialFunction<I, V> and(PartialFunction<O,V> pf) {
        return new PartialFunction<I, V>() {
            @Override
            public V apply(I i) {
                return Optional.ofNullable(i)
                        .filter(PartialFunction.this)
                        .map(PartialFunction.this)
                        .filter(pf)
                        .map(pf)
                        .orElseThrow(() -> new IllegalStateException("Oops. shouldnt happen"));
            }

            @Override
            public boolean test(I i) {
                return Optional.ofNullable(i)
                        .filter(PartialFunction.this)
                        .map(PartialFunction.this)
                        .filter(pf)
                        .isPresent();
            }
        };
    }

    default <V> PartialFunction<I, O> or(PartialFunction<I,O>... partials) {
        return fromPredicateAndFunction(
                i -> Stream.of(partials).filter(p -> p.test(i)).findFirst().isPresent(),
                i -> Stream.of(partials).filter(p -> p.test(i)).map(p -> p.apply(i)).findFirst().orElseThrow(() -> new IllegalStateException("No match"))
        );
    }

    default Tuple2<Predicate<I>, Function<I,O>> decompose() {
        return new Tuple2<>(){
            @Override
            public Predicate<I> _1() {
                return i -> PartialFunction.this.test(i);
            }

            @Override
            public Function<I,O> _2() {
                return i -> PartialFunction.this.apply(i);
            }
        };
    }

    default PartialConsumer<I> thenAccept(Consumer<O> c) {
        Tuple2<Predicate<I>, Function<I, O>> tuple = decompose();
        return new PartialConsumer<I>() {
            @Override
            public void accept(I i) {
                c.accept(tuple._2().apply(i));
            }

            @Override
            public boolean test(I i) {
                return tuple._1().test(i);
            }
        };
    }

    static <T> PartialFunction<T, T> identity() {
        return fromPredicateAndFunction(x -> true, Function.identity());
    }

    static <T,R> PartialFunction<T,T> fromPredicate(Predicate<T> p) {
        return fromPredicateAndFunction(p, Function.identity());
    }

    static <T,R> PartialFunction<T,R> fromFunction(Function<T,R> f) {
        return fromPredicateAndFunction(x -> true, f);
    }

    static <T,R> PartialFunction<T,R> fromPredicateAndFunction(Predicate<T> p, Function<T,R> f) {
        return new PartialFunction<T,R>() {
            @Override
            public boolean test(T t) {
                return p.test(t);
            }

            @Override
            public R apply(T t) {
                return f.apply(t);
            }
        };
    }
}

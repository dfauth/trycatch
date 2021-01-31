package com.github.dfauth.partial;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.dfauth.partial.PartialConsumer.fromPredicateAndConsumer;
import static com.github.dfauth.partial.VoidFunction.*;

public interface PartialFunction<I,O> extends Predicate<I> {

    O apply(I i);

    default Function<I,Optional<O>> asFunction() {
        return i -> test(i) ? Optional.ofNullable(apply(i)) : Optional.empty();
    }

    default <T extends I> boolean isDefinedAt(T t) {
        return test(t);
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
                        .map(_i -> PartialFunction.this.apply(_i))
                        .filter(pf)
                        .map(_i -> pf.apply(_i))
                        .orElseThrow(() -> new IllegalStateException("Oops. shouldnt happen"));
            }

            @Override
            public boolean test(I i) {
                return Optional.ofNullable(i)
                        .filter(PartialFunction.this)
                        .map(_i -> PartialFunction.this.apply(_i))
                        .filter(pf)
                        .isPresent();
            }
        };
    }

    default <V> PartialFunction<I, O> _or(PartialFunction<I,O>... partials) {
        Supplier<Stream<PartialFunction<I, O>>> s = () -> Stream.concat(Stream.of(this), Stream.of(partials));
        return fromPredicateAndFunction(
                i -> s.get().filter(p -> p.test(i)).findFirst().isPresent(),
                i -> s.get().filter(p -> p.test(i)).map(p -> p.apply(i)).findFirst().orElseThrow(() -> new IllegalStateException("No match"))
        );
    }

    default PartialFunction<I, O> or(Predicate<I> p, Function<I,O> f) {
        return _or(fromPredicateAndFunction(p, f));
    }

    default PartialFunction<I, O> _case(Predicate<I> p, Function<I,O> f) {
        return or(p, f);
    }

    default PartialFunction<I, Void> or(Predicate<I> p, Consumer<I> c) {
        return fromPredicateAndConsumer(
                this.or(p),
                toConsumer(i -> test(i) ? apply(i) : p.test(i) ? toFunction(c) : null)
        );
    }

    default PartialFunction<I, Void> _case(Predicate<I> p, Consumer<I> c) {
        return fromPredicateAndConsumer(this, i -> apply(i))._or(fromPredicateAndConsumer(p,c));
    }

    default Function<I,O> orDefault(O o) {
        return i -> asFunction().apply(i).orElse(o);
    }

    default Function<I,O> _otherwise(O o) {
        return orDefault(o);
    }

    default Function<I,O> _otherwise(Supplier<O> s) {
        return orGet(s);
    }

    default Function<I,Void> _otherwise(Runnable r) {
        return orGet(r);
    }

    default Function<I,O> orGet(Supplier<O> s) {
        return i -> asFunction().apply(i).orElseGet(s);
    }

    default Function<I,Void> orGet(Runnable r) {
            return i -> test(i) ? supplyVoid(() -> apply(i)) : toFunction(r).apply(i);
    }

    default Tuple2<Predicate<I>, Function<I,O>> decompose() {
        return Tuple2.of(i -> PartialFunction.this.test(i),i -> PartialFunction.this.apply(i));
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

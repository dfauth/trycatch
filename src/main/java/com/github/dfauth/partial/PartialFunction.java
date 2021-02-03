package com.github.dfauth.partial;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.dfauth.partial.PartialFunctions.fromPredicateAndFunction;
import static com.github.dfauth.partial.Unit.UNIT;
import static com.github.dfauth.partial.Unit.run;

public interface PartialFunction<I,O> {

    static <I,O> PartialFunction<I,O> of(Predicate<I> p, Function<I,O> f) {
        return fromPredicateAndFunction(p,f);
    }

    boolean isDefinedAt(I t);

    default O apply(I i) {
        return Optional.ofNullable(i).filter(asPredicate()).map(this::_apply).orElseThrow(() -> new IllegalArgumentException("partial function is not defined for input "+i));
    }

    O _apply(I i);

    default Predicate<I> asPredicate() {
        return i -> isDefinedAt(i);
    }

    default Function<I,Optional<O>> lift() {
        return i -> isDefinedAt(i) ? Optional.ofNullable(apply(i)) : Optional.empty();
    }

    default <U> PartialFunction<I, U> andThen(Function<O, U> f) {
        return of(asPredicate(), i -> f.apply(_apply(i)));
    }

    default <U> PartialFunction<I, U> andThen(PartialFunction<O, U> f) {
        return new PartialFunction<I, U>() {
            @Override
            public boolean isDefinedAt(I i) {
                return PartialFunction.this.isDefinedAt(i) ? f.isDefinedAt(PartialFunction.this._apply(i)) : false;
            }

            @Override
            public U _apply(I i) {
                return f.apply(PartialFunction.this._apply(i));
            }
        };
    }

    default <U> PartialFunction<I, Unit> andThen(Consumer<O> c) {
        return of(asPredicate(), i -> run(() -> c.accept(_apply(i))));
    }

    /*
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
            public V _apply(I i) {
                return Optional.ofNullable(i)
                        .filter(PartialFunction.this.asPredicate())
                        .map(_i -> PartialFunction.this._apply(_i))
                        .filter(pf.asPredicate())
                        .map(_i -> pf.apply(_i))
                        .orElseThrow(() -> new IllegalStateException("Oops. shouldnt happen"));
            }

            @Override
            public boolean isDefinedAt(I i) {
                return Optional.ofNullable(i)
                        .filter(PartialFunction.this.asPredicate())
                        .map(_i -> PartialFunction.this._apply(_i))
                        .filter(pf.asPredicate())
                        .isPresent();
            }
        };
    }
*/

    default <V> PartialFunction<I, O> _or(PartialFunction<I,O>... partials) {
        Supplier<Stream<PartialFunction<I, O>>> s = () -> Stream.concat(Stream.of(this), Stream.of(partials));
        return fromPredicateAndFunction(
                i -> s.get().filter(p -> p.isDefinedAt(i)).findFirst().isPresent(),
                i -> s.get().filter(p -> p.isDefinedAt(i)).map(p -> p.apply(i)).findFirst().orElseThrow(() -> new IllegalStateException("No match"))
        );
    }

    default PartialFunction<I, O> _or(Predicate<I> p, Function<I,O> f) {
        return _or(fromPredicateAndFunction(p, f));
    }

    default PartialFunction<I, O> _case(Predicate<I> p, Function<I,O> f) {
        return _or(p, f);
    }

    default PartialFunction<I, Unit> _or(Predicate<I> p, Consumer<I> c) {
        return fromPredicateAndFunction(
                asPredicate().or(p),
                i -> isDefinedAt(i) ? run(() -> apply(i)) : p.test(i) ? run(() -> c.accept(i)) : UNIT
        );
    }

    default PartialFunction<I, Unit> _case(Predicate<I> p, Consumer<I> c) {
        return this._or(p, c);
    }

    default Function<I,O> orDefault(O o) {
        return i -> lift().apply(i).orElse(o);
    }

    default Function<I,O> _otherwise(O o) {
        return orDefault(o);
    }

    default Function<I,O> _otherwise(Supplier<O> s) {
        return orGet(s);
    }

    default Function<I, Unit> _otherwise(Runnable r) {
        return orGet(r);
    }

    default Function<I,O> orGet(Supplier<O> s) {
        return i -> lift().apply(i).orElseGet(s);
    }

    default Function<I, Unit> orGet(Runnable r) {
            return i -> isDefinedAt(i) ? run(() -> apply(i)) : Unit.Function.of(r).apply(i);
    }

    default Tuple2<Predicate<I>, Function<I,O>> decompose() {
        return Tuple2.of(i -> PartialFunction.this.isDefinedAt(i), i -> PartialFunction.this.apply(i));
    }

    static <T> PartialFunction<T, T> identity() {
        return fromPredicateAndFunction(x -> true, Function.identity());
    }

}

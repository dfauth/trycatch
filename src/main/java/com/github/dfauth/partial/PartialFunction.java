package com.github.dfauth.partial;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.dfauth.partial.PartialFunctions.fromPredicateAndFunction;
import static com.github.dfauth.partial.Unit.UNIT;

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
        return of(asPredicate(), Unit.Function.of(i -> {
            c.accept(_apply(i));
        }));
    }

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
        return _case(of(p, f));
    }

    default PartialFunction<I, O> _case(PartialFunction<I,O> f) {
        return _or(f);
    }

    default PartialFunction<I, Unit> _or(Predicate<I> p, Consumer<I> c) {
        return fromPredicateAndFunction(
                asPredicate().or(p),
                i -> isDefinedAt(i) ? Unit.of(() -> apply(i)) : p.test(i) ? Unit.Function.of(c).apply(i) : UNIT
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
            return i -> isDefinedAt(i) ? Unit.of(() -> apply(i)) : Unit.of(() -> r.run());
    }

    default Tuple2<Predicate<I>, Function<I,O>> decompose() {
        return Tuple2.of(i -> PartialFunction.this.isDefinedAt(i), i -> PartialFunction.this.apply(i));
    }

    static <T> PartialFunction<T, T> identity() {
        return fromPredicateAndFunction(x -> true, Function.identity());
    }

}

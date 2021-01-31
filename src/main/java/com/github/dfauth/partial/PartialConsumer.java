package com.github.dfauth.partial;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.dfauth.partial.VoidFunction.peek;

public interface PartialConsumer<I> extends PartialFunction<I,Void>, Consumer<I> {

    default <T extends I> boolean isDefinedAt(T i) {
        return test(i);
    }

    @Override
    default Void apply(I i) {
        accept(i);
        return null;
    }

    default void accept(I i) {
    }

    default PartialConsumer<I> _otherwise(Consumer<I> c) {
        return PartialConsumer.compose(this, fromPredicateAndConsumer(
                i -> !PartialConsumer.this.test(i),
                c
        ));
    }

    static <I> PartialConsumer<I> fromPredicate(Predicate<I> p) {
        return fromPredicateAndConsumer(p, i -> {});
    }

    static <I> PartialConsumer<I> fromConsumer(Consumer<I> c) {
        return fromPredicateAndConsumer(i -> true, c);
    }

    static <I> PartialConsumer<I> fromPredicateAndConsumer(Predicate<I> p, Consumer<I> c) {
        return new PartialConsumer<I>() {
            @Override
            public void accept(I i) {
                c.accept(i);
            }

            @Override
            public boolean test(I i) {
                return p.test(i);
            }
        };
    }

    static <I> PartialConsumer<I> compose(PartialConsumer<I>... partials) {
        return fromPredicateAndConsumer(
                i -> Stream.of(partials).filter(p -> p.test(i)).findFirst().isPresent(),
                i -> {
                    Stream.of(partials).filter(p -> p.test(i)).findFirst().ifPresent(p -> p.accept(i));
                }
            );
    }

    default PartialConsumer<I> _or(PartialConsumer<I>... partials) {
        Supplier<Stream<PartialConsumer<I>>> s = () -> Stream.concat(Stream.of(this), Stream.of(partials));
        return fromPredicateAndConsumer(
                i -> s.get().filter(p -> p.test(i)).findFirst().isPresent(),
                i -> s.get().filter(p -> p.test(i)).map(peek(p -> p.accept(i))).findFirst().orElseThrow(() -> new IllegalStateException("No match"))
        );
    }
}

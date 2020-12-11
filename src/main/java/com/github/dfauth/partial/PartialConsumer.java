package com.github.dfauth.partial;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface PartialConsumer<I> extends Predicate<I>, Consumer<I> {

    default <T extends I> boolean isDefinedAt(T i) {
        return test(i);
    }

    default PartialConsumer<I> otherwise(Consumer<I> c) {
        return PartialConsumer.compose(this, fromPredicateAndConsumer(
                i -> !PartialConsumer.this.test(i),
                c
        ));
    }

    static <I> PartialConsumer<I> fromPredicate(Predicate<I> p) {
        return fromPredicateAndConsumer(p, i -> {});
    }

    static <I> PartialConsumer<I> fromConsumer(Predicate<I> p, Consumer<I> c) {
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

    default Tuple2<Predicate<I>, Consumer<I>> decompose() {
        return new Tuple2<>(){
            @Override
            public Predicate<I> _1() {
                return i -> PartialConsumer.this.test(i);
            }

            @Override
            public Consumer<I> _2() {
                return i -> PartialConsumer.this.accept(i);
            }
        };
    }

    default PartialConsumer<I> thenAccept(Consumer<I> c) {
        Tuple2<Predicate<I>, Consumer<I>> tuple = PartialConsumer.this.decompose();
        return fromPredicateAndConsumer(
                tuple._1(),
                i -> {
                    tuple._2().accept(i);
                    c.accept(i);
                }
        );
    }
}

package com.github.dfauth.partial;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.dfauth.partial.Unit.Function.peek;
import static com.github.dfauth.partial.Unit.UNIT;

public interface PartialConsumer<I> extends PartialFunction<I, Unit> {

    @Override
    default Unit _apply(I i) {
        accept(i);
        return UNIT;
    }

    void accept(I i);

    default PartialConsumer<I> _otherwise(Consumer<I> c) {
        return PartialConsumer.compose(this, fromPredicateAndConsumer(
                i -> !PartialConsumer.this.isDefinedAt(i),
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
            public boolean isDefinedAt(I i) {
                return p.test(i);
            }
        };
    }

    static <I> PartialConsumer<I> compose(PartialConsumer<I>... partials) {
        return fromPredicateAndConsumer(
                i -> Stream.of(partials).filter(p -> p.isDefinedAt(i)).findFirst().isPresent(),
                i -> {
                    Stream.of(partials).filter(p -> p.isDefinedAt(i)).findFirst().ifPresent(p -> p.accept(i));
                }
            );
    }

    default PartialConsumer<I> _or(PartialConsumer<I>... partials) {
        Supplier<Stream<PartialConsumer<I>>> s = () -> Stream.concat(Stream.of(this), Stream.of(partials));
        return fromPredicateAndConsumer(
                i -> s.get().filter(p -> p.isDefinedAt(i)).findFirst().isPresent(),
                i -> s.get().filter(p -> p.isDefinedAt(i)).map(peek(p -> p.accept(i))).findFirst().orElseThrow(() -> new IllegalStateException("No match"))
        );
    }
}

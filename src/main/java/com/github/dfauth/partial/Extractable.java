package com.github.dfauth.partial;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Extractable<T extends Tuple> {
    Optional<T> extract();
    static <T extends Tuple> PartialConsumer<Extractable<T>> _case(Predicate<T> p, Consumer<Extractable<T>> c) {
        return PartialConsumer.fromPredicateAndConsumer(m -> m.extract().map(t -> p.test(t)).orElse(false), c);
    }
    static <T1,T2> PartialConsumer<Tuple2<T1,T2>> _case(BiPredicate<T1,T2> p, BiConsumer<T1,T2> c) {
        return PartialConsumer.<Tuple2<T1,T2>>fromPredicateAndConsumer(m -> m.extract().map(t -> t.map((t1,t2) -> p.test(t1,t2))).orElse(false), t -> c.accept(t._1(), t._2()));
    }
}

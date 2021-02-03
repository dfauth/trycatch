package com.github.dfauth.partial;

import java.util.Optional;
import java.util.function.*;

public interface Extractable<T extends Tuple> {

    Optional<T> extract();

    static <T extends Tuple> PartialConsumer<Extractable<T>> _case(Predicate<T> p, Consumer<Extractable<T>> c) {
        return PartialConsumer.fromPredicateAndConsumer(m -> m.extract().map(t -> p.test(t)).orElse(false), c);
    }

    static <T1,T2> PartialFunction<Tuple2<T1,T2>, Unit> _case(BiPredicate<T1,T2> p, BiConsumer<T1,T2> c) {
        return PartialConsumer.fromPredicateAndConsumer(m -> m.extract().map(t -> t.map((t1,t2) -> p.test(t1,t2))).orElse(false), t -> c.accept(t._1(), t._2()));
    }

    static <T1,T2> PartialFunction<Tuple2<T1,T2>, Unit> _otherwise(BiConsumer<T1,T2> c) {
        return PartialConsumer.fromConsumer(t -> c.accept(t._1(), t._2()));
    }

    static <T1,T2,T> PartialFunction<Tuple2<T1,T2>,T> _case(BiPredicate<T1,T2> p, BiFunction<T1,T2,T> f) {
        return PartialFunctions.fromPredicateAndFunction(m -> m.extract().map(t -> t.map((t1,t2) -> p.test(t1,t2))).orElse(false), t -> f.apply(t._1(), t._2()));
    }

    static <T1,T2,T> PartialFunction<Tuple2<T1,T2>,T> _otherwise(BiFunction<T1,T2,T> f) {
        return PartialFunctions.fromFunction(t -> f.apply(t._1(), t._2()));
    }
}

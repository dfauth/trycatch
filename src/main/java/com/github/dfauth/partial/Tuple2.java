package com.github.dfauth.partial;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Tuple2<T1, T2> extends Tuple, Extractable<Tuple2<T1,T2>> {

    static <T1,T2> Optional<Tuple2<T1, T2>> unapply(Tuple2<T1, T2> t) {
        return t.extract();
    }

    static <T1,T2> Tuple2<T1, T2> of(T1 t1, T2 t2) {
        return new Tuple2<>() {
            @Override
            public T1 _1() {
                return t1;
            }

            @Override
            public T2 _2() {
                return t2;
            }
        };
    }

    T1 _1();
    T2 _2();

    default Optional<Tuple2<T1,T2>> extract() {
        return Optional.ofNullable(this);
    }

    default <T> T map(BiFunction<T1,T2,T> f) {
        return f.apply(_1(), _2());
    }

    default <T> T map(Function<T1,Function<T2,T>> f) {
        return f.apply(_1()).apply(_2());
    }

    default Map.Entry<T1,T2> toMapEntry() {
        return new Map.Entry<>(){
            @Override
            public T1 getKey() {
                return _1();
            }

            @Override
            public T2 getValue() {
                return _2();
            }

            @Override
            public T2 setValue(T2 value) {
                throw new UnsupportedOperationException();
            }
        };
    }

}

package com.github.dfauth.partial;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface Matcher<T> {

    static <R> R supply(Supplier<R> s) {
        return s.get();
    }

    T target();

    default <R> Optional<R> matchOpt(PartialFunction<T,R>... cases) {
        return Stream.of(cases).filter(p -> p.isDefinedAt(target())).map(p -> p.apply(target())).findFirst();
    }

    default <R> R match(Function<T, R> f) {
        return f.apply(target());
    }

    default <R> R match(PartialFunction<T, R> _case, R _default) {
        return matchOpt(_case).orElse(_default);
    }

    default <R> R match(PartialFunction<T, R> _case0, PartialFunction<T, R> _case1, R _default) {
        return matchOpt(_case0,_case1).orElse(_default);
    }

    default <R> R match(PartialFunction<T, R> _case0, PartialFunction<T, R> _case1, PartialFunction<T, R> _case2, R _default) {
        return matchOpt(_case0,_case1,_case2).orElse(_default);
    }

    default <R> R match(PartialFunction<T, R> _case0, PartialFunction<T, R> _case1, PartialFunction<T, R> _case2, PartialFunction<T, R> _case3, R _default) {
        return matchOpt(_case0,_case1,_case2,_case3).orElse(_default);
    }

    default <R> R match(PartialFunction<T, R> _case0, PartialFunction<T, R> _case1, PartialFunction<T, R> _case2, PartialFunction<T, R> _case3, PartialFunction<T, R> _case4, R _default) {
        return matchOpt(_case0,_case1,_case2,_case3,_case4).orElse(_default);
    }

    static <T> Matcher<T> matcher(T t) {
        return () -> t;
    }

}

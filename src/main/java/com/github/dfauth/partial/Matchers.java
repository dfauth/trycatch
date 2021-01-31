package com.github.dfauth.partial;

import java.util.Optional;
import java.util.function.Function;

import static com.github.dfauth.partial.Matcher.matcher;

public interface Matchers {

    static <T,R> Function<T,Optional<R>> match(PartialFunction<T,R>... cases) {
        return t -> matcher(t).matchOpt(cases);
    }

    static <T,R> Function<T,R> match(Function<T,Optional<R>> f, R _default) {
        return t -> f.apply(t).orElse(_default);
    }

    static <T,R> Function<T,R> match(PartialFunction<T,R> _case0, R _default) {
        return t -> matcher(t).match(_case0, _default);
    }

    static <T,R> Function<T,R> match(PartialFunction<T,R> _case0, PartialFunction<T,R> _case1, R _default) {
        return t -> matcher(t).match(_case0, _case1, _default);
    }

    static <T,R> Function<T,R> match(PartialFunction<T,R> _case0, PartialFunction<T,R> _case1, PartialFunction<T,R> _case2, R _default) {
        return t -> matcher(t).match(_case0, _case1, _case2, _default);
    }

    static <T,R> Function<T,R> match(PartialFunction<T,R> _case0, PartialFunction<T,R> _case1, PartialFunction<T,R> _case2, PartialFunction<T,R> _case3, R _default) {
        return t -> matcher(t).match(_case0, _case1, _case2, _case3, _default);
    }

    static <T,R> Function<T,R> match(PartialFunction<T,R> _case0, PartialFunction<T,R> _case1, PartialFunction<T,R> _case2, PartialFunction<T,R> _case3, PartialFunction<T,R> _case4, R _default) {
        return t -> matcher(t).match(_case0, _case1, _case2, _case3, _case4, _default);
    }

}

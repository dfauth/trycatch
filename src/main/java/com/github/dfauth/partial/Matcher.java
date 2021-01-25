package com.github.dfauth.partial;

import java.util.Optional;
import java.util.stream.Stream;

public class Matcher<T> {

    private T target;

    public Matcher(T target) {
        this.target = target;
    }

    public static final <T> Matcher<T> match(T t) {
        return new Matcher(t);
    }

    public <R> Optional<R> using(PartialFunction<T,R>... cases) {
        return Stream.of(cases).filter(p -> p.isDefinedAt(this.target)).map(p -> p.apply(this.target)).findFirst();
    }

}

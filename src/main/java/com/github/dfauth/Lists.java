package com.github.dfauth;

import com.github.dfauth.partial.Tuple2;

import java.util.List;
import java.util.Optional;

public class Lists {

    public static final <T> Optional<T> headOption(List<T> l) {
        return Optional.ofNullable(l.size() > 0 ? l.get(0) : null);
    }

    public static final <T> T head(List<T> l) {
        return headOption(l).orElseThrow();
    }

    public static final <T> List<T> tail(List<T> l) {
        return l.subList(1,l.size()-1);
    }

    public static final <T> Tuple2<T,List<T>> partition(List<T> l) {
        return Tuple2.of(head(l), tail(l));
    }

}

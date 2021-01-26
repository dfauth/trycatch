package com.github.dfauth;

import com.github.dfauth.partial.Tuple2;

import java.util.ArrayList;
import java.util.Collections;
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
        return l.size() > 1 ? l.subList(1,l.size()) : Collections.emptyList();
    }

    public static final <T> Tuple2<T,List<T>> segment(List<T> l) {
        return Tuple2.of(head(l), tail(l));
    }

    public static final <T> List<T> reverse(List<T> l) {
        List<T> tmp = new ArrayList<>(l);
        Collections.reverse(tmp);
        return tmp;
    }

}

package com.github.dfauth.trycatch;

public interface DespatchHandler<T, R> {
    R despatch(Failure<T> f);
    R despatch(Success<T> s);
}

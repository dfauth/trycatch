package com.github.dfauth.trycatch;

import com.github.dfauth.partial.Unit;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.github.dfauth.partial.Unit.UNIT;
import static com.github.dfauth.trycatch.TryCatch.tryCatch;

public class AsyncUtil {

    public static CompletableFuture<Unit> executeAsync(ExceptionalRunnable runnable) {
        return executeAsync(() -> {
            tryCatch(runnable, t -> UNIT);
            return null;
        }, Executors.newSingleThreadExecutor());
    }

    public static <T> CompletableFuture<T> executeAsync(Callable<T> callable) {
        return executeAsync(callable, Executors.newSingleThreadExecutor());
    }

    public static <T> CompletableFuture<T> executeAsync(Callable<T> callable, ExecutorService executor) {
        CompletableFuture<T> f = new CompletableFuture<>();
        executor.submit(() -> tryCatch(() -> {
            T result = callable.call();
            f.complete(result);
            return result;
        }, t -> f.completeExceptionally(t)));
        return f;
    }
}

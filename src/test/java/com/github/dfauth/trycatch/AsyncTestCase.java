package com.github.dfauth.trycatch;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.github.dfauth.trycatch.AssertingLogger.*;
import static com.github.dfauth.trycatch.Try.tryWith;
import static com.github.dfauth.trycatch.TryCatch.withExceptionLogging;
import static org.junit.Assert.*;

public class AsyncTestCase {

    private static final Logger logger = LoggerFactory.getLogger(AsyncTestCase.class);
    private RuntimeException runtimeOops = new RuntimeException("Oops");
    private Exception oops = new Exception("Oops");

    @Before
    public void setUp() {
        resetLogEvents();
    }

    @Test
    public void testCallable() throws InterruptedException, ExecutionException, TimeoutException {

        {
            CompletableFuture<Try<Integer>> f = AsyncUtil.executeAsync(() -> 1).thenApply(i -> tryWith(() -> 2/i));
            Try<Integer> result = f.get(1, TimeUnit.SECONDS);
            assertTrue(result.isSuccess());
            assertEquals(2, result.toSuccess().result().intValue());
            assertNothingLogged();
        }

        {
            CompletableFuture<Integer> f = AsyncUtil.executeAsync(() -> 0).thenApply(i -> 2/i);
            assertThrows(ExecutionException.class, () -> f.get(1, TimeUnit.SECONDS));
            assertNothingLogged(); // exception thrown outside of tryCatch
        }

        {
            CompletableFuture<Try<Integer>> f = AsyncUtil.executeAsync(() -> 0).thenApply(i -> tryWith(() -> 2/i));
            Try<Integer> result = f.get(1, TimeUnit.SECONDS);
            assertTrue(result.isFailure());
            assertThrows(ArithmeticException.class, () -> result.toFailure().throwException());
            assertExceptionLogged(new ArithmeticException("/ by zero"));
        }
    }

    @Test
    public void testRunnable() throws InterruptedException, ExecutionException, TimeoutException {

        {
            CompletableFuture<Void> f = AsyncUtil.executeAsync(() -> {});
            Void result = f.get(1, TimeUnit.SECONDS);
            assertNothingLogged();
        }

        {
            AtomicReference<Optional<Integer>> blah = new AtomicReference<>();
            CompletableFuture<Void> f = AsyncUtil.executeAsync(() -> {
                blah.set(Optional.ofNullable(1));
            }).thenAccept(withExceptionLogging((Consumer<Void>) _void -> blah.get().ifPresent(b -> blah.set(Optional.ofNullable(2/b)))));
            assertEquals(2, blah.get().get().intValue());
            assertNothingLogged();
        }

        {
            AtomicReference<Optional<Integer>> blah = new AtomicReference<>();
            CompletableFuture<Void> f = AsyncUtil.executeAsync(() -> {
                blah.set(Optional.ofNullable(0));
            }).thenAccept(withExceptionLogging((Consumer<Void>) _void -> blah.get().ifPresent(b -> blah.set(Optional.ofNullable(2/b)))));
            assertThrows(ExecutionException.class, () -> f.get(1, TimeUnit.SECONDS));
            assertExceptionLogged(new ArithmeticException("/ by zero"));
        }

    }

}

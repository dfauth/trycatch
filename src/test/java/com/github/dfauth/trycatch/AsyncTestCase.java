package com.github.dfauth.trycatch;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.github.dfauth.trycatch.AssertingLogger.*;
import static com.github.dfauth.trycatch.Try.tryWith;
import static org.junit.Assert.*;

public class AsyncTestCase {

    private static final Logger logger = LoggerFactory.getLogger(AsyncTestCase.class);
    private RuntimeException runtimeOops = new RuntimeException("Oops");
    private Exception oops = new Exception("Oops");

    @Test
    public void testTryWithAsync() throws InterruptedException, ExecutionException, TimeoutException {

        resetLogEvents();

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

}

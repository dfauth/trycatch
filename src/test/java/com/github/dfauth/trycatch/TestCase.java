package com.github.dfauth.trycatch;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.*;

import static com.github.dfauth.trycatch.ExceptionalRunnable.*;
import static com.github.dfauth.trycatch.Try.tryWith;
import static org.junit.Assert.*;

@Slf4j
public class TestCase {

    @Test
    public void testTryCatch() {

        // Runnable
        tryCatchRunnable(() -> {});

        // Callable
        assertEquals(1, tryCatch(() -> 1).intValue());

        // void return throws exception
        tryCatchRunnable(() -> Thread.sleep(100));

        // Runnable
        try {
            tryCatch(() -> {
                throw new RuntimeException("Oops");
            });
            fail("Oops, expected exception");
        } catch (RuntimeException e) {
            // expected;
        }

        // Callable
        try {
            tryCatch(() -> {
                throw new Exception("Oops");
            });
            fail("Oops, expected exception");
        } catch (RuntimeException e) {
            // expected;
        }

        // void return throws exception
        try {
            tryCatch(() -> {
                throw new Exception("Oops");
            });
            fail("Oops, expected exception");
        } catch (RuntimeException e) {
            // expected;
        }
    }

    @Test
    public void testTryCatchIgnore() {

        tryCatchIgnore(() -> {
            throw new Exception("Oops");
        });

        tryCatchIgnore(() -> {});

        assertEquals(1, tryCatchIgnore(() -> {
            throw new Exception("Oops");
        }, 1).intValue());
    }

    @Test
    public void testWithExceptionLogging() throws InterruptedException, ExecutionException, TimeoutException {

        // Runnable
        Executors.newSingleThreadExecutor().submit(toRunnable(() -> {})).get(1, TimeUnit.SECONDS);

        // Callable
        String result = "result";
        assertEquals(result, Executors.newSingleThreadExecutor().submit(() -> result).get(1, TimeUnit.SECONDS));

        // ExceptionalRunnable
        Future<?> f = Executors.newSingleThreadExecutor().submit(toRunnable(() -> {
            throw new Exception("Oops");
        }));
        try {
            f.get(1, TimeUnit.SECONDS);
            fail("Oops. expected ExecutionException");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            // expected
        }
    }

    @Test
    public void testTryWith() {
        {
            Try<Integer> t = tryWith(() -> 1);
            assertNotNull(t);
            assertTrue(t.isSuccess());
            Try<Integer> result = t.map(v -> 2*v);
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.toOptional().get().intValue());
        }

        {
            Try<Void> t = tryWith(() -> {
                throw new RuntimeException("Oops");
            });
            assertNotNull(t);
            assertTrue(t.isFailure());
            Try<Integer> result = t.map(v -> 1);
            assertNotNull(result);
            assertTrue(result.isFailure());
        }

        {
            Try<Integer> t = tryWith(() -> 0);
            assertNotNull(t);
            assertTrue(t.isSuccess());
            Try<Integer> result = t.map(v -> 1/v);
            assertNotNull(result);
            assertTrue(result.isFailure());
            assertTrue(result.toFailure().getThrowable() instanceof ArithmeticException);
            assertEquals(result.toFailure().getThrowable().getMessage(), "/ by zero");
        }
    }

    @Test
    public void testFlatMap() {
        {
            Try<Integer> t = tryWith(() -> 1);
            assertNotNull(t);
            assertTrue(t.isSuccess());
            Try<Integer> result = t.flatMap(v -> tryWith(() -> 2/v));
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.toSuccess().getValue().intValue());
        }

        {
            RuntimeException oops = new RuntimeException("Oops");
            Try<Integer> t = tryWith(() -> {
                throw oops;
            });
            assertNotNull(t);
            assertTrue(t.isFailure());
            Try<Integer> result = t.flatMap(v -> tryWith(() -> 2/v));
            assertNotNull(result);
            assertTrue(result.isFailure());
            assertEquals(oops, result.toFailure().getThrowable());
        }

        {
            Try<Integer> t = tryWith(() -> 0);
            assertNotNull(t);
            assertTrue(t.isSuccess());
            Try<Integer> result = t.flatMap(v -> tryWith(() -> 2/v));
            assertNotNull(result);
            assertTrue(result.isFailure());
            assertTrue(result.toFailure().getThrowable() instanceof ArithmeticException);
            assertThrows(ArithmeticException.class, () -> result.toFailure().getValue());
        }
    }

    @Test
    public void testTryWithAsync() throws InterruptedException, ExecutionException, TimeoutException {

        {
            CompletableFuture<Try<Integer>> f = executeAsync(() -> 1).thenApply(i -> tryWith(() -> 2/i));
            Try<Integer> result = f.get(1, TimeUnit.SECONDS);
            assertTrue(result.isSuccess());
            assertEquals(2, result.toSuccess().getValue().intValue());
        }

        {
            CompletableFuture<Integer> f = executeAsync(() -> 0).thenApply(i -> 2/i);
            assertThrows(ExecutionException.class, () -> f.get(1, TimeUnit.SECONDS));
        }

        {
            CompletableFuture<Try<Integer>> f = executeAsync(() -> 0).thenApply(i -> tryWith(() -> 2/i));
            Try<Integer> result = f.get(1, TimeUnit.SECONDS);
            assertTrue(result.isFailure());
            assertThrows(ArithmeticException.class, () -> result.toFailure().getValue());
        }
    }

    private <T> CompletableFuture<T> executeAsync(Callable<T> callable) {
        return executeAsync(callable, Executors.newSingleThreadExecutor());
    }

    private <T> CompletableFuture<T> executeAsync(Callable<T> callable, ExecutorService executor) {
        CompletableFuture<T> f = new CompletableFuture<>();
        executor.submit(() -> tryCatch(() -> {
            T result = callable.call();
            f.complete(result);
            return result;
        }, f::completeExceptionally));
        return f;
    }

}

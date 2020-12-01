package com.github.dfauth.trycatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.concurrent.*;

import static com.github.dfauth.trycatch.Try.tryWith;
import static com.github.dfauth.trycatch.TryCatch.*;
import static org.testng.Assert.*;

public class TestCase {

    private static final Logger logger = LoggerFactory.getLogger(TestCase.class);

    @Test
    public void testTryCatch() throws InterruptedException, ExecutionException, TimeoutException {

        tryCatch(() -> {});

        try {
            tryCatch(() -> {
                throw new RuntimeException("Oops");
            });
            fail("Oops, expected exception");
        } catch (RuntimeException e) {
            // expected;
        }
        tryCatchIgnore(() -> {
            throw new RuntimeException("Oops");
        });

        Executors.newSingleThreadExecutor().submit(withExceptionLogging(() -> {})).get(1, TimeUnit.SECONDS);

        String result = "result";
        assertEquals(result, Executors.newSingleThreadExecutor().submit(withExceptionLogging(() -> result)).get(1, TimeUnit.SECONDS));

        Future<?> f = Executors.newSingleThreadExecutor().submit(withExceptionLogging(() -> {
            throw new RuntimeException("Oops");
        }));
        try {
            f.get(1, TimeUnit.SECONDS);
            fail("Oops. expected ExecutionException");
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            // expected
        } catch (TimeoutException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
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
            assertEquals(result.toOptional().get().intValue(), 2);
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
            assertTrue(result.toFailure().exception() instanceof ArithmeticException);
            assertEquals(result.toFailure().exception().getMessage(), "/ by zero");
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
            assertEquals(result.toSuccess().result().intValue(), 2);
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
            assertEquals(result.toFailure().exception(), oops);
        }
    }
}

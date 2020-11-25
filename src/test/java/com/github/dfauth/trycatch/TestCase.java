package com.github.dfauth.trycatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static com.github.dfauth.trycatch.Try.tryWith;
import static com.github.dfauth.trycatch.TryCatch.tryCatch;
import static com.github.dfauth.trycatch.TryCatch.tryCatchIgnore;
import static org.testng.Assert.*;

public class TestCase {

    private static final Logger logger = LoggerFactory.getLogger(TestCase.class);

    @Test
    public void testTryCatch() {

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
            assertEquals(Try.Failure.class.cast(result).exception().getClass(), ArithmeticException.class);
            assertEquals(Try.Failure.class.cast(result).exception().getMessage(), "/ by zero");
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
            assertEquals(result.toOptional().get().intValue(), 2);
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
            assertEquals(Try.Failure.class.cast(result).exception(), oops);
        }
    }
}

package com.github.dfauth.trycatch;

import com.github.dfauth.partial.PartialConsumer;
import com.github.dfauth.partial.PartialFunction;
import com.github.dfauth.partial.PartialFunctions;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.*;

import static com.github.dfauth.partial.VoidFunction.peek;
import static com.github.dfauth.trycatch.AssertingLogger.*;
import static com.github.dfauth.trycatch.ExceptionalConsumer.toConsumer;
import static com.github.dfauth.trycatch.Try.tryWith;
import static com.github.dfauth.trycatch.TryCatch.*;
import static org.junit.Assert.*;

public class TryCatchTestCase {

    private static final Logger logger = LoggerFactory.getLogger(TryCatchTestCase.class);
    private static RuntimeException runtimeOops = new RuntimeException("Oops");
    private static Exception oops = new Exception("Oops");

    private <T> T throwRuntimeOops() {
        throw runtimeOops;
    }

    private <T> T throwOops() throws Exception {
        throw oops;
    }

    @Before
    public void setUp() {
        resetLogEvents();
    }

    @Test
    public void testTryCatch() {

        // Runnable
        tryCatch(() -> {
        });
        assertNothingLogged();

        // Callable
        assertEquals(1, tryCatch(() -> 1).intValue());
        assertNothingLogged();

        // void return throws exception
        tryCatch(() -> Thread.sleep(100));
        assertNothingLogged();

        // Runnable
        try {
            tryCatch(() -> throwRuntimeOops());
            fail("Oops, expected exception");
        } catch (RuntimeException e) {
            // expected;
            assertExceptionLogged(runtimeOops);
        }

        // Callable
        try {
            tryCatch(() -> true ? throwOops(): null);
            fail("Oops, expected exception");
        } catch (RuntimeException e) {
            // expected;
            assertExceptionLogged(oops);
        }

        // void return throws exception
        try {
            tryCatch(() -> throwOops());
            fail("Oops, expected exception");
        } catch (RuntimeException e) {
            // expected;
            assertExceptionLogged(oops);
        }
    }

    @Test
    public void testTryCatchIgnore() {

        tryCatchIgnore(() -> throwOops());
        assertExceptionLogged(oops);

        tryCatchIgnore(() -> {
        });
        assertNothingLogged();

        assertEquals(1, tryCatchIgnore(() -> throwOops(), 1).intValue());
        assertExceptionLogged(oops);
    }

    @Test
    public void testWithExceptionLogging() throws InterruptedException, ExecutionException, TimeoutException {

        // Runnable
        Executors.newSingleThreadExecutor().submit(withExceptionLogging(() -> {})).get(1, TimeUnit.SECONDS);

        // Callable
        String result = "result";
        assertEquals(result, Executors.newSingleThreadExecutor().submit(withExceptionLogging(() -> result)).get(1, TimeUnit.SECONDS));
        assertNothingLogged();

        // ExceptionalRunnable
        Future<?> f = Executors.newSingleThreadExecutor().submit(withExceptionLogging(() -> {
            throw oops;
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
        assertExceptionLogged(oops);

        // CallableFunction
        CallableFunction<CompletableFuture<String>, String> g = (CompletableFuture<String> _f) -> _f.get(1, TimeUnit.SECONDS);
        {
            assertEquals(result, Optional.of(CompletableFuture.completedFuture(result)).map(withExceptionLogging(g)).get());
            assertNothingLogged();
        }
        {
            assertTrue(tryWith(() -> Optional.of(CompletableFuture.<String>failedFuture(oops)).map(withExceptionLogging(g)).get()).isFailure());
            assertExceptionLogged(new ExecutionException(oops));
            assertExceptionLogged(new RuntimeException(new ExecutionException(oops))); // TODO why?
            assertExceptionLogged(new RuntimeException(new RuntimeException(new ExecutionException(oops)))); // TODO why?
            assertNothingLogged();
        }

        // ExceptionalConsumer
        {
            assertTrue(tryWith(() -> Optional.of(CompletableFuture.completedFuture(result)).ifPresent(toConsumer(_f -> _f.get(1, TimeUnit.SECONDS)))).isSuccess());
            assertNothingLogged();
        }
        {
            assertTrue(tryWith(() -> Optional.of(CompletableFuture.failedFuture(oops)).ifPresent(toConsumer(_f -> _f.get(1, TimeUnit.SECONDS)))).isSuccess());
            assertExceptionLogged(new ExecutionException(oops));
            assertNothingLogged();
        }

    }

    @Test
    public void testTheIgnorant() {

        // Runnable

        // Callable

        // ExceptionalRunnable

        // CallableFunction
        CallableFunction<CompletableFuture<String>, String> g = (CompletableFuture<String> _f) -> _f.get(1, TimeUnit.SECONDS);
        {
            assertEquals("oops", Optional.of(CompletableFuture.<String>failedFuture(oops)).map(ignorantCallableFunction(g, "oops")).get());
            assertExceptionLogged(new ExecutionException(oops));
        }

    }

    @Test
    public void testTryWith() {
        {
            Try<Integer> t = Try.tryWithCallable(() -> 1);
            assertNotNull(t);
            assertTrue(t.isSuccess());
            Try<Integer> result = t.map(v -> 2*v);
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.toOptional().get().intValue());
            assertNothingLogged();
        }

        {
            Try<Void> t = tryWith(() -> {
                throw runtimeOops;
            });
            assertNotNull(t);
            assertTrue(t.isFailure());
            Try<Integer> result = t.map(v -> 1);
            assertNotNull(result);
            assertTrue(result.isFailure());
            assertExceptionLogged(runtimeOops);
            assertExceptionLogged(new RuntimeException(runtimeOops)); // TODO why?
            assertNothingLogged();
        }

        {
            Try<Integer> t = Try.tryWithCallable(() -> 0);
            assertNotNull(t);
            assertTrue(t.isSuccess());
            Try<Integer> result = t.map(v -> 1/v);
            assertNotNull(result);
            assertTrue(result.isFailure());
            assertTrue(result.toFailure().exception() instanceof ArithmeticException);
            assertEquals(result.toFailure().exception().getMessage(), "/ by zero");
            assertExceptionLogged(new ArithmeticException("/ by zero"));
        }
    }

    @Test
    public void testFlatMap() {
        {
            Try<Integer> t = Try.tryWithCallable(() -> 1);
            assertNotNull(t);
            assertTrue(t.isSuccess());
            Try<Integer> result = t.flatMap(v -> Try.tryWithCallable(() -> 2/v));
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(2, result.toSuccess().result().intValue());
            assertNothingLogged();
        }

        {
            Try<Integer> t = Try.tryWithCallable(() -> true ? throwRuntimeOops() : null);
            assertNotNull(t);
            assertTrue(t.isFailure());
            Try<Integer> result = t.flatMap(v -> Try.tryWithCallable(() -> 2/v));
            assertNotNull(result);
            assertTrue(result.isFailure());
            assertEquals(runtimeOops, result.toFailure().exception());
            assertExceptionLogged(runtimeOops);
        }

        {
            Try<Integer> t = Try.tryWithCallable(() -> 0);
            assertNotNull(t);
            assertTrue(t.isSuccess());
            Try<Integer> result = t.flatMap(v -> Try.tryWithCallable(() -> 2/v));
            assertNotNull(result);
            assertTrue(result.isFailure());
            assertThrows(ArithmeticException.class, () -> {
                result.toFailure().throwException();
            });
            assertExceptionLogged(new ArithmeticException("/ by zero"));
        }
    }

    @Test
    public void testOnComplete() {
        {
            Try<Integer> t = Try.success(1);
            t.onComplete(
                    PartialConsumer._case((Try<Integer> _t) -> t.isSuccess())
                            .thenAccept(_t -> logger.info("t is success")),
                    PartialConsumer._case((Try<Integer> _t) -> t.isFailure())
                            .thenAccept(_t -> logger.info("t is failure"))
            );
            assertInfoLogged("t is success");
        }
        {
            Try<Integer> t = Try.tryWithCallable(() -> true ? throwRuntimeOops() : null);
            t.onComplete(
                    PartialConsumer._case((Try<Integer> _t) -> _t.isSuccess())
                            .thenAccept(_t -> logger.info("_t is success")),
                    PartialConsumer._case((Try<Integer> _t) -> _t.isFailure())
                            .thenAccept(_t -> logger.info("_t is failure"))
            );
            assertExceptionLogged(runtimeOops);
            assertInfoLogged("_t is failure");
        }
        {
            Try<Integer> t = Try.tryWithCallable(() -> true ? throwRuntimeOops() : null);
            t.onComplete(
                    PartialConsumer._case((Try<Integer> _t) -> _t.isSuccess())
                            .thenAccept(_t -> logger.info(_t+" is success"))
                    ._otherwise(_t -> logger.info("otherwise("+_t+")"))
            );
            assertExceptionLogged(runtimeOops);
            assertInfoLogged(msg -> msg.startsWith("otherwise("));
        }
        {
            Try<Integer> t = Try.tryWithCallable(() -> true ? throwRuntimeOops() : null);
            t.onComplete(
                    PartialConsumer._case((Try<Integer> _t) -> _t.isSuccess())
                            .thenAccept(_t -> logger.info(_t+" is success")),
                    PartialConsumer._case((Try<Integer> _t) -> _t.isFailure())
                            .thenAccept(_t -> logger.info(_t+" is failure"))
                    ._otherwise(_t -> logger.info("otherwise("+_t+")"))
            );
            assertExceptionLogged(runtimeOops);
            assertInfoLogged(msg -> msg.endsWith(" is failure"));
        }
        {
            Try<Integer> t = Try.success(1);
            t.onComplete(
                    PartialFunction._case(PartialFunctions.downcast((Try<Integer> _t) -> (Success<Integer>)_t))
                            .thenAccept(_t ->
                                    logger.info("result is "+_t.result()))
            );
            assertInfoLogged(msg -> msg.startsWith("result is"));
        }
    }

    @Test
    public void testRecover() {
        {
            Try<Integer> t = Try.success(1);
            t.map(peek(r -> logger.info("map: "+r)))
                    .recover(_t -> {
                        logger.error("recover: "+_t.getMessage(), t);
                    });
            assertInfoLogged(msg -> msg.startsWith("map: "));
        }
        {
            Try<Integer> t = Try.tryWithCallable(() -> true ? throwRuntimeOops() : null);
            t.map(peek(r -> logger.info("map: "+r)))
                    .recover(_t -> {
                        logger.info("recover: "+_t.getMessage());
                    });
            assertExceptionLogged(runtimeOops);
            assertInfoLogged(msg -> msg.startsWith("recover: "));
        }
    }


}

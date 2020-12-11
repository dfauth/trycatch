package com.github.dfauth.trycatch;

import com.github.dfauth.partial.PartialConsumer;
import com.github.dfauth.partial.PartialFunction;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dfauth.partial.VoidFunction.peek;
import static com.github.dfauth.trycatch.Try.tryWith;
import static com.github.dfauth.trycatch.TryCatch.*;
import static org.junit.Assert.*;

public class TestCase {

    private static final Logger logger = LoggerFactory.getLogger(TestCase.class);

    @Test
    public void testTryCatch() {

        // Runnable
        tryCatch(() -> {
        });

        // Callable
        assertEquals(1, tryCatch(() -> 1).intValue());

        // void return throws exception
        tryCatch(() -> Thread.sleep(100));

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
                if (true) {
                    throw new Exception("Oops");
                }
                return 1;
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

        tryCatchIgnore(() -> {
        });

        assertEquals(1, tryCatchIgnore(() -> {
            throw new Exception("Oops");
        }, 1).intValue());
    }

    @Test
    public void testWithExceptionLogging() throws InterruptedException, ExecutionException, TimeoutException {

        // Runnable
        Executors.newSingleThreadExecutor().submit(withExceptionLogging(() -> {})).get(1, TimeUnit.SECONDS);

        // Callable
        String result = "result";
        assertEquals(result, Executors.newSingleThreadExecutor().submit(withExceptionLogging(() -> result)).get(1, TimeUnit.SECONDS));

        // ExceptionalRunnable
        Future<?> f = Executors.newSingleThreadExecutor().submit(withExceptionLogging(() -> {
            throw new Exception("Oops");
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
            assertEquals(2, result.toSuccess().result().intValue());
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
            assertEquals(oops, result.toFailure().exception());
        }

        {
            Try<Integer> t = tryWith(() -> 0);
            assertNotNull(t);
            assertTrue(t.isSuccess());
            Try<Integer> result = t.flatMap(v -> tryWith(() -> 2/v));
            assertNotNull(result);
            assertTrue(result.isFailure());
            assertThrows(ArithmeticException.class, () -> {
                result.toFailure().throwException();
            });
        }
    }

    @Test
    public void testTryWithAsync() throws InterruptedException, ExecutionException, TimeoutException {

        {
            CompletableFuture<Try<Integer>> f = executeAsync(() -> 1).thenApply(i -> tryWith(() -> 2/i));
            Try<Integer> result = f.get(1, TimeUnit.SECONDS);
            assertTrue(result.isSuccess());
            assertEquals(2, result.toSuccess().result().intValue());
        }

        {
            CompletableFuture<Integer> f = executeAsync(() -> 0).thenApply(i -> 2/i);
            assertThrows(ExecutionException.class, () -> f.get(1, TimeUnit.SECONDS));
        }

        {
            CompletableFuture<Try<Integer>> f = executeAsync(() -> 0).thenApply(i -> tryWith(() -> 2/i));
            Try<Integer> result = f.get(1, TimeUnit.SECONDS);
            assertTrue(result.isFailure());
            assertThrows(ArithmeticException.class, () -> result.toFailure().throwException());
        }
    }

    @Test
    public void testOnComplete() {
        {
            Try<Integer> t = Try.success(1);
            t.onComplete(
                    _case((Try<Integer> _t) -> t.isSuccess())
                            .thenAccept(_t -> logger.info("t is success")),
                    _case((Try<Integer> _t) -> t.isFailure())
                            .thenAccept(_t -> logger.info("t is failu7re"))
            );
        }
        {
            Try<Integer> t = Try.failure(new RuntimeException("Oops"));
            t.onComplete(
                    _case((Try<Integer> _t) -> _t.isSuccess())
                            .thenAccept(_t -> logger.info(_t+" is success")),
                    _case((Try<Integer> _t) -> _t.isFailure())
                            .thenAccept(_t -> logger.info(_t+" is failu7re"))
            );
        }
        {
            Try<Integer> t = Try.failure(new RuntimeException("Oops"));
            t.onComplete(
                    _case((Try<Integer> _t) -> _t.isSuccess())
                            .thenAccept(_t -> logger.info(_t+" is success"))
                    .otherwise(_t -> logger.info("otherwise("+_t+")"))
            );
        }
        {
            Try<Integer> t = Try.failure(new RuntimeException("Oops"));
            t.onComplete(
                    _case((Try<Integer> _t) -> _t.isSuccess())
                            .thenAccept(_t -> logger.info(_t+" is success")),
                    _case((Try<Integer> _t) -> _t.isFailure())
                            .thenAccept(_t -> logger.info(_t+" is failu7re"))
                    .otherwise(_t -> logger.info("otherwise("+_t+")"))
            );
        }
        {
            Try<Integer> t = Try.success(1);
            t.onComplete(
                    _case(downcast((Try<Integer> _t) -> (Success<Integer>)_t))
                            .thenAccept(_t ->
                                    logger.info("result is "+_t.result()))
            );
        }
    }

    @Test
    public void testRecover() {
        {
            Try<Integer> t = Try.success(1);
            t.map(peek(r -> logger.info("map: "+r)))
                    .recover(_t -> logger.error("recover: "+_t.getMessage(), t));
        }
        {
            Try<Integer> t = Try.failure(new RuntimeException("Oops"));
            t.map(peek(r -> logger.info("map: "+r)))
                    .recover(_t -> logger.error("recover: "+_t.getMessage(), _t));
        }
    }


    public static <I> PartialConsumer<I> _case(Predicate<I> p) {
        return PartialConsumer.fromPredicate(p);
    }

    public static <T,R extends T> PartialFunction<T,R> downcast(Function<T,R> f) {
        return PartialFunction.fromPredicateAndFunction(i -> tryCatch(() -> {
            f.apply(i);
            return true;
        }, t -> false), f);
    }

    public static <I,O> PartialFunction<I,O> _case(PartialFunction<I,O> pf) {
        return pf;
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

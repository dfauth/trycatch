package com.github.dfauth.partial;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dfauth.partial.Matcher.matcher;
import static com.github.dfauth.partial.PartialConsumer.fromPredicateAndConsumer;
import static com.github.dfauth.partial.PartialFunctions._case;
import static com.github.dfauth.partial.Tuple2.of;
import static com.github.dfauth.trycatch.Try.tryWith;
import static junit.framework.TestCase.*;

public class PartialFunctionTest {

    private static final Logger logger = LoggerFactory.getLogger(PartialFunctionTest.class);
    private Predicate<Tuple2<Boolean, Boolean>> matchBoth = t -> t._1() && t._2();
    private Predicate<Tuple2<Boolean, Boolean>> match1 = t -> t._1() && !t._2();
    private Predicate<Tuple2<Boolean, Boolean>> match2 = t -> !t._1() && t._2();

    @Test
    public void testIt() {
        PartialFunction<Integer, String> pf = PartialFunction.of(tIs(1), i -> "ONE");
        assertTrue(pf.isDefinedAt(1));
        assertFalse(pf.isDefinedAt(0));
        assertEquals("ONE", pf.apply(1));
        assertTrue(tryWith(() -> pf.apply(2)).isFailure());

        PartialFunction<Integer, String> pf2 = PartialFunction.of(tIs(2), i -> "TWO");
        PartialFunction<Integer, String> pf3 = PartialFunction.of(tIs(3), i -> "THREE");

        assertEquals("ONE", pf._or(pf2).apply(1));
        assertEquals("TWO", pf._or(pf2)._or(pf3).apply(2));
        assertEquals("THREE", pf._or(pf2)._or(pf3).apply(3));
        assertTrue(tryWith(() -> pf._or(pf2)._or(pf3).apply(4)).isFailure());
    }

    @Test
    public void testSanity() {
        AtomicInteger result = new AtomicInteger(-1);
        Predicate<Integer> p = t -> t==1;
        Consumer<Integer> c = t -> result.set(t);
        PartialConsumer<Integer> pf = fromPredicateAndConsumer(p, c);
        matcher(1).match(pf._otherwise(() -> {}));
        assertEquals(1, result.get());
    }

    @Test
    public void testOr() {
        final AtomicInteger result = new AtomicInteger(-1);
        PartialConsumer<Integer> is1 = fromPredicateAndConsumer(tIs(1), t -> result.set(143));
        PartialConsumer<Integer> is2 = fromPredicateAndConsumer(tIs(2), t -> result.set(69));
        PartialConsumer<Integer> is3 = fromPredicateAndConsumer(tIs(3), t -> result.set(34));
        Runnable doNothing = () -> {};
        Function<Integer, Unit> pf = is1._or(is2)._or(is3)._otherwise(doNothing);
        {
            result.set(-1);
            matcher(1).match(pf);
            assertEquals(143, result.get());
        }
        {
            result.set(-1);
            matcher(2).match(pf);
            assertEquals(69, result.get());
        }
        {
            result.set(-1);
            matcher(3).match(pf);
            assertEquals(34, result.get());
        }
        {
            result.set(-1);
            matcher(4).match(pf);
            assertEquals(-1, result.get());
        }
    }

    private Predicate<Integer> tIs(int t) {
        return _t -> _t==t;
    }

    @Test
    public void testPartial() {
        assertAllCombinationsWithOneMissing(doitPartial);
    }

    @Test
    public void testDefault() {
        assertAllCombinations(doitDefault);
    }

    @Test
    public void testSupplier() {
        assertAllCombinations(doitSupplier);
    }

    @Test
    public void testPartialVoid() {
        assertAllCombinationsWithOneMissing(doitPartialVoid);
    }

    @Test
    public void testVoid() {
        assertAllCombinations(doitVoid);
    }

    private void assertAllCombinationsWithOneMissing(Function<Tuple2<Boolean, Boolean>, Optional<Integer>> f) {
        assertEquals(0, doitPartial.apply(of(true, true)).orElseThrow().intValue());
        assertEquals(1, doitPartial.apply(of(true, false)).orElseThrow().intValue());
        assertEquals(2, doitPartial.apply(of(false, true)).orElseThrow().intValue());
        assertEquals(Optional.empty(), doitPartial.apply(of(false, false)));
    }

    private void assertAllCombinations(Function<Tuple2<Boolean, Boolean>, Integer> f) {
        assertEquals(0, f.apply(of(true, true)).intValue());
        assertEquals(1, f.apply(of(true, false)).intValue());
        assertEquals(2, f.apply(of(false, true)).intValue());
        assertEquals(3, f.apply(of(false, false)).intValue());
    }

    private Function<Tuple2<Boolean, Boolean>, Optional<Integer>> doitPartial = t ->
        matcher(t).matchOpt(
                _case(matchBoth, _t -> 0)
                        ._case(match1, _t -> 1)
                        ._case(match2, _t -> 2)
        );

    private Function<Tuple2<Boolean, Boolean>,Integer> doitDefault = t ->
        matcher(t).match(
                _case(matchBoth, _t -> 0)
                        ._case(match1, _t -> 1)
                        ._case(match2, _t -> 2)
                        ._otherwise(3)
        );

    private Function<Tuple2<Boolean, Boolean>,Integer> doitSupplier = t ->
        matcher(t).match(
                _case(matchBoth, _t -> 0)
                        ._case(match1, _t -> 1)
                        ._case(match2, _t -> 2)
                        ._otherwise(() -> 3)
        );

    private Function<Tuple2<Boolean, Boolean>,Optional<Integer>> doitPartialVoid = t -> {
        AtomicReference<Optional<Integer>> tmp = new AtomicReference<>(Optional.empty());
        matcher(t).matchOpt(
                _case(matchBoth, _t -> {
                    tmp.set(Optional.ofNullable(0));
                })
                ._case(match1, _t -> {
                    tmp.set(Optional.ofNullable(1));
                })
                ._case(match2, _t -> {
                    tmp.set(Optional.ofNullable(2));
                })
        );
        return tmp.get();
    };

    private Function<Tuple2<Boolean, Boolean>,Integer> doitVoid = t -> {
        AtomicInteger tmp = new AtomicInteger(-1);
        matcher(t).match(
                _case(matchBoth, _t -> {
                    tmp.set(0);
                })
                ._case(match1, _t -> {
                    tmp.set(1);
                })
                ._case(match2, _t -> {
                    tmp.set(2);
                })
                ._otherwise(() -> tmp.set(3))
        );
        return tmp.get();
    };
}

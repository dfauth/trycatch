package com.github.dfauth.partial;

import com.github.dfauth.trycatch.Success;
import com.github.dfauth.trycatch.Try;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Function;

import static com.github.dfauth.partial.Matcher.matcher;
import static com.github.dfauth.partial.PartialFunction.identity;
import static com.github.dfauth.partial.PartialFunction.of;
import static com.github.dfauth.partial.PartialFunctions.*;
import static com.github.dfauth.partial.Unit.UNIT;
import static org.junit.Assert.assertEquals;

public class MatchTest {

    private static final Logger logger = LoggerFactory.getLogger(MatchTest.class);

    @Test
    public void testSimplified() {
        Try<Integer> test = Try.success(1);
        assertEquals(test, matcher(test).matchOpt(
                _case(identity())
                ).orElseThrow(() -> new RuntimeException(""))
        );
    }

    @Test
    public void testIt() {
        Try<Integer> test = Try.success(1);
        Assert.assertEquals(Optional.of(UNIT), matcher(test).matchOpt(
                _case(narrow((Try<Integer> t) -> (Success<Integer>)t)
                        .andThen(s -> {
                            s.result();
                        }))
                )
        );
    }

    @Test
    public void testAndIf() {
        {
            Try<Integer> test = Try.success(1);
            assertEquals("ONE", matcher(test).match(
                    _case(narrow((Try<Integer> t) -> (Success<Integer>)t, s -> s.result() == 1)
                            .andThen(s -> "ONE"))
                    ._otherwise("NONE")
            ));
        }
        {
            Try<Integer> test = Try.success(0);
            assertEquals("NO_MATCH", matcher(test).match(
                    _case(narrow((Try<Integer> a) -> (Success<Integer>)a, s -> s.result() == 1)
                            .andThen(s -> "ONE"))
                    ._otherwise("NO_MATCH")
            ));
        }
        {
            Try<String> test = Try.success("poo");
            assertEquals("NO_MATCH", matcher(test).match(
                    _case(isSuccessOf(String.class)
                            .andThen(of(s -> s.result() == "BLAH", s -> "BLAH")))
                    ._otherwise("NO_MATCH")
            ));
        }
        {
            Try<String> test = Try.success("poo");
            assertEquals("MATCH_POO", matcher(test).match(
                    _case(isSuccessOf(String.class, s -> s.equalsIgnoreCase("BLAH"))
                            .andThen(s -> "MATCH_BLAH"))
                    ._case(isSuccessOf(String.class,s -> s.equals("poo"))
                            .andThen(s -> "MATCH_POO"))
                    ._otherwise("NO_MATCH")
            ));
        }
    }

    @Test
    public void testSuccess() {
        Function<Try<Integer>, String> f = t -> matcher(t).match(
                _case(isSuccessOf(Integer.class)
                        .andThen(of(s -> s.result() == 0, s -> "ZERO")))
                ._case(isFailureOf(Integer.class)
                        .andThen(of(_f -> _f.exception() instanceof RuntimeException, _f -> _f.exception().getMessage())))
                ._otherwise("NO_MATCH")
        );
        {
            Try<Integer> test = Try.success(0);
            assertEquals("ZERO", f.apply(test));
        }
        {
            Try<Integer> test = Try.failure(new RuntimeException("Oops"));
            assertEquals("Oops", f.apply(test));
        }
        {
            Try<Integer> test = Try.success(1);
            assertEquals("NO_MATCH", f.apply(test));
        }
    }


}

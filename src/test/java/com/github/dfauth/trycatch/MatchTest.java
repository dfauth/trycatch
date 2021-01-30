package com.github.dfauth.trycatch;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

import static com.github.dfauth.partial.Matcher.matcher;
import static com.github.dfauth.partial.PartialFunctions._case;
import static com.github.dfauth.partial.PartialFunction.identity;
import static com.github.dfauth.partial.PartialFunctions.*;
import static org.junit.Assert.assertEquals;

public class MatchTest {

    private static final Logger logger = LoggerFactory.getLogger(MatchTest.class);

    @Test
    public void testSimplified() {
        Try<Integer> test = Try.success(1);
        assertEquals(test, matcher(test).matchFirstOf(
                _case(identity())
                ).orElseThrow(() -> new RuntimeException(""))
        );
    }

    @Test
    public void testIt() {
        Try<Integer> test = Try.success(1);
        assertEquals(1, matcher(test).matchFirstOf(
                _case(downcast((Try<Integer> t) -> (Success<Integer>)t)
                        .thenMap(s -> s.result()))
                ).orElse(0).intValue()
        );
    }

    @Test
    public void testAndIf() {
        {
            Try<Integer> test = Try.success(1);
            assertEquals("ONE", matcher(test).matchFirstOf(
                    _case(downcast((Try<Integer> a) -> (Success<Integer>)a)
                            .andIf(s -> s.result() == 1))
                            .thenMap(s -> "ONE"))
                    .orElse("NO_MATCH")
            );
        }
        {
            Try<Integer> test = Try.success(0);
            assertEquals("NO_MATCH", matcher(test).matchFirstOf(
                    _case(downcast((Try<Integer> a) -> (Success<Integer>)a)
                            .andIf(s -> s.result() == 1)
                            .thenMap(s -> "ONE"))
                    ).orElse("NO_MATCH")
            );
        }
        {
            Try<String> test = Try.success("poo");
            assertEquals("NO_MATCH", matcher(test).matchFirstOf(
                    _case(downcast((Try<String> a) -> (Success<String>)a)
                            .andIf(s -> s.result() == "BLAH")
                            .thenMap(s -> "BLAH"))
                    ).orElse("NO_MATCH")
            );
        }
        {
            Try<String> test = Try.success("poo");
            assertEquals("MATCH_POO", matcher(test).matchFirstOf(
                    _case(downcast((Try<String> a) -> (Success<String>)a)
                            .andIf(s -> s.result().equals("blah"))
                            .andIf(s -> s.result().equalsIgnoreCase("BLAH"))
                            .thenMap(s -> "MATCH_BLAH")),
                    _case(downcast((Try<String> a) -> (Success<String>)a)
                            .andIf(s -> s.result().equals("poo"))
                            .thenMap(s -> "MATCH_POO"))
                    ).orElse("NO_MATCH")
            );
        }
    }

    @Test
    public void testSuccess() {
        Function<Try<Integer>, String> f = t -> matcher(t).matchFirstOf(
                _case(isSuccessOf(Integer.class)
                        .andIf(s -> s.result() == 0)
                        .thenMap(s -> "ZERO")),
                _case(isFailureOf(Integer.class)
                        .andIf(s -> s.exception() instanceof RuntimeException)
                        .thenMap(_f -> _f.exception().getMessage()))
        ).orElse("NO_MATCH");
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

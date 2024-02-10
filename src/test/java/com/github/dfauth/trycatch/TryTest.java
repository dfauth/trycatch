package com.github.dfauth.trycatch;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.math.BigDecimal;

import static com.github.dfauth.trycatch.BigDecimalOps.HUNDRED;
import static com.github.dfauth.trycatch.BigDecimalOps.divide;
import static com.github.dfauth.trycatch.Try.success;
import static com.github.dfauth.trycatch.Try.tryWith;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.*;

@Slf4j
public class TryTest {

    @Test
    public void testIt() {
        assertEquals(ZERO, divide(ZERO).by(HUNDRED));
        assertThrows(ArithmeticException.class, () -> divide(HUNDRED).by(ZERO));
        assertEquals(success(ZERO), tryWith(() -> divide(ZERO).by(HUNDRED)));
        Try<BigDecimal> f = tryWith(() -> divide(HUNDRED).by(ZERO));
        assertTrue(f instanceof Try.Failure);
        assertThrows(ArithmeticException.class, f::getValue);
    }

}

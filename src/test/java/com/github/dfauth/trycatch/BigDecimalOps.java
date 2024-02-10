package com.github.dfauth.trycatch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.UnaryOperator;

public interface BigDecimalOps extends UnaryOperator<BigDecimal> {

    BigDecimal HUNDRED = create(100);

    static BigDecimal create(int i) {
        return scale(BigDecimal.valueOf(i));
    }

    static BigDecimal scale(BigDecimal bd) {
        return bd.setScale(3, RoundingMode.HALF_UP);
    }

    static BigDecimalOps divide(BigDecimal bd) {
        return _bd -> divide(bd,_bd);
    }

    static BigDecimal divide(BigDecimal bd1, BigDecimal bd2) {
        return bd1.divide(bd2, RoundingMode.HALF_UP);
    }

    default BigDecimal by(BigDecimal divisor) {
        return apply(divisor);
    }
}

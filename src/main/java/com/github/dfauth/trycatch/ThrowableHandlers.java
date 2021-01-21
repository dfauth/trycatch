package com.github.dfauth.trycatch;

import java.util.function.Function;

public class ThrowableHandlers {

    static Function<Throwable, Void> noOp() {
        return t -> null;
    }

    static Consumer consume(java.util.function.Consumer<Throwable> c) {
        return t -> c.accept(t);
    }


    interface Consumer extends Function<Throwable,Void> {
        @Override
        default Void apply(Throwable t) {
            accept(t);
            return null;
        }

        void accept(Throwable t);
    }
}

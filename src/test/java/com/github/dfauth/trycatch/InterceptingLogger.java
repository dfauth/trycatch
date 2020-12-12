package com.github.dfauth.trycatch;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.spi.LoggerFactoryBinder;

import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InterceptingLogger implements LoggerFactoryBinder, ILoggerFactory {

    private static Queue<Call> q = new ArrayDeque<>();

    public static <T> Logger of(Logger delegate) {
        return (Logger) Proxy.newProxyInstance(InterceptingLogger.class.getClassLoader(), new Class[]{Logger.class}, (proxy, method, args) -> {
            q.offer(Call.of(method.getName(), args));
            method.invoke(delegate, args);
            return null;
        });
    }

    public static void assertNothingLogged() {
        assertTrue(q.isEmpty());
    }

    public static void resetLogEvents() {
        q.clear();
    }

    public static void assertExceptionLogged(Throwable t) {
        assertFalse(q.isEmpty());
        Call call = q.remove();
        assertTrue(call.isError());
        assertTrue(call.argument(0).map(_t -> _t.equals(t.getMessage())).orElse(false));
        assertTrue(call.argument(1).map(_t -> _t.getClass() == t.getClass()).orElse(false));
    }

    public static void assertInfoLogged(String message) {
        assertInfoLogged(msg -> msg.equals(message));
    }

    public static void assertInfoLogged(Predicate<String>p) {
        assertFalse(q.isEmpty());
        Call call = q.remove();
        assertTrue(call.isInfo());
        assertTrue(call.argument(0).map(_t -> p.test((String)_t)).orElse(false));
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return this;
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return this.getClass().getName();
    }

    @Override
    public Logger getLogger(String name) {
        return (Logger) Proxy.newProxyInstance(InterceptingLogger.class.getClassLoader(), new Class[]{Logger.class}, (proxy, method, args) -> {
            q.offer(Call.of(method.getName(), args));
            return null;
        });
    }

    static class Call {

        private final String methodName;
        private final Object[] args;

        public Call(String methodName, Object[] args) {

            this.methodName = methodName;
            this.args = args;
        }

        static Call of(String methodName, Object[] args) {
            return new Call(methodName, args);
        }

        public boolean isError() {
            return "error".equals(methodName);
        }

        public Optional<Object> argument(int i) {
            return args.length <= i ? Optional.empty() : Optional.ofNullable(args[i]);
        }

        public boolean isInfo() {
            return "info".equals(methodName);
        }
    }
}

package com.github.dfauth.trycatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ValidatableLogger {

    private static final Logger logger = LoggerFactory.getLogger(ValidatableLogger.class);

    public <T> ValidatableLogger getLogger(Class<T> classOfT) {
        return getLogger(classOfT.getName());
    }

    public ValidatableLogger getLogger(String name) {
        return (ValidatableLogger) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Logger.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return null;
            }
        });
    }
}

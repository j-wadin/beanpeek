package dev.jenniferwadin.beanpeek.framework;

import dev.jenniferwadin.beanpeek.annotation.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@Slf4j
public class TimingInvocationHandler implements InvocationHandler {

    private final Object target;

    public TimingInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method realMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());

        if (realMethod.isAnnotationPresent(LogExecutionTime.class)) {
            long start = System.nanoTime();
            Object result = method.invoke(target, args);
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            log.info("{}.{} took {} ms", target.getClass().getSimpleName(), method.getName(), durationMs);
            return result;
        }

        return method.invoke(target, args);
    }
}

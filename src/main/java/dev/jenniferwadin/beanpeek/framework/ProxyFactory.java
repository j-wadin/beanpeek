package dev.jenniferwadin.beanpeek.framework;

import dev.jenniferwadin.beanpeek.annotation.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.util.Arrays;

@Slf4j
public class ProxyFactory {

    public static Object createProxyIfNeeded(Class<?> clazz, Object target) {
        // Kontroll: implementerar klassen något interface?
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length == 0) {
            return target; //kan bara göra proxy på interface
        }

        //Finns någon metod annoterad med @LogExecutionTime?
        boolean hasTimedMethod = Arrays.stream(clazz.getDeclaredMethods())
                .anyMatch(method -> method.isAnnotationPresent(LogExecutionTime.class));

        if (!hasTimedMethod) {
            return target;
        }

        return Proxy.newProxyInstance(
                clazz.getClassLoader(),
                interfaces,
                new TimingInvocationHandler(target)
        );
    }
}

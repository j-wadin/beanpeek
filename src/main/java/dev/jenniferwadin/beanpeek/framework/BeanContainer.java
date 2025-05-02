package dev.jenniferwadin.beanpeek.framework;

import dev.jenniferwadin.beanpeek.annotation.MiniPostConstruct;
import dev.jenniferwadin.beanpeek.annotation.MiniPreDestroy;
import dev.jenniferwadin.beanpeek.annotation.MiniService;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * BeanContainer is a simple container for managing beans manually.
 * It handles creation, storage, and post-construction initialization of beans.
 */
@Slf4j
public class BeanContainer {

    private final Map<Class<?>, Object> beans = new HashMap<>();

    /**
     * Attempts to register a bean if the class is annotated with @MiniService.

     * - If the bean has no constructor parameters, it is instantiated directly.
     * - If constructor parameters exist, the method checks if all required dependencies
     *   are already available in the container. If they are, it creates the bean
     *   with those dependencies injected.
     * - If any required dependency is missing, the method returns false, so the scanner
     *   can try registering the bean again later.

     * After instantiation, any methods annotated with @MiniPostConstruct are executed.

     * Note:
     * - Only one bean instance per class is allowed.
     * - Dependencies must be registered before this bean can be created.
     *
     * @param clazz the Class to register
     * @return true if the bean was successfully created and registered; false otherwise
     */
    public boolean tryRegisterBean(Class<?> clazz) {
        try {
            if (!clazz.isAnnotationPresent(MiniService.class)) {
                log.warn("Class {} is not annotated with MiniService", clazz.getSimpleName());
                return false;
            }
            if(beans.containsKey(clazz)) {
                return true;
            }

            Object instance;
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            Constructor<?> constructor = constructors[0];

            if (constructor.getParameterCount() == 0) {
                instance = clazz.getDeclaredConstructor().newInstance();
            } else {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                Object[] dependencies = new Object[parameterTypes.length];

                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> dependencyClass = parameterTypes[i];
                    Object dependency = beans.get(dependencyClass);

                    if (dependency == null) {
                        return false; //dependency saknas fortfarande
                    }
                    dependencies[i] = dependency;
                }
                instance = constructor.newInstance(dependencies);
            }
            beans.put(clazz, instance);
            log.info("Registered bean: {}", clazz.getSimpleName());

            for(Method method : clazz.getDeclaredMethods()) {
                if(method.isAnnotationPresent(MiniPostConstruct.class)) {
                    method.setAccessible(true);
                    log.info("Running MiniPostConstruct: {}.{}", clazz.getSimpleName(), method.getName());
                    method.invoke(instance);
                }
            }
            return true;

        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (Map.Entry<Class<?>, Object> entry : beans.entrySet()) {
                Object bean = entry.getValue();
                Class<?> clazz = bean.getClass();
                log.info("Shutdown initiated.");

                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(MiniPreDestroy.class)) {
                        try {
                            method.setAccessible(true);
                            log.info("Running MiniPreDestroy: {}.{}", clazz.getSimpleName(), method.getName());
                            method.invoke(bean);
                        } catch (InvocationTargetException | IllegalAccessException e) {
                            log.error("Failed to execute MiniPreDestroy method {}: {}", method.getName(), e.getMessage());
                        }
                    }
                }
            }
        }));

    }

    /**
     * Retrieves a bean instance by its class type.
     *
     * @param clazz the Class of the bean
     * @param <T> the type of the bean
     * @return the bean instance
     */
    public <T> T getBean(Class<T> clazz) {
        return clazz.cast(beans.get(clazz));
    }
}

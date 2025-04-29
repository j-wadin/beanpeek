package dev.jenniferwadin.beanpeek.framework;

import dev.jenniferwadin.beanpeek.annotation.MiniPostConstruct;
import dev.jenniferwadin.beanpeek.annotation.MiniService;
import lombok.extern.slf4j.Slf4j;

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
     * Registers a bean if the class is annotated with @MiniService.
     * Calls any methods annotated with @MiniPostConstruct.
     *
     * @param clazz the Class to register
     */
    public void registerBean(Class<?> clazz) throws NoSuchMethodException {
        try {
            if (!clazz.isAnnotationPresent(MiniService.class)) {
                log.warn("Class {} is not annotated with @MiniService", clazz.getSimpleName());
                return;
            }

            Object instance = clazz.getDeclaredConstructor().newInstance();
            beans.put(clazz, instance);
            log.info("Registered bean: {}", clazz.getSimpleName());

            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(MiniPostConstruct.class)) {
                    method.setAccessible(true);
                    log.info("Running @MiniPostConstruct: {}.{}", clazz.getSimpleName(), method.getName());
                    method.invoke(instance);
                }
            }

        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            log.error("Failed to register bean {}: {}", clazz.getName(), e.getMessage());
        }
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

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
     *
     * This method supports multiple constructors and will select the first one
     * for which all required dependencies are already available in the container.
     *
     * - If the class has only one constructor, it will be used.
     * - If the class has multiple constructors, the method tries each one in order
     *   and selects the first constructor whose parameter types can be resolved
     *   using existing beans.
     * - If no suitable constructor is found, the method logs a warning with details
     *   about all available constructors and returns false.
     *
     * After instantiation, any methods annotated with @MiniPostConstruct are executed.
     *
     * Note:
     * - Only one bean instance per class is allowed.
     * - Dependencies must be registered before this bean can be created.
     *
     * @param clazz the Class to register
     * @return true if the bean was successfully created and registered; false otherwise
     */
    public boolean tryRegisterBean(Class<?> clazz) {
        try {
            if (!isMiniService(clazz)) return false;
            if(isBeanRegistered(clazz)) return true;

            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            Constructor<?> selectedConstructor = null;
            Object[] resolvedDependencies = null;

            for (Constructor<?> constructor : constructors) {
                if(canResolve(constructor)) {
                    selectedConstructor = constructor;
                    resolvedDependencies = resolveDependencies(constructor);
                    break;
                }
            }

            if (selectedConstructor == null) {
                log.warn("Could not resolve any constructor for {}", clazz.getSimpleName());
                logConstructorList(clazz, constructors);
                log.warn("Make sure all required dependencies are registered before this bean.");
                return false;
            }

            Object instance = selectedConstructor.newInstance(resolvedDependencies);

            beans.put(clazz, instance);
            log.info("Registered bean: {}", clazz.getSimpleName());

            invokePostConstructMethods(instance);
            return true;

        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isBeanRegistered(Class<?> clazz) {
        return beans.containsKey(clazz);
    }

    private static boolean isMiniService(Class<?> clazz) {
        return clazz.isAnnotationPresent(MiniService.class);
    }

    private static void invokePostConstructMethods(Object instance) throws IllegalAccessException, InvocationTargetException {
        Class<?> clazz = instance.getClass();

        for(Method method : clazz.getDeclaredMethods()) {
            if(method.isAnnotationPresent(MiniPostConstruct.class)) {
                try {
                    method.setAccessible(true);
                    log.info("Running MiniPostConstruct: {}.{}", clazz.getSimpleName(), method.getName());
                    method.invoke(instance);
                } catch (Exception e) {
                    log.error("Failed to execute @MiniPostConstruct method {}: {}", method.getName(), e.getMessage());
                }
            }
        }
    }

    /**
     * Logs all constructors and their parameter types for the given class.
     * Used for debugging when no constructor can be resolved due to
     * missing dependencies.
     *
     * @param clazz the class whose constructors will be printed
     * @param constructors the list of constructors to log
     */
    private static void logConstructorList(Class<?> clazz, Constructor<?>[] constructors) {
        for (Constructor<?> constructor : constructors) {
            StringBuilder sb = new StringBuilder(" - ").append(clazz.getSimpleName()).append("(");
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                sb.append(parameterTypes[i].getSimpleName());
                if (i < parameterTypes.length - 1) sb.append(", ");
            }
            sb.append(")");
            log.warn(sb.toString());
        }
    }

    /**
     * Checks whether all parameter types required by the given constructor
     * are already available as beans in the container.
     *
     * @param constructor the constructor to evaluate
     * @return true if all dependencies can be resolved, false otherwise
     */
    private boolean canResolve(Constructor<?> constructor) {
        for (Class<?> type : constructor.getParameterTypes()) {
            if (!beans.containsKey(type)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Resolves the dependencies for the given constructor by retrieving
     * instances from the bean container.
     * This method assumes all dependencies can be resolved.
     *
     * @param constructor the constructor whose parameters will be resolved
     * @return an array of objects to be used as constructor arguments
     */
    private Object[] resolveDependencies(Constructor<?> constructor) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] resolvedDependencies = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            resolvedDependencies[i] = beans.get(parameterTypes[i]);
        }
        return resolvedDependencies;
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

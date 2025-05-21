package dev.jenniferwadin.beanpeek.framework;

import dev.jenniferwadin.beanpeek.annotation.MiniConfigProperty;
import dev.jenniferwadin.beanpeek.annotation.MiniPostConstruct;
import dev.jenniferwadin.beanpeek.annotation.MiniPreDestroy;
import dev.jenniferwadin.beanpeek.annotation.MiniService;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
    private final MiniConfiguration config = new MiniConfiguration();

    /**
     * Attempts to register a bean if the class is annotated with @MiniService.
     * This method supports multiple constructors and will select the first one
     * for which all requird dependencies are already available in the container.
     * - If the class has only one constructor, it will be used.
     * - If the class has multiple constructors, the method tries each one in order
     *   and selects the first constructor whose parameter types can be resolved
     *   using existing beans.
     * - If no suitable constructor is found, the method logs a warning with details
     *   about all available constructors and returns false.
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

            Object rawInstance = selectedConstructor.newInstance(resolvedDependencies);
            Object instance = ProxyFactory.createProxyIfNeeded(clazz, rawInstance);

            beans.put(clazz, instance);
            log.info("Registered bean: {}", clazz.getSimpleName());

            invokePostConstructMethods(instance);
            setMiniConfigProperties(instance);
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

    private static boolean isMiniConfigProperty(Field field) {
        return field.isAnnotationPresent(MiniConfigProperty.class);
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

    private void setMiniConfigProperties(Object instance) {
        Class<?> clazz = instance.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if(isMiniConfigProperty(field)) {
                try {
                    field.setAccessible(true);
                    log.info("Setting MiniConfigProperty: {}.{}", clazz.getSimpleName(), field.getName());
                    Class<?> fieldType = field.getType();
                    String value = this.config.get(field.getAnnotation(MiniConfigProperty.class).value());
                    Object convertedValue = getConvertedValue(fieldType, value);
                    field.set(instance, convertedValue);
                } catch (IllegalAccessException e) {
                    log.error("Failed to set MiniConfigProperty: {}.{}", clazz.getSimpleName(), field.getName());
                }
            }
        }
    }

    private static Object getConvertedValue(Class<?> fieldType, String value) {
        Object convertedValue;
        if (fieldType == String.class) {
            convertedValue = value;
        } else if (fieldType == int.class || fieldType == Integer.class) {
            convertedValue = Integer.parseInt(value);
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            convertedValue = Boolean.parseBoolean(value);
        } else if (fieldType == double.class || fieldType == Double.class) {
            convertedValue = Double.parseDouble(value);
        } else {
            throw new IllegalArgumentException("Unsupported config type: " + fieldType.getName());
        }
        return convertedValue;
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
     * Retrieves a bean instance from the container that matches the given type.
     * Lookup strategy:
     * 1. First tries to find a bean registered directly with the given class as key.
     * 2. If not found, searches all beans for one whose class implements or extends the given type.
     * This allows beans to be retrieved by interface or superclass, even when
     * a proxy is used or the concrete class is unknown.
     *
     * @param clazz the desired class or interface
     * @param <T> the type of the bean to return
     * @return the matching bean instance, or null if no match is found
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> clazz) {
        Object directHit = beans.get(clazz);
        if (directHit != null) {
            return (T) directHit;
        }

        for (Object bean : beans.values()) {
            if (clazz.isAssignableFrom(bean.getClass())) {
                return (T) bean;
            }
        }
        log.warn("No bean found for type: {}", clazz.getSimpleName());
        return null;
    }
}

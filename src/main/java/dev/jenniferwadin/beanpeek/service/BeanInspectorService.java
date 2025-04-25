package dev.jenniferwadin.beanpeek.service;

import dev.jenniferwadin.beanpeek.annotation.DemoBean;
import dev.jenniferwadin.beanpeek.annotation.RunAutomatically;
import dev.jenniferwadin.beanpeek.reflection.BeanDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@DemoBean
@Service
public class BeanInspectorService {
    private final ApplicationContext context;

    @Autowired
    public BeanInspectorService(ApplicationContext context) {
        this.context = context;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("""
                     \s
                      :::::::::  ::::::::::     :::     ::::    ::: :::::::::  :::::::::: :::::::::: :::    :::
                     :+:    :+: :+:          :+: :+:   :+:+:   :+: :+:    :+: :+:        :+:        :+:   :+: \s
                    +:+    +:+ +:+         +:+   +:+  :+:+:+  +:+ +:+    +:+ +:+        +:+        +:+  +:+   \s
                   +#++:++#+  +#++:++#   +#++:++#++: +#+ +:+ +#+ +#++:++#+  +#++:++#   +#++:++#   +#++:++     \s
                  +#+    +#+ +#+        +#+     +#+ +#+  +#+#+# +#+        +#+        +#+        +#+  +#+     \s
                 #+#    #+# #+#        #+#     #+# #+#   #+#+# #+#        #+#        #+#        #+#   #+#     \s
                #########  ########## ###     ### ###    #### ###        ########## ########## ###    ###     \s
               \s""");
        runAnnotatedMethods();
    }

    public List<String> getAllBeanNames() {
        return Arrays.asList(context.getBeanDefinitionNames());
    }

    public List<String> getBeanMethodNames(String beanName) {
        if (!context.containsBean(beanName)) {
            throw new IllegalArgumentException("No such bean: " + beanName);
        }

        Object bean = context.getBean(beanName);
        return Arrays.stream(bean.getClass().getDeclaredMethods())
                .map(Method::getName)
                .collect(Collectors.toList());
    }

    public Map<String, List<String>> getGroupedBeans() {
        return Arrays.stream(context.getBeanDefinitionNames())
                .collect(Collectors.groupingBy(beanName -> {
                    Object bean = context.getBean(beanName);
                    String packageName = bean.getClass().getPackageName();

                    if(packageName.startsWith("dev.jenniferwadin")) {
                        return "application";
                    } else if (packageName.startsWith("org.springframework.boot")) {
                        return "spring-boot";
                    } else if (packageName.startsWith("org.springframework")) {
                        return "spring-internal";
                    } else {
                        return "other";
                    }
                }));
    }

    public BeanDetails getBeanDetails(String beanName) {
        if (!context.containsBean(beanName)) {
            throw new IllegalArgumentException("No such bean: " + beanName);
        }

        Object bean = context.getBean(beanName);
        Class<?> beanClass = bean.getClass();

        List<String> classAnnotations = getAnnotationNames(beanClass.getAnnotations());

        Map<String, List<String>> methodAnnotations = Arrays.stream(beanClass.getDeclaredMethods())
                .collect(Collectors.toMap(
                        Method::getName,
                        method -> getAnnotationNames(method.getAnnotations()))
                );

        return new BeanDetails(beanName, classAnnotations, methodAnnotations);
    }

    private List<String> getAnnotationNames(Annotation[] annotations) {
        return Arrays.stream(annotations)
                .map(annotation -> annotation.annotationType().getSimpleName())
                .sorted()
                .toList();
    }

    public List<String> getBeansWithDemoBean() {
        return Arrays.stream(context.getBeanDefinitionNames())
                .filter(beanName -> {
                    Object bean = context.getBean(beanName);
                    Class<?> clazz = bean.getClass();
                    return clazz.isAnnotationPresent(DemoBean.class);
                })
                .toList();
    }

    public void runAnnotatedMethods() { //fungerar likt PostConstruct eller Scheduled
        Arrays.stream(context.getBeanDefinitionNames())
                .map(name -> Map.entry(name, context.getBean(name)))
                .filter(entry -> isOwnBean(entry.getValue()))
                .forEach(entry -> {
                    Object bean = entry.getValue();
                    Class<?> clazz = bean.getClass();

                    for(Method method : clazz.getDeclaredMethods()) {
                        if(method.isAnnotationPresent(RunAutomatically.class)) {
                            try {
                                method.setAccessible(true); //if private/protected
                                int paramCount = method.getParameterCount();

                                if (paramCount == 0) {
                                    log.info("Running: {}.{}", clazz.getSimpleName(), method.getName());
                                    log.info("Method from: {}", method.getDeclaringClass());
                                    log.info("Bean class: {}", bean.getClass());
                                    method.invoke(bean);
                                } else {
                                    log.warn("Skipping method {}.{} - needs {} parameter(s),", clazz.getSimpleName(), method.getName(), paramCount);
                                }
                            } catch (InvocationTargetException | IllegalAccessException e) {
                                log.error("Failed to run {}: {}", method.getName(), e.getMessage());
                            }
                        }
                    }
                });
    }

    private boolean isOwnBean(Object bean) {
        String packageName = bean.getClass().getPackageName();
        return packageName.startsWith("dev.jenniferwadin.beanpeek");
    }

    @RunAutomatically
    public void sayHello() {
        log.info("Hello from a @RunAutomatically method!");
    }

    @RunAutomatically
    public void greet(String name) {
        log.info("Hello {}", name);
    }
}

package dev.jenniferwadin.beanpeek.service;

import dev.jenniferwadin.beanpeek.reflection.BeanDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BeanInspectorService {
    private final ApplicationContext context;

    @Autowired
    public BeanInspectorService(ApplicationContext context) {
        this.context = context;
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
}

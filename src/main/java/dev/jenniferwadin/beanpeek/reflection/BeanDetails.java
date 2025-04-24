package dev.jenniferwadin.beanpeek.reflection;

import java.util.List;
import java.util.Map;

public record BeanDetails (
        String beanName,
        List<String> classAnnotations,
        Map<String, List<String>> methods
) {}

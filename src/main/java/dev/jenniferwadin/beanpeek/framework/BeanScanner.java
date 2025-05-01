package dev.jenniferwadin.beanpeek.framework;

import dev.jenniferwadin.beanpeek.annotation.MiniService;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * BeanScanner manually scans a given package for classes annotated with @MiniService.
 * This scanner:
 * - Converts a Java package to a file system path.
 * - Locates .class files in the target directory.
 * - Loads each class dynamically using Class.forName().
 * - Registers classes annotated with @MiniService into the BeanContainer.
 * Note: This approach works for unpacked classes (not inside jar files).
 */
@Slf4j
public class BeanScanner {

    private final BeanContainer beanContainer;

    public BeanScanner(BeanContainer beanContainer) {
        this.beanContainer = beanContainer;
    }

    /**
     * Scans the given base package for classes annotated with @MiniService.

     * For each discovered class:
     * - Attempts to register the bean via the BeanContainer.
     * - If registration fails due to missing dependencies, the method retries
     *   registration until all dependencies are satisfied or no further progress
     *   can be made.

     * If any beans cannot be registered due to unsatisfied dependencies,
     * an error is logged listing the remaining classes.
     *
     * @param basePackage the base package to scan")
     */
    public void scanAndRegister(String basePackage) {
        try {
            String path = basePackage.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resource = classLoader.getResource(path);

            if (resource == null) {
                log.error("Package {} not found.", basePackage);
                return;
            }
            File directory = new File(resource.toURI());
            File[] files = Objects.requireNonNull(directory.listFiles());

            List<Class<?>> candidates = new ArrayList<>();

            for (File file : files) {
                if (file.getName().endsWith(".class")) {
                    String className = basePackage + "." + file.getName().replace(".class", "");
                    Class<?> clazz = Class.forName(className);

                    if (clazz.isAnnotationPresent(MiniService.class)) {
                        candidates.add(clazz);
                    }
                }
            }
            int remaining = candidates.size();
            boolean progress;

            do {
                progress = false;
                Iterator<Class<?>> iterator = candidates.iterator();

                while (iterator.hasNext()) {
                    Class<?> clazz = iterator.next();
                    boolean success = beanContainer.tryRegisterBean(clazz);

                    if (success) {
                        iterator.remove();
                        progress = true;
                    }
                }

                if (!progress && !candidates.isEmpty()) {
                    log.error("Could not resolve dependencies for remaining beans: ");
                    for (Class<?> c : candidates) {
                        log.error(" - {}", c.getSimpleName());
                    }
                    break;
                }
            } while (!candidates.isEmpty());
        } catch (Exception e) {
            log.error("Failed to scan package {}: {}", basePackage, e.getMessage());
        }

    }
}

package dev.jenniferwadin.beanpeek.framework;

import dev.jenniferwadin.beanpeek.annotation.MiniService;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URISyntaxException;
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
     *
     * @param basePackage the package to scan (e.g. "com.example.myapp.services")
     */
    public void scanAndRegister(String basePackage) {
        try {
            List<Class<?>> candidates = findCandidateClasses(basePackage);
            if (candidates == null) return;
            registerWithRetry(candidates);
        } catch (Exception e) {
            log.error("Failed to scan package {}: {}", basePackage, e.getMessage());
        }

    }

    /**
     * Attempts to register all candidate classes into the BeanContainer.
     * The method will retry registration until all resolvable beans have been
     * successfully registered, or no progress can be made due to unsatisfied
     * dependencies.
     *
     * @param candidates the list of classes to register
     */
    private void registerWithRetry(List<Class<?>> candidates) {
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
    }

    /**
     * Finds all classes in the given package that are annotated with @MiniService.
     * This implementation only works with unpacked .class files on the file system.
     *
     * @param basePackage the base package to scan
     * @return a list of candidate classes to be registered as beans
     * @throws Exception if scanning fails (e.g. invalid path or class loading errors)
     */
    private static List<Class<?>> findCandidateClasses(String basePackage) throws URISyntaxException, ClassNotFoundException {
        String path = basePackage.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);

        if (resource == null) {
            log.error("Package {} not found.", basePackage);
            return null;
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
        return candidates;
    }
}

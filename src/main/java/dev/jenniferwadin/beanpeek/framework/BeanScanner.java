package dev.jenniferwadin.beanpeek.framework;

import dev.jenniferwadin.beanpeek.annotation.MiniService;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
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
     * Scans a given base package and automatically registers all classes
     * annotated with @MiniService into the BeanContainer.
     *
     * @param basePackage the base package to scan (e.g., "dev.jenniferwadin.beanpeek.service")
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

            for (File file : Objects.requireNonNull(directory.listFiles())) {
                if (file.getName().endsWith(".class")) {
                    String className = basePackage + "." + file.getName().replace(".class", "");
                    Class<?> clazz = Class.forName(className);

                    if (clazz.isAnnotationPresent(MiniService.class)) {
                        log.info("Found MiniService: {}", clazz.getSimpleName());
                        beanContainer.registerBean(clazz);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to scan package {}: {}", basePackage, e.getMessage());
        }

    }
}

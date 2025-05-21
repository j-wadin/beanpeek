package dev.jenniferwadin.beanpeek.framework;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class MiniConfiguration {

    private final Map<String, String> values = new HashMap<>();

    public MiniConfiguration() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                log.warn("No application.properties found.");
                return;
            }

            Properties properties = new Properties();
            properties.load(input);

            for (String name : properties.stringPropertyNames()) {
                values.put(name, properties.getProperty(name));
            }

            log.info("Loaded {} config values", values.size());
        } catch (IOException e) {
            log.error("Failed to load configuration: {}", e.getMessage());
        }
    }

    public String get(String key) {
        return values.get(key);
    }

    public boolean contains(String key) {
        return values.containsKey(key);
    }
}

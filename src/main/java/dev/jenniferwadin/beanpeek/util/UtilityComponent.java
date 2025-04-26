package dev.jenniferwadin.beanpeek.util;

import dev.jenniferwadin.beanpeek.annotation.RunAutomatically;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UtilityComponent {

    @RunAutomatically
    public void maintenanceTask() {
        log.info("Performing maintenance task...");
    }
}

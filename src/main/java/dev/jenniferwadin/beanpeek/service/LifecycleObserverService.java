package dev.jenniferwadin.beanpeek.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LifecycleObserverService {

    public LifecycleObserverService() {
        log.info("Constructor called - Bean is being created.");
    }

    @PostConstruct
    public void init() {
        log.info("PostConstruct called - Bean is fully created and dependencies injected.");
    }

    @PreDestroy
    public void destroy() {
        log.info("PreDestroy called - Bean is about to be destroyed.");
    }
}

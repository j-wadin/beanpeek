package dev.jenniferwadin.beanpeek.service;

import dev.jenniferwadin.beanpeek.annotation.DemoBean;
import dev.jenniferwadin.beanpeek.annotation.RunAutomatically;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@DemoBean
@Slf4j
@Service
public class BasicService {

    @RunAutomatically
    public void showStartupMessage() {
        log.info("🚀 BasicService is active at startup!");
    }

    @RunAutomatically
    public void greetUser(String username) {
        log.info("👋 Hello, {}!", username);
    }
}

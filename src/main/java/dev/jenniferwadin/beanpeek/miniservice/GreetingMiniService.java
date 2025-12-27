package dev.jenniferwadin.beanpeek.miniservice;

import dev.jenniferwadin.beanpeek.annotation.MiniConfigProperty;
import dev.jenniferwadin.beanpeek.annotation.MiniPreDestroy;
import dev.jenniferwadin.beanpeek.annotation.MiniService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@MiniService
public class GreetingMiniService {

    private final HelloMiniService helloMiniService;
    @MiniConfigProperty("welcome.message")
    private String message;

    public GreetingMiniService(HelloMiniService helloMiniService) {
        this.helloMiniService = helloMiniService;
    }

    public void greet() {
        helloMiniService.sayHi();
        log.info(message);
    }

    @MiniPreDestroy
    public void cleanUp() {
        log.info("Cleaning up!");
    }
}

package dev.jenniferwadin.beanpeek.examples;

import dev.jenniferwadin.beanpeek.annotation.MiniPreDestroy;
import dev.jenniferwadin.beanpeek.annotation.MiniService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@MiniService
public class GreetingService {

    private final HelloService helloService;

    public GreetingService(HelloService helloService) {
        this.helloService = helloService;
    }

    public void greet() {
        helloService.sayHi();
        log.info("Greeting from GreetingService!");
    }

    @MiniPreDestroy
    public void cleanUp() {
        log.info("Cleaning up!");
    }
}

package dev.jenniferwadin.beanpeek.miniservice;

import dev.jenniferwadin.beanpeek.annotation.MiniPostConstruct;
import dev.jenniferwadin.beanpeek.annotation.MiniService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@MiniService
public class HelloMiniService {

    @MiniPostConstruct
    public void init() {
        log.info("Hello from a @MiniPostConstruct method!");
    }

    public void sayHi() {
        log.info("sayHi() was called manually.");
    }

}

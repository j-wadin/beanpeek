package dev.jenniferwadin.beanpeek.miniservice;

import dev.jenniferwadin.beanpeek.annotation.LogExecutionTime;
import dev.jenniferwadin.beanpeek.annotation.MiniService;

@MiniService
public class MathMiniServiceImpl implements MathMiniService {

    @Override
    @LogExecutionTime
    public int slowAddition(int a, int b) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return a + b;
    }
}

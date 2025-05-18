package dev.jenniferwadin.beanpeek.examples;

import dev.jenniferwadin.beanpeek.annotation.LogExecutionTime;
import dev.jenniferwadin.beanpeek.annotation.MiniService;

@MiniService
public class MathServiceImpl implements MathService{

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

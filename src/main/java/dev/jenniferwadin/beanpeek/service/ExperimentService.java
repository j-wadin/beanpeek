package dev.jenniferwadin.beanpeek.service;

import dev.jenniferwadin.beanpeek.annotation.DemoBean;
import dev.jenniferwadin.beanpeek.annotation.RunAutomatically;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@DemoBean
@Slf4j
@Service
public class ExperimentService {

    @RunAutomatically
    public void runExperiment() {
        log.info("Running a small experiment..");
    }

    @RunAutomatically
    public void helperMethod() {
        log.info("This method is not automatically run.");
    }
}

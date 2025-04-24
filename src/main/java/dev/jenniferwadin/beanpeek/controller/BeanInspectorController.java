package dev.jenniferwadin.beanpeek.controller;

import dev.jenniferwadin.beanpeek.reflection.BeanDetails;
import dev.jenniferwadin.beanpeek.service.BeanInspectorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inspect")
public class BeanInspectorController {

    private final BeanInspectorService beanInspectorService;

    public BeanInspectorController(BeanInspectorService beanInspectorService) {
        this.beanInspectorService = beanInspectorService;
    }

    @GetMapping("/beans")
    public List<String> getAllBeans() {
        return beanInspectorService.getAllBeanNames();
    }

    @GetMapping("/beans/{beanName}/methods")
    public List<String> getBeanMethods(@PathVariable String beanName) {
        return beanInspectorService.getBeanMethodNames(beanName);
    }

    @GetMapping("/beans/grouped")
    public Map<String, List<String>> getGroupedBeans() {
        return beanInspectorService.getGroupedBeans();
    }

    @GetMapping("/beans/{beanName}/details")
    public BeanDetails getBeanDetails(@PathVariable String beanName) {
        return beanInspectorService.getBeanDetails(beanName);
    }

}

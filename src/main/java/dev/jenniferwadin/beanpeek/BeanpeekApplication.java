package dev.jenniferwadin.beanpeek;

import dev.jenniferwadin.beanpeek.miniservice.GreetingMiniService;
import dev.jenniferwadin.beanpeek.miniservice.MathMiniService;
import dev.jenniferwadin.beanpeek.framework.BeanContainer;
import dev.jenniferwadin.beanpeek.framework.BeanScanner;
import dev.jenniferwadin.beanpeek.miniservice.HelloMiniService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BeanpeekApplication {

	public static void main(String[] args) throws NoSuchMethodException {

		BeanContainer beanContainer = new BeanContainer();
		BeanScanner beanScanner = new BeanScanner(beanContainer);
		beanScanner.scanAndRegister("dev.jenniferwadin.beanpeek.examples");

		HelloMiniService helloMiniService = beanContainer.getBean(HelloMiniService.class);
		helloMiniService.sayHi();

		GreetingMiniService greetingMiniService = beanContainer.getBean(GreetingMiniService.class);
		greetingMiniService.greet();

		MathMiniService mathMiniService = beanContainer.getBean(MathMiniService.class);
		int result = mathMiniService.slowAddition(2,3);
		log.info("Result: {}", result);

		beanContainer.registerShutdownHook();
	}

}

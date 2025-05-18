package dev.jenniferwadin.beanpeek;

import dev.jenniferwadin.beanpeek.examples.MathService;
import dev.jenniferwadin.beanpeek.framework.BeanContainer;
import dev.jenniferwadin.beanpeek.framework.BeanScanner;
import dev.jenniferwadin.beanpeek.examples.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class BeanpeekApplication {

	public static void main(String[] args) throws NoSuchMethodException {

		SpringApplication.run(BeanpeekApplication.class, args);

		BeanContainer beanContainer = new BeanContainer();
		BeanScanner beanScanner = new BeanScanner(beanContainer);
		beanScanner.scanAndRegister("dev.jenniferwadin.beanpeek.examples");

		HelloService helloService = beanContainer.getBean(HelloService.class);
		helloService.sayHi();

		MathService mathService = beanContainer.getBean(MathService.class);
		int result = mathService.slowAddition(2,3);
		log.info("Result: {}", result);

		beanContainer.registerShutdownHook();
	}

}

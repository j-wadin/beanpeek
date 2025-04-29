package dev.jenniferwadin.beanpeek;

import dev.jenniferwadin.beanpeek.framework.BeanContainer;
import dev.jenniferwadin.beanpeek.framework.BeanScanner;
import dev.jenniferwadin.beanpeek.service.HelloService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BeanpeekApplication {

	public static void main(String[] args) throws NoSuchMethodException {

		SpringApplication.run(BeanpeekApplication.class, args);

		BeanContainer beanContainer = new BeanContainer();
		BeanScanner beanScanner = new BeanScanner(beanContainer);
		beanScanner.scanAndRegister("dev.jenniferwadin.beanpeek.service");

		HelloService helloService = beanContainer.getBean(HelloService.class);
		helloService.sayHi();
	}

}

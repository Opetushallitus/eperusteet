package fi.vm.sade.eperusteet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource({"classpath*:spring/application-context.xml"})
public class EperusteetApplication { //extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(EperusteetApplication.class, args);
	}

}
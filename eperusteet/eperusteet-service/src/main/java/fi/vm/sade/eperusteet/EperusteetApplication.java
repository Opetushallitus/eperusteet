package fi.vm.sade.eperusteet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
public class EperusteetApplication {

	public static void main(String[] args) {
		SpringApplication.run(EperusteetApplication.class, args);
	}

}
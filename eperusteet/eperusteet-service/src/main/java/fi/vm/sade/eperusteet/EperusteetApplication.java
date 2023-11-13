package fi.vm.sade.eperusteet;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@ImportResource({"spring/application-context.xml"})
public class EperusteetApplication {

	public static void main(String[] args) {
		SpringApplication.run(EperusteetApplication.class, args);
	}

}
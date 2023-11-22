package fi.vm.sade.eperusteet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

@Profile("dev")
@Configuration
@ImportResource({"classpath*:spring/security-context-backend-dev.xml"})
public class WebSecurityConfigurationDev {
}

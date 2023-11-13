package fi.vm.sade.eperusteet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

@Profile("dev")
@ImportResource({"spring/security-context-backend-dev.xml"})
@Configuration
public class WebSecurityConfigurationDev {
}

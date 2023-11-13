package fi.vm.sade.eperusteet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

@Profile({"!dev & !test"})
@ImportResource({"spring/security-context-backend.xml"})
@Configuration
public class WebSecurityConfiguration {
}

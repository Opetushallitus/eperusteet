package fi.vm.sade.eperusteet.config;

import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;

@Profile({"!dev & !test"})
@Configuration
@ImportResource({"classpath*:spring/security-context-backend.xml"})
public class WebSecurityConfiguration {

}

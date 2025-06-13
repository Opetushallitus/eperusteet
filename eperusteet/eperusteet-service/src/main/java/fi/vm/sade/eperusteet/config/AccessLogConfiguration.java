package fi.vm.sade.eperusteet.config;

import org.apache.catalina.valves.AccessLogValve;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccessLogConfiguration {

    @Bean
    @ConditionalOnProperty(name = "logback.access")
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> containerCustomizer() {
        return container -> container.addContextCustomizers(context -> {
            AccessLogValve accessLogValve = new AccessLogValve();
            accessLogValve.setPattern("%h %l %u %t \"%r\" %s %b %D");
            accessLogValve.setDirectory("logs");
            accessLogValve.setPrefix("access_log");
            accessLogValve.setSuffix(".log");
            context.getPipeline().addValve(accessLogValve);
        });
    }
}
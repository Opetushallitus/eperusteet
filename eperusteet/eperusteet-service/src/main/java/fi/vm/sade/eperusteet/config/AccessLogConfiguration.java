package fi.vm.sade.eperusteet.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class AccessLogConfiguration {

//    @Bean
//    @ConditionalOnProperty(name = "logback.access")
//    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> containerCustomizer() {
//        return container -> container.addContextCustomizers(context -> {
//            LogbackValve logbackValve = new LogbackValve();
//            logbackValve.setFilename("logback-access.xml");
//            context.getPipeline().addValve(logbackValve);
//        });
//    }
}
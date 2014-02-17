package fi.vm.sade.eperusteet.resource.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import fi.vm.sade.eperusteet.dto.EntityReference;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 *
 * @author jhyoty
 */
@Configuration
@EnableWebMvc
//@EnableCaching
public class MvcConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    EntityManagerFactory emf;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/index.html").addResourceLocations("/index.html");
        super.addResourceHandlers(registry); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(converter());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //registry.addWebRequestInterceptor(openEntityManagerInViewInterceptor());
        super.addInterceptors(registry);
    }

    @Bean
    OpenEntityManagerInViewInterceptor openEntityManagerInViewInterceptor() {
        OpenEntityManagerInViewInterceptor i = new OpenEntityManagerInViewInterceptor();
        i.setEntityManagerFactory(emf);
        return i;
    }

    @Bean
    MappingJackson2HttpMessageConverter converter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setPrettyPrint(true);
        converter.getObjectMapper().setPropertyNamingStrategy(new PropertyNamingStrategy() {
            
            @Override
            public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method,
            String defaultName)
            {
                return tryToconvertFromMethodName(method, defaultName);
            }
            
            @Override
            public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method,
            String defaultName)
            {
                return tryToconvertFromMethodName(method, defaultName);
            }
            
            private String tryToconvertFromMethodName(AnnotatedMethod annotatedMethod, String defaultName) {
                if((annotatedMethod.getParameterCount() == 1 && EntityReference.class.isAssignableFrom(annotatedMethod.getParameter(0).getRawType()))
                        || EntityReference.class.isAssignableFrom(annotatedMethod.getRawReturnType())) {
                    defaultName = '_' + defaultName;
                }
                return defaultName;
            }
        });
        converter.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        converter.getObjectMapper().registerModule(new JodaModule());
        converter.getObjectMapper().registerModule(new EPerusteetMappingModule());
        converter.getObjectMapper().registerModule(new Hibernate4Module().enable(Hibernate4Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS));
        return converter;
    }

}

/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.resource.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.base.Optional;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.AbstractRakenneOsaDto;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import fi.vm.sade.eperusteet.dto.util.PerusteenOsaUpdateDto;
import fi.vm.sade.eperusteet.resource.util.CacheHeaderInterceptor;
import fi.vm.sade.eperusteet.resource.util.LoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.*;

import javax.persistence.EntityManagerFactory;
import java.lang.reflect.Type;
import java.util.List;

/**
 *
 * @author jhyoty
 */
@Configuration
@EnableWebMvc
public class MvcConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    EntityManagerFactory emf;

    @Override
    public void configurePathMatch(PathMatchConfigurer matcher) {
        matcher.setUseRegisteredSuffixPatternMatch(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/index.html").addResourceLocations("/index.html");
        registry.addResourceHandler("/internal.html").addResourceLocations("/internal.html");
        super.addResourceHandlers(registry); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(byteArrayConverter());
        converters.add(converter()); // keep last, will override any non-explicitely declared media types
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoggingInterceptor());
        registry.addInterceptor(new CacheHeaderInterceptor());
        super.addInterceptors(registry);
    }

    @Bean
    MappingJackson2HttpMessageConverter converter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setPrettyPrint(true);
        converter.getObjectMapper().enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        converter.getObjectMapper().enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        converter.getObjectMapper().setPropertyNamingStrategy(new PropertyNamingStrategy() {

            @Override
            public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method,
                String defaultName) {
                return getName(config, method.getGenericType(), defaultName);
            }

            @Override
            public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method,
                String defaultName) {
                return getName(config, method.getParameter(0).getGenericType(), defaultName);
            }

            private String getName(MapperConfig<?> config, Type type, String defaultName) {
                final JavaType ot = config.getTypeFactory().constructParametrizedType(Optional.class,
                        Optional.class, EntityReference.class);
                final JavaType et = config.getTypeFactory().constructType(EntityReference.class);
                final JavaType t = config.getTypeFactory().constructType(type);

                if (et.equals(t) || ot.equals(t)) {
                    return "_" + defaultName;
                }

                return defaultName;
            }
        });
        converter.getObjectMapper().registerModule(new JodaModule());
        converter.getObjectMapper().registerModule(new GuavaModule());
        EPerusteetMappingModule module = new EPerusteetMappingModule();
        module
            .addDeserializer(AbstractRakenneOsaDto.class, new AbstractRakenneOsaDeserializer())
            .addDeserializer(PerusteenOsaUpdateDto.class, new PerusteenOsaUpdateDtoDeserializer());
        converter.getObjectMapper().registerModule(module);
        return converter;
    }

    @Bean
    ByteArrayHttpMessageConverter byteArrayConverter() {
        ByteArrayHttpMessageConverter converter = new ByteArrayHttpMessageConverter();
        converter.setSupportedMediaTypes(MediaType.parseMediaTypes("application/pdf"));
        return converter;
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(20);
        executor.setThreadFactory(new CustomizableThreadFactory("AsyncThreadFactory-"));
        executor.afterPropertiesSet();

        configurer.setTaskExecutor(executor).setDefaultTimeout(120000);
    }
}

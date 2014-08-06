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

import com.fasterxml.classmate.GenericType;
import com.fasterxml.classmate.TypeResolver;
import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.models.alternates.Alternates;
import com.mangofactory.swagger.models.alternates.WildcardType;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;
import com.wordnik.swagger.model.ApiInfo;
import java.util.concurrent.Callable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author jhyoty
 */
@Configuration
@EnableSwagger
public class SwaggerConfig {

    @Autowired
    private SpringSwaggerConfig springSwaggerConfig;
    private static final String SWAGGER_GROUP = "eperusteet";

    @Bean
    public SwaggerSpringMvcPlugin swaggerPlugin() {
        final TypeResolver typeResolver = new TypeResolver();
        return new SwaggerSpringMvcPlugin(this.springSwaggerConfig)
            .apiInfo(apiInfo())
            .alternateTypeRules(
                Alternates.newRule(typeResolver.resolve(ResponseEntity.class, WildcardType.class), typeResolver.resolve(WildcardType.class)),
                Alternates.newRule(typeResolver.resolve(Page.class, WildcardType.class), typeResolver.resolve(PageImpl.class, WildcardType.class)),
                Alternates.newRule(typeResolver.resolve(new GenericType<Callable<ResponseEntity<Object>>>(){}), typeResolver.resolve(Object.class))
            );

    }

    /**
     * API Info as it appears on the swagger-ui page
     */
    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo(
            "Oppijan verkkopalvelukokonaisuus / ePerusteet",
            "Spring MVC API based on the swagger 1.2 spec",
            "https://confluence.csc.fi/display/oppija/Rajapinnat+toisen+asteen+ja+perusasteen+toimijoille",
            null,
            "EUPL 1.1",
            "http://ec.europa.eu/idabc/eupl"
        );
        return apiInfo;
    }

}

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
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import com.mangofactory.swagger.annotations.ApiIgnore;
import com.mangofactory.swagger.configuration.JacksonSwaggerSupport;
import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.core.ResourceGroupingStrategy;
import com.mangofactory.swagger.core.SwaggerCache;
import com.mangofactory.swagger.models.ModelProvider;
import com.mangofactory.swagger.models.alternates.AlternateTypeProvider;
import com.mangofactory.swagger.models.alternates.Alternates;
import com.mangofactory.swagger.models.dto.ApiInfo;
import com.mangofactory.swagger.models.dto.ResponseMessage;
import com.mangofactory.swagger.paths.RelativeSwaggerPathProvider;
import com.mangofactory.swagger.paths.SwaggerPathProvider;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerPluginAdapter;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;
import com.wordnik.swagger.annotations.ApiModelProperty;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.servlet.ServletContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 *
 * @author jhyoty
 */
@Configuration
@EnableSwagger
@Profile("default")
public class SwaggerConfig {

    @Autowired
    private SpringSwaggerConfig springSwaggerConfig;

    @Bean
    public SwaggerSpringMvcPlugin swaggerPlugin(ServletContext ctx) {

        RelativeSwaggerPathProvider relativeSwaggerPathProvider = new RelativeSwaggerPathProvider(ctx);
        relativeSwaggerPathProvider.setApiResourcePrefix("api");
        final TypeResolver typeResolver = new TypeResolver();

        SwaggerSpringMvcPlugin plugin = new SwaggerSpringMvcPlugin(this.springSwaggerConfig)
            .apiInfo(apiInfo())
            .excludeAnnotations(InternalApi.class)
            .pathProvider(relativeSwaggerPathProvider)
            .directModelSubstitute(fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto.class, LokalisoituTekstiDto.class)
            .directModelSubstitute(EntityReference.class, Long.class)
            .directModelSubstitute(JsonNode.class, Object.class)
            .genericModelSubstitutes(ResponseEntity.class, Optional.class)
            .alternateTypeRules(
                Alternates.newRule(typeResolver.resolve(new GenericType<Callable<ResponseEntity<Object>>>() {
                }), typeResolver.resolve(Object.class))
            )
            .swaggerGroup("public");
        return plugin;

    }

    @Bean
    public SwaggerSpringMvcPlugin swaggerPluginInternal(ServletContext ctx) {

        //Swagger korjaussarja
        SpringSwaggerConfig internalSwaggerConfig = new SpringSwaggerConfig() {

            @Override
            public List<RequestMappingHandlerMapping> swaggerRequestMappingHandlerMappings() {
                return springSwaggerConfig.swaggerRequestMappingHandlerMappings();
            }

            @Override
            public ResourceGroupingStrategy defaultResourceGroupingStrategy() {
                return springSwaggerConfig.defaultResourceGroupingStrategy();
            }

            @Override
            public SwaggerPathProvider defaultSwaggerPathProvider() {
                return springSwaggerConfig.defaultSwaggerPathProvider();
            }

            @Override
            public SwaggerCache swaggerCache() {
                return springSwaggerConfig.swaggerCache();
            }

            @Override
            public Set<Class> defaultIgnorableParameterTypes() {
                return springSwaggerConfig.defaultIgnorableParameterTypes();
            }

            @Override
            public AlternateTypeProvider defaultAlternateTypeProvider() {
                return springSwaggerConfig.defaultAlternateTypeProvider();
            }

            @Override
            public Map<RequestMethod, List<ResponseMessage>> defaultResponseMessages() {
                return springSwaggerConfig.defaultResponseMessages();
            }

            @Override
            public SwaggerPluginAdapter swaggerPluginAdapter() {
                return springSwaggerConfig.swaggerPluginAdapter();
            }

            @Override
            public ModelProvider defaultModelProvider() {
                return springSwaggerConfig.defaultModelProvider();
            }

            @Override
            public JacksonSwaggerSupport jacksonSwaggerSupport() {
                return springSwaggerConfig.jacksonSwaggerSupport();
            }

            //Swagger "muistaa" aiemmin määritellyt exclude-annotaatiot, joten pieleen menee....
            @Override
            public List<Class<? extends Annotation>> defaultExcludeAnnotations() {
                ArrayList<Class<? extends Annotation>> a = new ArrayList<>();
                a.add(ApiIgnore.class);
                return a;
            }

        };

        RelativeSwaggerPathProvider relativeSwaggerPathProvider = new RelativeSwaggerPathProvider(ctx);
        relativeSwaggerPathProvider.setApiResourcePrefix("api");
        final TypeResolver typeResolver = new TypeResolver();
        SwaggerSpringMvcPlugin plugin = new SwaggerSpringMvcPlugin(internalSwaggerConfig)
            .apiInfo(apiInfoInternal())
            .pathProvider(relativeSwaggerPathProvider)
            .directModelSubstitute(fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto.class, LokalisoituTekstiDto.class)
            .directModelSubstitute(EntityReference.class, Long.class)
            .directModelSubstitute(JsonNode.class, Object.class)
            .genericModelSubstitutes(ResponseEntity.class, Optional.class)
            .alternateTypeRules(
                Alternates.newRule(typeResolver.resolve(new GenericType<Callable<ResponseEntity<Object>>>() {
                }), typeResolver.resolve(Object.class))
            )
            .swaggerGroup("internal");

        return plugin;

    }

    /**
     * API Info as it appears on the swagger-ui page
     */
    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo(
            "Oppijan verkkopalvelukokonaisuus / ePerusteet julkinen rajapinta",
            "Spring MVC API based on the swagger 1.2 spec",
            "https://confluence.csc.fi/display/oppija/Rajapinnat+toisen+asteen+ja+perusasteen+toimijoille",
            null,
            "EUPL 1.1",
            "http://ec.europa.eu/idabc/eupl"
        );
        return apiInfo;
    }

    private ApiInfo apiInfoInternal() {
        ApiInfo apiInfo = new ApiInfo(
            "Oppijan verkkopalvelukokonaisuus / ePerusteet sisäinen rajapinta",
            "Spring MVC API based on the swagger 1.2 spec",
            "https://confluence.csc.fi/display/oppija/Rajapinnat+toisen+asteen+ja+perusasteen+toimijoille",
            null,
            "EUPL 1.1",
            "http://ec.europa.eu/idabc/eupl"
        );
        return apiInfo;
    }

    //swagger ei osaa esittää tätä järkevästi.
    @Getter
    @Setter
    public static class LokalisoituTekstiDto {

        @ApiModelProperty(required = false)
        private Long _id;
        @ApiModelProperty(required = true)
        private String fi;
        private String sv;
    }

}

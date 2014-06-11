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

import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mangofactory.swagger.configuration.JacksonScalaSupport;
import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.configuration.SwaggerGlobalSettings;
import com.mangofactory.swagger.core.SwaggerApiResourceListing;
import com.mangofactory.swagger.core.SwaggerPathProvider;
import com.mangofactory.swagger.models.alternates.Alternates;
import com.mangofactory.swagger.models.alternates.WildcardType;
import com.mangofactory.swagger.scanners.ApiListingReferenceScanner;
import com.wordnik.swagger.model.ApiInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author jhyoty
 */
@Configuration
@Import(SpringSwaggerConfig.class)
public class SwaggerConfig {

    @Autowired
    private SpringSwaggerConfig springSwaggerConfig;
    //@Autowired
    //private ModelProvider modelProvider;
    private static final String SWAGGER_GROUP = "eperusteet";

    /**
     * Adds the jackson scala module to the MappingJackson2HttpMessageConverter registered with spring Swagger core
     * models are scala so we need to be able to convert to JSON Also registers some custom serializers needed to
     * transform swagger models to swagger-ui required json format
     */
    @Bean
    public JacksonScalaSupport jacksonScalaSupport() {
        JacksonScalaSupport jacksonScalaSupport = new JacksonScalaSupport();
        //Set to false to disable
        jacksonScalaSupport.setRegisterScalaModule(true);
        return jacksonScalaSupport;
    }

    /**
     * Object mapper.
     *
     * @return the configured object mapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        //This is the opportunity to override object mapper behavior
        return new ObjectMapper();
    }

    /**
     * Global swagger settings
     */
    @Bean
    public SwaggerGlobalSettings swaggerGlobalSettings() {
        SwaggerGlobalSettings swaggerGlobalSettings = new SwaggerGlobalSettings();
        swaggerGlobalSettings.setGlobalResponseMessages(springSwaggerConfig.defaultResponseMessages());

        // This is where we add types to ignore (or use the default provided types)
        swaggerGlobalSettings.setIgnorableParameterTypes(springSwaggerConfig.defaultIgnorableParameterTypes());
        // This is where we add type substitutions (or use the default provided alternates)
        swaggerGlobalSettings.setAlternateTypeProvider(springSwaggerConfig.defaultAlternateTypeProvider());

        TypeResolver typeResolver = new TypeResolver();
        swaggerGlobalSettings.getAlternateTypeProvider().addRule(Alternates.newRule(
            typeResolver.resolve(ResponseEntity.class, WildcardType.class),
            typeResolver.resolve(WildcardType.class))
        );
        swaggerGlobalSettings.getAlternateTypeProvider().addRule(Alternates.newRule(
            typeResolver.resolve(Page.class, WildcardType.class),
            typeResolver.resolve(PageImpl.class, WildcardType.class))
        );

        return swaggerGlobalSettings;
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

    /**
     * Configure a SwaggerApiResourceListing for each swagger instance within your app. e.g. 1. private 2. external apis
     * Required to be a spring bean as spring will call the postConstruct method to bootstrap swagger scanning.
     *
     * @return
     */
    @Bean
    public SwaggerApiResourceListing swaggerApiResourceListing() {
        //The group name is important and should match the group set on ApiListingReferenceScanner
        //Note that swaggerCache() is by DefaultSwaggerController to serve the swagger json
        SwaggerApiResourceListing swaggerApiResourceListing = new SwaggerApiResourceListing(springSwaggerConfig.swaggerCache(), SWAGGER_GROUP);

        //Set the required swagger settings
        swaggerApiResourceListing.setSwaggerGlobalSettings(swaggerGlobalSettings());

        //Use a custom path provider or springSwaggerConfig.defaultSwaggerPathProvider()
        swaggerApiResourceListing.setSwaggerPathProvider(new PathProvider(springSwaggerConfig.defaultSwaggerPathProvider()));

        //Supply the API Info as it should appear on swagger-ui web page
        swaggerApiResourceListing.setApiInfo(apiInfo());

        //Every SwaggerApiResourceListing needs an ApiListingReferenceScanner to scan the spring request mappings
        swaggerApiResourceListing.setApiListingReferenceScanner(apiListingReferenceScanner());

        return swaggerApiResourceListing;
    }

    @Bean
    /**
     * The ApiListingReferenceScanner does most of the work. Scans the appropriate spring RequestMappingHandlerMappings
     * Applies the correct absolute paths to the generated swagger resources
     */
    public ApiListingReferenceScanner apiListingReferenceScanner() {
        ApiListingReferenceScanner apiListingReferenceScanner = new ApiListingReferenceScanner();
        //Must match the swagger group set on the SwaggerApiResourceListing
        apiListingReferenceScanner.setSwaggerGroup(SWAGGER_GROUP);

        //Picks up all of the registered spring RequestMappingHandlerMappings for scanning
        apiListingReferenceScanner.setRequestMappingHandlerMapping(springSwaggerConfig.swaggerRequestMappingHandlerMappings());

        //Excludes any controllers with the supplied annotations
        apiListingReferenceScanner.setExcludeAnnotations(springSwaggerConfig.defaultExcludeAnnotations());

        //How to group request mappings to ApiResource's typically by spring controller clesses or @Api.value()
        apiListingReferenceScanner.setResourceGroupingStrategy(springSwaggerConfig.defaultResourceGroupingStrategy());

        //Path provider used to generate the appropriate uri's
        apiListingReferenceScanner.setSwaggerPathProvider(new PathProvider(springSwaggerConfig.defaultSwaggerPathProvider()));

        return apiListingReferenceScanner;
    }

    static class PathProvider implements SwaggerPathProvider {

        private final SwaggerPathProvider delegate;

        public PathProvider(SwaggerPathProvider delegate) {
            this.delegate = delegate;
        }


        @Override
        public String getApiResourcePrefix() {
            return "/api";
        }

        @Override
        public String getAppBasePath() {
            return delegate.getAppBasePath();
        }

        @Override
        public String sanitizeRequestMappingPattern(String requestMappingPattern) {
            return delegate.sanitizeRequestMappingPattern(requestMappingPattern);
        }

    }

}

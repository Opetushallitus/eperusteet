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
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.AlternateTypeRules;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.paths.AbstractPathProvider;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger1.annotations.EnableSwagger;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.ServletContext;
import java.util.concurrent.Callable;

import static com.google.common.base.Predicates.not;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 *
 * @author jhyoty
 */
@Configuration
@EnableSwagger
@EnableSwagger2
@Profile("default")
public class SwaggerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SwaggerConfig.class);

    @Autowired
    private TypeResolver typeResolver;

    @Bean
    public Docket swaggerApi(ServletContext ctx) {
        LOG.info("Starting Swagger API");

        return new Docket(DocumentationType.SWAGGER_12)
                .apiInfo(apiInfo())
                .pathProvider(new RelativeSwaggerPathProvider(ctx))
                .select()
                .apis(not(RequestHandlerSelectors.withClassAnnotation(InternalApi.class)))
                .build()
                .directModelSubstitute(fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto.class,
                        LokalisoituTekstiDto.class)
                .directModelSubstitute(EntityReference.class, Long.class)
                .directModelSubstitute(JsonNode.class, Object.class)
                .alternateTypeRules(
                        AlternateTypeRules.newRule(
                                typeResolver.resolve(new GenericType<Callable<ResponseEntity<Object>>>() {}),
                                typeResolver.resolve(Object.class)
                        )
                );
    }

    @Bean
    public Docket swaggerInternalApi(ServletContext ctx) {
        LOG.info("Starting Swagger internal API");

        return new Docket(DocumentationType.SWAGGER_12)
                .apiInfo(apiInfo())
                .pathProvider(new RelativeSwaggerPathProvider(ctx))
                .directModelSubstitute(fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto.class,
                        LokalisoituTekstiDto.class)
                .directModelSubstitute(EntityReference.class, Long.class)
                .directModelSubstitute(JsonNode.class, Object.class)
                .alternateTypeRules(
                        AlternateTypeRules.newRule(
                                typeResolver.resolve(new GenericType<Callable<ResponseEntity<Object>>>() {}),
                                typeResolver.resolve(Object.class)
                        )
                )
                .groupName("internal");
    }


    /**
     * API Info as it appears on the swagger-ui page
     */
    private ApiInfo apiInfo() {
        Contact contact = null;
        return new ApiInfo(
            "Oppijan verkkopalvelukokonaisuus / ePerusteet julkinen rajapinta",
            "",
            "Spring MVC API based on the swagger 2.0 and 1.2 spec",
            "https://confluence.csc.fi/display/oppija/Rajapinnat+toisen+asteen+ja+perusasteen+toimijoille",
            contact,
            "EUPL 1.1",
            "http://ec.europa.eu/idabc/eupl"
        );
    }

    // Parsitaan lokalisoitu teksti manuaalisesti
    @Getter
    @Setter
    public static class LokalisoituTekstiDto {
        @ApiModelProperty(required = false)
        private Long _id;
        @ApiModelProperty(required = true)
        private String fi;
        private String sv;
    }

    private class RelativeSwaggerPathProvider extends AbstractPathProvider {
        String ROOT = "/";
        private final ServletContext servletContext;

        RelativeSwaggerPathProvider(ServletContext servletContext) {
            super();
            this.servletContext = servletContext;
        }

        @Override
        protected String applicationPath() {
            return isNullOrEmpty(servletContext.getContextPath())
                    ? ROOT : servletContext.getContextPath() + "/api";
        }

        @Override
        protected String getDocumentationPath() {
            return ROOT;
        }
    }
}

package fi.vm.sade.eperusteet.config;

import com.fasterxml.classmate.GenericType;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.AlternateTypeRules;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Callable;

import static com.google.common.base.Predicates.not;

@Configuration
@EnableSwagger2
@Profile("!test")
public class SwaggerConfig {

    @Bean
    public Docket api(TypeResolver typeResolver) {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .directModelSubstitute(JsonNode.class, Object.class)
                .genericModelSubstitutes(ResponseEntity.class, Optional.class)
                .forCodeGeneration(true)
                .select()
                .apis(not(RequestHandlerSelectors.withClassAnnotation(InternalApi.class)))
                .build()
                .alternateTypeRules(
                        AlternateTypeRules.newRule(
                                typeResolver.resolve(new GenericType<Callable<ResponseEntity<Object>>>() {
                                }),
                                typeResolver.resolve(Object.class)));
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "ePerusteet rajapinta",
                "",
                "Spring MVC API based on the swagger 2.0 and 1.2 specification",
                null,
                null,
                "EUPL 1.1",
                "http://ec.europa.eu/idabc/eupl",
                new ArrayList<>());
    }
}
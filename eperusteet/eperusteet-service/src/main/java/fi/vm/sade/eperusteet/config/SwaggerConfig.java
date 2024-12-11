package fi.vm.sade.eperusteet.config;

import com.fasterxml.jackson.databind.type.SimpleType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.customizers.PropertyCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
//@EnableSwagger2
//@Profile("!test")
public class SwaggerConfig {

//    @Bean
//    public Docket api(TypeResolver typeResolver) {
//        return new Docket(DocumentationType.SWAGGER_2)
//                .apiInfo(apiInfo())
//                .directModelSubstitute(JsonNode.class, Object.class)
//                .genericModelSubstitutes(ResponseEntity.class, Optional.class)
//                .forCodeGeneration(true)
//                .select()
//                .apis(not(RequestHandlerSelectors.withClassAnnotation(InternalApi.class)))
//                .build()
//                .alternateTypeRules(
//                        AlternateTypeRules.newRule(
//                                typeResolver.resolve(new GenericType<Callable<ResponseEntity<Object>>>() {
//                                }),
//                                typeResolver.resolve(Object.class)));
//    }

//    private ApiInfo apiInfo() {
//        return new ApiInfo(
//                "ePerusteet rajapinta",
//                "",
//                "Spring MVC API based on the swagger 2.0 and 1.2 specification",
//                null,
//                null,
//                "EUPL 1.1",
//                "http://ec.europa.eu/idabc/eupl",
//                new ArrayList<>());
//    }

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                .title("ePerusteet rajapinta")
                .description("Spring MVC API based on the swagger 3.0 specification")
                .version("v3.0.0")
                .license(new License().name("EUPL 1.1").url("https://interoperable-europe.ec.europa.eu/licence/european-union-public-licence-version-11-or-later-eupl")));
    }

    @Bean
    public PropertyCustomizer enumPropertyCustomizer() {
        return (schema, type) -> {
            Type javaType = type.getType();
            if (javaType instanceof SimpleType && ((SimpleType) javaType).isEnumType()) {
                Class<?> enumClass = ((SimpleType)javaType).getRawClass();
                schema.setEnum(Arrays.stream(enumClass.getEnumConstants())
                        .map(enumConstant -> ((Enum<?>) enumConstant).name())
                        .collect(Collectors.toList()));
            }
            return schema;
        };
    }

    @Bean
    public GroupedOpenApi externalOpenApi() {
        return GroupedOpenApi.builder()
                .group("external")
                .packagesToScan("fi.vm.sade.eperusteet.resource.julkinen")
                .pathsToMatch("/api/external/**")
                .build();
    }

//    @Bean
//    public OpenApiCustomiser customOpenApi() {
//        return openApi -> {
//            // Define the base path of the controller to keep visible
//            String visibleControllerBasePath = "/api/external";
//
//            // Filter paths to only include the ones that match the desired controller
//            openApi.setPaths(openApi.getPaths().entrySet().stream()
//                    .filter(entry -> entry.getKey().startsWith(visibleControllerBasePath))
//                    .collect(
//                            Paths::new,
//                            (paths, entry) -> paths.addPathItem(entry.getKey(), entry.getValue()),
//                            HashMap::putAll
//                    ));
//        };
//    }
}
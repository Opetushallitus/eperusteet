package fi.vm.sade.eperusteet.config;

import com.fasterxml.jackson.databind.type.SimpleType;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springdoc.core.customizers.PropertyCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .servers(
                        List.of(
                                new Server()
                                        .description("ePerusteet API")
                                        .url("https://virkailija.opintopolku.fi/eperusteet-service")
                        ))
                .info(new Info()
                .title("ePerusteet rajapinta")
                .description("Spring MVC API based on the swagger 3.0 specification")
                .version("v3.0.0")
                .license(new License().name("EUPL 1.1").url("https://interoperable-europe.ec.europa.eu/licence/european-union-public-licence-version-11-or-later-eupl")));
    }

    @Bean
    public PropertyCustomizer enumPropertyCustomizer() {
        return new PropertyCustomizer() {

            @Override
            public Schema customize(Schema schema, AnnotatedType type) {
                Type javaType = type.getType();
                if (javaType instanceof SimpleType && ((SimpleType) javaType).isEnumType()) {
                    Class<?> enumClass = ((SimpleType) javaType).getRawClass();
                    schema.setEnum(Arrays.stream(enumClass.getEnumConstants())
                            .map(enumConstant -> ((Enum<?>) enumConstant).name())
                            .collect(Collectors.toList()));
                }
                return schema;
            }
        };
    }

    @Bean
    public ParameterCustomizer enumParameterCustomizer() {
        return new ParameterCustomizer() {

            @Override
            public Parameter customize(Parameter parameter, MethodParameter methodParameter) {
                Class<?> paramType = methodParameter.getParameterType();
                if (paramType.isEnum()) {
                    parameter.getSchema().setEnum(Arrays.stream(paramType.getEnumConstants())
                            .map(enumConstant -> ((Enum<?>) enumConstant).name())  // Ensures uppercase
                            .collect(Collectors.toList()));
                }
                return parameter;
            }
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
}
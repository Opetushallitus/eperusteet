package fi.vm.sade.eperusteet.service.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import fi.vm.sade.eperusteet.config.ReferenceNamingStrategy;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

@Component
public class JsonMapperFactory implements FactoryBean<JsonMapper> {
    @Override
    public JsonMapper getObject() throws Exception {
        return ObjectMapperJsonMapperAdapter.of(
                Jackson2ObjectMapperBuilder
                        .json()
                        .featuresToEnable(
                                SerializationFeature.WRITE_ENUMS_USING_TO_STRING,
                                DeserializationFeature.READ_ENUMS_USING_TO_STRING
                        )
                        .modules(new Jdk8Module())
                        .propertyNamingStrategy(new ReferenceNamingStrategy())
                        .build()
        );
    }

    @Override
    public Class<?> getObjectType() {
        return JsonMapper.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}

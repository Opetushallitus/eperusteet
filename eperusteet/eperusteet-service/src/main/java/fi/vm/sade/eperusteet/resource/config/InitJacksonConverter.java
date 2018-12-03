package fi.vm.sade.eperusteet.resource.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.AbstractRakenneOsaDto;
import fi.vm.sade.eperusteet.dto.util.PerusteenOsaUpdateDto;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

public class InitJacksonConverter {
    static public void configureObjectMapper(ObjectMapper mapper) {
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        mapper.registerModule(new JodaModule());
        mapper.registerModule(new GuavaModule());
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(createMappingModule());
        mapper.setPropertyNamingStrategy(new ReferenceNamingStrategy());

    }

    static public MappingModule createMappingModule() {
        MappingModule module = new MappingModule();
        module.addDeserializer(AbstractRakenneOsaDto.class, new AbstractRakenneOsaDeserializer());
        module.addDeserializer(PerusteenOsaUpdateDto.class, new PerusteenOsaUpdateDtoDeserializer());
        return module;
    }

    static public MappingJackson2HttpMessageConverter createConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setPrettyPrint(true);
        configureObjectMapper(converter.getObjectMapper().enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING));
        return converter;
    }
}

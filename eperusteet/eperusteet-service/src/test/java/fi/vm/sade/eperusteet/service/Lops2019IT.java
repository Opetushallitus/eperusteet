package fi.vm.sade.eperusteet.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.Assert;

import java.io.IOException;

@Slf4j
@DirtiesContext
public class Lops2019IT {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private DtoMapper dtoMapper;

    @Before
    public void setup() {
        objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        objectMapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void readLops2019Peruste() throws IOException {
        PerusteKaikkiDto perusteDto = readPerusteFile("material/lops.json");
        Assert.notNull(perusteDto, "Perusteen lukeminen epäonnistui");
    }

    private PerusteKaikkiDto readPerusteFile(String file) throws IOException {
        Resource resource = new ClassPathResource(file);
        return objectMapper.readValue(resource.getFile(), PerusteKaikkiDto.class);
    }
}

package fi.vm.sade.eperusteet.service.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.eperusteet.dto.fakes.Referable;
import fi.vm.sade.eperusteet.dto.fakes.Referer;
import fi.vm.sade.eperusteet.dto.fakes.RefererDto;
import fi.vm.sade.eperusteet.config.InitJacksonConverter;
import fi.vm.sade.eperusteet.service.AbstractPerusteprojektiTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@Transactional
public class MappingTestIT extends AbstractPerusteprojektiTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TekstiPalanenConverter tekstiPalanenConverter;

    @Autowired
    private ReferenceableEntityConverter cachedEntityConverter;

    @Autowired
    private KoodistokoodiConverter koodistokoodiConverter;

    @Autowired
    private ArviointiConverter arviointiConverter;

    @Override
    @Before
    public void setup() {
        InitJacksonConverter.configureObjectMapper(objectMapper);
        super.setup();
    }

    // Tests mapping optional References from entity to json
    @Test
    public void testReferenceMapping() throws IOException {
        Referable a = new Referable();
        a.setId(41L);
        Referable b = new Referable();
        b.setId(42L);
        Referable c = new Referable();
        c.setId(43L);

        Referer referer = new Referer();
        referer.setJavaOptional(b);
        referer.setRef(c);

        RefererDto refererDto = mapper.map(referer, RefererDto.class);
        String mapped = objectMapper.writeValueAsString(refererDto);
        JsonNode node = objectMapper.readTree(mapped);
        assertThat(node.get("_ref").asText()).isEqualTo("43");
        assertThat(node.get("_javaOptional").asText()).isEqualTo("42");
    }
}

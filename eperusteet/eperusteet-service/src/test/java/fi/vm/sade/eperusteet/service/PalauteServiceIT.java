package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.PalauteDto;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
public class PalauteServiceIT extends AbstractIntegrationTest {

    @Autowired
    PalauteService palauteService;

    @Ignore
    @Test
    public void testSendMessage() throws Exception {
        palauteService.lahetaPalaute(PalauteDto.builder()
                .stars(3)
                .build());
    }
}

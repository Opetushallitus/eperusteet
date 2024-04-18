package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.PalauteStatus;
import fi.vm.sade.eperusteet.dto.PalauteDto;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import java.util.Date;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    public void testCRUD() {
        assertThat(palauteService.getPalauteStatus("opintopolku")).hasSize(0);

        Date createdAt = new Date();
        assertThat(palauteService.paivitaPalaute(
                PalauteDto.builder()
                        .stars(3)
                        .key("opintopolku")
                        .createdAt(createdAt)
                        .status(PalauteStatus.HYLATTY)
                        .build()))
                .extracting("stars", "key", "createdAt", "status")
                .doesNotContainNull()
                .containsExactly(3, "opintopolku", createdAt, PalauteStatus.HYLATTY);

        assertThat(palauteService.paivitaPalaute(
                PalauteDto.builder()
                        .stars(3)
                        .key("eperusteet")
                        .createdAt(createdAt)
                        .status(PalauteStatus.POISTETTU)
                        .build()))
                .extracting("stars", "key", "createdAt", "status")
                .doesNotContainNull()
                .containsExactly(3, "eperusteet", createdAt, PalauteStatus.POISTETTU);

        assertThat(palauteService.getPalauteStatus("opintopolku")).hasSize(1);
        assertThat(palauteService.getPalauteStatus("eperusteet")).hasSize(1);

        assertThat(palauteService.paivitaPalaute(
                PalauteDto.builder()
                        .stars(3)
                        .key("opintopolku")
                        .createdAt(createdAt)
                        .status(PalauteStatus.JATKOKEHITYS)
                        .build()))
                .extracting("stars", "key", "createdAt", "status")
                .doesNotContainNull()
                .containsExactly(3, "opintopolku", createdAt, PalauteStatus.JATKOKEHITYS);

        assertThat(palauteService.getPalauteStatus("opintopolku")).hasSize(1);
        assertThat(palauteService.getPalauteStatus("eperusteet")).hasSize(1);
    }
}

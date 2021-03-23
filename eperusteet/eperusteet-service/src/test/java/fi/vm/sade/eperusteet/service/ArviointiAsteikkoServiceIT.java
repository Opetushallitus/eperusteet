package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.OsaamistasoDto;
import fi.vm.sade.eperusteet.dto.arviointi.ArviointiAsteikkoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@DirtiesContext
public class ArviointiAsteikkoServiceIT extends AbstractIntegrationTest {

    @Dto
    @Autowired
    private DtoMapper mapper;

    @Autowired
    private ArviointiAsteikkoService arviointiAsteikkoService;

    @Test
    @Rollback
    public void testCreate() {
        assertThat(arviointiAsteikkoService.getAll()).hasSize(0);
        arviointiAsteikkoService.update(create());
        assertThat(arviointiAsteikkoService.getAll()).hasSize(2);
    }

    @Test
    @Rollback
    public void testCreate_koodiMissing() {
        assertThat(arviointiAsteikkoService.getAll()).hasSize(0);

        assertThatThrownBy(() -> arviointiAsteikkoService.update(Arrays.asList(
                ArviointiAsteikkoDto.builder()
                        .osaamistasot(Arrays.asList(
                                OsaamistasoDto.builder()
                                        .otsikko(LokalisoituTekstiDto.of("otsikko1"))
                                        .build(),
                                OsaamistasoDto.builder()
                                        .otsikko(LokalisoituTekstiDto.of("otsikko2"))
                                        .build()
                        ))
                        .build(),
                ArviointiAsteikkoDto.builder()
                        .osaamistasot(Arrays.asList(
                                OsaamistasoDto.builder()
                                        .otsikko(LokalisoituTekstiDto.of("otsikko2"))
                                        .koodi(KoodiDto.builder().koodisto("koodisto1").uri("uri1").build())
                                        .build(),
                                OsaamistasoDto.builder()
                                        .otsikko(LokalisoituTekstiDto.of("otsikko2"))
                                        .koodi(KoodiDto.builder().koodisto("koodisto1").uri("uri2").build())
                                        .build()
                        ))
                        .build()
        ))).hasMessage("osaamistaso-koodi-puuttuu");
    }

    @Test
    @Rollback
    public void testDelete() {
        assertThat(arviointiAsteikkoService.getAll()).hasSize(0);
        arviointiAsteikkoService.update(create());
        List<ArviointiAsteikkoDto> arviointiAsteikkoDtos = arviointiAsteikkoService.getAll();
        assertThat(arviointiAsteikkoDtos.get(0).getOsaamistasot()).hasSize(2);
        Long osaamistasoId = arviointiAsteikkoDtos.get(0).getOsaamistasot().get(1).getId();

        arviointiAsteikkoDtos.get(0).getOsaamistasot().remove(0);
        arviointiAsteikkoDtos = arviointiAsteikkoService.update(arviointiAsteikkoDtos);
        assertThat(arviointiAsteikkoDtos.get(0).getOsaamistasot()).hasSize(1);
        assertThat(arviointiAsteikkoDtos.get(0).getOsaamistasot().get(0).getId()).isEqualTo(osaamistasoId);

        Long arviointiasteikkoId = arviointiAsteikkoDtos.get(1).getId();
        arviointiAsteikkoDtos.remove(0);
        arviointiAsteikkoDtos = arviointiAsteikkoService.update(arviointiAsteikkoDtos);
        assertThat(arviointiAsteikkoDtos).hasSize(1);
        assertThat(arviointiAsteikkoDtos.get(0).getId()).isEqualTo(arviointiasteikkoId);
    }

    private List<ArviointiAsteikkoDto> create() {
        return Arrays.asList(
                ArviointiAsteikkoDto.builder()
                        .osaamistasot(Arrays.asList(
                                OsaamistasoDto.builder()
                                        .otsikko(LokalisoituTekstiDto.of("otsikko1"))
                                        .koodi(KoodiDto.builder().koodisto("arviointiasteikkoammatillinen15").uri("arviointiasteikkoammatillinen15_1").build())
                                        .build(),
                                OsaamistasoDto.builder()
                                        .otsikko(LokalisoituTekstiDto.of("otsikko2"))
                                        .koodi(KoodiDto.builder().koodisto("arviointiasteikkoammatillinen15").uri("arviointiasteikkoammatillinen15_2").build())
                                        .build()
                        ))
                        .build(),
                ArviointiAsteikkoDto.builder()
                        .osaamistasot(Arrays.asList(
                                OsaamistasoDto.builder()
                                        .otsikko(LokalisoituTekstiDto.of("otsikko2"))
                                        .koodi(KoodiDto.builder().koodisto("arviointiasteikkoammatillinen15").uri("arviointiasteikkoammatillinen15_1").build())
                                        .build(),
                                OsaamistasoDto.builder()
                                        .otsikko(LokalisoituTekstiDto.of("otsikko2"))
                                        .koodi(KoodiDto.builder().koodisto("arviointiasteikkoammatillinen15").uri("arviointiasteikkoammatillinen15_2").build())
                                        .build()
                        ))
                        .build());
    }
}

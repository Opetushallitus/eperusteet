package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.domain.arviointi.ArviointiAsteikko;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.OsaamistasoDto;
import fi.vm.sade.eperusteet.dto.arviointi.ArviointiAsteikkoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.*;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.DtoMapperImpl;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static fi.vm.sade.eperusteet.service.test.util.TestUtils.lt;

@Transactional
@DirtiesContext
public class TutkinnonOsaServiceIT extends AbstractIntegrationTest {

    @Autowired
    @Dto
    private DtoMapper mapper;

    private ArviointiAsteikkoDto asteikko;

    @Autowired
    public PerusteprojektiTestUtils ppTestUtils;

    @Before
    public void setup() {
        asteikko = ArviointiAsteikkoDto.builder()
                .id(1L)
                .osaamistaso(OsaamistasoDto.builder()
                        .id(2L)
                        .otsikko(lt("Kiitettävä"))
                        .build())
                .osaamistaso(OsaamistasoDto.builder()
                        .id(3L)
                        .otsikko(lt("Hyvä"))
                        .build())
                .osaamistaso(OsaamistasoDto.builder()
                        .id(4L)
                        .otsikko(lt("Tyydyttävä"))
                        .build())
                .build();
    }

    @Test
    public void tyyppikonversiotUusillaTutkinnonOsilla() {
        Ammattitaitovaatimus2018Dto opiskelija_osaa = Ammattitaitovaatimus2018Dto.builder()
                .koodi(1L)
                .nimi(lt("Opiskelija osaa"))
                .osaamistaso(Osaamistaso2018Dto.builder()
                        .kriteeri(new OsaamistasonKriteeri2018Dto(lt("Kriteeri 1")))
                        .kriteeri(new OsaamistasonKriteeri2018Dto(lt("Kriteeri 2")))
                        .kriteeri(new OsaamistasonKriteeri2018Dto(lt("Kriteeri 3")))
                        .build())
                .build();

        TutkinnonOsa2018Dto tosa = new TutkinnonOsa2018Dto();
        tosa.setAmmattitaitovaatimukset(Arrays.asList(opiskelija_osaa));
        tosa.setTyyppi(TutkinnonOsaTyyppi.TUTKINNONOSA2018);
        tosa.setKoodi(KoodiDto.of("tutkinnonosa", "123456"));

        TutkinnonOsaViiteDto<AbstractTutkinnonOsaDto> tov = TutkinnonOsaViiteDto.builder()
                .nimi(lt("Viitteen nimi"))
                .tutkinnonOsaDto(tosa)
                .build();


        TutkinnonOsa osa = mapper.map(tov.getTutkinnonOsaDto(), TutkinnonOsa.class);
        TutkinnonOsaViite viite = mapper.map(tov, TutkinnonOsaViite.class);
    }
}

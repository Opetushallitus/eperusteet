package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.views.TekstiHakuTulos;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.util.TekstiHakuTulosDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@DirtiesContext
public class TekstihakuServiceIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteprojektiTestUtils testUtils;

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private EntityManager em;

    @Autowired
    @LockCtx(TutkinnonRakenneLockContext.class)
    private LockService<TutkinnonRakenneLockContext> lockService;

    private Perusteprojekti projekti;
    private Peruste peruste;
    private Suoritustapa suoritustapa;
    private RakenneModuuli rakenne;

    @Before
    public void setup() {
        PerusteprojektiDto projektiDto = testUtils.createPerusteprojekti();
        projekti = perusteprojektiRepository.findOne(projektiDto.getId());
        peruste = projekti.getPeruste();
        suoritustapa = peruste.getSuoritustapa(Suoritustapakoodi.REFORMI);
        rakenne = suoritustapa.getRakenne();
    }

    @Test
    public void testMappingTulos() {
        TekstiHakuTulos tulos = TekstiHakuTulos.builder()
                .id(1L)
                .perusteprojekti(this.projekti)
                .peruste(this.peruste)
                .suoritustapa(this.suoritustapa)
                .pov(this.suoritustapa.getSisalto())
                .kieli(Kieli.FI)
                .teksti("hello")
                .build();

        TekstiHakuTulosDto tulosDto = mapper.map(tulos, TekstiHakuTulosDto.class);
        TekstiHakuTulos mapped = mapper.map(tulosDto, TekstiHakuTulos.class);
        assertThat(mapped)
                .extracting(
                        "id",
                        "perusteprojekti.id",
                        "peruste.id",
                        "suoritustapa.id",
                        "pov.id",
                        "tov.id",
                        "kieli",
                        "teksti")
                .containsExactly(
                        1L,
                        this.projekti.getId(),
                        this.peruste.getId(),
                        this.suoritustapa.getId(),
                        this.suoritustapa.getSisalto().getId(),
                        null,
                        Kieli.FI,
                        "hello");

    }

}

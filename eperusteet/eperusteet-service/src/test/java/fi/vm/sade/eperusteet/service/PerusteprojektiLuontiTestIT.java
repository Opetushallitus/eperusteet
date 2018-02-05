package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteHakuDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteQuery;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.repository.KoulutusRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@Transactional
public class PerusteprojektiLuontiTestIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteRepository repo;

    @Autowired
    private PlatformTransactionManager manager;

    @Autowired
    private KoulutusRepository koulutusRepository;

    @Autowired
    @LockCtx(TutkinnonRakenneLockContext.class)
    private LockService<TutkinnonRakenneLockContext> lockService;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusteprojektiTestUtils ppTestUtils;

    private Peruste peruste;

    @Test
    @Rollback
    public void testLuonnissaOlevatEiHakuun() {
//        PerusteKaikkiDto pohja = perusteService.getAmosaaYhteinenPohja();
//        assertThat(pohja).isNull();
//        PerusteprojektiDto perusteprojekti = ppTestUtils.createPeruste((PerusteprojektiLuontiDto pp) -> {
//        });
//        PerusteDto perusteDto = ppTestUtils.editPeruste(perusteprojekti.getPeruste().getIdLong(), (PerusteDto peruste) -> {
//            peruste.setDiaarinumero("amosaa/yhteiset");
//        });
//        ppTestUtils.asetaTila(perusteprojekti.getId(), ProjektiTila.VIIMEISTELY);
//        ppTestUtils.asetaTila(perusteprojekti.getId(), ProjektiTila.VALMIS);
        PerusteQuery pquery = new PerusteQuery();
        Page<PerusteHakuDto> perusteet = perusteService.findBy(new PageRequest(0, 10), pquery);
        assertThat(perusteet.getTotalElements()).isEqualTo(0);
    }


    @Test
    @Rollback
    public void testAmosaaJaettuPohja() {
//        PerusteKaikkiDto pohja = perusteService.getAmosaaYhteinenPohja();
//        assertThat(pohja).isNull();
//        PerusteprojektiDto perusteprojekti = ppTestUtils.createPeruste((PerusteprojektiLuontiDto pp) -> {
//        });
//        PerusteDto perusteDto = ppTestUtils.editPeruste(perusteprojekti.getPeruste().getIdLong(), (PerusteDto peruste) -> {
//            peruste.setDiaarinumero("amosaa/yhteiset");
//        });
////        ppTestUtils.asetaTila(perusteprojekti.getId(), ProjektiTila.VIIMEISTELY);
////        ppTestUtils.asetaTila(perusteprojekti.getId(), ProjektiTila.VALMIS);
        PerusteQuery pquery = new PerusteQuery();
        Page<PerusteHakuDto> perusteet = perusteService.findBy(new PageRequest(0, 10), pquery);
//        Set<PerusteHakuDto> filtered = StreamSupport.stream(perusteet.spliterator(), false)
//                .collect(Collectors.toSet());
//        assertThat(StreamSupport.stream(perusteet.spliterator(), false)
//                .filter(p -> p.getId() == perusteDto.getId())).isEmpty();
//        assertThat(StreamSupport.stream(perusteet.spliterator(), false)
    }

}

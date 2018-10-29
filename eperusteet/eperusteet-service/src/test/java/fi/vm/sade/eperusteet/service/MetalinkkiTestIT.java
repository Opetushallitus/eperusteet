package fi.vm.sade.eperusteet.service;


import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.dto.Metalink;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.MuodostumisSaantoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.GregorianCalendar;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@DirtiesContext
public class MetalinkkiTestIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteprojektiTestUtils ppTestUtils;

    @Autowired
    @LockCtx(TutkinnonRakenneLockContext.class)
    private LockService<TutkinnonRakenneLockContext> lockService;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Test
    public void testPerusteMetalinkki() {
        Peruste peruste = new Peruste();
        peruste.setId(42L);
//        assertThat(peruste.getMetalink())
//                .extracting(Metalink::resourceUrl, Metalink::esikatseluUrl)
//                .contains("/perusteet/42", "/#/fi/kooste/42");
    }

    @Test
    public void testTutkinnonOsaViiteMetalinkki() {
        PerusteprojektiDto projekti = ppTestUtils.createPerusteprojekti(perusteprojektiLuontiDto -> {
            perusteprojektiLuontiDto.setReforminMukainen(true);
            perusteprojektiLuontiDto.setKoulutustyyppi(KoulutusTyyppi.ERIKOISAMMATTITUTKINTO.toString());
        });
        PerusteDto perusteDto = ppTestUtils.initPeruste(projekti.getPeruste().getIdLong());

        lockService.lock(TutkinnonRakenneLockContext.of(perusteDto.getId(), Suoritustapakoodi.REFORMI));
        RakenneModuuliDto rakenne = mapper.map(TestUtils.rakenneModuuli()
                .laajuus(180)
                .build(), RakenneModuuliDto.class);
        perusteService.updateTutkinnonRakenne(perusteDto.getId(), Suoritustapakoodi.REFORMI, rakenne);
        PerusteKaikkiDto kokoSisalto = perusteService.getKokoSisalto(perusteDto.getId());
    }
}

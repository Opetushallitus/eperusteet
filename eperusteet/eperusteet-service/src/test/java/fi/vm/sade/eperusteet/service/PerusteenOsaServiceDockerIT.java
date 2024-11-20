package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.util.UpdateDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.test.AbstractDockerIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@ActiveProfiles(profiles = {"docker"})
@Transactional
@SpringBootTest
public class PerusteenOsaServiceDockerIT extends AbstractDockerIntegrationTest {

    @Autowired
    private PerusteenOsaService perusteenOsaService;

    @Autowired
    public PerusteprojektiTestUtils ppTestUtils;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteenOsaViiteService perusteenOsaViiteService;

    @Autowired
    private PerusteenOsaViiteRepository perusteenOsaViiteRepository;

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    private PerusteRepository perusteRepository;

    @Test
    public void testSamaTekstikappaleUseammassaPerusteessaKopiointiTallennettaessa() {
        startNewTransaction();

        PerusteprojektiDto projekti1 = ppTestUtils.createPerusteprojekti(ppl -> {
            ppl.setKoulutustyyppi(KoulutusTyyppi.VARHAISKASVATUS.toString());
            ppl.setTyyppi(PerusteTyyppi.NORMAALI);
        });

        PerusteenOsaViiteDto.Matala perusteenOsaViiteDto1 = perusteService.addSisaltoUUSI(
                projekti1.getPeruste().getIdLong(),
                null,
                new PerusteenOsaViiteDto.Matala(new TekstiKappaleDto()));
        PerusteenOsaViite viite1 = perusteenOsaViiteRepository.findOne(perusteenOsaViiteDto1.getId());

        PerusteprojektiDto projekti2 = ppTestUtils.createPerusteprojekti(ppl -> {
            ppl.setKoulutustyyppi(KoulutusTyyppi.VARHAISKASVATUS.toString());
            ppl.setTyyppi(PerusteTyyppi.NORMAALI);
        });

        PerusteenOsaViiteDto.Matala perusteenOsaViiteDto2 = perusteService.addSisaltoUUSI(
                projekti2.getPeruste().getIdLong(),
                null,
                new PerusteenOsaViiteDto.Matala(new TekstiKappaleDto()));

        PerusteenOsaViite viite2 = perusteenOsaViiteRepository.findOne(perusteenOsaViiteDto2.getId());
        viite2.setPerusteenOsa(viite1.getPerusteenOsa());
        perusteenOsaViiteRepository.save(viite2);

        perusteenOsaViiteDto2 =  perusteenOsaViiteService.getSisalto(projekti2.getPeruste().getIdLong(), viite2.getId(), PerusteenOsaViiteDto.Matala.class);
        assertThat(perusteenOsaViiteDto1.getPerusteenOsa().getId()).isEqualTo(perusteenOsaViiteDto2.getPerusteenOsa().getId());

        Perusteprojekti perusteProjekti1 = perusteprojektiRepository.findById(projekti1.getId()).get();
        perusteProjekti1.setTila(ProjektiTila.POISTETTU);
        perusteprojektiRepository.saveAndFlush(perusteProjekti1);

        perusteenOsaService.lock(perusteenOsaViiteDto2.getPerusteenOsa().getId());
        perusteenOsaService.update(projekti2.getPeruste().getIdLong(), perusteenOsaViiteDto2.getId(), new UpdateDto<>(perusteenOsaViiteDto2.getPerusteenOsa()));

        perusteenOsaViiteDto2 =  perusteenOsaViiteService.getSisalto(projekti2.getPeruste().getIdLong(), viite2.getId(), PerusteenOsaViiteDto.Matala.class);
        assertThat(perusteenOsaViiteDto1.getPerusteenOsa().getId()).isEqualTo(perusteenOsaViiteDto2.getPerusteenOsa().getId());

        perusteProjekti1.setTila(ProjektiTila.LAADINTA);
        perusteprojektiRepository.saveAndFlush(perusteProjekti1);

        startNewTransaction();

        perusteenOsaService.update(projekti2.getPeruste().getIdLong(), perusteenOsaViiteDto2.getId(), new UpdateDto<>(perusteenOsaViiteDto2.getPerusteenOsa()));

        perusteenOsaViiteDto2 =  perusteenOsaViiteService.getSisalto(projekti2.getPeruste().getIdLong(), viite2.getId(), PerusteenOsaViiteDto.Matala.class);
        assertThat(perusteenOsaViiteDto1.getPerusteenOsa().getId()).isNotEqualTo(perusteenOsaViiteDto2.getPerusteenOsa().getId());
    }
}

package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import jakarta.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@DirtiesContext
public class LukutaitokoulutusIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteenOsaViiteService perusteenOsaViiteService;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private PerusteprojektiTestUtils testUtils;

    @Autowired
    private EntityManager em;

    private Peruste peruste;

    @Before
    public void setUp() {
        PerusteprojektiDto projektiDto = testUtils.createPerusteprojekti(pp ->
                pp.setKoulutustyyppi(KoulutusTyyppi.LUKUTAITOKOULUTUS.toString()));
        peruste = perusteRepository.findOne(projektiDto.getPeruste().getIdLong());
    }

    @Test
    public void testPerusteenLuonti() {
        assertThat(peruste.getKoulutustyyppi()).isEqualTo(KoulutusTyyppi.LUKUTAITOKOULUTUS.toString());
        assertThat(peruste.getToteutus()).isEqualTo(KoulutustyyppiToteutus.KOTOUTUMISKOULUTUS);
        assertThat(peruste.getLukutaitokoulutusSisalto()).isNotNull();
        assertThat(peruste.getLukutaitokoulutusSisalto().getSisalto()).isNotNull();
        assertThat(peruste.getVstSisalto()).isNull();
        assertThat(peruste.getSisallot()).containsExactly(peruste.getLukutaitokoulutusSisalto());
    }

    @Test
    public void testTekstikappaleenLisays() {
        PerusteenOsaViite juuri = peruste.getLukutaitokoulutusSisalto().getSisalto();
        PerusteenOsaViiteDto.Matala added = lisaaTekstikappale(juuri, "Tekstikappale");

        assertThat(added.getId()).isNotNull();
        assertThat(added.getPerusteenOsa()).isInstanceOf(TekstiKappaleDto.class);

        em.flush();
        em.refresh(juuri);
        assertThat(juuri.getLapset()).hasSize(1);
        assertThat(juuri.getLapset().get(0).getPeruste().getId()).isEqualTo(peruste.getId());
        assertThat(peruste.containsViite(juuri.getLapset().get(0))).isTrue();
    }

    @Test
    public void testKloonaus() {
        lisaaTekstikappale(peruste.getLukutaitokoulutusSisalto().getSisalto(), "Pohjan tekstikappale");
        em.flush();

        PerusteprojektiDto uusiProjekti = testUtils.createPerusteprojekti(pp -> {
            pp.setKoulutustyyppi(KoulutusTyyppi.LUKUTAITOKOULUTUS.toString());
            pp.setPerusteId(peruste.getId());
        });
        Peruste uusi = perusteRepository.findOne(uusiProjekti.getPeruste().getIdLong());

        assertThat(uusi.getId()).isNotEqualTo(peruste.getId());
        assertThat(uusi.getVstSisalto()).isNull();
        assertThat(uusi.getLukutaitokoulutusSisalto()).isNotNull();
        assertThat(uusi.getLukutaitokoulutusSisalto().getId())
                .isNotEqualTo(peruste.getLukutaitokoulutusSisalto().getId());

        PerusteenOsaViite uusiJuuri = uusi.getLukutaitokoulutusSisalto().getSisalto();
        assertThat(uusiJuuri.getLapset()).hasSize(1);
        assertThat(uusiJuuri.getLapset().get(0).getPerusteenOsa()).isInstanceOf(TekstiKappale.class);
        assertThat(uusiJuuri.getLapset().get(0).getPerusteenOsa().getNimi().getTeksti().get(Kieli.FI))
                .isEqualTo("Pohjan tekstikappale");
    }

    private PerusteenOsaViiteDto.Matala lisaaTekstikappale(PerusteenOsaViite vanhempi, String nimi) {
        TekstiKappaleDto tekstikappale = new TekstiKappaleDto();
        tekstikappale.setNimi(LokalisoituTekstiDto.of(nimi));
        PerusteenOsaViiteDto.Matala viiteDto = new PerusteenOsaViiteDto.Matala();
        viiteDto.setPerusteenOsa(tekstikappale);
        return perusteenOsaViiteService.addSisalto(peruste.getId(), vanhempi.getId(), viiteDto);
    }
}

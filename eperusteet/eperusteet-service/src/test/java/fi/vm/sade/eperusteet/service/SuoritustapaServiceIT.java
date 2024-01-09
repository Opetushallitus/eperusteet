package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneOsa;
import fi.vm.sade.eperusteet.repository.SuoritustapaRepository;
import fi.vm.sade.eperusteet.service.internal.SuoritustapaService;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaRepository;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@DirtiesContext
public class SuoritustapaServiceIT extends AbstractIntegrationTest {

    @Autowired
    private SuoritustapaService suoritustapaService;

    @Autowired
    private TutkinnonOsaRepository tutkinnonOsaRepository;

    @Autowired
    private SuoritustapaRepository suoritustapaRepository;

    public SuoritustapaServiceIT() {
    }

    private TutkinnonOsaViite uusiTutkinnonOsaViite(Suoritustapa st) {
        TutkinnonOsa tosa = new TutkinnonOsa();
        tosa = tutkinnonOsaRepository.save(tosa);
        TutkinnonOsaViite tov = new TutkinnonOsaViite();
        tov.setTutkinnonOsa(tosa);
        tov.setSuoritustapa(st);
        return tov;
    }

    @Test
    @Rollback
    public void testCreateFromOther() {
        Suoritustapa st = suoritustapaService.createSuoritustapaWithSisaltoAndRakenneRoots(Suoritustapakoodi.OPS, LaajuusYksikko.OSAAMISPISTE);
        PerusteenOsaViite sisalto = st.getSisalto();

        List<PerusteenOsaViite> lapset = sisalto.getLapset();
        Set<TutkinnonOsaViite> tosat = st.getTutkinnonOsat();

        tosat.add(uusiTutkinnonOsaViite(st));
        tosat.add(uusiTutkinnonOsaViite(st));

        Suoritustapa stUusi = suoritustapaService.createFromOther(st.getId());

        List<PerusteenOsaViite> lapset1 = st.getSisalto().getLapset();
        List<PerusteenOsaViite> lapset2 = stUusi.getSisalto().getLapset();

        Assert.assertFalse(Objects.equals(st.getId(), stUusi.getId()));
        Assert.assertTrue(Objects.equals(st.getLaajuusYksikko(), stUusi.getLaajuusYksikko()));
        Assert.assertTrue(Objects.equals(st.getTutkinnonOsat().size(), stUusi.getTutkinnonOsat().size()));
    }

    @Test
    @Rollback
    public void testRakenteenKopiointi() {
        Suoritustapa st = suoritustapaService.createSuoritustapaWithSisaltoAndRakenneRoots(Suoritustapakoodi.REFORMI, LaajuusYksikko.OSAAMISPISTE);

        RakenneOsa osa = new RakenneOsa();
        osa.setErikoisuus("erikoisuus");
        osa.setPakollinen(true);
        osa.setKuvaus(TekstiPalanen.of(Kieli.FI, "kuvaus"));
        st.getRakenne().getOsat().add(osa);

        st = suoritustapaRepository.save(st);

        Suoritustapa uusi = suoritustapaService.createFromOther(st.getId());
        assertThat(uusi.getRakenne()).isNotNull();
        RakenneOsa uusiOsa = (RakenneOsa) uusi.getRakenne().getOsat().get(0);
        assertThat(uusiOsa.getPakollinen()).isTrue();
        assertThat(uusiOsa.getErikoisuus()).isEqualTo("erikoisuus");
        assertThat(uusiOsa.getKuvaus().getTeksti().get(Kieli.FI)).isEqualTo("kuvaus");
    }
}

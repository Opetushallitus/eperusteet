package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.PoistettuSisaltoDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import java.util.List;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PoistoServiceIT extends AbstractIntegrationTest {

    @Autowired
    private PoistoService poistoService;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteenOsaViiteService perusteenOsaViiteService;

    @Test
    public void tekstikappaleenPoistoOkTest() {
        TestTransaction.end();
        TestTransaction.start();
        TestTransaction.flagForCommit();

        PerusteprojektiDto pp = ppTestUtils.createPerusteprojekti(ppl -> ppl.setKoulutustyyppi(KoulutusTyyppi.VARHAISKASVATUS.toString()));
        PerusteDto peruste = ppTestUtils.initPeruste(pp.getPeruste().getIdLong());

        TekstiKappaleDto tekstiKappaleDto = new TekstiKappaleDto();
        tekstiKappaleDto.setNimi(LokalisoituTekstiDto.of("nimi1"));
        tekstiKappaleDto.setTeksti(LokalisoituTekstiDto.of("teksti1"));
        PerusteenOsaViiteDto.Matala viite = new PerusteenOsaViiteDto.Matala(tekstiKappaleDto);
        PerusteenOsaViiteDto.Matala uusi = perusteService.addSisaltoUUSI(peruste.getId(), Suoritustapakoodi.REFORMI, viite);

        PerusteenOsaViiteDto.Laaja sisalto = perusteService.getSuoritustapaSisalto(peruste.getId(), Suoritustapakoodi.REFORMI);
        assertThat(sisalto.getLapset()).hasSize(1);

        TestTransaction.end();
        TestTransaction.start();
        TestTransaction.flagForCommit(); // jotta poisto ilmestyy audit tauluun

        perusteenOsaViiteService.removeSisalto(peruste.getId(), uusi.getId(), false);

        TestTransaction.end();
        TestTransaction.start();

        sisalto = perusteService.getSuoritustapaSisalto(peruste.getId(), Suoritustapakoodi.REFORMI);
        assertThat(sisalto.getLapset()).hasSize(0);

        List<PoistettuSisaltoDto> poistetut = poistoService.getRemoved(peruste.getId());
        assertThat(poistetut).hasSize(1);

        poistoService.restore(peruste.getId(), poistetut.get(0).getId());
        sisalto = perusteService.getSuoritustapaSisalto(peruste.getId(), Suoritustapakoodi.REFORMI);
        assertThat(sisalto.getLapset()).hasSize(1);
        assertThat(sisalto.getLapset().get(0).getPerusteenOsa().getNimi().getTekstit().get(Kieli.FI)).isEqualTo("nimi1");
        assertThat(((TekstiKappaleDto)sisalto.getLapset().get(0).getPerusteenOsa()).getTeksti().getTekstit().get(Kieli.FI)).isEqualTo("teksti1");
        assertThat(poistoService.getRemoved(peruste.getId())).hasSize(0);
        TestTransaction.end();
    }
}

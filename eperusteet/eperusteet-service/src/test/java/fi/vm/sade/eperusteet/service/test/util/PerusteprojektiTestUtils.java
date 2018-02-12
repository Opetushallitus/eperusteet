package fi.vm.sade.eperusteet.service.test.util;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.MuodostumisSaanto;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@Service
@Transactional
public class PerusteprojektiTestUtils {
    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteRepository perusteRepository;

    public PerusteprojektiDto createPeruste() {
        return createPeruste((PerusteprojektiLuontiDto pp) -> {});
    }

    public PerusteprojektiDto createPeruste(Consumer<PerusteprojektiLuontiDto> withPerusteprojekti) {
        PerusteprojektiLuontiDto result = new PerusteprojektiLuontiDto();
        result.setNimi(TestUtils.uniikkiString());
        result.setKoulutustyyppi("koulutustyyppi_15");
        result.setLaajuusYksikko(LaajuusYksikko.OSAAMISPISTE);
        result.setReforminMukainen(true);
        result.setTyyppi(PerusteTyyppi.NORMAALI);
        result.setRyhmaOid("000");
        result.setDiaarinumero(TestUtils.uniikkiString());
        withPerusteprojekti.accept(result);
        PerusteprojektiDto pp = perusteprojektiService.save(result);
        return pp;
    }

    public PerusteDto editPeruste(Long perusteId) {
        return editPeruste(perusteId, (p) -> {});
    }

    public PerusteDto editPeruste(Long perusteId, Consumer<PerusteDto> perusteFn) {
        HashSet<Kieli> kielet = new HashSet<>();
        kielet.add(Kieli.FI);

        PerusteDto p = perusteService.get(perusteId);

        p.setVoimassaoloAlkaa((new GregorianCalendar(2017, 5, 4)).getTime());
        p.setNimi(TestUtils.lt("x"));
        p.getNimi().getTekstit().put(Kieli.FI, "ap_fi");
        p.getNimi().getTekstit().put(Kieli.SV, "ap_sv");
        p.setKielet(kielet);
        p.setDiaarinumero("OPH-" + Long.toString(TestUtils.uniikkiId()) + "-1234");
        perusteFn.accept(p);
        return perusteService.update(p.getId(), p);
    }

    public void asetaTila(Long projektiId, ProjektiTila tila) {
        Date siirtyma = (new GregorianCalendar(2099, 5, 4)).getTime();
        TilaUpdateStatus status = perusteprojektiService.updateTila(projektiId, tila, siirtyma);
        assertThat(status.isVaihtoOk()).isTrue();
    }

    public void julkaise(Long projektiId) {
        Date siirtyma = (new GregorianCalendar(2099, 5, 4)).getTime();
        TilaUpdateStatus status = perusteprojektiService.updateTila(projektiId, ProjektiTila.VIIMEISTELY, siirtyma);
        assertThat(status.isVaihtoOk()).isTrue();
        status = perusteprojektiService.updateTila(projektiId, ProjektiTila.VALMIS, siirtyma);
        assertThat(status.isVaihtoOk()).isTrue();
        status = perusteprojektiService.updateTila(projektiId, ProjektiTila.JULKAISTU, siirtyma);
        assertThat(status.isVaihtoOk()).isTrue();
    }

    public void asetaMuodostumiset(Long perusteId) {
        Peruste peruste = perusteRepository.findOne(perusteId);
        peruste.getSuoritustavat().stream().forEach(st -> {
            st.getRakenne().setMuodostumisSaanto(new MuodostumisSaanto(new MuodostumisSaanto.Laajuus(0, 180, LaajuusYksikko.OSAAMISPISTE), null));
        });
    }
}

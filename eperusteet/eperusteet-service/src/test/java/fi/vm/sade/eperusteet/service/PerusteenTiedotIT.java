package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;


@DirtiesContext
@Transactional
public class PerusteenTiedotIT extends AbstractPerusteprojektiTest {

    @Before
    public void setup() {
        super.setup();
    }

    @Test
    public void testPerusteprojektinMetatiedoissaTuleeOsaamisalat() {
        KoodiDto osaamisala = KoodiDto.of("osaamisalat", "1234");
        PerusteDto perusteDto = perusteService.get(this.peruste.getId());

        perusteDto.setOsaamisalat(new HashSet<>(Collections.singletonList(osaamisala)));
        PerusteDto updated = perusteService.update(this.peruste.getId(), perusteDto);
        this.uusiTekstiKappale(LokalisoituTekstiDto.of("oa nimi"), LokalisoituTekstiDto.of("oa teksti"), osaamisala);

        Map<Suoritustapakoodi, Map<String, List<TekstiKappaleDto>>> kuvaukset = perusteService.getOsaamisalaKuvaukset(this.peruste.getId());
        TekstiKappaleDto tk = kuvaukset.values().iterator().next().values().iterator().next().get(0);


        assertThat(tk.getNimi().get(Kieli.FI)).isEqualTo("oa nimi");
        assertThat(tk.getTeksti().get(Kieli.FI)).isEqualTo("oa teksti");
        assertThat(tk)
                .extracting("osaamisala.uri", "osaamisala.koodisto")
                .containsExactly(
                        osaamisala.getUri(),
                        osaamisala.getKoodisto());
    }

    @Test
    public void testPerusteprojektiValidators() {
        TilaUpdateStatus status = perusteprojektiService.validoiProjekti(projekti.getId(), ProjektiTila.LAADINTA);
        assertThat(status.isVaihtoOk()).isTrue();

        status = perusteprojektiService.validoiProjekti(projekti.getId(), ProjektiTila.JULKAISTU);
        assertThat(status)
                .returns(false, from(TilaUpdateStatus::isVaihtoOk));
        assertThat(status.getInfot())
                .extracting(TilaUpdateStatus.Status::getViesti)
                .contains(
                        "koulutuskoodi-puuttuu",
                        "kvliite-validointi-tyotehtavat-joissa-voi-toimia");
        status = perusteprojektiService.validoiProjekti(projekti.getId(), ProjektiTila.VIIMEISTELY);
        assertThat(status)
                .returns(false, from(TilaUpdateStatus::isVaihtoOk));
        status = perusteprojektiService.validoiProjekti(projekti.getId(), ProjektiTila.KOMMENTOINTI);
        assertThat(status)
                .returns(true, from(TilaUpdateStatus::isVaihtoOk));
        status = perusteprojektiService.validoiProjekti(projekti.getId(), ProjektiTila.POISTETTU);
        assertThat(status)
                .returns(true, from(TilaUpdateStatus::isVaihtoOk));
    }

}

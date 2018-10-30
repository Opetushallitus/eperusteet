package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.OsaamisalaDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;


@DirtiesContext
@Transactional
public class PerusteenTiedotIT extends AbstractPerusteprojektiTest {

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

}

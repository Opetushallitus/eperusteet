package fi.vm.sade.eperusteet.service.dokumentti.impl.util;

import fi.vm.sade.eperusteet.domain.AIPEOpetuksenSisalto;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.dto.peruste.KVLiiteJulkinenDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class DokumenttiPeruste extends DokumenttiBase {
    CharapterNumberGenerator generator;
    PerusteenOsaViite sisalto;
    AIPEOpetuksenSisalto aipeOpetuksenSisalto;
    KVLiiteJulkinenDto kvLiiteJulkinenDto;

    public LaajuusYksikko getLaajuusYksikko() {
        Set<Suoritustapa> suoritustavat = this.getPeruste().getSuoritustavat();
        if (!suoritustavat.isEmpty()) {
            for (Suoritustapa suoritustapa : suoritustavat) {
                return suoritustapa.getLaajuusYksikko();
            }
            return LaajuusYksikko.OSAAMISPISTE;
        }

        return LaajuusYksikko.OSAAMISPISTE;
    }
}

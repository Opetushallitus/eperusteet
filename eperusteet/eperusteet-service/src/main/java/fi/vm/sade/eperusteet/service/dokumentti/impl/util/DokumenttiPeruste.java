/*
 * Copyright (c) 2013 The Finnish National Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.service.dokumentti.impl.util;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.peruste.KVLiiteJulkinenDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author isaul
 */
@Getter
@Setter
public class DokumenttiPeruste extends DokumenttiBase {
    CharapterNumberGenerator generator;
    PerusteenOsaViite sisalto;
    AIPEOpetuksenSisalto aipeOpetuksenSisalto;
    KVLiiteJulkinenDto kvLiiteJulkinenDto;
    Map<Suoritustapakoodi, Map<String, List<TekstiKappaleDto>>> osaamisalaKuvaukset;

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

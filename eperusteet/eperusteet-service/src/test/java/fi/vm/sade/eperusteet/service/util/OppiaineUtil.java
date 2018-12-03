/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.service.util;

import fi.vm.sade.eperusteet.dto.util.EntityReference;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineDto;
import fi.vm.sade.eperusteet.service.yl.OppiaineOpetuksenSisaltoTyyppi;
import fi.vm.sade.eperusteet.service.yl.OppiaineService;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


/**
 * User: tommiratamaa
 * Date: 21.9.15
 * Time: 17.13
 */
public class OppiaineUtil {
    private OppiaineUtil() {
    }

    @Getter
    @Setter
    public static class Reference<T> {
        private T id;
    }

    @Getter
    public static class OppiaineLuontiPuu {
        private final LokalisoituTekstiDto teksti;
        private final List<OppiaineLuontiPuu> lapset = new ArrayList<>();
        private Long id;
        private OppiaineLuontiPuu vanhempi;
        private boolean koosteinen = false;
        private Reference<Long> idRef;

        public OppiaineLuontiPuu(LokalisoituTekstiDto teksti) {
            this.teksti = teksti;
        }

        public OppiaineLuontiPuu maara(OppiaineLuontiPuu lapsi) {
            lapsi.vanhempi = this.koosteinen();
            lapset.add(lapsi);
            return this;
        }

        public OppiaineLuontiPuu koosteinen() {
            this.koosteinen = true;
            return this;
        }

        public OppiaineLuontiPuu as(Reference<Long> idRef) {
            this.idRef = idRef;
            return this;
        }

        public OppiaineDto luo(OppiaineService service, Long perusteId, OppiaineOpetuksenSisaltoTyyppi tyyppi) {
            OppiaineDto luotava = oppaine(teksti, vanhempi == null ? null : vanhempi.id);
            luotava.setKoosteinen(of(this.koosteinen));
            OppiaineDto luotu = service.addOppiaine(perusteId, luotava, tyyppi);
            this.id = luotu.getId();
            if (this.idRef != null) {
                this.idRef.setId(luotu.getId());
            }
            this.lapset.forEach(lapsi -> lapsi.luo(service, perusteId, tyyppi));
            return luotu;
        }
    }

    public static OppiaineLuontiPuu oppiaine(LokalisoituTekstiDto nimi) {
        return new OppiaineLuontiPuu(nimi);
    }

    public static OppiaineDto oppaine(LokalisoituTekstiDto nimi, Long parentId) {
        OppiaineDto dto = new OppiaineDto();
        dto.setNimi(of(nimi));
        dto.setOppiaine(parentId == null ? absent() : of(new EntityReference(parentId)));
        return dto;
    }



}

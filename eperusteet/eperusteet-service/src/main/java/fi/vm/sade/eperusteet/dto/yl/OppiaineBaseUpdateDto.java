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

package fi.vm.sade.eperusteet.dto.yl;

import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

/**
 * User: tommiratamaa
 * Date: 3.11.2015
 * Time: 18.43
 */
@Getter
@Setter
public abstract class OppiaineBaseUpdateDto extends OppiaineBaseDto {
    private String koodiUri;
    private String koodiArvo;

    private TekstiOsaDto tehtava;
    private TekstiOsaDto tavoitteet;
    private TekstiOsaDto arviointi;

    private LokalisoituTekstiDto pakollinenKurssiKuvaus;
    private LokalisoituTekstiDto syventavaKurssiKuvaus;
    private LokalisoituTekstiDto soveltavaKurssiKuvaus;

    private Boolean partial;

    public TekstiOsaDto getOsa(Oppiaine.OsaTyyppi tyyppi) {
        switch (tyyppi) {
            case arviointi:
                return arviointi;
            case tavoitteet:
                return tavoitteet;
            case tehtava:
                return tehtava;
            default:
                return null;
        }
    }
}

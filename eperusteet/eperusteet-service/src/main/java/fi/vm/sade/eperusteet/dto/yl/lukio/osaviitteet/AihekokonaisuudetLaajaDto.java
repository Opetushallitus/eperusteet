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

package fi.vm.sade.eperusteet.dto.yl.lukio.osaviitteet;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto.Laaja;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.yl.lukio.AihekokonaisuusDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * User: tommiratamaa
 * Date: 19.10.15
 * Time: 13.17
 */
@Getter
@Setter
@JsonTypeName("aihekokonaisuudet")
public class AihekokonaisuudetLaajaDto extends Laaja {
    private UUID uuidTunniste;
    private LokalisoituTekstiDto otsikko;
    private LokalisoituTekstiDto yleiskuvaus;
    private List<AihekokonaisuusDto> aihekokonaisuudet;

    public AihekokonaisuudetLaajaDto() {
    }

    public AihekokonaisuudetLaajaDto(LokalisoituTekstiDto nimi, PerusteTila tila, PerusteenOsaTunniste tunniste) {
        super(nimi, tila, tunniste);
    }

    public String getOsanTyyppi() {
        return "aihekokonaisuudet";
    }
}

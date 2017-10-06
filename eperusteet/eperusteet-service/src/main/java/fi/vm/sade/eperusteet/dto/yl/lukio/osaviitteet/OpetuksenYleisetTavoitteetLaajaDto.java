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
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * User: tommiratamaa
 * Date: 21.10.15
 * Time: 20.02
 */
@Getter
@Setter
@JsonTypeName("opetuksenyleisettavoitteet")
public class OpetuksenYleisetTavoitteetLaajaDto extends PerusteenOsaDto.Laaja {
    private UUID uuidTunniste;
    private LokalisoituTekstiDto otsikko;
    private LokalisoituTekstiDto kuvaus;

    public OpetuksenYleisetTavoitteetLaajaDto() {
    }

    public OpetuksenYleisetTavoitteetLaajaDto(LokalisoituTekstiDto nimi, PerusteTila tila, PerusteenOsaTunniste tunniste) {
        super(nimi, tila, tunniste);
    }

    public String getOsanTyyppi() {
        return "opetuksenyleisettavoitteet";
    }
}

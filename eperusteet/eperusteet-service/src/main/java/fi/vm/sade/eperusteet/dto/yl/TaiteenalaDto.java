/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.dto.yl;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.dto.KevytTekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeName("taiteenala")
public class TaiteenalaDto extends PerusteenOsaDto.Laaja {
    public TaiteenalaDto() {
    }

    public TaiteenalaDto(LokalisoituTekstiDto nimi, PerusteTila tila, PerusteenOsaTunniste tunniste) {
        super(nimi, tila, tunniste);
    }

    private LokalisoituTekstiDto teksti;
    private KoodiDto koodi;
    private KevytTekstiKappaleDto kasvatus;
    private KevytTekstiKappaleDto yhteisetOpinnot;
    private KevytTekstiKappaleDto teemaopinnot;
    private KevytTekstiKappaleDto aikuistenOpetus;
    private KevytTekstiKappaleDto tyotavatOpetuksessa;
    private KevytTekstiKappaleDto oppimisenArviointiOpetuksessa;

    @Override
    public String getOsanTyyppi() {
        return "taiteenala";
    }
}

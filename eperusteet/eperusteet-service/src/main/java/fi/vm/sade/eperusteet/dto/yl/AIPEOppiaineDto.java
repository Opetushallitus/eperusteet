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

import com.google.common.base.Optional;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 *
 * @author nkala
 */
@Getter
@Setter
public class AIPEOppiaineDto extends AIPEOppiaineSuppeaDto {
    private Optional<TekstiOsaDto> tehtava;
    private Optional<TekstiOsaDto> arviointi;
    private Optional<KoodiDto> koodi;
    private Optional<TekstiOsaDto> tyotavat;
    private Optional<TekstiOsaDto> ohjaus;
    private Optional<TekstiOsaDto> sisaltoalueinfo;

    private Optional<LokalisoituTekstiDto> pakollinenKurssiKuvaus;
    private Optional<LokalisoituTekstiDto> syventavaKurssiKuvaus;
    private Optional<LokalisoituTekstiDto> soveltavaKurssiKuvaus;

    private List<OpetuksenTavoiteDto> tavoitteet;

    private Optional<List<KeskeinenSisaltoalueDto>> sisaltoalueet;

    private Optional<List<AIPEKurssiSuppeaDto>> kurssit;
}

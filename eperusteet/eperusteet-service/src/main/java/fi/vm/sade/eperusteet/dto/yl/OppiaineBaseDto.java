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

import com.fasterxml.jackson.annotation.JsonInclude;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.ReferenceableDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 *
 * @author jhyoty
 */
@Getter
@Setter
public abstract class OppiaineBaseDto implements ReferenceableDto {
    private Long id;
    private UUID tunniste;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    //"äitioppiaine" jos kyseessä on oppiaineen oppimäärä
    private Optional<Reference> oppiaine;
    private Optional<Boolean> koosteinen;
    private Optional<Boolean> abstrakti;
    private Optional<LokalisoituTekstiDto> nimi;
    private Optional<Long> jnro;
    private Date muokattu;
}

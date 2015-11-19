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

package fi.vm.sade.eperusteet.dto.yl.lukio;

import fi.vm.sade.eperusteet.dto.yl.OppiaineBaseDto;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * User: tommiratamaa
 * Date: 4.11.2015
 * Time: 11.32
 */
@Getter
public class LukioOpetussuunnitelmaRakenneRevisionDto<OppiaineType extends OppiaineBaseDto> {
    private final Long perusteId;
    private final Integer rakenneRevision;
    private final List<LukiokurssiListausDto> kurssit = new ArrayList<>();
    private final List<OppiaineType> oppiaineet = new ArrayList<>();

    public LukioOpetussuunnitelmaRakenneRevisionDto(Long perusteId, Integer rakenneRevision) {
        this.perusteId = perusteId;
        this.rakenneRevision = rakenneRevision;
    }
}

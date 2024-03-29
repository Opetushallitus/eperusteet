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

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * User: tommiratamaa
 * Date: 29.9.15
 * Time: 15.43
 */
@Getter
@EqualsAndHashCode
public class OppiaineKurssiHakuDto {
    private final Long oppiaineId;
    private final Long kurssiId;
    private final Long oppiaineNimiId;
    private final Integer jarjestys;

    public OppiaineKurssiHakuDto(Long oppiaineId, Long kurssiId, Integer jarjestys, Long oppiaineNimiId) {
        this.oppiaineId = oppiaineId;
        this.kurssiId = kurssiId;
        this.jarjestys = jarjestys;
        this.oppiaineNimiId = oppiaineNimiId;
    }
}

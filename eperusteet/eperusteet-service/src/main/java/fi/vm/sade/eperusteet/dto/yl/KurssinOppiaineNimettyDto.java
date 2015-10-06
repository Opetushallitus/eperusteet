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

import fi.vm.sade.eperusteet.dto.util.Lokalisoitava;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import lombok.Getter;
import lombok.Setter;

import java.util.stream.Stream;

import static fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto.localizeLaterById;

/**
 * User: tommiratamaa
 * Date: 5.10.15
 * Time: 20.41
 */
@Getter
@Setter
public class KurssinOppiaineNimettyDto extends KurssinOppiaineDto implements Lokalisoitava {
    protected LokalisoituTekstiDto oppiaineNimi;

    public KurssinOppiaineNimettyDto() {
    }

    public KurssinOppiaineNimettyDto(Long oppiaineId, Integer jarjestys, Long oppiaineNimiId) {
        super(oppiaineId, jarjestys);
        this.oppiaineNimi = localizeLaterById(oppiaineNimiId);
    }

    @Override
    public Stream<LokalisoituTekstiDto> lokalisoitavatTekstit() {
        return Stream.of(oppiaineNimi);
    }
}

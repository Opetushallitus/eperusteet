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

package fi.vm.sade.eperusteet.service.mapping;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;

/**
 * For conveniance if you want to convert different DTOs to others in test etc.
 *
 * User: tommiratamaa
 * Date: 6.10.15
 * Time: 13.54
 */
public class LokalisoituTekstiDtoCopyConverter extends BidirectionalConverter<LokalisoituTekstiDto,LokalisoituTekstiDto> {
    @Override
    public LokalisoituTekstiDto convertTo(LokalisoituTekstiDto source, Type<LokalisoituTekstiDto> destinationType) {
        return convertFrom(source, destinationType);
    }

    @Override
    public LokalisoituTekstiDto convertFrom(LokalisoituTekstiDto source, Type<LokalisoituTekstiDto> destinationType) {
        return new LokalisoituTekstiDto(source.getId(), source.getTekstit());
    }
}

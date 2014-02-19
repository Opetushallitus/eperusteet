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

package fi.vm.sade.eperusteet.service.mapping;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.KoodistoMetadataDto;
import fi.vm.sade.eperusteet.dto.LokalisoituTekstiDto;
import java.util.EnumMap;
import java.util.Map;
import ma.glasnost.orika.Converter;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;

/**
 *
 * @author harrik
 */
public class KoodistoMetadataConverter {
    
    public static final Converter<KoodistoMetadataDto[], LokalisoituTekstiDto> TO_MAP = new CustomConverter<KoodistoMetadataDto[], LokalisoituTekstiDto>() {

        @Override
        public LokalisoituTekstiDto convert(KoodistoMetadataDto[] s, Type<? extends LokalisoituTekstiDto> type) {
            
            Map<Kieli, String> nimi = new EnumMap<>(Kieli.class);
            for (KoodistoMetadataDto metadata : s) {
                nimi.put(Kieli.of(metadata.getKieli()), metadata.getNimi());
            }
            return new LokalisoituTekstiDto(null, nimi);
        }
    };
    
}

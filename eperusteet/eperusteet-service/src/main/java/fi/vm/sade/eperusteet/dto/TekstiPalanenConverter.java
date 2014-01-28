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
package fi.vm.sade.eperusteet.dto;

import fi.vm.sade.eperusteet.domain.LokalisoituTeksti;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import ma.glasnost.orika.Converter;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;

/**
 *
 * @author jhyoty
 */
public class TekstiPalanenConverter {

    public static final Converter<TekstiPalanen, LokalisoituTekstiDto> TO_MAP = new CustomConverter<TekstiPalanen, LokalisoituTekstiDto>() {

        @Override
        public LokalisoituTekstiDto convert(TekstiPalanen s, Type<? extends LokalisoituTekstiDto> type) {
            LokalisoituTekstiDto teksti = new LokalisoituTekstiDto();
            for (LokalisoituTeksti t : s.getTeksti()) {
                teksti.put(t.getKieli(), t.getTeksti());
            }
            return teksti;
        }
    };
}

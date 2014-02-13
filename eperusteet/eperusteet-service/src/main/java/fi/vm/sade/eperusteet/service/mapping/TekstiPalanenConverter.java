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
import fi.vm.sade.eperusteet.domain.LokalisoituTeksti;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import java.util.HashMap;
import java.util.Map;
import ma.glasnost.orika.converter.BidirectionalConverter;
import fi.vm.sade.eperusteet.dto.LokalisoituTekstiDto;
import ma.glasnost.orika.metadata.Type;

/**
 *
 * @author jhyoty
 */
public class TekstiPalanenConverter extends BidirectionalConverter<TekstiPalanen, LokalisoituTekstiDto> {

    @Override
    public LokalisoituTekstiDto convertTo(TekstiPalanen tekstiPalanen, Type<LokalisoituTekstiDto> type) {
        LokalisoituTekstiDto dto = new LokalisoituTekstiDto();
        
        for(LokalisoituTeksti teksti : tekstiPalanen.getTeksti()) {
            dto.put(teksti.getKieli(), teksti.getTeksti());
        }
        
        return dto;
    }

    @Override
    public TekstiPalanen convertFrom(LokalisoituTekstiDto dto, Type<TekstiPalanen> type) {
        
        Map<Kieli, LokalisoituTeksti> tekstit = new HashMap<>();
        
        for(Kieli kieli : dto.keySet()) {
            tekstit.put(kieli, new LokalisoituTeksti(kieli, dto.get(kieli)));
        }
        
        return new TekstiPalanen(tekstit);
    }
}

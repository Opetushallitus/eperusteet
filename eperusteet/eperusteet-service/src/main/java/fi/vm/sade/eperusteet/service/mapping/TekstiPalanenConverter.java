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
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import ma.glasnost.orika.converter.BidirectionalConverter;
import fi.vm.sade.eperusteet.dto.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.TekstiPalanenRepository;
import java.util.Map;
import ma.glasnost.orika.metadata.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author jhyoty
 */
@Component
public class TekstiPalanenConverter extends BidirectionalConverter<TekstiPalanen, LokalisoituTekstiDto> {

    @Autowired
    private TekstiPalanenRepository repository;

    @Override
    public LokalisoituTekstiDto convertTo(TekstiPalanen tekstiPalanen, Type<LokalisoituTekstiDto> type) {
        return new LokalisoituTekstiDto(tekstiPalanen.getId(), tekstiPalanen.getTeksti());
    }

    @Override
    public TekstiPalanen convertFrom(LokalisoituTekstiDto dto, Type<TekstiPalanen> type) {

        if (dto.getId() != null) {
            /*
            Jos id on mukana, yritä yhdistää olemassa olevaan tekstipalaseen
            Koska tekstipalanen on muuttumaton ja cachetettu, niin oletustapaus on että
            tekstipalanen on jo cachessa (luettu aikaisemmin) ja tietokantahaku vältetään.
            Huom! vihamielinen/virheellinen client voisi keksiä id:n aiheuttaen turhia tietokantahakuja.
            */
            TekstiPalanen current = repository.findOne(dto.getId());
            if (current != null) {
                Map<Kieli, String> teksti = current.getTeksti();
                teksti.putAll(dto.getTekstit());
                TekstiPalanen tekstiPalanen = new TekstiPalanen(teksti);
                if ( tekstiPalanen.equals(current) ) {
                    return current;
                }
                return tekstiPalanen;
            }
        }
        return new TekstiPalanen(dto.getTekstit());
    }
}

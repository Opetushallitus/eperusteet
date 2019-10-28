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

import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.TekstiPalanenRepository;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author jhyoty
 */
@Component
@Dto
public class TekstiPalanenConverter extends BidirectionalConverter<TekstiPalanen, LokalisoituTekstiDto> {

    private static final Logger LOG = LoggerFactory.getLogger(TekstiPalanenConverter.class);

    @Autowired
    private TekstiPalanenRepository repository;

    @Override
    public LokalisoituTekstiDto convertTo(TekstiPalanen source, Type<LokalisoituTekstiDto> destinationType, MappingContext mappingContext) {
        return new LokalisoituTekstiDto(source.getId(), source.getTunniste(), source.getTeksti());
    }

    @Override
    public TekstiPalanen convertFrom(LokalisoituTekstiDto source, Type<TekstiPalanen> destinationType, MappingContext mappingContext) {
        if (source.getId() != null) {
            /*
            Jos id on mukana, yritä yhdistää olemassa olevaan tekstipalaseen
            Koska tekstipalanen on muuttumaton ja cachetettu, niin oletustapaus on että
            tekstipalanen on jo cachessa (luettu aikaisemmin) ja tietokantahaku vältetään.
            Huom! vihamielinen/virheellinen client voisi keksiä id:n aiheuttaen turhia tietokantahakuja.
            */
            TekstiPalanen current = repository.findOne(source.getId());
            if (current != null) {
                TekstiPalanen tekstiPalanen = TekstiPalanen.of(source.getTekstit(), current.getTunniste());
                if ( current.equals(tekstiPalanen) ) {
                    return current;
                }
                return tekstiPalanen;
            }
        }
        return TekstiPalanen.of(source.getTekstit());
    }
}

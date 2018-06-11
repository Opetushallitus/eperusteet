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

import fi.vm.sade.eperusteet.domain.Koodistokoodi;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.springframework.stereotype.Component;

/**
 *
 * @author harrik
 */
@Slf4j
@Component
public class KoodistokoodiConverter extends BidirectionalConverter<Koodistokoodi, String> {


    @Override
    public String convertTo(Koodistokoodi source, Type<String> destinationType, MappingContext mappingContext) {
        log.info("KoodistokoodiConverter convertTo kutsuttu luokalla: " + destinationType.getRawType());
        return source.getKoodi();
    }

    @Override
    public Koodistokoodi convertFrom(String source, Type<Koodistokoodi> destinationType, MappingContext mappingContext) {
        /*
        log.info("KoodistokoodiConverter convertFrom kutsuttu luokalla: " + type.getRawType());
        Class<?> klass = type.getRawType();
        if (klass == Koulutusala.class) {
            return koulutusalaRepo.findOneByKoodi(d);
        } else if (klass == Opintoala.class) {
            return opintoalaRepo.findOneByKoodi(d);
        }
        */
        return null;
    }
}
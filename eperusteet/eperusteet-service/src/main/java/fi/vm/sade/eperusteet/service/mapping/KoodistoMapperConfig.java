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

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.dto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.KoulutusalaDto;
import fi.vm.sade.eperusteet.dto.OpintoalaDto;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author harrik
 */
@Configuration
public class KoodistoMapperConfig {
    
    @Bean
    @Koodisto
    public DtoMapper koodistoMapper() {
        DefaultMapperFactory factory = new DefaultMapperFactory.Builder()
            .build();
        factory.getConverterFactory().registerConverter("metadataConverter", KoodistoMetadataConverter.TO_MAP);
        factory.getConverterFactory().registerConverter("metadataToTekstipalanenConverter", KoodistoMetadataConverter.TO_TEKSTIPALANEN);
        factory.getConverterFactory().registerConverter("koodistoPaivaysConverter", KoodistoConverter.TO_DATE);

        factory.classMap(KoodistoKoodiDto.class, KoulutusalaDto.class)
                .field("koodiUri", "koodi")
                .fieldMap("metadata", "nimi").converter("metadataConverter").add()
                .byDefault()
                .register();
        
        factory.classMap(KoodistoKoodiDto.class, Peruste.class)
                //.fieldMap("voimassaAlkuPvm", "paivays").converter("koodistoPaivaysConverter").add()
                .fieldMap("metadata", "nimi").converter("metadataToTekstipalanenConverter").add()
                .byDefault()
                .register();
        
        factory.classMap(KoodistoKoodiDto.class, OpintoalaDto.class)
                .field("koodiUri", "koodi")
                .fieldMap("metadata", "nimi").converter("metadataConverter").add()
                .byDefault()
                .register();
        
        return new DtoMapperImpl(factory.getMapperFacade());
    }
    
    
}

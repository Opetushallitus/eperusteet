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

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TutkinnonOsa;
import fi.vm.sade.eperusteet.dto.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.TutkinnonOsaDto;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author jhyoty
 */
@Configuration
public class DtoMapperConfig {

    @Bean
    @Dto
    public DtoMapper dtoMapper(
        TekstiPalanenConverter tekstiPalanenConverter,
        CachedEntityConverter cachedEntityConverter) {
        DefaultMapperFactory factory = new DefaultMapperFactory.Builder()
            .build();
        factory.getConverterFactory().registerConverter(tekstiPalanenConverter);
        factory.getConverterFactory().registerConverter(cachedEntityConverter);

        factory.classMap(PerusteenOsaDto.class, PerusteenOsa.class)
            .byDefault()
            .register();
        factory.classMap(TutkinnonOsaDto.class, TutkinnonOsa.class)
            .use(PerusteenOsaDto.class, PerusteenOsa.class)
            .byDefault()
            .register();
        factory.classMap(TekstiKappaleDto.class, TekstiKappale.class)
            .use(PerusteenOsaDto.class, PerusteenOsa.class)
            .byDefault()
            .register();

        return new DtoMapperImpl(factory.getMapperFacade());
    }

}

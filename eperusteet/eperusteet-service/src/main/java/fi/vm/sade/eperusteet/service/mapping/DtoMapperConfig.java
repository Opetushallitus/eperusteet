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
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.AbstractRakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.peruste.SuoritustapaDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonOsa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.AbstractRakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import ma.glasnost.orika.converter.builtin.PassThroughConverter;
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
        ReferenceableEntityConverter cachedEntityConverter,
        KoodistokoodiConverter koodistokoodiConverter) {
        DefaultMapperFactory factory = new DefaultMapperFactory.Builder()
            .build();
        factory.getConverterFactory().registerConverter(tekstiPalanenConverter);
        factory.getConverterFactory().registerConverter(cachedEntityConverter);
        factory.getConverterFactory().registerConverter("koodistokoodiConverter", koodistokoodiConverter);
        factory.getConverterFactory().registerConverter(new PassThroughConverter(TekstiPalanen.class));
        factory.getConverterFactory().registerConverter(new TypeNameConverter());

        factory.classMap(PerusteenOsaDto.Suppea.class, PerusteenOsa.class)
            .fieldBToA("class", "osanTyyppi")
            .byDefault()
            .register();
        factory.classMap(PerusteenOsaDto.Laaja.class, PerusteenOsa.class)
            .byDefault()
            .register();
        factory.classMap(TutkinnonOsaDto.class, TutkinnonOsa.class)
            .use(PerusteenOsaDto.Laaja.class, PerusteenOsa.class)
            .byDefault()
            .register();
        factory.classMap(TekstiKappaleDto.class, TekstiKappale.class)
            .use(PerusteenOsaDto.Laaja.class, PerusteenOsa.class)
            .byDefault()
            .register();
        factory.classMap(PerusteDto.class, Peruste.class)
            .byDefault()
            .register();
        factory.classMap(PerusteprojektiDto.class, Perusteprojekti.class)
            .byDefault()
            .register();
        factory.classMap(AbstractRakenneOsaDto.class, AbstractRakenneOsa.class)
            .byDefault()
            .register();
        factory.classMap(RakenneModuuliDto.class, RakenneModuuli.class)
            .use(AbstractRakenneOsaDto.class, AbstractRakenneOsa.class)
            .byDefault()
            .register();
        factory.classMap(RakenneOsaDto.class, RakenneOsa.class)
            .use(AbstractRakenneOsaDto.class, AbstractRakenneOsa.class)
            .byDefault()
            .register();
        factory.classMap(TutkinnonOsaViiteDto.class, TutkinnonOsaViite.class)
            .fieldBToA("tutkinnonOsa.nimi", "nimi")
            .fieldBToA("tutkinnonOsa.tyyppi", "tyyppi")
            .byDefault()
            .register();
        factory.classMap(SuoritustapaDto.class, Suoritustapa.class)
            .byDefault()
            .register();

        return new DtoMapperImpl(factory.getMapperFacade());
    }

}

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
import fi.vm.sade.eperusteet.domain.yl.OpetuksenTavoite;
import fi.vm.sade.eperusteet.domain.yl.OppiaineenVuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.domain.yl.TekstiOsa;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.yl.OpetuksenTavoiteDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineenVuosiluokkaKokonaisuusDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static fi.vm.sade.eperusteet.service.test.util.TestUtils.olt;
import static org.junit.Assert.*;

/**
 *
 * @author jhyoty
 */
public class MappingTest {

    @Test
    public void testOptionalMapping() {

        DefaultMapperFactory factory = new DefaultMapperFactory.Builder()
            .build();
        factory.registerMapper(new ReferenceableCollectionMergeMapper());
        factory.getConverterFactory().registerConverter(new TekstiPalanenConverter());
        OptionalSupport.register(factory);
        MapperFacade mapper = factory.getMapperFacade();

        OpetuksenTavoite ot = new OpetuksenTavoite();
        ot.setId(42L);
        OppiaineenVuosiluokkaKokonaisuus ovk = new OppiaineenVuosiluokkaKokonaisuus();
        List<OpetuksenTavoite> tavoitteet = ovk.getTavoitteet();
        tavoitteet.add(ot);
        ovk.setTehtava(new TekstiOsa(TekstiPalanen.of(Kieli.FI, "Otsikko"), null));
        ovk.setTavoitteet(tavoitteet);
        ovk.setOhjaus(new TekstiOsa());
        ovk.setArviointi(new TekstiOsa());

        OppiaineenVuosiluokkaKokonaisuusDto dto = mapper.map(ovk, OppiaineenVuosiluokkaKokonaisuusDto.class);
        dto.setArviointi(null);
        dto.setVuosiluokkaKokonaisuus(Optional.of(new Reference(0L)));
        dto.setOhjaus(Optional.empty());
        dto.getTavoitteet().get(0).setTavoite(olt("Tavoite"));
        mapper.map(dto, ovk);

        assertNull(ovk.getOhjaus());
        assertNotNull(ovk.getArviointi());
        assertTrue(ovk.getTavoitteet().get(0) == ot);
        assertEquals("Tavoite", ovk.getTavoitteet().get(0).getTavoite().getTeksti().get(Kieli.FI));

        dto.setTavoitteet(null);
        mapper.map(dto, ovk);
        assertTrue(ovk.getTavoitteet().get(0) == ot);

        dto.setTavoitteet(Collections.<OpetuksenTavoiteDto>emptyList());
        mapper.map(dto, ovk);
        assertTrue(ovk.getTavoitteet().isEmpty());
    }

    @Test
    public void testOptionalImmutableMapping() {

        DefaultMapperFactory factory = new DefaultMapperFactory.Builder()
            .build();
        OptionalSupport.register(factory);
        MapperFacade mapper = factory.getMapperFacade();
        B b = mapper.map(new A(), B.class);
        A a = mapper.map(b, A.class);
        assertEquals(new A(), a);
        a.setL(null);
        mapper.map(a, b);
        assertEquals(Long.valueOf(42L), b.getL());
        a.setL(Optional.empty());
        mapper.map(a, b);
        assertNull(b.getL());
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    static public class A {
        Optional<Long> l = Optional.of(42L);
        Optional<String> t = Optional.of("Bar");
    }

    @Getter
    @Setter
    static public class B {
        Long l;
        String t;
    }

}

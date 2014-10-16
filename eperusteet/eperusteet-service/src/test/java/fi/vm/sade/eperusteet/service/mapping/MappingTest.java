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

import com.google.common.base.Optional;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.yl.OpetuksenTavoite;
import fi.vm.sade.eperusteet.domain.yl.OppiaineenVuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.domain.yl.TekstiOsa;
import fi.vm.sade.eperusteet.dto.yl.OppiaineenVuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.dto.yl.TekstiOsaDto;
import java.util.List;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.Test;

import static fi.vm.sade.eperusteet.service.test.util.TestUtils.olt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author jhyoty
 */
public class MappingTest {

    @Test
    public void testCollectionMapping() {
        DefaultMapperFactory factory = new DefaultMapperFactory.Builder()
            .build();
        factory.registerMapper(new DtoMapperConfig.OpetuksenTavoiteCollectionMapper());
        factory.getConverterFactory().registerConverter(new TekstiPalanenConverter());
        factory.getConverterFactory().registerConverter(new OptionalConverter());
        factory.getConverterFactory().registerConverter(new ToOptionalConverter());
        MapperFacade mapper = factory.getMapperFacade();

        OpetuksenTavoite ot = new OpetuksenTavoite();
        ot.setId(42L);
        OppiaineenVuosiluokkaKokonaisuus ovk = new OppiaineenVuosiluokkaKokonaisuus();
        List<OpetuksenTavoite> tavoitteet = ovk.getTavoitteet();
        tavoitteet.add(ot);
        ovk.setTehtava(new TekstiOsa(TekstiPalanen.of(Kieli.FI, "Otsikko"), null));
        ovk.setTavoitteet(tavoitteet);

        OppiaineenVuosiluokkaKokonaisuusDto dto = mapper.map(ovk, OppiaineenVuosiluokkaKokonaisuusDto.class);
        dto.setOhjaus(Optional.<TekstiOsaDto>absent());
        dto.getTavoitteet().get(0).setTavoite(olt("Tavoite"));
        mapper.map(dto, ovk);

        assertTrue(ovk.getTavoitteet().get(0) == ot);
        assertEquals("Tavoite", ovk.getTavoitteet().get(0).getTavoite().getTeksti().get(Kieli.FI));
    }

}

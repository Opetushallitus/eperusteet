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
package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.repository.PerusopetuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.yl.PerusopetuksenPerusteenSisaltoService;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static fi.vm.sade.eperusteet.service.test.util.TestUtils.olt;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author jhyoty
 */
@DirtiesContext
public class PerusopetuksenSisaltoServiceIT extends AbstractIntegrationTest {

    @Autowired
    private PerusopetuksenPerusteenSisaltoService service;
    @Autowired
    private PerusteService perusteService;
    @Autowired
    private PerusopetuksenPerusteenSisaltoRepository sisaltoRepository;

    private Long perusteId;
    @Before
    public void setup() {
        Peruste peruste = perusteService.luoPerusteRunko("koulutustyyppi_16", LaajuusYksikko.OPINTOVIIKKO, PerusteTyyppi.NORMAALI);
        perusteId = peruste.getId();
    }

    @Test
    public void testUpdateLaajaalaiset() throws IOException {

        LaajaalainenOsaaminenDto lo = new LaajaalainenOsaaminenDto();
        lo.setNimi(olt("Nimi"));
        lo = service.addLaajaalainenOsaaminen(perusteId, lo);
        assertEquals(1, service.getLaajaalaisetOsaamiset(perusteId).size());
        lo.setNimi(null);
        lo.setKuvaus(olt("Kuvaus"));
        lo = service.updateLaajaalainenOsaaminen(perusteId, lo);
        assertEquals("Kuvaus", lo.getKuvaus().get().get(Kieli.FI));
        assertEquals("Nimi", lo.getNimi().get().get(Kieli.FI));
    }

}

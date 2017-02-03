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

import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.yl.AIPEVaiheDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.yl.AIPEOpetuksenPerusteenSisaltoService;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author nkala
 */
@Transactional
@DirtiesContext
public class AIPEServicesIT extends AbstractIntegrationTest {
    @Autowired
    private AIPEOpetuksenPerusteenSisaltoService sisalto;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteRepository perusteRepository;

    private Long perusteId;

    @Before
    public void setup() {
        Peruste peruste = perusteService.luoPerusteRunko(KoulutusTyyppi.AIKUISTENPERUSOPETUS, LaajuusYksikko.KURSSI, PerusteTyyppi.NORMAALI);
        perusteId = peruste.getId();
    }

    @Test
    public void testPerusteenLuonti() {
        Peruste peruste = perusteRepository.findOne(perusteId);
        assertEquals(peruste.getKoulutustyyppi(), KoulutusTyyppi.AIKUISTENPERUSOPETUS.toString());
        assertNotNull(peruste.getAipeOpetuksenPerusteenSisalto());
        assertNotNull(peruste.getSisalto(Suoritustapakoodi.AIPE));
    }

    @Test
    public void testVaiheidenLuonti() {
        AIPEVaiheDto vaiheDto = new AIPEVaiheDto();
        vaiheDto = sisalto.addVaihe(perusteId, vaiheDto);
        assertNotNull(vaiheDto.getId());
        Peruste peruste = perusteRepository.findOne(perusteId);
        assertTrue(peruste.getAipeOpetuksenPerusteenSisalto().getVaiheet().size() == 1);
    }

    @Test
    public void testLaajaalaistenLuonti() {

    }

    @Test
    public void testOppiaineidenLuonti() {

    }

    @Test
    public void testKurssienLuonti() {

    }

    @Test
    public void testSisalto() {

    }

    @Test
    public void testTavoitteet() {

    }
}

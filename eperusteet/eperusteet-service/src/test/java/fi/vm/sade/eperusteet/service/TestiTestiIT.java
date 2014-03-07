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

import fi.vm.sade.eperusteet.dto.KoodistoDto;
import fi.vm.sade.eperusteet.dto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.service.impl.KoulutusalaServiceImpl;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author nkala
 */
public class TestiTestiIT extends AbstractIntegrationTest {
    private static final Logger LOG = LoggerFactory.getLogger(KoulutusalaServiceImpl.class);
    
    @Autowired
    KoodistoService service;

    @Test
    public void testGetAll() {
        List<KoodistoKoodiDto> all = service.getAll("tutkinnonosat");
        LOG.debug("Koko: " + String.valueOf(all.size()));
        for (KoodistoKoodiDto x : all) {
            LOG.debug(x.getKoodiUri());
        }
    }
}

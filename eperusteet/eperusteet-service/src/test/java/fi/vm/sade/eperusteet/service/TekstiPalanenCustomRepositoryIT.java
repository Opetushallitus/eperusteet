/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiHakuDto;
import fi.vm.sade.eperusteet.repository.TekstiPalanenRepositoryCustom;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.LongStream;

import static org.junit.Assert.assertEquals;

/**
 * User: tommiratamaa
 * Date: 2.11.15
 * Time: 16.11
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TekstiPalanenCustomRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private TekstiPalanenRepositoryCustom tekstiPalanenRepository;

    @Test
    @Transactional
    public void test() {
        Set<Long> ids = new HashSet<>();
        LongStream.range(1, 34465).forEach(ids::add);
        List<LokalisoituTekstiHakuDto> results = tekstiPalanenRepository.findLokalisoitavatTekstit(ids);
        assertEquals(0, results.size());
    }
}

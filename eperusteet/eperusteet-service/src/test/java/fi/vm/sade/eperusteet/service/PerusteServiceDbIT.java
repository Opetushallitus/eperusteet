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
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.dto.peruste.PerusteHakuDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.test.AbstractDbIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Yksinkertainen integraatiotesti käyttäen "oikeaa" tietokantaa.
 * @see AbstractDbIntegrationTest
 * @author jhyoty
 */
@Transactional
public class PerusteServiceDbIT extends AbstractDbIntegrationTest {

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteRepository repo;

    @PersistenceContext
    private EntityManager em;

        
    @Before
    public void setUp() {
        Peruste p = new Peruste();
        p.setNimi(TekstiPalanen.of(Collections.singletonMap(Kieli.FI, "Nimi")));
        repo.save(p);
        em.flush();
    }

    @Test
    @Rollback
    public void testGet() {
        Page<PerusteHakuDto> perusteet = perusteService.getAll(PageRequest.of(0, 10), "fi");
        assertEquals(perusteet.getTotalElements(), 1);
    }

}

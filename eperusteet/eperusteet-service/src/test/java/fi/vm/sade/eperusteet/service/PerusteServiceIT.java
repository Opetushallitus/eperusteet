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

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/**
 * Yksinkertainen integraatiotesti muistinvaraista kantaa vasten.
 * @author jhyoty
 */
@Transactional
public class PerusteServiceIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteService perusteService;
    @Autowired
    private PerusteRepository repo;
    @PersistenceContext
    private EntityManager em;
 
    public PerusteServiceIT() {
    }

    @Before
    public void setUp() {
        repo.saveAndFlush(TestUtils.createPeruste());
    }

    @Test
    @Rollback(true)
    public void testGetAll() {
        Page<Peruste> perusteet = perusteService.getAll(new PageRequest(0, 10), "fi");
        assertEquals(perusteet.getTotalElements(), 1);
    }

}

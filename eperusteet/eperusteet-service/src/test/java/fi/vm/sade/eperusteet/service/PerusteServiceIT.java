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

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.Tila;
import fi.vm.sade.eperusteet.dto.PerusteDto;
import fi.vm.sade.eperusteet.dto.PerusteQuery;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.AbstractRakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;

/**
 * Integraatiotesti muistinvaraista kantaa vasten.
 *
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
    @Autowired
    private PerusteenOsaRepository perusteenOsaRepository;
    @Autowired
    private PerusteenOsaService perusteenOsaService;

    private Peruste peruste;

    public PerusteServiceIT() {
    }

    @Before
    public void setUp() {
        Peruste p = TestUtils.createPeruste();
        p.setSiirtymaAlkaa(new GregorianCalendar(2000, Calendar.MARCH, 12).getTime());
        p.setVoimassaoloLoppuu(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) + 2, Calendar.MARCH, 12).getTime());
        p.setTila(Tila.VALMIS);
        Suoritustapa s = new Suoritustapa();
        s.setSuoritustapakoodi(Suoritustapakoodi.OPS);
        p.setSuoritustavat(Sets.newHashSet(s));
        peruste = repo.save(p);

        p = TestUtils.createPeruste();
        p.setSiirtymaAlkaa(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) + 2, Calendar.MARCH, 12).getTime());
        p.setVoimassaoloLoppuu(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) + 4, Calendar.MARCH, 12).getTime());
        p.setTila(Tila.VALMIS);
        repo.save(p);

        p = TestUtils.createPeruste();
        p.setTila(Tila.VALMIS);
        repo.save(p);

        p = TestUtils.createPeruste();
        p.setVoimassaoloLoppuu(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 2, Calendar.MARCH, 12).getTime());
        p.setTila(Tila.VALMIS);
        repo.save(p);

        perusteService.lock(peruste.getId(), Suoritustapakoodi.OPS);
    }

    @After
    public void cleanUp() {
        perusteService.unlock(peruste.getId(), Suoritustapakoodi.OPS);
    }

    @Test
    public void testGetAll() {
        Page<PerusteDto> perusteet = perusteService.getAll(new PageRequest(0, 10), Kieli.FI.toString());
        assertEquals(perusteet.getTotalElements(), 2);
    }

    @Test
    public void testFindBy() {
        PerusteQuery pquery = new PerusteQuery();
        pquery.setSiirtyma(true);
        Page<PerusteDto> perusteet = perusteService.findBy(new PageRequest(0, 10), pquery);
        assertEquals(perusteet.getTotalElements(), 3);
    }

    @Test
    @Rollback(true)
    public void testAddTutkinnonRakenne() {

        TutkinnonOsaViiteDto v1 = perusteService.addTutkinnonOsa(peruste.getId(), Suoritustapakoodi.OPS, new TutkinnonOsaViiteDto());
        TutkinnonOsaViiteDto v2 = perusteService.addTutkinnonOsa(peruste.getId(), Suoritustapakoodi.OPS, new TutkinnonOsaViiteDto());

        RakenneModuuliDto rakenne = new RakenneModuuliDto();

        RakenneOsaDto o1 = new RakenneOsaDto();
        o1.setTutkinnonOsa(v1.getTutkinnonOsa());

        RakenneModuuliDto ryhma = new RakenneModuuliDto();

        RakenneOsaDto o2 = new RakenneOsaDto();
        o2.setTutkinnonOsa(v2.getTutkinnonOsa());
        ryhma.setOsat(Arrays.<AbstractRakenneOsaDto>asList(o2));

        rakenne.setOsat(Arrays.<AbstractRakenneOsaDto>asList(o1, ryhma));

        RakenneModuuliDto updatedTutkinnonRakenne = perusteService.updateTutkinnonRakenne(peruste.getId(), Suoritustapakoodi.OPS, rakenne);

        updatedTutkinnonRakenne = perusteService.updateTutkinnonRakenne(peruste.getId(), Suoritustapakoodi.OPS, updatedTutkinnonRakenne);
        assertEquals(v1.getTutkinnonOsa(), ((RakenneOsaDto) updatedTutkinnonRakenne.getOsat().get(0)).getTutkinnonOsa());
    }

    @Value("${fi.vm.sade.eperusteet.tutkinnonrakenne.maksimisyvyys}")
    private int maxDepth;

    @Test(expected = BusinessRuleViolationException.class)
    @Rollback(true)
    public void addiningDeepTutkinnonRakenneShouldFail() {

        RakenneModuuliDto rakenne = new RakenneModuuliDto();
        for (int i = 0; i < maxDepth + 1; i++) {
            RakenneModuuliDto ryhma = new RakenneModuuliDto();
            ryhma.setOsat(Arrays.<AbstractRakenneOsaDto>asList(rakenne));
            rakenne = ryhma;
        }
        RakenneModuuliDto updatedTutkinnonRakenne = perusteService.updateTutkinnonRakenne(peruste.getId(), Suoritustapakoodi.OPS, rakenne);
    }

    private static final Logger LOG = LoggerFactory.getLogger(PerusteServiceIT.class);
}

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
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.AbstractRakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.RakenneRepository;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

/**
 *
 * @author harrik
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PerusteServiceETagIT extends AbstractIntegrationTest {
    
    @Autowired
    private PerusteService perusteService;
    @Autowired
    private PerusteRepository repo;
    @Autowired
    private RakenneRepository rakenneRepository;
    
    private Peruste peruste;
    
    @Before
    public void setUp() {
        Peruste p = TestUtils.teePeruste();
        p.setSiirtymaAlkaa(new GregorianCalendar(2000, Calendar.MARCH, 12).getTime());
        p.setVoimassaoloLoppuu(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) + 2, Calendar.MARCH, 12).getTime());
        p.setTila(PerusteTila.VALMIS);
        Suoritustapa s = new Suoritustapa();
        s.setSuoritustapakoodi(Suoritustapakoodi.OPS);
        p.setSuoritustavat(Sets.newHashSet(s));
        RakenneModuuli rakenne = new RakenneModuuli();
        s.setRakenne(rakenne);
        rakenneRepository.save(rakenne);
        peruste = repo.save(p);

        perusteService.lock(peruste.getId(), Suoritustapakoodi.OPS);
    }

    @After
    public void cleanUp() {
       perusteService.unlock(peruste.getId(), Suoritustapakoodi.OPS);
    }
    
    @Test
    public void testGetTutkinnonRakenneETag() {
        RakenneModuuliDto rakenne = new RakenneModuuliDto();
        rakenne.setOsat(new ArrayList<AbstractRakenneOsaDto>());
        rakenne.getOsat().add(new RakenneModuuliDto());
        perusteService.updateTutkinnonRakenne(peruste.getId(), Suoritustapakoodi.OPS, rakenne);
        
        rakenne = perusteService.getTutkinnonRakenne(peruste.getId(), Suoritustapakoodi.OPS, null);
        assertNotNull(rakenne);
        assertNotNull(rakenne.getVersioId());

        rakenne = perusteService.getTutkinnonRakenne(peruste.getId(), Suoritustapakoodi.OPS, rakenne.getVersioId());
        Assert.assertNull(rakenne);      
    }
    
}

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

import fi.vm.sade.eperusteet.service.internal.SuoritustapaService;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaViiteRepository;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author nkala
 */
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class SuoritustapaServiceIT extends AbstractIntegrationTest {

    @Autowired
    private SuoritustapaService suoritustapaService;

    @Autowired
    private TutkinnonOsaRepository tutkinnonOsaRepository;

    @Autowired
    private TutkinnonOsaViiteRepository tutkinnonOsaViiteRepository;

    public SuoritustapaServiceIT() {
    }

    private TutkinnonOsaViite uusiTutkinnonOsaViite(Suoritustapa st) {
        TutkinnonOsa tosa = new TutkinnonOsa();
        tosa = tutkinnonOsaRepository.save(tosa);
        TutkinnonOsaViite tov = new TutkinnonOsaViite();
        tov.setTutkinnonOsa(tosa);
        tov.setSuoritustapa(st);
        return tov;
    }

    /**
     * Test of testCreateFromOther method, of class SuoritustapaServiceImpl.
     */
    @Test
    @Rollback(true)
    public void testCreateFromOther() {
        Suoritustapa st = suoritustapaService.createSuoritustapaWithSisaltoAndRakenneRoots(Suoritustapakoodi.OPS, LaajuusYksikko.OSAAMISPISTE);
        PerusteenOsaViite sisalto = st.getSisalto();

        List<PerusteenOsaViite> lapset = sisalto.getLapset();
        Set<TutkinnonOsaViite> tosat = st.getTutkinnonOsat();

        tosat.add(uusiTutkinnonOsaViite(st));
        tosat.add(uusiTutkinnonOsaViite(st));

        Suoritustapa stUusi = suoritustapaService.createFromOther(st.getId());

        List<PerusteenOsaViite> lapset1 = st.getSisalto().getLapset();
        List<PerusteenOsaViite> lapset2 = stUusi.getSisalto().getLapset();

        Assert.assertFalse(Objects.equals(st.getId(), stUusi.getId()));
        Assert.assertTrue(Objects.equals(st.getLaajuusYksikko(), stUusi.getLaajuusYksikko()));
        Assert.assertTrue(Objects.equals(st.getTutkinnonOsat().size(), stUusi.getTutkinnonOsat().size()));

//        Assert.assertTrue(lapset1 != null && lapset2 != null);
//        Assert.assertTrue(Objects.equals(lapset1.size(), lapset2.size()));
    }
}

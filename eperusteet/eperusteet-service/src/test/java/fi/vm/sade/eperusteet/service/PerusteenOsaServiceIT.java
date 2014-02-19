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

import fi.vm.sade.eperusteet.domain.Arviointi;
import fi.vm.sade.eperusteet.domain.ArviointiAsteikko;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Osaamistaso;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.TutkinnonOsa;
import fi.vm.sade.eperusteet.dto.ArviointiDto;
import fi.vm.sade.eperusteet.dto.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.repository.ArviointiRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author teele1
 */
@Transactional
public class PerusteenOsaServiceIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteenOsaService perusteenOsaService;
    @Autowired
    private PerusteenOsaRepository perusteenOsaRepository;
    @Autowired
    private ArviointiRepository arviointiRepository;
    @PersistenceContext
    private EntityManager em;

    private Arviointi persistedArviointi;

    @Before
    public void setUp() {
        TekstiPalanen osaamistasoOtsikko = new TekstiPalanen(Collections.singletonMap(Kieli.FI, "otsikko"));
        em.persist(osaamistasoOtsikko);

        Osaamistaso osaamistaso = new Osaamistaso();
        osaamistaso.setId(1L);
        osaamistaso.setOtsikko(osaamistasoOtsikko);

        em.persist(osaamistaso);

        ArviointiAsteikko arviointiasteikko = new ArviointiAsteikko();
        arviointiasteikko.setId(1L);
        arviointiasteikko.setOsaamistasot(Collections.singletonList(osaamistaso));

        em.persist(arviointiasteikko);
        em.flush();

        persistedArviointi = arviointiRepository.saveAndFlush(TestUtils.createArviointi(arviointiasteikko));
    }

    @Test
    @Rollback(true)
    public void testSaveWithArviointi() {
        TutkinnonOsa tutkinnonOsa = new TutkinnonOsa();
        tutkinnonOsa.setArviointi(persistedArviointi);
        perusteenOsaRepository.save(tutkinnonOsa);
        em.flush();

        List<PerusteenOsaDto> perusteenOsat = perusteenOsaService.getAll();

        Assert.assertNotNull(perusteenOsat);
        Assert.assertEquals(1, perusteenOsat.size());

        Assert.assertTrue(TutkinnonOsaDto.class.isInstance(perusteenOsat.get(0)));
        TutkinnonOsaDto to = (TutkinnonOsaDto) perusteenOsat.get(0);
        ArviointiDto arviointi = to.getArviointi();
        Assert.assertNotNull(tutkinnonOsa.getArviointi());
    }

}

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

import com.google.common.collect.Lists;
import fi.vm.sade.eperusteet.domain.Arviointi.ArviointiAsteikko;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Osaamistaso;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.TutkinnonOsa;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonOsa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.ArviointiRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaRepository;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolationException;
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
    @Autowired
    private TutkinnonOsaRepository tutkinnonOsaRepository;
    @PersistenceContext
    private EntityManager em;

    private ArviointiAsteikko arviointiasteikko;

    @Before
    public void setUp() {
        TekstiPalanen osaamistasoOtsikko = TekstiPalanen.of(Collections.singletonMap(Kieli.FI, "otsikko"));
        em.persist(osaamistasoOtsikko);

        Osaamistaso osaamistaso = new Osaamistaso();
        osaamistaso.setId(1L);
        osaamistaso.setOtsikko(osaamistasoOtsikko);

        em.persist(osaamistaso);

        ArviointiAsteikko arviointiasteikko = new ArviointiAsteikko();
        arviointiasteikko.setId(1L);
        arviointiasteikko.setOsaamistasot(Lists.newArrayList(osaamistaso));

        em.persist(arviointiasteikko);
        this.arviointiasteikko = arviointiasteikko;
        em.flush();
    }

    @Test
    @Rollback(true)
    public void testSaveWithArviointi() {
        TutkinnonOsa tutkinnonOsa = new TutkinnonOsa();
        tutkinnonOsa.setNimi(TestUtils.tekstiPalanenOf(Kieli.FI, "Nimi"));
        tutkinnonOsa.setTila(PerusteTila.LUONNOS);
        tutkinnonOsa.setArviointi(TestUtils.teeArviointi(arviointiasteikko));
        tutkinnonOsa = (TutkinnonOsa) perusteenOsaRepository.saveAndFlush(tutkinnonOsa);
        PerusteenOsaDto.Laaja dto = perusteenOsaService.get(tutkinnonOsa.getId());

        Assert.assertNotNull(dto);
        
        Assert.assertTrue(TutkinnonOsaDto.class.isInstance(dto));
        TutkinnonOsaDto to = (TutkinnonOsaDto) dto;
        to.getArviointi();
        Assert.assertNotNull(tutkinnonOsa.getArviointi());
    }

    @Test
    @Rollback(true)
    public void testFindTutkinnonOsaByName() {
    	TutkinnonOsa tutkinnonOsa = new TutkinnonOsa();
    	tutkinnonOsa.setNimi(TestUtils.tekstiPalanenOf(Kieli.FI, "Nimi"));
        tutkinnonOsa.setTila(PerusteTila.LUONNOS);
    	tutkinnonOsa = tutkinnonOsaRepository.saveAndFlush(tutkinnonOsa);

    	tutkinnonOsa = new TutkinnonOsa();
    	tutkinnonOsa.setNimi(TestUtils.tekstiPalanenOf(Kieli.SV, "Namnet"));
        tutkinnonOsa.setTila(PerusteTila.LUONNOS);
    	tutkinnonOsa = tutkinnonOsaRepository.saveAndFlush(tutkinnonOsa);

    	List<TutkinnonOsa> tutkinnonOsat = tutkinnonOsaRepository.findByNimiTekstiTekstiContainingIgnoreCase("nim");

    	Assert.assertNotNull(tutkinnonOsat);
    	Assert.assertEquals(1, tutkinnonOsat.size());

    	tutkinnonOsat = tutkinnonOsaRepository.findByNimiTekstiTekstiContainingIgnoreCase("nAm");

    	Assert.assertNotNull(tutkinnonOsat);
    	Assert.assertEquals(1, tutkinnonOsat.size());
    }

    @Test(expected = ConstraintViolationException.class)
    @Rollback(true)
    public void testWithInvalidHtml() {
    	TekstiKappaleDto dto = new TekstiKappaleDto();
    	dto.setNimi(new LokalisoituTekstiDto(Collections.singletonMap("fi", "<i>otsikko</i>")));
    	perusteenOsaService.add(dto);
    }
}

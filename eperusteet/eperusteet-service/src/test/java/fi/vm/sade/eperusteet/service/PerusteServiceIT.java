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
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.dto.peruste.*;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.AbstractRakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneModuuliDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.RakenneOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.TutkinnonOsaViiteDto;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import fi.vm.sade.eperusteet.repository.KoulutusRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import static fi.vm.sade.eperusteet.service.test.util.TestUtils.tekstiPalanenOf;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import static org.assertj.core.api.Assertions.*;

/**
 * Integraatiotesti muistinvaraista kantaa vasten.
 *
 * @author jhyoty
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PerusteServiceIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteRepository repo;

    @Autowired
    private PlatformTransactionManager manager;

    @Autowired
    private KoulutusRepository koulutusRepository;

    @Autowired
    @LockCtx(TutkinnonRakenneLockContext.class)
    private LockService<TutkinnonRakenneLockContext> lockService;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @Autowired
    private PerusteprojektiTestUtils ppTestUtils;

    private Peruste peruste;

    public PerusteServiceIT() {
    }

    @Before
    public void setUp() {

        TransactionStatus transaction = manager.getTransaction(new DefaultTransactionDefinition());

        Koulutus koulutus = new Koulutus(tekstiPalanenOf(Kieli.FI,"Koulutus"), "koulutuskoodiArvo", "koulutuskoodiUri","koulutusalakoodi","opintoalakoodi");
        koulutus = koulutusRepository.save(koulutus);

        Peruste p = TestUtils.teePeruste();
        p.setSiirtymaPaattyy(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) + 4, Calendar.MARCH, 12).getTime());
        p.setVoimassaoloLoppuu(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 2, Calendar.MARCH, 12).getTime());
        p.asetaTila(PerusteTila.LUONNOS);
        Suoritustapa s = new Suoritustapa();
        s.setRakenne(new RakenneModuuli());
        s.setSuoritustapakoodi(Suoritustapakoodi.OPS);
        p.setSuoritustavat(Sets.newHashSet(s));
        s.getPerusteet().add(p);
        p.setKoulutukset(Sets.newHashSet(koulutus));

        peruste = repo.save(p);

        p = TestUtils.teePeruste();
        p.setSiirtymaPaattyy(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) + 6, Calendar.MARCH, 12).getTime());
        p.setVoimassaoloLoppuu(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) + 4, Calendar.MARCH, 12).getTime());
        p.asetaTila(PerusteTila.VALMIS);
        repo.save(p);


        p = TestUtils.teePeruste();
        p.asetaTila(PerusteTila.VALMIS);
        repo.save(p);

        p = TestUtils.teePeruste();
        p.setSiirtymaPaattyy(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 2, Calendar.MARCH, 12).getTime());
        p.setVoimassaoloLoppuu(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 3, Calendar.MARCH, 12).getTime());
        p.asetaTila(PerusteTila.VALMIS);
        repo.save(p);

        manager.commit(transaction);
        lockService.lock(TutkinnonRakenneLockContext.of(peruste.getId(), Suoritustapakoodi.OPS));
    }

    @After
    public void cleanUp() {
        lockService.unlock(TutkinnonRakenneLockContext.of(peruste.getId(), Suoritustapakoodi.OPS));
    }

    @Test
    public void testGetAll() {
        Page<PerusteHakuDto> perusteet = perusteService.getAll(new PageRequest(0, 10), Kieli.FI.toString());
        assertEquals(4, perusteet.getTotalElements());
    }

    @Test
    public void testFindBy() {
        PerusteQuery pquery = new PerusteQuery();
        pquery.setPoistunut(false);
        Page<PerusteHakuDto> perusteet = perusteService.findBy(new PageRequest(0, 10), pquery);
        assertEquals(3, perusteet.getTotalElements());
    }

    @Test
    public void testFindByKoulutus() {
        PerusteQuery pquery = new PerusteQuery();
        pquery.setSiirtyma(true);
        pquery.setKoulutuskoodi("koulutuskoodiArvo");
        Page<PerusteHakuDto> perusteet = perusteService.findBy(new PageRequest(0, 10), pquery);
        assertEquals(1, perusteet.getTotalElements());
    }

    @Test
    public void testFindByOsaamisala() {
//        PerusteQuery pquery = new PerusteQuery();
//        pquery.setSiirtyma(true);
//        pquery.setKoulutuskoodi("koulutuskoodiArvo");
//        Page<PerusteHakuDto> perusteet = perusteService.findBy(new PageRequest(0, 10), pquery);
//        assertEquals(1, perusteet.getTotalElements());
    }

    @Test
    public void testTutkintonimikkeenLisays() {
        List<TutkintonimikeKoodiDto> tutkintonimikeKoodit = perusteService.getTutkintonimikeKoodit(peruste.getId());
        assertTrue(tutkintonimikeKoodit.isEmpty());

        TutkintonimikeKoodiDto tutkintonimikeKoodiDto = new TutkintonimikeKoodiDto(peruste.getReference(), "102047", "1632", "10003");
        TutkintonimikeKoodiDto koodi = perusteService.addTutkintonimikeKoodi(peruste.getId(), tutkintonimikeKoodiDto);
        assertEquals("osaamisala_1632", koodi.getOsaamisalaUri());
        assertEquals("tutkinnonosat_102047", koodi.getTutkinnonOsaUri());
        assertEquals("tutkintonimikkeet_10003", koodi.getTutkintonimikeUri());
    }

    @Test
    public void testDiaarinumeroValidi() {
        assertFalse(perusteService.isDiaariValid("diaari"));
        assertFalse(perusteService.isDiaariValid("1-234/567/8910, päivitetty"));
        assertFalse(perusteService.isDiaariValid("oph-12345-1111"));
        assertFalse(perusteService.isDiaariValid("1-234/567/8910"));
        assertFalse(perusteService.isDiaariValid("OPH-12345-111"));
        assertTrue(perusteService.isDiaariValid(""));
        assertTrue(perusteService.isDiaariValid(null));
        assertTrue(perusteService.isDiaariValid("234/567/8910"));
        assertTrue(perusteService.isDiaariValid("amosaa/yhteiset"));
        assertTrue(perusteService.isDiaariValid("OPH-1-1111"));
        assertTrue(perusteService.isDiaariValid("OPH-12-1111"));
        assertTrue(perusteService.isDiaariValid("OPH-123-1111"));
        assertTrue(perusteService.isDiaariValid("OPH-1234-1111"));
        assertTrue(perusteService.isDiaariValid("OPH-12345-1111"));
        assertTrue(perusteService.isDiaariValid("OPH-12345-1134"));
    }

    @Test
    public void testAddTutkinnonRakenne() {

        TutkinnonOsaViiteDto v1 = perusteService.addTutkinnonOsa(peruste.getId(), Suoritustapakoodi.OPS, new TutkinnonOsaViiteDto());
        TutkinnonOsaViiteDto v2 = perusteService.addTutkinnonOsa(peruste.getId(), Suoritustapakoodi.OPS, new TutkinnonOsaViiteDto());

        RakenneModuuliDto rakenne = new RakenneModuuliDto();

        RakenneOsaDto o1 = new RakenneOsaDto();
        o1.setTutkinnonOsaViite(new EntityReference(v1.getId()));

        RakenneModuuliDto ryhma = new RakenneModuuliDto();

        RakenneOsaDto o2 = new RakenneOsaDto();
        o2.setTutkinnonOsaViite(new EntityReference(v2.getId()));
        ryhma.setOsat(Arrays.<AbstractRakenneOsaDto>asList(o2));

        rakenne.setOsat(Arrays.<AbstractRakenneOsaDto>asList(o1, ryhma));

        RakenneModuuliDto updatedTutkinnonRakenne = perusteService.updateTutkinnonRakenne(peruste.getId(), Suoritustapakoodi.OPS, rakenne);

        updatedTutkinnonRakenne = perusteService.updateTutkinnonRakenne(peruste.getId(), Suoritustapakoodi.OPS, updatedTutkinnonRakenne);
        assertEquals(new EntityReference(v1.getId()), ((RakenneOsaDto) updatedTutkinnonRakenne.getOsat().get(0)).getTutkinnonOsaViite());

        PerusteKaikkiDto kokoSisalto = perusteService.getKokoSisalto(peruste.getId());
        assertNotNull(kokoSisalto.getTutkinnonOsat());
        Assertions.assertThat(kokoSisalto.getSuoritustavat()).hasSize(1);
        Assertions.assertThat(kokoSisalto.getTutkinnonOsat()).hasSize(2);
    }

    @Test
    public void testGetByDiaari() {

        Peruste p = perusteService.luoPerusteRunko(KoulutusTyyppi.PERUSTUTKINTO, LaajuusYksikko.OPINTOVIIKKO, PerusteTyyppi.NORMAALI);
        p.setDiaarinumero(new Diaarinumero(TestUtils.validiDiaarinumero()));
        perusteService.update(p.getId(), mapper.map(p, PerusteDto.class));
        p.asetaTila(PerusteTila.VALMIS);

        // FIXME Flush repo
//        PerusteInfoDto haettu = perusteService.getByDiaari(p.getDiaarinumero());
//        Assert.assertNotNull(haettu);

        PerusteInfoDto haettu = perusteService.getByDiaari(new Diaarinumero("ei_loydy"));
        Assert.assertNull(haettu);

    }

    @Value("${fi.vm.sade.eperusteet.tutkinnonrakenne.maksimisyvyys}")
    private int maxDepth;

    @Test(expected = BusinessRuleViolationException.class)
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

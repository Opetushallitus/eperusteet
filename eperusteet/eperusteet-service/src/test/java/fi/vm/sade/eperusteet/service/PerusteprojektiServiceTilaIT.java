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
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.PerusteenOsaTunniste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuliRooli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus.Status;
import fi.vm.sade.eperusteet.dto.peruste.*;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.*;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import fi.vm.sade.eperusteet.service.util.PerusteenRakenne.Ongelma;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.*;
import static org.junit.Assert.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author harrik
 */
@DirtiesContext
public class PerusteprojektiServiceTilaIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteprojektiRepository repo;

    @Autowired
    private PerusteRepository perusteRepo;

    @Autowired
    private PerusteenOsaRepository perusteenOsaRepo;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private PerusteprojektiService service;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteenOsaService perusteenOsaService;

    @Autowired
    @LockCtx(TutkinnonRakenneLockContext.class)
    private LockService<TutkinnonRakenneLockContext> lockService;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    private PerusteprojektiTestUtils ppTestUtils;

    private final String ryhmaId = "1.2.246.562.28.11287634288";
    private final LaajuusYksikko yksikko = LaajuusYksikko.OSAAMISPISTE;
    private final String yhteistyotaho = TestUtils.uniikkiString();
    private final String tehtava = TestUtils.uniikkiString();
    private final PerusteTyyppi tyyppi = PerusteTyyppi.NORMAALI;
    private final String koulutustyyppi = KoulutusTyyppi.ERIKOISAMMATTITUTKINTO.toString();

    private TransactionTemplate transactionTemplate;

    public PerusteprojektiServiceTilaIT() {

    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {

    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testUpdateTilaLaadintaToKommentointi() {
        final PerusteprojektiDto projektiDto = teePerusteprojekti(ProjektiTila.LAADINTA, null, PerusteTila.LUONNOS);
        long perusteId = projektiDto.getPeruste().getIdLong();
        luoSisalto(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, PerusteTila.LUONNOS);
        luoTutkinnonOsa(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO);

        final TilaUpdateStatus status = service.updateTila(projektiDto.getId(), ProjektiTila.KOMMENTOINTI, null);

        transactionTemplate = new TransactionTemplate(transactionManager);
        // the code in this method executes in a transactional context
        transactionTemplate.execute(transactionStatus -> {
                Perusteprojekti pp = repo.findOne(projektiDto.getId());
                assertTrue(status.isVaihtoOk());
                assertThat(status.getInfot()).isEmpty();
                assertTrue(pp.getTila().equals(ProjektiTila.KOMMENTOINTI));
                assertTrue(pp.getPeruste().getTila().equals(PerusteTila.LUONNOS));
                for (Suoritustapa suoritustapa : pp.getPeruste().getSuoritustavat()) {
                    commonAssertTekstikappaleTila(suoritustapa.getSisalto(), PerusteTila.LUONNOS);
                    commonAssertOsienTila(suoritustapa.getTutkinnonOsat(), PerusteTila.LUONNOS);
                }
            return null;
        });
    }

    @Test
    public void testUpdateTilaKommentointiToLaadinta() {
        final PerusteprojektiDto projektiDto = teePerusteprojekti(ProjektiTila.KOMMENTOINTI, null, PerusteTila.LUONNOS);
        PerusteenOsaViiteDto sisaltoViite = luoSisalto(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, PerusteTila.LUONNOS);
        TutkinnonOsaViiteDto osaDto = luoTutkinnonOsa(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO);

        final TilaUpdateStatus status = service.updateTila(projektiDto.getId(), ProjektiTila.LAADINTA, null);

        transactionTemplate = new TransactionTemplate(transactionManager);
        // the code in this method executes in a transactional context
        transactionTemplate.execute(transactionStatus -> {
            Perusteprojekti pp = repo.findOne(projektiDto.getId());
            assertTrue(status.isVaihtoOk());
            assertThat(status.getInfot()).isEmpty();
            assertTrue(pp.getTila().equals(ProjektiTila.LAADINTA));
            assertTrue(pp.getPeruste().getTila().equals(PerusteTila.LUONNOS));
            for (Suoritustapa suoritustapa : pp.getPeruste().getSuoritustavat()) {
                commonAssertTekstikappaleTila(suoritustapa.getSisalto(), PerusteTila.LUONNOS);
                commonAssertOsienTila(suoritustapa.getTutkinnonOsat(), PerusteTila.LUONNOS);
            }
            return null;
        });

    }

    @Test
    public void testUpdateTilaLaadintaToViimeistelyValidiRakenne() {

        final PerusteprojektiDto projektiDto = teePerusteprojekti(ProjektiTila.LAADINTA, null, PerusteTila.LUONNOS);
        final TutkinnonRakenneLockContext ctx = TutkinnonRakenneLockContext.of(Long.valueOf(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO);
        lockService.lock(ctx);
        perusteService.updateTutkinnonRakenne(ctx.getPerusteId(), ctx.getKoodi(), luoValidiRakenne(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, PerusteTila.LUONNOS));

        final TilaUpdateStatus status = service.updateTila(projektiDto.getId(), ProjektiTila.VIIMEISTELY, null);
        tulostaInfo(status);
        transactionTemplate = new TransactionTemplate(transactionManager);
        // the code in this method executes in a transactional context
        transactionTemplate.execute(transactionStatus -> {
            Perusteprojekti pp = repo.findOne(projektiDto.getId());
            assertTrue(status.isVaihtoOk());
            assertThat(status.getInfot()).isEmpty();
            assertTrue(pp.getTila().equals(ProjektiTila.VIIMEISTELY));
            assertTrue(pp.getPeruste().getTila().equals(PerusteTila.LUONNOS));
            for (Suoritustapa suoritustapa : pp.getPeruste().getSuoritustavat()) {
                commonAssertTekstikappaleTila(suoritustapa.getSisalto(), PerusteTila.LUONNOS);
                commonAssertOsienTila(suoritustapa.getTutkinnonOsat(), PerusteTila.LUONNOS);
            }
            return null;
        });
        lockService.unlock(ctx);
    }

    @Test
    public void testUpdateTilaLaadintaToViimeistelyEpaValidiRakenne() {

        final PerusteprojektiDto projektiDto = teePerusteprojekti(ProjektiTila.LAADINTA, null, PerusteTila.LUONNOS);
        PerusteenOsaViiteDto sisaltoViite = luoSisalto(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, PerusteTila.LUONNOS);
        final TutkinnonRakenneLockContext ctx = TutkinnonRakenneLockContext.of(Long.valueOf(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO);
        lockService.lock(ctx);

        long perusteId = projektiDto.getPeruste().getIdLong();
        PerusteVersionDto versionDto = perusteService.getPerusteVersion(perusteId);
        perusteService.updateTutkinnonRakenne(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, luoEpaValidiRakenne(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, PerusteTila.LUONNOS));
        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());

        final TilaUpdateStatus status = service.updateTila(projektiDto.getId(), ProjektiTila.VIIMEISTELY, null);

        tulostaInfo(status);
        transactionTemplate = new TransactionTemplate(transactionManager);
        // the code in this method executes in a transactional context
        transactionTemplate.execute(transactionStatus -> {
            Perusteprojekti pp = repo.findOne(projektiDto.getId());
            assertFalse(status.isVaihtoOk());
            assertNotNull(status.getInfot());
            assertTrue(pp.getTila().equals(ProjektiTila.LAADINTA));
            assertTrue(pp.getPeruste().getTila().equals(PerusteTila.LUONNOS));
            for (Suoritustapa suoritustapa : pp.getPeruste().getSuoritustavat()) {
                commonAssertTekstikappaleTila(suoritustapa.getSisalto(), PerusteTila.LUONNOS);
                commonAssertOsienTila(suoritustapa.getTutkinnonOsat(), PerusteTila.LUONNOS);
            }
            return null;
        });
        lockService.unlock(ctx);
    }

    @Test
    public void testUpdateTilaLaadintaToViimeistelyVapaitaTutkinnonOsia() {

        final PerusteprojektiDto projektiDto = teePerusteprojekti(ProjektiTila.LAADINTA, null, PerusteTila.LUONNOS);
        PerusteenOsaViiteDto sisaltoViite = luoSisalto(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, PerusteTila.LUONNOS);
        TutkinnonOsaViiteDto osaDto = luoTutkinnonOsa(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO);

        final TilaUpdateStatus status = service.updateTila(projektiDto.getId(), ProjektiTila.VIIMEISTELY, null);

        transactionTemplate = new TransactionTemplate(transactionManager);
        // the code in this method executes in a transactional context
        transactionTemplate.execute(transactionStatus -> {
            Perusteprojekti pp = repo.findOne(projektiDto.getId());
            assertFalse(status.isVaihtoOk());
            assertNotNull(status.getInfot());
            assertTrue(pp.getTila().equals(ProjektiTila.LAADINTA));
            assertTrue(pp.getPeruste().getTila().equals(PerusteTila.LUONNOS));
            for (Suoritustapa suoritustapa : pp.getPeruste().getSuoritustavat()) {
                commonAssertTekstikappaleTila(suoritustapa.getSisalto(), PerusteTila.LUONNOS);
                commonAssertOsienTila(suoritustapa.getTutkinnonOsat(), PerusteTila.LUONNOS);
            }
            return null;
        });

    }

    @Test
    public void testUpdateTilaViimeistelyToLaadinta() {

        final PerusteprojektiDto projektiDto = teePerusteprojekti(ProjektiTila.VIIMEISTELY, null, PerusteTila.LUONNOS);
        PerusteenOsaViiteDto sisaltoViite = luoSisalto(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, PerusteTila.LUONNOS);
        final TutkinnonRakenneLockContext ctx = TutkinnonRakenneLockContext.of(Long.valueOf(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO);
        lockService.lock(ctx);
        perusteService.updateTutkinnonRakenne(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, luoValidiRakenne(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, PerusteTila.LUONNOS));

        final TilaUpdateStatus status = service.updateTila(projektiDto.getId(), ProjektiTila.LAADINTA, null);
        tulostaInfo(status);
        transactionTemplate = new TransactionTemplate(transactionManager);
        // the code in this method executes in a transactional context
        Object object = transactionTemplate.execute(transactionStatus -> {
            Perusteprojekti pp = repo.findOne(projektiDto.getId());
            assertTrue(status.isVaihtoOk());
            assertThat(status.getInfot()).isEmpty();
            assertTrue(pp.getTila().equals(ProjektiTila.LAADINTA));
            assertTrue(pp.getPeruste().getTila().equals(PerusteTila.LUONNOS));
            for (Suoritustapa suoritustapa : pp.getPeruste().getSuoritustavat()) {
                commonAssertTekstikappaleTila(suoritustapa.getSisalto(), PerusteTila.LUONNOS);
                commonAssertOsienTila(suoritustapa.getTutkinnonOsat(), PerusteTila.LUONNOS);
            }
            return null;
        });
        lockService.unlock(ctx);
    }

    @Test
    public void testUpdateTilaViimeistelyToValmis() {

        final PerusteprojektiDto projektiDto = teePerusteprojekti(ProjektiTila.VIIMEISTELY, null, PerusteTila.LUONNOS);
        PerusteenOsaViiteDto sisaltoViite = luoSisalto(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, PerusteTila.LUONNOS);
        final TutkinnonRakenneLockContext ctx = TutkinnonRakenneLockContext.of(Long.valueOf(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO);
        lockService.lock(ctx);
        perusteService.updateTutkinnonRakenne(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, luoValidiRakenne(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, PerusteTila.LUONNOS));
        ppTestUtils.luoValidiKVLiite(projektiDto.getPeruste().getIdLong());

        final TilaUpdateStatus status = service.updateTila(projektiDto.getId(), ProjektiTila.VALMIS, null);
        tulostaInfo(status);
        transactionTemplate = new TransactionTemplate(transactionManager);
        // the code in this method executes in a transactional context
        Object object = transactionTemplate.execute(transactionStatus -> {
            Perusteprojekti pp = repo.findOne(projektiDto.getId());
            assertTrue(status.isVaihtoOk());
            assertThat(status.getInfot()).isEmpty();
            assertTrue(pp.getTila().equals(ProjektiTila.VALMIS));
            assertTrue(pp.getPeruste().getTila().equals(PerusteTila.LUONNOS));
            for (Suoritustapa suoritustapa : pp.getPeruste().getSuoritustavat()) {
                commonAssertTekstikappaleTila(suoritustapa.getSisalto(), PerusteTila.LUONNOS);
                commonAssertOsienTila(suoritustapa.getTutkinnonOsat(), PerusteTila.LUONNOS);
            }
            return null;
        });
        lockService.unlock(ctx);

    }

    @Test
    public void testUpdateTilaValmisToJulkaistu() {

        final PerusteprojektiDto projektiDto = teePerusteprojekti(ProjektiTila.VALMIS, null, PerusteTila.LUONNOS);
        PerusteenOsaViiteDto sisaltoViite = luoSisalto(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, PerusteTila.LUONNOS);
        final TutkinnonRakenneLockContext ctx = TutkinnonRakenneLockContext.of(Long.valueOf(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO);
        lockService.lock(ctx);
        perusteService.updateTutkinnonRakenne(ctx.getPerusteId(), ctx.getKoodi(), luoValidiRakenne(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, PerusteTila.LUONNOS));
        ppTestUtils.luoValidiKVLiite(projektiDto.getPeruste().getIdLong());

        final TilaUpdateStatus status = service.updateTila(projektiDto.getId(), ProjektiTila.JULKAISTU, TestUtils.createTiedote());
        tulostaInfo(status);
        transactionTemplate = new TransactionTemplate(transactionManager);
        // the code in this method executes in a transactional context
        Object object = transactionTemplate.execute(transactionStatus -> {
            Perusteprojekti pp = repo.findOne(projektiDto.getId());
            assertTrue(status.isVaihtoOk());
            assertThat(status.getInfot()).isEmpty();
            assertTrue(pp.getTila().equals(ProjektiTila.JULKAISTU));
            assertTrue(pp.getPeruste().getTila().equals(PerusteTila.VALMIS));
            for (Suoritustapa suoritustapa : pp.getPeruste().getSuoritustavat()) {
                commonAssertTekstikappaleTila(suoritustapa.getSisalto(), PerusteTila.VALMIS);
                commonAssertOsienTila(suoritustapa.getTutkinnonOsat(), PerusteTila.VALMIS);
            }

            return null;
        });
        lockService.unlock(ctx);
    }

    @Test
    public void testUpdateTilaValmisToJulkaistuEiDiaaria() {

        final PerusteprojektiDto projektiDto = teePerusteprojekti(ProjektiTila.VALMIS, null, PerusteTila.LUONNOS);
        PerusteenOsaViiteDto sisaltoViite = luoSisalto(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, PerusteTila.LUONNOS);
        PerusteDto perusteDto = perusteService.get(new Long(projektiDto.getPeruste().getId()));
        perusteDto.setDiaarinumero(null);
        perusteService.update(perusteDto.getId(), perusteDto);
        final TutkinnonRakenneLockContext ctx = TutkinnonRakenneLockContext.of(Long.valueOf(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO);
        lockService.lock(ctx);
        perusteService.updateTutkinnonRakenne(ctx.getPerusteId(), ctx.getKoodi(), luoValidiRakenne(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, PerusteTila.LUONNOS));

        final TilaUpdateStatus status = service.updateTila(projektiDto.getId(), ProjektiTila.JULKAISTU, null);
        tulostaInfo(status);
        transactionTemplate = new TransactionTemplate(transactionManager);
        // the code in this method executes in a transactional context
        Object object = transactionTemplate.execute(transactionStatus -> {
            Perusteprojekti pp = repo.findOne(projektiDto.getId());
            assertFalse(status.isVaihtoOk());
            assertNotNull(status.getInfot());
            assertTrue(pp.getTila().equals(ProjektiTila.VALMIS));
            assertTrue(pp.getPeruste().getTila().equals(PerusteTila.LUONNOS));
            for (Suoritustapa suoritustapa : pp.getPeruste().getSuoritustavat()) {
                commonAssertTekstikappaleTila(suoritustapa.getSisalto(), PerusteTila.LUONNOS);
                commonAssertOsienTila(suoritustapa.getTutkinnonOsat(), PerusteTila.LUONNOS);
            }
            return null;
        });
        lockService.unlock(ctx);

    }

    @Test
    public void testUpdateTilaValmisToJulkaistuEiVoimassaolonAlkamisaikaa() {

        final PerusteprojektiDto projektiDto = teePerusteprojekti(ProjektiTila.VALMIS, null, PerusteTila.LUONNOS);
        PerusteenOsaViiteDto sisaltoViite = luoSisalto(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, PerusteTila.LUONNOS);
        PerusteDto perusteDto = perusteService.get(new Long(projektiDto.getPeruste().getId()));
        perusteDto.setVoimassaoloAlkaa(null);
        perusteService.update(perusteDto.getId(), perusteDto);
        final TutkinnonRakenneLockContext ctx = TutkinnonRakenneLockContext.of(Long.valueOf(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO);
        lockService.lock(ctx);
        perusteService.updateTutkinnonRakenne(ctx.getPerusteId(), ctx.getKoodi(), luoValidiRakenne(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, PerusteTila.LUONNOS));

        final TilaUpdateStatus status = service.updateTila(projektiDto.getId(), ProjektiTila.JULKAISTU, null);
        tulostaInfo(status);
        transactionTemplate = new TransactionTemplate(transactionManager);
        // the code in this method executes in a transactional context
        Object object = transactionTemplate.execute(transactionStatus -> {
            Perusteprojekti pp = repo.findOne(projektiDto.getId());
            assertFalse(status.isVaihtoOk());
            assertNotNull(status.getInfot());
            assertTrue(pp.getTila().equals(ProjektiTila.VALMIS));
            assertTrue(pp.getPeruste().getTila().equals(PerusteTila.LUONNOS));
            for (Suoritustapa suoritustapa : pp.getPeruste().getSuoritustavat()) {
                commonAssertTekstikappaleTila(suoritustapa.getSisalto(), PerusteTila.LUONNOS);
                commonAssertOsienTila(suoritustapa.getTutkinnonOsat(), PerusteTila.LUONNOS);
            }
            return null;
        });
        lockService.unlock(ctx);

    }


    @Test
    public void testUpdateTilaJulkaistuToValmis() {

        final PerusteprojektiDto projektiDto = teePerusteprojekti(ProjektiTila.LAADINTA, null, PerusteTila.LUONNOS);
        PerusteenOsaViiteDto sisaltoViite = luoSisalto(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, PerusteTila.VALMIS);
        setPerusteSisaltoTila(perusteService.getSuoritustapaSisalto(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO), PerusteTila.VALMIS);
        final TutkinnonRakenneLockContext ctx = TutkinnonRakenneLockContext.of(Long.valueOf(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO);
        lockService.lock(ctx);
        perusteService.updateTutkinnonRakenne(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, luoValidiRakenne(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, PerusteTila.VALMIS));
        ppTestUtils.luoValidiKVLiite(projektiDto.getPeruste().getIdLong());

        service.updateTila(projektiDto.getId(), ProjektiTila.VIIMEISTELY, null);
        service.updateTila(projektiDto.getId(), ProjektiTila.VALMIS, null);
        service.updateTila(projektiDto.getId(), ProjektiTila.JULKAISTU, TestUtils.createTiedote());

        final TilaUpdateStatus status = service.updateTila(projektiDto.getId(), ProjektiTila.VALMIS, null);
        tulostaInfo(status);
        transactionTemplate = new TransactionTemplate(transactionManager);
        // the code in this method executes in a transactional context
        Object object = transactionTemplate.execute(transactionStatus -> {
            Perusteprojekti pp = repo.findOne(projektiDto.getId());
            assertFalse(status.isVaihtoOk());
            assertNotNull(status.getInfot());
            assertTrue(pp.getTila().equals(ProjektiTila.JULKAISTU));
            assertTrue(pp.getPeruste().getTila().equals(PerusteTila.VALMIS));
            for (Suoritustapa suoritustapa : pp.getPeruste().getSuoritustavat()) {
                commonAssertTekstikappaleTila(suoritustapa.getSisalto(), PerusteTila.VALMIS);
                commonAssertOsienTila(suoritustapa.getTutkinnonOsat(), PerusteTila.VALMIS);
            }
            return null;
        });
        lockService.unlock(ctx);

    }

    @Test
    public void testUpdateTilaViimeistelyToValmisInvalidKVLiite() {

        final PerusteprojektiDto projektiDto = teePerusteprojekti(ProjektiTila.VIIMEISTELY, null, PerusteTila.LUONNOS);
        PerusteenOsaViiteDto sisaltoViite = luoSisalto(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, PerusteTila.LUONNOS);
        final TutkinnonRakenneLockContext ctx = TutkinnonRakenneLockContext.of(Long.valueOf(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO);
        lockService.lock(ctx);
        perusteService.updateTutkinnonRakenne(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, luoValidiRakenne(new Long(projektiDto.getPeruste().getId()), Suoritustapakoodi.NAYTTO, PerusteTila.LUONNOS));

        final TilaUpdateStatus status = service.updateTila(projektiDto.getId(), ProjektiTila.VALMIS, null);
        tulostaInfo(status);
        transactionTemplate = new TransactionTemplate(transactionManager);
        // the code in this method executes in a transactional context
        Object object = transactionTemplate.execute(transactionStatus -> {
            Perusteprojekti pp = repo.findOne(projektiDto.getId());
            assertFalse(status.isVaihtoOk());
            assertEquals(13, status.getInfot().size());
            assertEquals(pp.getTila(), ProjektiTila.VIIMEISTELY);
            assertEquals(pp.getPeruste().getTila(), PerusteTila.LUONNOS);
            for (Suoritustapa suoritustapa : pp.getPeruste().getSuoritustavat()) {
                commonAssertTekstikappaleTila(suoritustapa.getSisalto(), PerusteTila.LUONNOS);
                commonAssertOsienTila(suoritustapa.getTutkinnonOsat(), PerusteTila.LUONNOS);
            }
            return null;
        });
        lockService.unlock(ctx);

    }

    private void tulostaInfo(TilaUpdateStatus status) {
        if (status.getInfot() != null) {
            for (Status info : status.getInfot()) {
                System.out.println("Info: " + info.getViesti());
                if (info.getValidointi() != null) {
                    for (Ongelma ongelma : info.getValidointi().ongelmat) {
                        System.out.println("Validointi ongelma: " + ongelma.ongelma);
                    }

                }
            }
        }
    }

    private void commonAssertTekstikappaleTila(PerusteenOsaViite sisalto, PerusteTila haluttuTila) {
        if (sisalto.getPerusteenOsa() != null && sisalto.getPerusteenOsa().getTunniste() != PerusteenOsaTunniste.RAKENNE) {
            assertTrue(sisalto.getPerusteenOsa().getTila().equals(haluttuTila));
        }
        if (sisalto.getLapset() != null) {
            for (PerusteenOsaViite viite : sisalto.getLapset()) {
                commonAssertTekstikappaleTila(viite, haluttuTila);
            }
        }
    }

    private void commonAssertOsienTila(Set<TutkinnonOsaViite> osat, PerusteTila haluttuTila) {
        for (TutkinnonOsaViite osa : osat) {
            assertTrue(osa.getTutkinnonOsa().getTila().equals(haluttuTila));
        }
    }


    private PerusteprojektiDto teePerusteprojekti(ProjektiTila tila, Long perusteId, PerusteTila perusteTila) {
        PerusteprojektiLuontiDto ppldto;
        ppldto = new PerusteprojektiLuontiDto(koulutustyyppi, yksikko, perusteId, tila, tyyppi, ryhmaId);
        ppldto.setDiaarinumero(TestUtils.uniikkiString());
        ppldto.setYhteistyotaho(yhteistyotaho);
        ppldto.setTehtava(tehtava);
        Assert.assertNotNull(ppldto);
        ppldto.setNimi(TestUtils.uniikkiString());
        PerusteprojektiDto projektiDto = service.save(ppldto);

        Perusteprojekti pp = repo.findOne(projektiDto.getId());
        pp.setTila(tila);
        repo.save(pp);

        PerusteDto pDto = perusteService.get(new Long(projektiDto.getPeruste().getId()));
        pDto.setNimi(TestUtils.lt(TestUtils.uniikkiString()));
        pDto.setDiaarinumero(TestUtils.validiDiaarinumero());
        pDto.setVoimassaoloAlkaa(new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 1, Calendar.MARCH, 12).getTime());
        perusteService.update(pDto.getId(), pDto);

        Peruste peruste = perusteRepo.findOne(pDto.getId());
        HashSet<Kieli> kielet = new HashSet<>();
        peruste.setKielet(kielet);
        peruste.asetaTila(perusteTila);
        perusteRepo.save(peruste);

        return service.get(projektiDto.getId());
    }

    private PerusteenOsaViiteDto.Matala luoPerusteenOsaViiteDto(LokalisoituTekstiDto nimi, PerusteTila tila, PerusteenOsaTunniste tunniste) {
        TekstiKappaleDto kappaleDto = new TekstiKappaleDto(nimi, tila, tunniste);
        PerusteenOsaViiteDto.Matala matala = new PerusteenOsaViiteDto.Matala(kappaleDto);
        return matala;
    }

    private void asetaPerusteenOsanTila(Long id, PerusteTila tila) {
        PerusteenOsa osa = perusteenOsaRepo.findOne(id);
        osa.asetaTila(tila);
        perusteenOsaRepo.save(osa);
    }


    private PerusteenOsaViiteDto luoSisalto(Long perusteId, Suoritustapakoodi suoritustapakoodi, PerusteTila tila) {
        PerusteenOsaViiteDto sisalto = perusteService.addSisalto(perusteId, suoritustapakoodi, luoPerusteenOsaViiteDto(TestUtils.lt(TestUtils.uniikkiString()), tila, PerusteenOsaTunniste.NORMAALI));
        asetaPerusteenOsanTila(sisalto.getPerusteenOsa().getId(), tila);

        PerusteenOsaViiteDto lapsi = perusteService.addSisaltoLapsi(perusteId, sisalto.getId(), luoPerusteenOsaViiteDto(TestUtils.lt(TestUtils.uniikkiString()), tila, PerusteenOsaTunniste.NORMAALI));
        asetaPerusteenOsanTila(lapsi.getPerusteenOsa().getId(), tila);
        for (int i = 0; i < 2; i++) {
            lapsi = perusteService.addSisaltoLapsi(perusteId, lapsi.getId(), luoPerusteenOsaViiteDto(TestUtils.lt(TestUtils.uniikkiString()), tila, PerusteenOsaTunniste.NORMAALI));
            asetaPerusteenOsanTila(lapsi.getPerusteenOsa().getId(), tila);
        }
        lapsi = perusteService.addSisaltoLapsi(perusteId, sisalto.getId(), luoPerusteenOsaViiteDto(TestUtils.lt(TestUtils.uniikkiString()), tila, PerusteenOsaTunniste.NORMAALI));
        asetaPerusteenOsanTila(lapsi.getPerusteenOsa().getId(), tila);

        return sisalto;
    }

    private void setPerusteSisaltoTila(PerusteenOsaViiteDto.Laaja viite, PerusteTila tila) {

        if (viite.getPerusteenOsa() != null) {
            perusteenOsaService.lock(viite.getPerusteenOsa().getId());
            viite.getPerusteenOsa().setTila(tila);
            perusteenOsaService.update((PerusteenOsaDto.Laaja) viite.getPerusteenOsa());
            perusteenOsaService.unlock(viite.getPerusteenOsa().getId());
        }

        if (viite.getLapset() != null) {
            for (PerusteenOsaViiteDto.Laaja lapsi : viite.getLapset()) {
                setPerusteSisaltoTila(lapsi, tila);
            }
        }
    }

    private TutkinnonOsaViiteDto luoTutkinnonOsa(Long id, Suoritustapakoodi suoritustapakoodi) {
        TutkinnonOsaViiteDto dto = new TutkinnonOsaViiteDto(BigDecimal.ONE, 1, TestUtils.lt(TestUtils.uniikkiString()), TutkinnonOsaTyyppi.NORMAALI);
        TutkinnonOsaDto tosa = new TutkinnonOsaDto();
        tosa.setNimi(dto.getNimi());

        dto.setTutkinnonOsaDto(tosa);
        TutkinnonOsaViiteDto lisatty = perusteService.addTutkinnonOsa(id, suoritustapakoodi, dto);
        return lisatty;
    }

    private RakenneOsaDto teeRakenneOsaDto(long id, Suoritustapakoodi suoritustapa, PerusteTila tila, Integer laajuus) {
        TutkinnonOsaDto to = new TutkinnonOsaDto();
        to.setTila(tila);

        KoodiDto koodiDto = new KoodiDto();
        koodiDto.setUri(TestUtils.uniikkiString());
        koodiDto.setArvo(TestUtils.uniikkiString());
        koodiDto.setKoodisto("tutkinnonosat");
        to.setKoodi(koodiDto);
        to.setKoodiArvo(TestUtils.uniikkiString());
        to.setKoodiUri(TestUtils.uniikkiString());

        TutkinnonOsaViiteDto tov = new TutkinnonOsaViiteDto();
        tov.setTutkinnonOsaDto(to);
        tov.setLaajuus(new BigDecimal(laajuus));

        TutkinnonOsaViiteDto luotuDto = perusteService.addTutkinnonOsa(id, suoritustapa, tov);

        asetaPerusteenOsanTila(new Long(luotuDto.getTutkinnonOsa().getId()), tila);

        RakenneOsaDto ro = new RakenneOsaDto();
        ro.setTutkinnonOsaViite(new EntityReference(luotuDto.getId()));
        return ro;
    }

    private RakenneModuuliDto teeRyhma(Integer laajuusMinimi, Integer laajuusMaksimi, Integer kokoMinimi, Integer kokoMaksimi, AbstractRakenneOsaDto... osat) {
        RakenneModuuliDto rakenne = new RakenneModuuliDto();

        MuodostumisSaantoDto.Laajuus msl = laajuusMinimi != null && laajuusMinimi != -1
            ? new MuodostumisSaantoDto.Laajuus(laajuusMinimi, laajuusMaksimi, LaajuusYksikko.OPINTOVIIKKO) : null;
        MuodostumisSaantoDto.Koko msk = kokoMinimi != null && kokoMinimi != -1 ? new MuodostumisSaantoDto.Koko(kokoMinimi, kokoMaksimi) : null;
        MuodostumisSaantoDto ms = (msl != null || msk != null) ? new MuodostumisSaantoDto(msl, msk) : null;

        ArrayList<AbstractRakenneOsaDto> aosat = new ArrayList<>();
        aosat.addAll(Arrays.asList(osat));
        rakenne.setOsat(aosat);
        rakenne.setMuodostumisSaanto(ms);
        rakenne.setRooli(RakenneModuuliRooli.NORMAALI);
        return rakenne;
    }

    private RakenneModuuliDto luoValidiRakenne(Long id, Suoritustapakoodi suoritustapa, PerusteTila tila) {
        RakenneModuuliDto rakenne = teeRyhma(
            10, 20, 1, 1,
            teeRakenneOsaDto(id, suoritustapa, tila, 10),
            teeRyhma(
                10, 10, 1, 1,
                teeRakenneOsaDto(id, suoritustapa, tila, 10),
                teeRakenneOsaDto(id, suoritustapa, tila, 20)
            )
        );

        return rakenne;
    }

    private RakenneModuuliDto luoEpaValidiRakenne(Long id, Suoritustapakoodi suoritustapa, PerusteTila tila) {
        RakenneModuuliDto rakenne = teeRyhma(
            10, 2000, 1, 1,
            teeRakenneOsaDto(id, suoritustapa, tila, 10),
            teeRyhma(
                10, 10, 1, 1,
                teeRakenneOsaDto(id, suoritustapa, tila, 10),
                teeRakenneOsaDto(id, suoritustapa, tila, 20)
            )
        );

        return rakenne;
    }
}

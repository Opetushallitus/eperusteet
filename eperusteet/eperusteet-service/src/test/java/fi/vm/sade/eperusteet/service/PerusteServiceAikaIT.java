package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.dto.peruste.*;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.repository.KoulutusRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Integraatiotesti muistinvaraista kantaa vasten.
 *
 * @author isaul
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PerusteServiceAikaIT extends AbstractIntegrationTest {

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

    private Peruste peruste;

    private Date nykyinenAika;

    public PerusteServiceAikaIT() {

    }

    private GregorianCalendar gc;

    @Before
    public void setUp() {
        TransactionStatus transaction = manager.getTransaction(new DefaultTransactionDefinition());

        gc = new GregorianCalendar(2017, 5, 4);
        nykyinenAika = gc.getTime();

        // Tuleva
        Peruste p = TestUtils.teePeruste();
        gc.set(2017, Calendar.JUNE, 5);
        p.setVoimassaoloAlkaa(gc.getTime());
        p.asetaTila(PerusteTila.VALMIS);
        peruste = repo.save(p);


        // Tulevan ja voimassa olevan rajalla
        p = TestUtils.teePeruste();
        gc.set(2017, Calendar.JUNE, 4);
        p.setVoimassaoloAlkaa(gc.getTime());
        p.asetaTila(PerusteTila.VALMIS);
        peruste = repo.save(p);

        // Voimassa oleva
        p = TestUtils.teePeruste();
        gc.set(2017, Calendar.JUNE, 3);
        p.setVoimassaoloAlkaa(gc.getTime());
        p.asetaTila(PerusteTila.VALMIS);
        peruste = repo.save(p);

        // Voimassa olevan ja siirtymän rajalla
        p = TestUtils.teePeruste();
        gc.set(2017, Calendar.JUNE, 4);
        p.setVoimassaoloLoppuu(gc.getTime());
        gc.set(2017, Calendar.JUNE, 5);
        p.setSiirtymaPaattyy(gc.getTime());
        p.asetaTila(PerusteTila.VALMIS);
        peruste = repo.save(p);

        // Siirtymässä
        p = TestUtils.teePeruste();
        gc.set(2017, Calendar.JUNE, 3);
        p.setVoimassaoloLoppuu(gc.getTime());
        gc.set(2017, Calendar.JUNE, 5);
        p.setSiirtymaPaattyy(gc.getTime());
        p.asetaTila(PerusteTila.VALMIS);
        peruste = repo.save(p);

        // Siirtymän ja poistuneen rajalla
        p = TestUtils.teePeruste();
        gc.set(2017, Calendar.JUNE, 4);
        p.setSiirtymaPaattyy(gc.getTime());
        p.asetaTila(PerusteTila.VALMIS);
        peruste = repo.save(p);

        // Poistuneet
        p = TestUtils.teePeruste();
        gc.set(2017, Calendar.JUNE, 3);
        p.setSiirtymaPaattyy(gc.getTime());
        p.asetaTila(PerusteTila.VALMIS);
        peruste = repo.save(p);

        manager.commit(transaction);
    }

    @Test
    public void testGetAll() {
        Page<PerusteHakuDto> perusteet = perusteService.getAll(new PageRequest(0, 10), Kieli.FI.toString());
        assertEquals(7, perusteet.getTotalElements());
    }

    private PerusteprojektiLuontiDto createPeruste() {
        PerusteprojektiLuontiDto result = new PerusteprojektiLuontiDto();
        return result;
    }

    @Test
    @Rollback
    public void testEiDuplikaatteja() {
        p = TestUtils.teePeruste();
        gc.set(2017, Calendar.JUNE, 3);
        p.setSiirtymaPaattyy(gc.getTime());
        p.asetaTila(PerusteTila.VALMIS);
        PerusteQuery pquery = new PerusteQuery();
        pquery.setKoulutustyyppi(Arrays.asList("koulutustyyppi_5"));
        Page<PerusteHakuDto> perusteet = perusteService.findBy(new PageRequest(0, 10), pquery);
        assertEquals(1, perusteet.getTotalElements());
    }

    @Test
    public void testTulevat() {
        PerusteQuery pquery = new PerusteQuery();
        pquery.setNykyinenAika(nykyinenAika.getTime());
        pquery.setTuleva(true);
        pquery.setVoimassaolo(false);
        pquery.setSiirtyma(false);
        pquery.setPoistunut(false);
        Page<PerusteHakuDto> perusteet = perusteService.findBy(new PageRequest(0, 10), pquery);
        assertEquals(1, perusteet.getTotalElements());
    }

    @Test
    public void testVoimassaOlevat() {
        PerusteQuery pquery = new PerusteQuery();
        pquery.setNykyinenAika(nykyinenAika.getTime());
        pquery.setTuleva(false);
        pquery.setVoimassaolo(true);
        pquery.setSiirtyma(false);
        pquery.setPoistunut(false);
        Page<PerusteHakuDto> perusteet = perusteService.findBy(new PageRequest(0, 10), pquery);
        assertEquals(5, perusteet.getTotalElements());
    }

    @Test
    public void testSiirtymassa() {
        PerusteQuery pquery = new PerusteQuery();
        pquery.setNykyinenAika(nykyinenAika.getTime());
        pquery.setTuleva(false);
        pquery.setVoimassaolo(false);
        pquery.setSiirtyma(true);
        pquery.setPoistunut(false);
        Page<PerusteHakuDto> perusteet = perusteService.findBy(new PageRequest(0, 10), pquery);
        assertEquals(1, perusteet.getTotalElements());
    }

    @Test
    public void testPoistuneet() {
        PerusteQuery pquery = new PerusteQuery();
        pquery.setNykyinenAika(nykyinenAika.getTime());
        pquery.setTuleva(false);
        pquery.setVoimassaolo(false);
        pquery.setSiirtyma(false);
        pquery.setPoistunut(true);
        Page<PerusteHakuDto> perusteet = perusteService.findBy(new PageRequest(0, 10), pquery);
        assertEquals(1, perusteet.getTotalElements());
    }
}

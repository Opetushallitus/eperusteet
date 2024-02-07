package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteHakuDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteQuery;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@DirtiesContext
@Transactional
public class PerusteServiceAikaIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Autowired
    private PerusteRepository repo;

    @Autowired
    private PlatformTransactionManager manager;

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

    private PerusteprojektiDto createPeruste() {
        PerusteprojektiLuontiDto result = new PerusteprojektiLuontiDto();
        result.setReforminMukainen(false);
        result.setNimi(TestUtils.uniikkiString());
        result.setKoulutustyyppi("koulutustyyppi_15");
        result.setLaajuusYksikko(LaajuusYksikko.OSAAMISPISTE);
        result.setReforminMukainen(true);
        result.setTyyppi(PerusteTyyppi.NORMAALI);
        result.setRyhmaOid("000");
        result.setDiaarinumero(TestUtils.uniikkiDiaari());
        PerusteprojektiDto pp = perusteprojektiService.save(result);
        return pp;
    }

    private void julkaise(Long projektiId) {
        TilaUpdateStatus status = perusteprojektiService.updateTila(projektiId, ProjektiTila.VIIMEISTELY, null);
        assertThat(status.isVaihtoOk()).isTrue();
        status = perusteprojektiService.updateTila(projektiId, ProjektiTila.VALMIS, null);
        assertThat(status.isVaihtoOk()).isTrue();
        status = perusteprojektiService.updateTila(projektiId, ProjektiTila.JULKAISTU, TestUtils.createTiedote());
        assertThat(status.isVaihtoOk()).isTrue();
    }

    @Test
    public void testEiDuplikaatteja() {
        HashSet<Kieli> kielet = new HashSet<>();
        kielet.add(Kieli.SV);
        kielet.add(Kieli.FI);

        PerusteprojektiDto a = createPeruste();
        PerusteDto ap = perusteService.get(a.getPeruste().getIdLong());
        gc.set(2017, Calendar.JUNE, 3);

        ap.setVoimassaoloAlkaa(gc.getTime());
        ap.setNimi(TestUtils.lt("ap"));
        ap.getNimi().getTekstit().put(Kieli.FI, "ap_fi");
        ap.getNimi().getTekstit().put(Kieli.SV, "ap_sv");
        ap.setKielet(kielet);
        ap.setDiaarinumero("OPH-12345-1234");
        perusteService.update(ap.getId(), ap);

        julkaise(a.getId());
        PerusteQuery pquery = new PerusteQuery();
        pquery.setKoulutustyyppi(Arrays.asList("koulutustyyppi_15"));

        Page<PerusteHakuDto> perusteet = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertEquals(1, perusteet.getTotalElements());

        pquery.setKieli(Collections.singleton("FI"));
        perusteet = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertEquals(1, perusteet.getTotalElements());

        pquery.setKieli(kielet.stream().map(k -> k.toString()).collect(Collectors.toSet()));
        perusteet = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);

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
        Page<PerusteHakuDto> perusteet = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
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
        Page<PerusteHakuDto> perusteet = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
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
        Page<PerusteHakuDto> perusteet = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
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
        Page<PerusteHakuDto> perusteet = perusteService.findJulkinenBy(new PageRequest(0, 10), pquery);
        assertEquals(1, perusteet.getTotalElements());
    }
}

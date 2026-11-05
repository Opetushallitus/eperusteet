package fi.vm.sade.eperusteet.service;

import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.Tiedote;
import fi.vm.sade.eperusteet.domain.TiedoteJulkaisuPaikka;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.TiedoteDto;
import fi.vm.sade.eperusteet.dto.TiedoteKevytDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKevytDto;
import fi.vm.sade.eperusteet.dto.peruste.SuoritustapaDto;
import fi.vm.sade.eperusteet.dto.peruste.TiedoteQuery;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.TiedoteRepositoryCustom;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;

import java.util.*;
import jakarta.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import static fi.vm.sade.eperusteet.service.test.util.TestUtils.lt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TiedoteServiceIT extends AbstractIntegrationTest {
    @Autowired
    private TiedoteService tiedoteService;

    @Autowired
    private PerusteprojektiTestUtils ppTestUtils;

    @Autowired
    private TiedoteRepositoryCustom tiedoteRepositoryCustom;

    @Autowired
    private PerusteRepository perusteRepository;

    @Dto
    @Autowired
    private DtoMapper dtoMapper;

    private List<Long> perusteIds = new ArrayList<>();
    private PerusteKevytDto perusteDto1;

    @Autowired
    private EntityManager entityManager;

    @Before
    public void setUp()
    {

        Peruste peruste1 = TestUtils.teePeruste();
        Peruste peruste2 = TestUtils.teePeruste();
        perusteRepository.save(peruste1);
        perusteRepository.save(peruste2);

        PerusteKevytDto perusteDto1 = dtoMapper.map(peruste1, PerusteKevytDto.class);
        PerusteKevytDto perusteDto2 = dtoMapper.map(peruste2, PerusteKevytDto.class);

        perusteIds.add(perusteDto1.getId());
        perusteIds.add(perusteDto2.getId());
        this.perusteDto1 = perusteDto1;

        TiedoteDto tiedoteDto;
        tiedoteDto = new TiedoteDto();
        tiedoteDto.setOtsikko(lt("otsikko"));
        tiedoteDto.setSisalto(lt("sisalto"));
        tiedoteDto.setJulkinen(true);
        tiedoteDto.setPerusteet(Sets.newHashSet(perusteDto2));
        tiedoteDto.setTutkinnonosat(Sets.newHashSet(KoodiDto.of(KoodistoUriArvo.TUTKINNONOSAT, "1")));
        tiedoteDto.setOsaamisalat(Sets.newHashSet(KoodiDto.of(KoodistoUriArvo.OSAAMISALA, "1")));
        tiedoteService.addTiedote(tiedoteDto);

        tiedoteDto = new TiedoteDto();
        tiedoteDto.setOtsikko(lt("otsikko2"));
        tiedoteDto.setSisalto(lt("sisalto2"));
        tiedoteDto.setJulkaisupaikat(Sets.newHashSet(TiedoteJulkaisuPaikka.AMOSAA, TiedoteJulkaisuPaikka.OPINTOPOLKU));
        tiedoteDto.setKoulutustyypit(Sets.newHashSet(KoulutusTyyppi.PERUSTUTKINTO, KoulutusTyyppi.ERIKOISAMMATTITUTKINTO));
        tiedoteDto.setPerusteet(Sets.newHashSet(perusteDto1, perusteDto2));
        tiedoteDto.setJulkinen(false);
        tiedoteService.addTiedote(tiedoteDto);

    }

    @Test
    public void testGetAll() {
        // Back to the near future
        Date alkaen = new Date((new Date()).getTime() + 1000);
        List<TiedoteDto> tiedotteet = tiedoteService.getAll(false, alkaen.getTime());
        assertEquals(0, tiedotteet.size());

        tiedotteet = tiedoteService.getAll(true, 0L);
        assertEquals(1, tiedotteet.size());
        assertThat(tiedotteet.get(0).getTutkinnonosat()).hasSize(1);
        assertThat(tiedotteet.get(0).getTutkinnonosat()).extracting("uri").containsExactlyInAnyOrder(KoodistoUriArvo.TUTKINNONOSAT + "_1");
        assertThat(tiedotteet.get(0).getOsaamisalat()).hasSize(1);
        assertThat(tiedotteet.get(0).getOsaamisalat()).extracting("uri").containsExactlyInAnyOrder(KoodistoUriArvo.OSAAMISALA + "_1");

        tiedotteet = tiedoteService.getAll(false, 0L);
        assertEquals(2, tiedotteet.size());

        TiedoteDto tiedote1 = tiedotteet.get(0);
        TiedoteDto tiedote2 = tiedotteet.get(1);

        // Tarkista että lista on järjestetty muokattu-päivämäärän mukaan, uusin ensin
        assertTrue(tiedote1.getMuokattu().compareTo(tiedote2.getMuokattu()) >= 0);

        assertThat(tiedote1.getJulkaisupaikat()).containsExactlyInAnyOrder(TiedoteJulkaisuPaikka.AMOSAA, TiedoteJulkaisuPaikka.OPINTOPOLKU);
        assertThat(tiedote1.getKoulutustyypit()).containsExactlyInAnyOrder(KoulutusTyyppi.PERUSTUTKINTO, KoulutusTyyppi.ERIKOISAMMATTITUTKINTO);
        assertThat(tiedote1.getPerusteet().stream().map(PerusteKevytDto::getId)).containsExactlyInAnyOrderElementsOf(perusteIds);
    }

    @Test
    public void testGetById() {
        List<TiedoteDto> tiedotteet = tiedoteService.getAll(false, 0L);
        assertEquals(2, tiedotteet.size());

        Long id = tiedotteet.get(0).getId();
        assertNotNull(id);
        TiedoteDto tiedote = tiedoteService.getTiedote(id);
        assertNotNull(tiedote);
        assertEquals(id, tiedote.getId());
    }

    @Test
    public void testUpdate() {
        List<TiedoteDto> tiedotteet = tiedoteService.getAll(false, 0L);
        assertEquals(2, tiedotteet.size());

        Long id = tiedotteet.get(0).getId();
        TiedoteDto tiedote = tiedoteService.getTiedote(id);
        String muokattuSisalto = "muokattu sisalto";
        tiedote.setSisalto(lt(muokattuSisalto));
        tiedote.setJulkaisupaikat(Sets.newHashSet(TiedoteJulkaisuPaikka.AMOSAA));
        tiedote.setKoulutustyypit(Sets.newHashSet(KoulutusTyyppi.PERUSTUTKINTO));
        tiedote.setPerusteet(Sets.newHashSet(this.perusteDto1));
        tiedoteService.updateTiedote(tiedote);

        tiedote = tiedoteService.getTiedote(id);
        assertEquals(muokattuSisalto, tiedote.getSisalto().get(Kieli.FI));

        assertThat(tiedote.getJulkaisupaikat()).containsExactlyInAnyOrder(TiedoteJulkaisuPaikka.AMOSAA);
        assertThat(tiedote.getKoulutustyypit()).containsExactlyInAnyOrder(KoulutusTyyppi.PERUSTUTKINTO);
        assertThat(tiedote.getPerusteet().stream().map(PerusteKevytDto::getId)).containsExactlyInAnyOrder(this.perusteDto1.getId());

    }

    @Test
    public void testDelete() {
        List<TiedoteDto> tiedotteet = tiedoteService.getAll(false, 0L);
        assertEquals(2, tiedotteet.size());
        assertThat(entityManager.createNativeQuery("SELECT * FROM tiedote_julkaisupaikka").getResultList()).isNotEmpty();
        assertThat(entityManager.createNativeQuery("SELECT * FROM tiedote_koulutustyyppi").getResultList()).isNotEmpty();
        assertThat(entityManager.createNativeQuery("SELECT * FROM tiedote_peruste").getResultList()).isNotEmpty();

        tiedoteService.removeTiedote(tiedotteet.get(0).getId());
        tiedoteService.removeTiedote(tiedotteet.get(1).getId());

        assertEquals(0, tiedoteService.getAll(false, 0L).size());
        assertThat(entityManager.createNativeQuery("SELECT * FROM tiedote_julkaisupaikka").getResultList()).isEmpty();
        assertThat(entityManager.createNativeQuery("SELECT * FROM tiedote_koulutustyyppi").getResultList()).isEmpty();
        assertThat(entityManager.createNativeQuery("SELECT * FROM tiedote_peruste").getResultList()).isEmpty();

    }

    @Test
    public void testHakuJulkinen() {
        TiedoteQuery tq = new TiedoteQuery();
        tq.setJulkinen(true);
        Page<TiedoteDto> tiedotteet = tiedoteService.findBy(tq);
        assertEquals(tiedotteet.getTotalElements(), 1);
    }

    @Test
    public void testHakuSisainen() {
        TiedoteQuery tq = new TiedoteQuery();
        tq.setJulkinen(false);
        Page<TiedoteDto> tiedotteet = tiedoteService.findBy(tq);
        assertEquals(1, tiedotteet.getTotalElements());
    }

    @Test
    public void testPerusteeton() {
        TiedoteQuery tq = new TiedoteQuery();
        tq.setPerusteeton(true);
        Page<TiedoteDto> tiedotteet = tiedoteService.findBy(tq);
        assertEquals(0, tiedotteet.getTotalElements());
    }

    @Test
    public void testTiedoteJarjestys() {
        TiedoteQuery tq = new TiedoteQuery();
        tq.setJarjestys("muokattu");
        tq.setJarjestysNouseva(true);
        Page<TiedoteDto> tiedotteet = tiedoteService.findBy(tq);
        Iterator<TiedoteDto> iterator = tiedotteet.iterator();
        TiedoteDto t1 = iterator.next();
        assertEquals(t1.getOtsikko().get(Kieli.FI), LokalisoituTekstiDto.of("otsikko").get(Kieli.FI));
        TiedoteDto t2 = iterator.next();
        assertEquals(t2.getOtsikko().get(Kieli.FI), LokalisoituTekstiDto.of("otsikko2").get(Kieli.FI));
    }

    @Test
    public void testHakuJulkaisupaikat() {
        {
            TiedoteQuery tq = new TiedoteQuery();
            tq.setTiedoteJulkaisuPaikka(Arrays.asList(TiedoteJulkaisuPaikka.AMOSAA.toString(), TiedoteJulkaisuPaikka.OPS.toString()));
            Page<TiedoteDto> tiedotteet = tiedoteService.findBy(tq);
            assertEquals(1, tiedotteet.getTotalElements());
        }

        {
            TiedoteQuery tq = new TiedoteQuery();
            tq.setTiedoteJulkaisuPaikka(Arrays.asList(TiedoteJulkaisuPaikka.OPS.toString()));
            Page<TiedoteDto> tiedotteet = tiedoteService.findBy(tq);
            assertEquals(0, tiedotteet.getTotalElements());
        }
    }

    @Test
    public void testHakuKoulutuspaikat() {
        {
            TiedoteQuery tq = new TiedoteQuery();
            tq.setKoulutusTyyppi(Arrays.asList(KoulutusTyyppi.PERUSTUTKINTO.toString(), KoulutusTyyppi.LUKIOKOULUTUS.toString()));
            Page<TiedoteDto> tiedotteet = tiedoteService.findBy(tq);
            assertEquals(1, tiedotteet.getTotalElements());
        }

        {
            TiedoteQuery tq = new TiedoteQuery();
            tq.setKoulutusTyyppi(Arrays.asList(KoulutusTyyppi.LUKIOKOULUTUS.toString()));
            Page<TiedoteDto> tiedotteet = tiedoteService.findBy(tq);
            assertEquals(0, tiedotteet.getTotalElements());
        }
    }

    @Test
    public void testHakuPerusteet() {

        {
            TiedoteQuery tq = new TiedoteQuery();
            tq.setPerusteIds(perusteIds);
            Page<TiedoteDto> tiedotteet = tiedoteService.findBy(tq);
            assertEquals(2, tiedotteet.getTotalElements());
        }

        {
            TiedoteQuery tq = new TiedoteQuery();
            tq.setPerusteIds(Arrays.asList(perusteDto1.getId(), 999l));
            Page<TiedoteDto> tiedotteet = tiedoteService.findBy(tq);
            assertEquals(1, tiedotteet.getTotalElements());
        }

        {
            TiedoteQuery tq = new TiedoteQuery();
            tq.setPerusteIds(Arrays.asList(999l));
            Page<TiedoteDto> tiedotteet = tiedoteService.findBy(tq);
            assertEquals(0, tiedotteet.getTotalElements());
        }
    }

    @Test
    public void testDtoConversion() {
        List<TiedoteDto> tiedotteet = tiedoteService.getAll(false, 0L);
        assertEquals(2, tiedotteet.size());

        PerusteprojektiDto projekti = ppTestUtils.createPerusteprojekti(dto -> dto.setKoulutustyyppi(KoulutusTyyppi.AMMATTITUTKINTO.toString()));
        PerusteDto perusteDto = ppTestUtils.initPeruste(projekti.getPeruste().getIdLong(), dto -> {
            SuoritustapaDto stDto = new SuoritustapaDto();
            stDto.setLaajuusYksikko(LaajuusYksikko.OSAAMISPISTE);
            stDto.setSuoritustapakoodi(Suoritustapakoodi.REFORMI);
            dto.getSuoritustavat().add(stDto);
        });
        assertEquals(2, tiedotteet.size());
        Long id = tiedotteet.iterator().next().getId();

        // Liitä perusteprojekti
        TiedoteDto tiedoteDto = tiedoteService.getTiedote(id);
        tiedoteDto.setPerusteprojekti(new Reference(projekti.getId()));
        tiedoteService.updateTiedote(tiedoteDto);

        // Koitetaan poistaa suoritustapa tiedotteen kautta
        tiedoteDto = tiedoteService.getTiedote(id);
        tiedoteDto.getPeruste().getSuoritustavat().clear();
        tiedoteDto = tiedoteService.updateTiedote(tiedoteDto);

        assertEquals(1, tiedoteDto.getPeruste().getSuoritustavat().size());
    }

    @Test
    @Transactional
    public void testLisaaTiedoteUseallaVastavuoroisellaTiedotteella() {
        TiedoteDto b = luoTestiTiedote("Tiedote B");
        TiedoteDto c = luoTestiTiedote("Tiedote C");

        TiedoteDto uusi = uusiTestiTiedote("Tiedote A");
        uusi.setTiedotteet(Sets.newHashSet(kevytTiedote(b), kevytTiedote(c)));
        TiedoteDto a = tiedoteService.addTiedote(uusi);

        assertTiedotteet(a, b.getId(), c.getId());
        tallennaJaTyhjennaValimuisti();

        TiedoteDto tallennettuA = tiedoteService.getTiedote(a.getId());
        assertTiedotteet(tallennettuA, b.getId(), c.getId());
        assertTiedotteet(tiedoteService.getTiedote(b.getId()), a.getId());
        assertTiedotteet(tiedoteService.getTiedote(c.getId()), a.getId());

        assertThat(tallennettuA.getTiedotteet())
                .extracting(tiedote -> tiedote.getOtsikko().get(Kieli.FI))
                .containsExactlyInAnyOrder("Tiedote B", "Tiedote C");
    }

    @Test
    @Transactional
    public void testPaivitaTiedotteetSailyttaaMuutSuhteet() {
        TiedoteDto a = luoTestiTiedote("Tiedote A");
        TiedoteDto b = luoTestiTiedote("Tiedote B");
        TiedoteDto c = luoTestiTiedote("Tiedote C");
        TiedoteDto d = luoTestiTiedote("Tiedote D");

        asetaTiedotteet(b.getId(), d);
        asetaTiedotteet(a.getId(), b, c);
        tallennaJaTyhjennaValimuisti();

        assertTiedotteet(tiedoteService.getTiedote(a.getId()), b.getId(), c.getId());
        assertTiedotteet(tiedoteService.getTiedote(b.getId()), a.getId(), d.getId());

        // A-B poistuu, A-C säilyy, A-D lisätään ja B-D säilyy.
        TiedoteDto paivitetty = asetaTiedotteet(a.getId(), c, d);
        assertTiedotteet(paivitetty, c.getId(), d.getId());
        tallennaJaTyhjennaValimuisti();

        assertTiedotteet(tiedoteService.getTiedote(a.getId()), c.getId(), d.getId());
        assertTiedotteet(tiedoteService.getTiedote(b.getId()), d.getId());
        assertTiedotteet(tiedoteService.getTiedote(c.getId()), a.getId());
        assertTiedotteet(tiedoteService.getTiedote(d.getId()), a.getId(), b.getId());
    }

    @Test
    @Transactional
    public void testTyhjaTiedotevalintaPoistaaMolemmatSuunnat() {
        TiedoteDto a = luoTestiTiedote("Tiedote A");
        TiedoteDto b = luoTestiTiedote("Tiedote B");
        asetaTiedotteet(a.getId(), b);
        tallennaJaTyhjennaValimuisti();

        assertTiedotteet(tiedoteService.getTiedote(b.getId()), a.getId());
        asetaTiedotteet(a.getId());
        tallennaJaTyhjennaValimuisti();

        assertTiedotteet(tiedoteService.getTiedote(a.getId()));
        assertTiedotteet(tiedoteService.getTiedote(b.getId()));
    }

    @Test
    @Transactional
    public void testNullTiedotevalintaPoistaaMolemmatSuunnat() {
        TiedoteDto a = luoTestiTiedote("Tiedote A");
        TiedoteDto b = luoTestiTiedote("Tiedote B");
        asetaTiedotteet(a.getId(), b);
        tallennaJaTyhjennaValimuisti();

        assertTiedotteet(tiedoteService.getTiedote(b.getId()), a.getId());
        TiedoteDto paivitettava = tiedoteService.getTiedote(a.getId());
        paivitettava.setTiedotteet(null);
        tiedoteService.updateTiedote(paivitettava);
        tallennaJaTyhjennaValimuisti();

        assertTiedotteet(tiedoteService.getTiedote(a.getId()));
        assertTiedotteet(tiedoteService.getTiedote(b.getId()));
    }

    @Test
    @Transactional
    public void testPoistaTiedotePoistaaSuhteetMuttaSailyttaaMuutTiedotteet() {
        TiedoteDto a = luoTestiTiedote("Tiedote A");
        TiedoteDto b = luoTestiTiedote("Tiedote B");
        TiedoteDto c = luoTestiTiedote("Tiedote C");
        asetaTiedotteet(b.getId(), c);
        asetaTiedotteet(a.getId(), b, c);
        tallennaJaTyhjennaValimuisti();

        assertTiedotteet(tiedoteService.getTiedote(a.getId()), b.getId(), c.getId());
        tiedoteService.removeTiedote(a.getId());
        tallennaJaTyhjennaValimuisti();

        assertThat(entityManager.find(Tiedote.class, a.getId())).isNull();
        assertTiedotteet(tiedoteService.getTiedote(b.getId()), c.getId());
        assertTiedotteet(tiedoteService.getTiedote(c.getId()), b.getId());
        assertThat(entityManager.createNativeQuery(
                        "SELECT lahde_id, kohde_id FROM tiedote_tiedote "
                                + "WHERE lahde_id = :id OR kohde_id = :id")
                .setParameter("id", a.getId())
                .getResultList()).isEmpty();
    }

    @Test
    @Transactional
    public void testTiedotettaEiVoiLiittaaItseensa() {
        TiedoteDto a = luoTestiTiedote("Tiedote A");
        tallennaJaTyhjennaValimuisti();

        TiedoteDto paivitettava = tiedoteService.getTiedote(a.getId());
        paivitettava.setTiedotteet(Sets.newHashSet(kevytTiedote(a)));

        assertThatThrownBy(() -> tiedoteService.updateTiedote(paivitettava))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    @Transactional
    public void testOlematontaTiedotettaEiVoiLiittaa() {
        TiedoteDto a = luoTestiTiedote("Tiedote A");
        tallennaJaTyhjennaValimuisti();

        TiedoteKevytDto puuttuva = new TiedoteKevytDto();
        puuttuva.setId(Long.MAX_VALUE);
        TiedoteDto paivitettava = tiedoteService.getTiedote(a.getId());
        paivitettava.setTiedotteet(Sets.newHashSet(puuttuva));

        assertThatThrownBy(() -> tiedoteService.updateTiedote(paivitettava))
                .isInstanceOf(NotExistsException.class);
    }

    private TiedoteDto uusiTestiTiedote(String otsikko) {
        TiedoteDto tiedote = new TiedoteDto();
        tiedote.setOtsikko(lt(otsikko));
        tiedote.setSisalto(lt("Testisisältö"));
        tiedote.setJulkinen(true);
        tiedote.setTiedotteet(new HashSet<>());
        return tiedote;
    }

    private TiedoteDto luoTestiTiedote(String otsikko) {
        return tiedoteService.addTiedote(uusiTestiTiedote(otsikko));
    }

    private TiedoteKevytDto kevytTiedote(TiedoteDto tiedote) {
        TiedoteKevytDto kevyt = new TiedoteKevytDto();
        kevyt.setId(tiedote.getId());
        kevyt.setOtsikko(tiedote.getOtsikko());
        return kevyt;
    }

    private TiedoteDto asetaTiedotteet(Long id, TiedoteDto... valitut) {
        TiedoteDto tiedote = tiedoteService.getTiedote(id);
        Set<TiedoteKevytDto> tiedotteet = new HashSet<>();
        for (TiedoteDto valittu : valitut) {
            tiedotteet.add(kevytTiedote(valittu));
        }
        tiedote.setTiedotteet(tiedotteet);
        return tiedoteService.updateTiedote(tiedote);
    }

    private void assertTiedotteet(TiedoteDto tiedote, Long... odotetutIdt) {
        assertThat(tiedote).isNotNull();
        assertThat(tiedote.getTiedotteet()).isNotNull();
        assertThat(tiedote.getTiedotteet())
                .extracting(TiedoteKevytDto::getId)
                .containsExactlyInAnyOrder(odotetutIdt);
    }

    private void tallennaJaTyhjennaValimuisti() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void testKoulutustyypiton() {
        TiedoteQuery tq = new TiedoteQuery();
        tq.setKoulutusTyyppi(Arrays.asList(KoulutusTyyppi.PERUSTUTKINTO.toString(), KoulutusTyyppi.LUKIOKOULUTUS.toString()));
        tq.setKoulutustyypiton(true);
        Page<TiedoteDto> tiedotteet = tiedoteService.findBy(tq);
        assertEquals(2, tiedotteet.getTotalElements());
    }

}

package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteVersionDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.repository.PerusteenOsaViiteRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@DirtiesContext
public class PerusteenOsaViiteIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteenOsaViiteService service;
    @Autowired
    private PerusteService perusteService;
    @Autowired
    private PerusteenOsaViiteRepository repo;
    @PersistenceContext
    private EntityManager em;

    private Long perusteId = null;
    private Long juuriId = null;
    private Long lapsiId = null;
    private Long lapsenlapsiId = null;
    private Long tekstikappaleId = null;
    private Long tekstikappaleLapsenlapsiId = null;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Luodaan juuri viite. Juurelle lapsi, millä on tekstikappale sekä lapsenlapsi, millä on tekstikappale.
     */
    @Before
    public void setUp() {
        Peruste peruste = new Peruste();
        em.persist(peruste);
        perusteId = peruste.getId();

        Suoritustapa suoritustapa = new Suoritustapa();
        suoritustapa.setSuoritustapakoodi(Suoritustapakoodi.NAYTTO);
        em.persist(suoritustapa);
        peruste.getSuoritustavat().add(suoritustapa);
        suoritustapa.getPerusteet().add(peruste);

        PerusteenOsaViite juuri = new PerusteenOsaViite();
        juuri.setLapset(new ArrayList<>());
        juuri.setVanhempi(null);

        juuri = repo.save(juuri);
        juuriId = juuri.getId();
        suoritustapa.setSisalto(juuri);
        juuri.setSuoritustapa(suoritustapa);

        PerusteenOsaViite lapsi = new PerusteenOsaViite();
        TekstiKappale tekstikappale = new TekstiKappale();
        em.persist(tekstikappale);

        tekstikappaleId = tekstikappale.getId();
        lapsi.setPerusteenOsa(tekstikappale);
        lapsi.setVanhempi(juuri);
        lapsi.setLapset(new ArrayList<>());
        lapsi = repo.save(lapsi);
        lapsiId = lapsi.getId();

        PerusteenOsaViite lapsenlapsi = new PerusteenOsaViite();
        tekstikappale = new TekstiKappale();
        em.persist(tekstikappale);

        tekstikappaleLapsenlapsiId = tekstikappale.getId();
        lapsenlapsi.setPerusteenOsa(tekstikappale);
        lapsenlapsi.setVanhempi(lapsi);
        lapsenlapsi.setLapset(new ArrayList<>());
        lapsenlapsi = repo.save(lapsenlapsi);
        lapsenlapsiId = lapsenlapsi.getId();

        lapsi.getLapset().add(lapsenlapsi);
        juuri.getLapset().add(lapsi);

        em.flush();
    }

    @Test
    @Rollback
    public void testRemoveSisaltoOK() {
        service.removeSisalto(perusteId, lapsenlapsiId);
        em.flush();
        assertNotEquals(null, repo.findOne(juuriId));
        assertNotEquals(null, repo.findOne(lapsiId));
        assertNotEquals(null, em.find(TekstiKappale.class, tekstikappaleId));
        assertNull(repo.findOne(lapsenlapsiId));
        assertNull(em.find(TekstiKappale.class, tekstikappaleLapsenlapsiId));
    }

    @Test(expected = BusinessRuleViolationException.class)
    @Rollback
    public void testRemoveSisaltoJuuri() throws BusinessRuleViolationException {
        service.removeSisalto(perusteId, juuriId);
        assertNotEquals(null, repo.findOne(juuriId));
        assertNotEquals(null, repo.findOne(lapsiId));
        assertNotEquals(null, em.find(TekstiKappale.class, tekstikappaleId));
        assertNotEquals(null, repo.findOne(lapsenlapsiId));
        assertNotEquals(null, em.find(TekstiKappale.class, tekstikappaleLapsenlapsiId));
    }

    @Test
    @Rollback
    public void testRemoveSisaltoLapsia() throws BusinessRuleViolationException {
        service.removeSisalto(perusteId, lapsiId);
        assertNotEquals(null, repo.findOne(juuriId));
        assertNull(repo.findOne(lapsiId));
        assertNull(em.find(TekstiKappale.class, tekstikappaleId));
        assertNull(repo.findOne(lapsenlapsiId));
        assertNull(em.find(TekstiKappale.class, tekstikappaleLapsenlapsiId));
    }

    @Test
    public void tekstikappaleJarjestysTest() {
        PerusteprojektiDto pp = ppTestUtils.createPerusteprojekti(ppl -> {
            ppl.setEsikatseltavissa(true);
            ppl.setKoulutustyyppi(KoulutusTyyppi.ESIOPETUS.toString());
        });
        PerusteDto peruste = ppTestUtils.initPeruste(pp.getPeruste().getIdLong());
        perusteId = peruste.getId();

        PerusteenOsaViiteDto.Matala tekstiKappale1 = perusteService.addSisaltoUUSI(peruste.getId(), Suoritustapakoodi.REFORMI, new PerusteenOsaViiteDto.Matala(new TekstiKappaleDto()));
        PerusteenOsaViiteDto.Matala tekstiKappale11 = perusteService.addSisaltoLapsi(peruste.getId(), tekstiKappale1.getId(), new PerusteenOsaViiteDto.Matala(new TekstiKappaleDto()));
        PerusteenOsaViiteDto.Matala tekstiKappale111 = perusteService.addSisaltoLapsi(peruste.getId(), tekstiKappale11.getId(), new PerusteenOsaViiteDto.Matala(new TekstiKappaleDto()));
        PerusteenOsaViiteDto.Matala tekstiKappale12 = perusteService.addSisaltoLapsi(peruste.getId(), tekstiKappale1.getId(), new PerusteenOsaViiteDto.Matala(new TekstiKappaleDto()));

        PerusteenOsaViiteDto.Matala tekstiKappale2 = perusteService.addSisaltoUUSI(peruste.getId(), Suoritustapakoodi.REFORMI, new PerusteenOsaViiteDto.Matala(new TekstiKappaleDto()));

        PerusteenOsaViiteDto.Matala tekstiKappale3 = perusteService.addSisaltoUUSI(peruste.getId(), Suoritustapakoodi.REFORMI, new PerusteenOsaViiteDto.Matala(new TekstiKappaleDto()));

        PerusteenOsaViiteDto.Laaja juuri = perusteService.getSuoritustapaSisalto(perusteId, Suoritustapakoodi.REFORMI, PerusteenOsaViiteDto.Laaja.class);

        assertThat(juuri.getLapset().size()).isEqualTo(3);
        assertThat(juuri.getLapset().get(0).getId()).isEqualTo(tekstiKappale1.getId());
        assertThat(juuri.getLapset().get(0).getLapset().size()).isEqualTo(2);
        assertThat(juuri.getLapset().get(0).getLapset().get(0).getLapset().size()).isEqualTo(1);
        assertThat(juuri.getLapset().get(1).getLapset().size()).isEqualTo(0);
        assertThat(juuri.getLapset().get(1).getId()).isEqualTo(tekstiKappale2.getId());
        assertThat(juuri.getLapset().get(2).getLapset().size()).isEqualTo(0);
        assertThat(juuri.getLapset().get(2).getId()).isEqualTo(tekstiKappale3.getId());

        juuri.setLapset(List.of(juuri.getLapset().get(2), juuri.getLapset().get(1), juuri.getLapset().get(0)));
        service.reorderSubTree(perusteId, juuri.getId(), juuri);

        juuri = perusteService.getSuoritustapaSisalto(perusteId, Suoritustapakoodi.REFORMI, PerusteenOsaViiteDto.Laaja.class);

        assertThat(juuri.getLapset().size()).isEqualTo(3);
        assertThat(juuri.getLapset().get(2).getId()).isEqualTo(tekstiKappale1.getId());
        assertThat(juuri.getLapset().get(2).getLapset().size()).isEqualTo(2);
        assertThat(juuri.getLapset().get(2).getLapset().get(0).getId()).isEqualTo(tekstiKappale11.getId());
        assertThat(juuri.getLapset().get(2).getLapset().get(1).getId()).isEqualTo(tekstiKappale12.getId());
        assertThat(juuri.getLapset().get(2).getLapset().get(0).getLapset().size()).isEqualTo(1);
        assertThat(juuri.getLapset().get(2).getLapset().get(0).getLapset().get(0).getId()).isEqualTo(tekstiKappale111.getId());
        assertThat(juuri.getLapset().get(1).getLapset().size()).isEqualTo(0);
        assertThat(juuri.getLapset().get(1).getId()).isEqualTo(tekstiKappale2.getId());
        assertThat(juuri.getLapset().get(0).getLapset().size()).isEqualTo(0);
        assertThat(juuri.getLapset().get(0).getId()).isEqualTo(tekstiKappale3.getId());

    }

    private PerusteenOsaViiteDto.Suppea newSuppeaWithLapset(Long id) {
        PerusteenOsaViiteDto.Suppea suppea = new PerusteenOsaViiteDto.Suppea();
        suppea.setId(id);
        return suppea;
    }
}

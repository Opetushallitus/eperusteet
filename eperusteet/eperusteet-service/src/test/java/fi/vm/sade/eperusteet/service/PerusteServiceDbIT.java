package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.dto.peruste.PerusteHakuDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.test.AbstractDbIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Yksinkertainen integraatiotesti käyttäen "oikeaa" tietokantaa.
 * @see AbstractDbIntegrationTest
 */
@Transactional
public class PerusteServiceDbIT extends AbstractDbIntegrationTest {

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteRepository repo;

    @PersistenceContext
    private EntityManager em;


    @Before
    public void setUp() {
        Peruste p = new Peruste();
        p.setNimi(TekstiPalanen.of(Collections.singletonMap(Kieli.FI, "Nimi")));
        repo.save(p);
        em.flush();
    }

    @Test
    @Rollback
    public void testGet() {
        Page<PerusteHakuDto> perusteet = perusteService.getAll(new PageRequest(0, 10), "fi");
        assertEquals(perusteet.getTotalElements(), 1);
    }

}

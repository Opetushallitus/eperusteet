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
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.tutkinnonOsa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.TutkinnonOsaViite;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author harrik
 */
@Transactional
public class PerusteprojektiServiceTilaIT extends AbstractIntegrationTest {
    
    @Autowired
    private PerusteprojektiRepository repo;
    @PersistenceContext
    private EntityManager em;
    @Autowired
    private PerusteprojektiService service;
    
    private Perusteprojekti projekti;
    private Peruste peruste;
    private TekstiKappale tekstikappale;
    private TutkinnonOsa osa;
    private Suoritustapa naytto;
    
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
      
        projekti = new Perusteprojekti();
        projekti.setDiaarinumero("12345");
        projekti.setNimi("Testi projekti");
        projekti.setTila(ProjektiTila.LAADINTA);
        
        naytto = new Suoritustapa();
        PerusteenOsaViite sisalto = new PerusteenOsaViite();
        tekstikappale = new TekstiKappale();
        tekstikappale.setNimi(TestUtils.tekstiPalanenOf(Kieli.FI, "testi_tekstikappale"));
        tekstikappale.setTeksti(TestUtils.tekstiPalanenOf(Kieli.FI, "Hodor, hodor - hodor, hodor. Hodor hodor hodor! Hodor, hodor, hodor. Hodor hodor - hodor hodor. Hodor. Hodor. Hodor hodor hodor hodor? Hodor hodor hodor; hodor hodor... Hodor hodor hodor... Hodor hodor hodor. Hodor. "));
        tekstikappale.setTila(PerusteTila.LUONNOS);
        em.persist(tekstikappale);
        sisalto.setPerusteenOsa(tekstikappale);
        naytto.setSisalto(sisalto);
        em.persist(sisalto);
        naytto.setSuoritustapakoodi(Suoritustapakoodi.NAYTTO);
        naytto.setTutkinnonOsat(new HashSet<TutkinnonOsaViite>());
        em.persist(naytto);
        naytto.setRakenne(luoValidiRakenne());

        peruste = new Peruste();
        peruste.setSuoritustavat(new HashSet<Suoritustapa>());
        peruste.getSuoritustavat().add(naytto);
        peruste.setTila(PerusteTila.LUONNOS);
        peruste.setTyyppi(PerusteTyyppi.NORMAALI);

        projekti.setPeruste(peruste);
        em.persist(peruste);
        
        //projekti.getPeruste().getSuoritustapa(Suoritustapakoodi.NAYTTO).setRakenne(luoValidiRakenne());
        
        
        repo.save(projekti);
        em.flush();

    }
    
    @After
    public void tearDown() {
    }

    @Test
    @Rollback(true)
    public void testUpdateTilaLaadintaToKommentointi() {
        TilaUpdateStatus status = service.updateTila(projekti.getId(), ProjektiTila.KOMMENTOINTI);
        assertTrue(status.isVaihtoOk());
        assertNull(status.getInfot());
        assertTrue(repo.getOne(projekti.getId()).getTila().equals(ProjektiTila.KOMMENTOINTI));
        assertTrue(em.find(Peruste.class, peruste.getId()).getTila().equals(PerusteTila.LUONNOS));
        assertTrue(em.find(TekstiKappale.class, tekstikappale.getId()).getTila().equals(PerusteTila.LUONNOS));
        assertTrue(em.find(TutkinnonOsa.class, osa.getId()).getTila().equals(PerusteTila.LUONNOS));
    }
    
    @Test
    @Rollback(true)
    public void testUpdateTilaKommentointiToLaadinta() {
        projekti.setTila(ProjektiTila.KOMMENTOINTI);
        repo.save(projekti);
        TilaUpdateStatus status = service.updateTila(projekti.getId(), ProjektiTila.LAADINTA);
        assertTrue(status.isVaihtoOk());
        assertNull(status.getInfot());
        assertTrue(repo.getOne(projekti.getId()).getTila().equals(ProjektiTila.LAADINTA));
        assertTrue(em.find(Peruste.class, peruste.getId()).getTila().equals(PerusteTila.LUONNOS));
        assertTrue(em.find(TekstiKappale.class, tekstikappale.getId()).getTila().equals(PerusteTila.LUONNOS));
        assertTrue(em.find(TutkinnonOsa.class, osa.getId()).getTila().equals(PerusteTila.LUONNOS));
    }
    
    @Test
    @Rollback(true)
    public void testUpdateTilaLaadintaToViimeistelyValidiRakenne() {
        
        /*projekti.getPeruste().getSuoritustapa(Suoritustapakoodi.NAYTTO).setRakenne(luoValidiRakenne());
        repo.save(projekti);*/
        
        TilaUpdateStatus status = service.updateTila(projekti.getId(), ProjektiTila.VIIMEISTELY);
        assertTrue(status.isVaihtoOk());
        assertNull(status.getInfot());
        assertTrue(repo.getOne(projekti.getId()).getTila().equals(ProjektiTila.VIIMEISTELY));
        assertTrue(em.find(Peruste.class, peruste.getId()).getTila().equals(PerusteTila.LUONNOS));
        assertTrue(em.find(TekstiKappale.class, tekstikappale.getId()).getTila().equals(PerusteTila.LUONNOS));
        assertTrue(em.find(TutkinnonOsa.class, osa.getId()).getTila().equals(PerusteTila.LUONNOS));
    }
    
    
    @Test
    @Rollback(true)
    public void testUpdateTilaLaadintaToViimeistelyEpaValidiRakenne() {
        
        projekti.getPeruste().getSuoritustapa(Suoritustapakoodi.NAYTTO).setRakenne(luoEpaValidiRakenne());
        repo.save(projekti);
        
        TilaUpdateStatus status = service.updateTila(projekti.getId(), ProjektiTila.VIIMEISTELY);
        assertFalse(status.isVaihtoOk());
        assertNotNull(status.getInfot());
        assertTrue(repo.getOne(projekti.getId()).getTila().equals(ProjektiTila.LAADINTA));
        assertTrue(em.find(Peruste.class, peruste.getId()).getTila().equals(PerusteTila.LUONNOS));
        assertTrue(em.find(TekstiKappale.class, tekstikappale.getId()).getTila().equals(PerusteTila.LUONNOS));
        assertTrue(em.find(TutkinnonOsa.class, osa.getId()).getTila().equals(PerusteTila.LUONNOS));
    }
    
    @Test
    @Rollback(true)
    public void testUpdateTilaLaadintaToViimeistelyVapaitaTutkinnonOsia() {
        

        projekti.getPeruste().getSuoritustapa(Suoritustapakoodi.NAYTTO).getTutkinnonOsat().add(luoTutkinnonOsaViite());
        repo.save(projekti);
        
        TilaUpdateStatus status = service.updateTila(projekti.getId(), ProjektiTila.VIIMEISTELY);
        assertFalse(status.isVaihtoOk());
        assertNotNull(status.getInfot());
        assertTrue(repo.getOne(projekti.getId()).getTila().equals(ProjektiTila.LAADINTA));
        assertTrue(em.find(Peruste.class, peruste.getId()).getTila().equals(PerusteTila.LUONNOS));
        assertTrue(em.find(TekstiKappale.class, tekstikappale.getId()).getTila().equals(PerusteTila.LUONNOS));
        assertTrue(em.find(TutkinnonOsa.class, osa.getId()).getTila().equals(PerusteTila.LUONNOS));
    }
    
    @Test
    @Rollback(true)
    public void testUpdateTilaViimeistelyToLaadinta() {
        projekti.setTila(ProjektiTila.VIIMEISTELY);
        repo.save(projekti);
        TilaUpdateStatus status = service.updateTila(projekti.getId(), ProjektiTila.LAADINTA);
        assertTrue(status.isVaihtoOk());
        assertNull(status.getInfot());
        assertTrue(repo.getOne(projekti.getId()).getTila().equals(ProjektiTila.LAADINTA));
        assertTrue(em.find(Peruste.class, peruste.getId()).getTila().equals(PerusteTila.LUONNOS));
        assertTrue(em.find(TekstiKappale.class, tekstikappale.getId()).getTila().equals(PerusteTila.LUONNOS));
        assertTrue(osa.getTila().equals(PerusteTila.LUONNOS));
    }
    
    @Test
    @Rollback(true)
    public void testUpdateTilaViimeistelyToValmis() {
        projekti.setTila(ProjektiTila.VIIMEISTELY);
        repo.save(projekti);
        TilaUpdateStatus status = service.updateTila(projekti.getId(), ProjektiTila.VALMIS);
        assertTrue(status.isVaihtoOk());
        assertNull(status.getInfot());
        assertTrue(repo.getOne(projekti.getId()).getTila().equals(ProjektiTila.VALMIS));
        assertTrue(em.find(Peruste.class, peruste.getId()).getTila().equals(PerusteTila.LUONNOS));
        assertTrue(em.find(TekstiKappale.class, tekstikappale.getId()).getTila().equals(PerusteTila.LUONNOS));
        assertTrue(em.find(TutkinnonOsa.class, osa.getId()).getTila().equals(PerusteTila.LUONNOS));
    }
    
    @Test
    @Rollback(true)
    public void testUpdateTilaValmisToJulkaistu() {
        projekti.setTila(ProjektiTila.VALMIS);
        repo.save(projekti);
        TilaUpdateStatus status = service.updateTila(projekti.getId(), ProjektiTila.JULKAISTU);
        assertTrue(status.isVaihtoOk());
        assertNull(status.getInfot());
        assertTrue(repo.getOne(projekti.getId()).getTila().equals(ProjektiTila.JULKAISTU));
        assertTrue(em.find(Peruste.class, peruste.getId()).getTila().equals(PerusteTila.VALMIS));
        assertTrue(em.find(TekstiKappale.class, tekstikappale.getId()).getTila().equals(PerusteTila.VALMIS));
        assertTrue(em.find(TutkinnonOsa.class, osa.getId()).getTila().equals(PerusteTila.VALMIS));
    }
    
    @Test
    @Rollback(true)
    public void testUpdateTilaJulkaistuToValmis() {
        projekti.setTila(ProjektiTila.JULKAISTU);
        peruste.setTila(PerusteTila.VALMIS);
        em.persist(peruste);
        tekstikappale.setTila(PerusteTila.VALMIS);
        em.persist(tekstikappale);
        osa.setTila(PerusteTila.VALMIS);
        em.persist(osa);
        em.flush();
        repo.save(projekti);
        
        TilaUpdateStatus status = service.updateTila(projekti.getId(), ProjektiTila.VALMIS);
        assertFalse(status.isVaihtoOk());
        assertNotNull(status.getInfot());
        assertTrue(repo.getOne(projekti.getId()).getTila().equals(ProjektiTila.JULKAISTU));
        assertTrue(em.find(Peruste.class, peruste.getId()).getTila().equals(PerusteTila.VALMIS));
        assertTrue(em.find(TekstiKappale.class, tekstikappale.getId()).getTila().equals(PerusteTila.VALMIS));
        assertTrue(em.find(TutkinnonOsa.class, osa.getId()).getTila().equals(PerusteTila.VALMIS));
    }
      
    
    private RakenneModuuli luoEpaValidiRakenne() {
        RakenneModuuli rakenne = TestUtils.teeRyhma(
            100, 240, -1, -1,
            TestUtils.teeRakenneOsa(1, 10),
            TestUtils.teeRyhma(
                10, 90, -1, -1,
                TestUtils.teeRakenneOsa(1, 10),
                TestUtils.teeRakenneOsa(2, 20)    
            )
        );
        
        return rakenne;
    }
    
       
    private RakenneModuuli luoValidiRakenne() {
        RakenneModuuli juuri = new RakenneModuuli();
        em.persist(juuri);
        
        juuri.getOsat().add(luoRakenneOsa());
        juuri.getOsat().add(luoRakenneOsa());
        
        RakenneModuuli ryhmaA = new RakenneModuuli();
        em.persist(ryhmaA);
        juuri.getOsat().add(ryhmaA);
        
        ryhmaA.getOsat().add(luoRakenneOsa());    
        
        return juuri;
    }
    
    private TutkinnonOsaViite luoTutkinnonOsaViite() {
        TutkinnonOsaViite osaViite = new TutkinnonOsaViite();
        TutkinnonOsa tutkinnonosa = new TutkinnonOsa();
        tutkinnonosa.setTila(PerusteTila.LUONNOS);
        tutkinnonosa.setKoodiArvo("123456");
        Map<Kieli,String> tekstiMap = new HashMap<>();
        tekstiMap.put(Kieli.FI, "Teksti");
        tutkinnonosa.setNimi(TekstiPalanen.of(tekstiMap));
        em.persist(tutkinnonosa);
        osaViite.setSuoritustapa(naytto);
        osaViite.setTutkinnonOsa(tutkinnonosa);
        osaViite.setPoistettu(Boolean.FALSE);
        em.persist(osaViite);
        
        return osaViite;
    }
    
    private RakenneOsa luoRakenneOsa() {
        RakenneOsa rakenneOsa = TestUtils.teeRakenneOsa(0, 0);
        
        osa = new TutkinnonOsa();
        osa.setTila(PerusteTila.LUONNOS);
        osa.setKoodiArvo("12345");
        Map<Kieli,String> tekstiMap = new HashMap<>();
        tekstiMap.put(Kieli.FI, "Teksti");
        osa.setNimi(TekstiPalanen.of(tekstiMap));
        em.persist(osa);        
        TutkinnonOsaViite osaViite = new TutkinnonOsaViite();
        osaViite.setSuoritustapa(naytto);
        osaViite.setTutkinnonOsa(osa);
        osaViite.setPoistettu(Boolean.FALSE);
        em.persist(osaViite);
        naytto.getTutkinnonOsat().add(osaViite);
        rakenneOsa.setTutkinnonOsaViite(osaViite);
        em.persist(rakenneOsa);
        
        return rakenneOsa;
    }

}

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
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaTyoryhmaDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiInfoDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.TyoryhmaHenkiloDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author nkala
 */

@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PerusteprojektiServiceIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private PerusteprojektiRepository repository;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteenOsaService perusteenOsaService;

    @Autowired
    private PerusteprojektiService service;

    @PersistenceContext
    private EntityManager em;

    private final String ryhmaId = "1.2.246.562.28.11287634288";
    private final LaajuusYksikko yksikko = LaajuusYksikko.OSAAMISPISTE;
    private final String yhteistyotaho = TestUtils.uniikkiString();
    private final String tehtava = TestUtils.uniikkiString();

    private PerusteprojektiDto teePerusteprojekti(PerusteTyyppi tyyppi, String koulutustyyppi) {
        PerusteprojektiLuontiDto ppldto = null;
        if (tyyppi == PerusteTyyppi.NORMAALI) {
            ppldto = new PerusteprojektiLuontiDto(koulutustyyppi, yksikko, null, null, tyyppi, ryhmaId);
            ppldto.setDiaarinumero(TestUtils.uniikkiString());
            ppldto.setYhteistyotaho(yhteistyotaho);
            ppldto.setTehtava(tehtava);
        } else if (tyyppi == PerusteTyyppi.POHJA) {
            ppldto = new PerusteprojektiLuontiDto(koulutustyyppi, null, null, null, tyyppi, ryhmaId);
            ppldto.setYhteistyotaho(yhteistyotaho);
            ppldto.setTehtava(tehtava);
        }

        Assert.assertNotNull(ppldto);

        ppldto.setNimi(TestUtils.uniikkiString());
        return service.save(ppldto);
    }

    private void perusteprojektiLuontiCommonAsserts(PerusteprojektiDto ppdto, Perusteprojekti pp) {
        Assert.assertNotNull(pp);
        Assert.assertEquals(pp.getNimi(), ppdto.getNimi());
        Assert.assertEquals(pp.getDiaarinumero(), ppdto.getDiaarinumero());
        Assert.assertEquals(ryhmaId, pp.getRyhmaOid());
        Assert.assertEquals(pp.getTila(), ProjektiTila.LAADINTA);
        Assert.assertEquals(pp.getYhteistyotaho(), yhteistyotaho);
        Assert.assertEquals(pp.getTehtava(), tehtava);
        Assert.assertNotNull(pp.getPeruste());
        Assert.assertNotNull(pp.getPeruste().getSuoritustavat());
    }

    private void perusteprojektiLuontiCommonSuoritustavat(Perusteprojekti pp, long expectedSize) {
        for (Suoritustapa st : pp.getPeruste().getSuoritustavat()) {
            Assert.assertNotNull(st.getSisalto());
            Assert.assertNotNull(st.getSisalto().getLapset());
            Assert.assertEquals(expectedSize, st.getSisalto().getLapset().size());
        }
    }

    @Test
    @Rollback(true)
    public void testPerustprojektiluonti12() {
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.NORMAALI, KoulutusTyyppi.ERIKOISAMMATTITUTKINTO.toString());
        Perusteprojekti pp = repository.findOne(ppdto.getId());
        perusteprojektiLuontiCommonAsserts(ppdto, pp);
        Assert.assertEquals(KoulutusTyyppi.ERIKOISAMMATTITUTKINTO.toString(), pp.getPeruste().getKoulutustyyppi());
        Assert.assertEquals(1, pp.getPeruste().getSuoritustavat().size());
        perusteService.addSisalto(pp.getPeruste().getId(), Suoritustapakoodi.NAYTTO, null);
        perusteService.addSisalto(pp.getPeruste().getId(), Suoritustapakoodi.NAYTTO, null);
        perusteprojektiLuontiCommonSuoritustavat(pp, 3);
    }

    @Test
    @Rollback(true)
    public void testPerustprojektiluonti9999() {
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.NORMAALI, KoulutusTyyppi.PERUSOPETUS.toString());
        Perusteprojekti pp = repository.findOne(ppdto.getId());
        perusteprojektiLuontiCommonAsserts(ppdto, pp);
        Assert.assertEquals(KoulutusTyyppi.PERUSOPETUS.toString(), pp.getPeruste().getKoulutustyyppi());
        Assert.assertEquals(0, pp.getPeruste().getSuoritustavat().size());
        Assert.assertNotNull(pp.getPeruste().getPerusopetuksenPerusteenSisalto());
    }

    @Test
    @Rollback(true)
    public void testPerustpohjaluonti() {
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.POHJA, KoulutusTyyppi.PERUSTUTKINTO.toString());
        Perusteprojekti pp = repository.findOne(ppdto.getId());
        perusteprojektiLuontiCommonAsserts(ppdto, pp);
        Assert.assertEquals(KoulutusTyyppi.PERUSTUTKINTO.toString(), pp.getPeruste().getKoulutustyyppi());
        Assert.assertEquals(2, pp.getPeruste().getSuoritustavat().size());
        perusteService.addSisalto(pp.getPeruste().getId(), Suoritustapakoodi.OPS, null);
        perusteService.addSisalto(pp.getPeruste().getId(), Suoritustapakoodi.NAYTTO, null);
        perusteprojektiLuontiCommonSuoritustavat(pp, 2);
    }

    @Test
    @Rollback(true)
    public void testPerusteprojektiLuontiPohjasta() {
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.POHJA, KoulutusTyyppi.PERUSTUTKINTO.toString());
        Perusteprojekti pp = repository.findOne(ppdto.getId());
        perusteService.addSisalto(pp.getPeruste().getId(), Suoritustapakoodi.NAYTTO, null);
        perusteService.addSisalto(pp.getPeruste().getId(), Suoritustapakoodi.NAYTTO, null);
        perusteService.addSisalto(pp.getPeruste().getId(), Suoritustapakoodi.OPS, null);
        perusteService.addSisalto(pp.getPeruste().getId(), Suoritustapakoodi.OPS, null);
        perusteprojektiLuontiCommonSuoritustavat(pp, 3);

        PerusteprojektiLuontiDto luontiDto = new PerusteprojektiLuontiDto(KoulutusTyyppi.ERIKOISAMMATTITUTKINTO.toString(), LaajuusYksikko.OPINTOVIIKKO, null, null, PerusteTyyppi.NORMAALI, ryhmaId);
        luontiDto.setPerusteId(pp.getPeruste().getId());
        luontiDto.setDiaarinumero(TestUtils.uniikkiString());
        luontiDto.setNimi(TestUtils.uniikkiString());
        PerusteprojektiDto uusiDto = service.save(luontiDto);

        repository.flush();
        Perusteprojekti uusi = repository.findOne(uusiDto.getId());
        Assert.assertEquals(LaajuusYksikko.OPINTOVIIKKO, uusi.getPeruste().getSuoritustapa(Suoritustapakoodi.OPS).getLaajuusYksikko());
        Assert.assertEquals(2, uusi.getPeruste().getSuoritustavat().size());
        perusteprojektiLuontiCommonSuoritustavat(uusi, 3);
    }

    private void lazyAssertTyoryhma(TyoryhmaHenkiloDto trh, String ryhma, String henkilo) {
        Assert.assertEquals(trh.getKayttajaOid(), henkilo);
        Assert.assertEquals(trh.getNimi(), ryhma);
        Assert.assertNotNull(trh.getId());
    }

    @Test
    @Rollback(true)
    public void testTyoryhmat() {
        // Lisääminen
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.NORMAALI, KoulutusTyyppi.ERIKOISAMMATTITUTKINTO.toString());
        Perusteprojekti pp = repository.findOne(ppdto.getId());

        String ryhmaA = TestUtils.uniikkiString();
        String ryhmaB = TestUtils.uniikkiString();
        String ryhmaC = TestUtils.uniikkiString();

        String henkiloA = TestUtils.uniikkiString();
        String henkiloB = TestUtils.uniikkiString();
        String henkiloC = TestUtils.uniikkiString();
        String henkiloD = TestUtils.uniikkiString();
        String henkiloE = TestUtils.uniikkiString();

        TyoryhmaHenkiloDto trh = service.saveTyoryhma(pp.getId(), new TyoryhmaHenkiloDto(ryhmaA, henkiloA));
        lazyAssertTyoryhma(trh, ryhmaA, henkiloA);
        trh = service.saveTyoryhma(pp.getId(), new TyoryhmaHenkiloDto(ryhmaB, henkiloB));
        lazyAssertTyoryhma(trh, ryhmaB, henkiloB);
        trh = service.saveTyoryhma(pp.getId(), new TyoryhmaHenkiloDto(ryhmaA, henkiloC));
        lazyAssertTyoryhma(trh, ryhmaA, henkiloC);

        List<String> henkilot = new ArrayList<>();
        henkilot.add(henkiloA);
        henkilot.add(henkiloB);
        henkilot.add(henkiloC);
        henkilot.add(henkiloD);
        henkilot.add(henkiloE);

        List<TyoryhmaHenkiloDto> tyoryhma = service.saveTyoryhma(pp.getId(), ryhmaC, henkilot);
        Assert.assertEquals(5, tyoryhma.size());

        // Hakeminen
        tyoryhma = service.getTyoryhmaHenkilot(pp.getId(), ryhmaA);
        Assert.assertEquals(2, tyoryhma.size());
        tyoryhma = service.getTyoryhmaHenkilot(pp.getId(), ryhmaB);
        Assert.assertEquals(1, tyoryhma.size());
        tyoryhma = service.getTyoryhmaHenkilot(pp.getId(), ryhmaC);
        Assert.assertEquals(5, tyoryhma.size());
        tyoryhma = service.getTyoryhmaHenkilot(pp.getId());
        Assert.assertEquals(8, tyoryhma.size());

        // Poistaminen
        service.removeTyoryhma(pp.getId(), ryhmaC);
        tyoryhma = service.getTyoryhmaHenkilot(pp.getId(), ryhmaC);
        Assert.assertEquals(0, tyoryhma.size());
        tyoryhma = service.getTyoryhmaHenkilot(pp.getId(), ryhmaB);
        Assert.assertEquals(1, tyoryhma.size());
    }

    @Test
    @Rollback(true)
    public void testPerusteprojektiTyoryhmat() {
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.NORMAALI, KoulutusTyyppi.ERIKOISAMMATTITUTKINTO.toString());
        Perusteprojekti pp = repository.findOne(ppdto.getId());

        List<String> henkilot = new ArrayList<>();
        henkilot.add("a");
        service.saveTyoryhma(pp.getId(), "a", henkilot);
        service.saveTyoryhma(pp.getId(), "b", henkilot);
        service.saveTyoryhma(pp.getId(), "c", henkilot);

        List<String> nimet = new ArrayList<>();
        nimet.add("a");
        nimet.add("b");
        nimet.add("c");

        PerusteenOsaViiteDto.Matala poA = perusteService.addSisalto(pp.getPeruste().getId(), Suoritustapakoodi.NAYTTO, null);
        PerusteenOsaViiteDto.Matala poB = perusteService.addSisalto(pp.getPeruste().getId(), Suoritustapakoodi.NAYTTO, null);

        service.setPerusteenOsaViiteTyoryhmat(pp.getId(), poA.getId(), nimet);
        service.setPerusteenOsaViiteTyoryhmat(pp.getId(), poB.getId(), nimet);
        List<PerusteenOsaTyoryhmaDto> st = service.getSisallonTyoryhmat(pp.getId());
        Assert.assertEquals(3, st.size());
    }

    @Test
    @Rollback(true)
    public void testPerusteenOsaViiteTyoryhmat() {
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.NORMAALI, KoulutusTyyppi.PERUSTUTKINTO.toString());
        Perusteprojekti pp = repository.findOne(ppdto.getId());
        Suoritustapa st = pp.getPeruste().getSuoritustapa(Suoritustapakoodi.OPS);
        Long perusteenOsaId = st.getSisalto().getLapset().get(0).getPerusteenOsa().getId();

        String ryhma = TestUtils.uniikkiString();
        String henkilo = TestUtils.uniikkiString();
        service.saveTyoryhma(pp.getId(), new TyoryhmaHenkiloDto(ryhma, henkilo));

        // Lisäys
        List<String> ryhmat = new ArrayList<>();
        ryhmat.add(ryhma);
        List<String> tagit = service.setPerusteenOsaViiteTyoryhmat(pp.getId(), perusteenOsaId, ryhmat);
        Assert.assertEquals(1, tagit.size());

        // Haku
        tagit = service.getPerusteenOsaViiteTyoryhmat(pp.getId(), perusteenOsaId);
        Assert.assertEquals(1, tagit.size());
    }

    @Test
    @Rollback(true)
    public void testPerusteprojektiKopiointiToisestaProjektista() {
    }

    @Test
    @Rollback(true)
    public void testPerusteprojektiSisaltaaMuodostumiset() {
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.NORMAALI, KoulutusTyyppi.PERUSTUTKINTO.toString());
        repository.flush();
        Perusteprojekti pp = repository.findOne(ppdto.getId());
        Set<Suoritustapa> suoritustavat = pp.getPeruste().getSuoritustavat();

        Assert.assertEquals(2, suoritustavat.size());

        for (Suoritustapa suoritustapa : suoritustavat) {
            PerusteenOsaViite sisalto = suoritustapa.getSisalto();
            Assert.assertNotNull(sisalto);
            List<PerusteenOsaViite> lapset = sisalto.getLapset();
            Assert.assertEquals(1, lapset.size());
        }
    }

    @Test
    @Rollback(true)
    public void testMisc() {
        PerusteprojektiDto tpp = teePerusteprojekti(PerusteTyyppi.NORMAALI, KoulutusTyyppi.PERUSTUTKINTO.toString());
        teePerusteprojekti(PerusteTyyppi.NORMAALI, KoulutusTyyppi.ERIKOISAMMATTITUTKINTO.toString());
        teePerusteprojekti(PerusteTyyppi.POHJA, KoulutusTyyppi.AMMATTITUTKINTO.toString());

        List<PerusteprojektiInfoDto> info = service.getBasicInfo();
        Assert.assertEquals(3, info.size());

        info = service.getOmatProjektit();
        Assert.assertEquals(3, info.size());

        PerusteprojektiDto ppdto = service.get(tpp.getId());
        Assert.assertNotNull(ppdto);
    }

    @Test(expected = BusinessRuleViolationException.class)
    @Rollback(true)
    public void testOnkoDiaarinumeroKaytossa() {
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.NORMAALI, KoulutusTyyppi.ERIKOISAMMATTITUTKINTO.toString());
        service.onkoDiaarinumeroKaytossa(ppdto.getDiaarinumero());
    }

    @Test
    @Rollback(true)
    public void testUpdate() {
        PerusteprojektiDto vanhaDto = teePerusteprojekti(PerusteTyyppi.NORMAALI, KoulutusTyyppi.PERUSTUTKINTO.toString());
        PerusteprojektiDto update = new PerusteprojektiDto(
                "uusinimi",
                vanhaDto.getPeruste(),
                "uusidiaari",
                null,
                null,
                null,
                "uusitehtavaluokka",
                "uusitehtava",
                "uusiyhteistyotaho",
                vanhaDto.getTila(),
                "uusioid"
        );
        PerusteprojektiDto updated = service.update(vanhaDto.getId(), update);
        Assert.assertEquals("uusinimi", updated.getNimi());
        Assert.assertEquals(vanhaDto.getPeruste(), updated.getPeruste());
        Assert.assertEquals("uusidiaari", updated.getDiaarinumero());
        Assert.assertEquals(null, updated.getPaatosPvm());
        Assert.assertEquals(null, updated.getToimikausiAlku());
        Assert.assertEquals(null, updated.getToimikausiLoppu());
        Assert.assertEquals("uusitehtavaluokka", updated.getTehtavaluokka());
        Assert.assertEquals("uusitehtava", updated.getTehtava());
        Assert.assertEquals("uusiyhteistyotaho", updated.getYhteistyotaho());
        Assert.assertEquals(vanhaDto.getTila(), updated.getTila());
        Assert.assertEquals("uusioid", updated.getRyhmaOid());
    }

    @Test
    @Rollback(true)
    public void testPerustepohjaTilaJaNimi() {
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.POHJA, KoulutusTyyppi.PERUSTUTKINTO.toString());

        Perusteprojekti pp = repository.findOne(ppdto.getId());
        pp.getPeruste().setNimi(null);
        repository.save(pp);
        em.persist(pp);

        TilaUpdateStatus status = service.updateTila(ppdto.getId(), ProjektiTila.VALMIS);
        Assert.assertFalse(status.isVaihtoOk());

        pp.getPeruste().setNimi(TekstiPalanen.of(Kieli.FI, "nimi"));
        repository.save(pp);
        em.persist(pp);
        status = service.updateTila(ppdto.getId(), ProjektiTila.VALMIS);
        Assert.assertTrue(status.isVaihtoOk());
        Assert.assertEquals(ProjektiTila.VALMIS, pp.getTila());

        status = service.updateTila(ppdto.getId(), ProjektiTila.LAADINTA);
        Assert.assertFalse(status.isVaihtoOk());
    }
}

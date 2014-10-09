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

import fi.vm.sade.eperusteet.domain.Koulutus;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.Suoritustapa;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author nkala
 */
public class PerusteprojektiServiceIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private PerusteprojektiRepository repository;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteprojektiService service;

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
        }

        Assert.assertNotNull(ppldto);

        ppldto.setNimi(TestUtils.uniikkiString());
        return service.save(ppldto);
    }

    @Test
    @Transactional
    public void testPerustprojektiluonti() {
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.NORMAALI, "koulutustyyppi_12");
        Perusteprojekti pp = repository.findOne(ppdto.getId());
        Assert.assertNotNull(pp);
        Assert.assertEquals(pp.getNimi(), ppdto.getNimi());
        Assert.assertEquals(pp.getDiaarinumero(), ppdto.getDiaarinumero());
        Assert.assertEquals(ryhmaId, pp.getRyhmaOid());
        Assert.assertEquals(pp.getTila(), ProjektiTila.LAADINTA);
        Assert.assertEquals(pp.getYhteistyotaho(), yhteistyotaho);
        Assert.assertEquals(pp.getTehtava(), tehtava);
    }

    @Test
    @Transactional
    public void testPerustpohjaluonti() {
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.POHJA, "koulutustyyppi_12");
        Perusteprojekti pp = repository.findOne(ppdto.getId());
        Assert.assertNotNull(pp);
        Assert.assertEquals(pp.getNimi(), ppdto.getNimi());
        Assert.assertNull(pp.getDiaarinumero());
        Assert.assertEquals(ryhmaId, pp.getRyhmaOid());
        Assert.assertEquals(pp.getTila(), ProjektiTila.LAADINTA);
        Assert.assertEquals(pp.getYhteistyotaho(), null);
        Assert.assertEquals(pp.getTehtava(), null);
    }

    public void lazyAssertTyoryhma(TyoryhmaHenkiloDto trh, String ryhma, String henkilo) {
        Assert.assertEquals(trh.getKayttajaOid(), henkilo);
        Assert.assertEquals(trh.getNimi(), ryhma);
        Assert.assertNotNull(trh.getId());
    }

    @Test
    @Transactional
    public void testTyoryhmat() {
        // Lisääminen
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.NORMAALI, "koulutustyyppi_12");
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
    @Transactional
    public void testPerusteenOsaViiteTyoryhmat() {
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.NORMAALI, "koulutustyyppi_1");
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
    @Transactional
    public void testPerusteprojektiKopiointiToisestaProjektista() {
    }

    @Test
    @Transactional
    public void testPerusteprojektiKopiointiPerustepohjasta() {
        String ldiaarinumero = TestUtils.uniikkiString();
        String lnimi = TestUtils.uniikkiString();

        PerusteprojektiDto pohja = teePerusteprojekti(PerusteTyyppi.POHJA, "koulutustyyppi_1");
        pohja.setTehtava(TestUtils.uniikkiString());
        pohja.setTehtavaluokka(TestUtils.uniikkiString());
        pohja.setYhteistyotaho(TestUtils.uniikkiString());
        Peruste pohjaperuste = perusteRepository.findOne(Long.parseLong(pohja.getPeruste().getId()));
        Set<Koulutus> koulutukset = new HashSet<>();
        koulutukset.add(new Koulutus(null, TestUtils.uniikkiString(), TestUtils.uniikkiString(), TestUtils.uniikkiString(), TestUtils.uniikkiString()));
        pohjaperuste.setKoulutukset(koulutukset);

        PerusteprojektiLuontiDto ppldto = new PerusteprojektiLuontiDto(null, yksikko, Long.parseLong(pohja.getPeruste().getId()), null, PerusteTyyppi.NORMAALI, ryhmaId);
        ppldto.setDiaarinumero(ldiaarinumero);
        ppldto.setNimi(lnimi);

        // Projektin tiedot
        PerusteprojektiDto pp = service.save(ppldto);
        Assert.assertNotNull(pp);
        Assert.assertEquals(ProjektiTila.LAADINTA, pp.getTila());
        Assert.assertEquals(ldiaarinumero, pp.getDiaarinumero());
        Assert.assertEquals(lnimi, pp.getNimi());
        Assert.assertEquals(pohja.getRyhmaOid(), pp.getRyhmaOid());
        Assert.assertEquals(null, pp.getTehtava());
        Assert.assertEquals(null, pp.getTehtavaluokka());
        Assert.assertEquals(null, pp.getYhteistyotaho());

        Peruste uusiperuste = perusteRepository.findOne(Long.parseLong(pp.getPeruste().getId()));

        // Perusteen tiedot
        Assert.assertNotNull(pohjaperuste);
        Assert.assertNotNull(uusiperuste);
        Assert.assertNotEquals(pohjaperuste.getId(), uusiperuste.getId());
        Assert.assertEquals(pohjaperuste.getKoulutustyyppi(), uusiperuste.getKoulutustyyppi());
        Assert.assertNotNull(pohjaperuste.getKoulutukset());
        Assert.assertNotNull(uusiperuste.getKoulutukset());
        Assert.assertEquals(pohjaperuste.getKoulutukset().size(), uusiperuste.getKoulutukset().size());
        Assert.assertEquals(PerusteTila.LUONNOS, uusiperuste.getTila());
        Assert.assertEquals(PerusteTyyppi.NORMAALI, uusiperuste.getTyyppi());

        // Perusteen osat
        Set<Suoritustapa> pohjast = pohjaperuste.getSuoritustavat();
        Set<Suoritustapa> uusist = uusiperuste.getSuoritustavat();
        Assert.assertEquals(pohjast.size(), uusist.size());

        Map<Suoritustapakoodi, Suoritustapa> pohjastMap = new HashMap<>();
        for (Suoritustapa st : pohjast) {
            pohjastMap.put(st.getSuoritustapakoodi(), st);
        }
        for (Suoritustapa ust : uusist) {
            Suoritustapa pst = pohjastMap.get(ust.getSuoritustapakoodi());
            Assert.assertNotNull(pst);
            Assert.assertNotEquals(pst.getId(), ust.getId());
            Assert.assertEquals(pst.getLaajuusYksikko(), ust.getLaajuusYksikko());
            Assert.assertEquals(pst.getTutkinnonOsat().size(), ust.getTutkinnonOsat().size());
            Assert.assertNotEquals(pst.getSisalto().getId(), ust.getSisalto().getId());
            Assert.assertNotEquals(pst.getRakenne().getId(), ust.getRakenne().getId());
        }
    }

    @Test
    @Transactional
    public void testPerusteprojektiSisaltaaMuodostumiset() {
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.NORMAALI, "koulutustyyppi_1");
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
    @Transactional
    public void testMisc() {
        PerusteprojektiDto tpp = teePerusteprojekti(PerusteTyyppi.NORMAALI, "koulutustyyppi_1");
        teePerusteprojekti(PerusteTyyppi.NORMAALI, "koulutustyyppi_12");
        teePerusteprojekti(PerusteTyyppi.POHJA, "koulutustyyppi_11");

        List<PerusteprojektiInfoDto> info = service.getBasicInfo();
        Assert.assertEquals(3, info.size());

        info = service.getOmatProjektit();
        Assert.assertEquals(3, info.size());

        PerusteprojektiDto ppdto = service.get(tpp.getId());
        Assert.assertNotNull(ppdto);
    }

    @Test(expected = BusinessRuleViolationException.class)
    @Transactional
    public void testOnkoDiaarinumeroKaytossa() {
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.NORMAALI, "koulutustyyppi_12");
        service.onkoDiaarinumeroKaytossa(ppdto.getDiaarinumero());
    }

    @Test
    @Transactional
    public void testUpdate() {
        PerusteprojektiDto vanhaDto = teePerusteprojekti(PerusteTyyppi.NORMAALI, "koulutustyyppi_1");
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
}

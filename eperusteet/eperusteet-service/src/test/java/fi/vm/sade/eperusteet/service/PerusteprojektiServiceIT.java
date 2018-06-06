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

import com.google.common.base.Function;
import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.MuodostumisSaanto;
import fi.vm.sade.eperusteet.dto.TilaUpdateStatus;
import fi.vm.sade.eperusteet.dto.peruste.*;
import fi.vm.sade.eperusteet.dto.perusteprojekti.*;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.Assert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    @Autowired
    private PerusteprojektiTestUtils ppTestUtils;

    @Autowired
    @Dto
    private DtoMapper mapper;

    @PersistenceContext
    private EntityManager em;

    private final String ryhmaId = "1.2.246.562.28.11287634288";
    private final LaajuusYksikko yksikko = LaajuusYksikko.OSAAMISPISTE;
    private final String yhteistyotaho = TestUtils.uniikkiString();
    private final String tehtava = TestUtils.uniikkiString();

    private PerusteprojektiDto teePerusteprojekti(PerusteTyyppi tyyppi, String koulutustyyppi) {
        return service.save(teePerusteprojektiLuontiDto(tyyppi, koulutustyyppi));
    }

    private PerusteprojektiLuontiDto teePerusteprojektiLuontiDto(PerusteTyyppi tyyppi, String koulutustyyppi) {
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
        return ppldto;
    }

    private void perusteprojektiLuontiCommonAsserts(PerusteprojektiDto ppdto, Perusteprojekti pp) {
        Assert.assertNotNull(pp);
        Assert.assertEquals(pp.getNimi(), ppdto.getNimi());
        if (pp.getPeruste().getTyyppi().equals(PerusteTyyppi.NORMAALI)) {
            Assert.assertEquals(pp.getDiaarinumero(), new Diaarinumero(ppdto.getDiaarinumero()));
        }
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

    @Test(expected = BusinessRuleViolationException.class)
    @Rollback(true)
    public void testPerusteprojektiLuontiVirheellinenTyyppi() {
        PerusteprojektiLuontiDto luontiDto = new PerusteprojektiLuontiDto(KoulutusTyyppi.ERIKOISAMMATTITUTKINTO.toString(), LaajuusYksikko.OPINTOVIIKKO, null, null, PerusteTyyppi.OPAS, ryhmaId);
        service.save(luontiDto);
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

    @Test
    @Rollback(true)
    public void testKvLiiteLuonti() {
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.POHJA, KoulutusTyyppi.PERUSTUTKINTO.toString());
        PerusteprojektiDto projekti = service.get(ppdto.getId());
        repository.flush();
        perusteRepository.flush();
        PerusteDto peruste = perusteService.get(projekti.getPeruste().getIdLong());
        Assert.assertNotNull(peruste.getKvliite());
        KVLiiteJulkinenDto julkinenKVLiite = perusteService.getJulkinenKVLiite(peruste.getId());
        Assert.assertNotNull(julkinenKVLiite);
    }

    void setKvliiteValues(KVLiiteDto kvliite, String prefix) {
        kvliite.setJatkoopintoKelpoisuus(TestUtils.lt(prefix + "1"));
        kvliite.setKansainvalisetSopimukset(TestUtils.lt(prefix + "2"));
        kvliite.setLisatietoja(TestUtils.lt(prefix + "3"));
        kvliite.setPohjakoulutusvaatimukset(TestUtils.lt(prefix + "4"));
        kvliite.setSaadosPerusta(TestUtils.lt(prefix + "5"));
        kvliite.setSuorittaneenOsaaminen(TestUtils.lt(prefix + "6"));
        kvliite.setTutkintotodistuksenSaaminen(TestUtils.lt(prefix + "8"));
        kvliite.setTyotehtavatJoissaVoiToimia(TestUtils.lt(prefix + "9"));
        kvliite.setTutkinnostaPaattavaViranomainen(TestUtils.lt(prefix + "10"));
        kvliite.setTutkintotodistuksenAntaja(TestUtils.lt(prefix + "11"));
    }

    KVLiiteDto testKvliite(KVLiiteDto kvliite, String prefix) {
        Assert.assertEquals(kvliite.getJatkoopintoKelpoisuus().get(Kieli.FI), prefix + "1");
        Assert.assertEquals(kvliite.getKansainvalisetSopimukset().get(Kieli.FI), prefix + "2");
        Assert.assertEquals(kvliite.getLisatietoja().get(Kieli.FI), prefix + "3");
        Assert.assertEquals(kvliite.getPohjakoulutusvaatimukset().get(Kieli.FI), prefix + "4");
        Assert.assertEquals(kvliite.getSaadosPerusta().get(Kieli.FI), prefix + "5");
        Assert.assertEquals(kvliite.getSuorittaneenOsaaminen().get(Kieli.FI), prefix + "6");
        Assert.assertEquals(kvliite.getTutkintotodistuksenSaaminen().get(Kieli.FI), prefix + "8");
        Assert.assertEquals(kvliite.getTyotehtavatJoissaVoiToimia().get(Kieli.FI), prefix + "9");
        Assert.assertEquals(kvliite.getTutkinnostaPaattavaViranomainen().get(Kieli.FI), prefix + "10");
        Assert.assertEquals(kvliite.getTutkintotodistuksenAntaja().get(Kieli.FI), prefix + "11");
        return kvliite;
    }

    @Test
    @Rollback(true)
    public void testKvLiiteMuokkaus() {
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.POHJA, KoulutusTyyppi.PERUSTUTKINTO.toString());
        PerusteprojektiDto projekti = service.get(ppdto.getId());
        PerusteDto peruste = perusteService.get(projekti.getPeruste().getIdLong());
        Assert.assertNotNull(peruste.getKvliite());
        KVLiiteDto kvliite = peruste.getKvliite();
        setKvliiteValues(kvliite, "a");
        peruste.setKvliite(kvliite);
        peruste = perusteService.updateFull(peruste.getId(), peruste);
        kvliite = peruste.getKvliite();
        testKvliite(kvliite, "a");
    }

    @Test
    @Rollback(true)
    public void testKvLiitePohjasta() {
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.POHJA, KoulutusTyyppi.PERUSTUTKINTO.toString());
        PerusteDto pohjaDto = perusteService.get(service.get(ppdto.getId()).getPeruste().getIdLong());
        KVLiiteDto pohjaLiite = pohjaDto.getKvliite();
        setKvliiteValues(pohjaLiite, "a");
        pohjaDto.setKvliite(pohjaLiite);
        pohjaDto = perusteService.updateFull(pohjaDto.getId(), pohjaDto);

        PerusteprojektiLuontiDto luontiDto = new PerusteprojektiLuontiDto(KoulutusTyyppi.ERIKOISAMMATTITUTKINTO.toString(), LaajuusYksikko.OPINTOVIIKKO, null, null, PerusteTyyppi.NORMAALI, ryhmaId);
        luontiDto.setPerusteId(pohjaDto.getId());
        luontiDto.setDiaarinumero(TestUtils.uniikkiString());
        luontiDto.setNimi(TestUtils.uniikkiString());
        PerusteprojektiDto pp = service.save(luontiDto);
        PerusteDto perusteDto = perusteService.get(pp.getPeruste().getIdLong());
        KVLiiteDto kvliite = perusteDto.getKvliite();
        setKvliiteValues(kvliite, "b");
        perusteDto.setKvliite(kvliite);
        perusteDto = perusteService.updateFull(perusteDto.getId(), perusteDto);
        testKvliite(perusteDto.getKvliite(), "b");

        KVLiiteJulkinenDto julkinenKVLiite = perusteService.getJulkinenKVLiite(perusteDto.getId());
        Assert.assertEquals("a1", julkinenKVLiite.getJatkoopintoKelpoisuus().get(Kieli.FI));
        Assert.assertEquals("a2", julkinenKVLiite.getKansainvalisetSopimukset().get(Kieli.FI));
        Assert.assertEquals("a3", julkinenKVLiite.getLisatietoja().get(Kieli.FI));
        Assert.assertEquals("a4", julkinenKVLiite.getPohjakoulutusvaatimukset().get(Kieli.FI));
        Assert.assertEquals("a5", julkinenKVLiite.getSaadosPerusta().get(Kieli.FI));
        Assert.assertEquals("b6", julkinenKVLiite.getSuorittaneenOsaaminen().get(Kieli.FI));
        Assert.assertEquals("a8", julkinenKVLiite.getTutkintotodistuksenSaaminen().get(Kieli.FI));
        Assert.assertEquals("b9", julkinenKVLiite.getTyotehtavatJoissaVoiToimia().get(Kieli.FI));
        Assert.assertEquals("a10", julkinenKVLiite.getTutkinnostaPaattavaViranomainen().get(Kieli.FI));
        Assert.assertEquals("a11", julkinenKVLiite.getTutkintotodistuksenAntaja().get(Kieli.FI));

        Assert.assertEquals("b6", perusteDto.getSuorittaneenOsaaminen().get(Kieli.FI));
        Assert.assertEquals("b9", perusteDto.getTyotehtavatJoissaVoiToimia().get(Kieli.FI));
    }

    private void lazyAssertTyoryhma(TyoryhmaHenkiloDto trh, String ryhma, String henkilo) {
        Assert.assertEquals(trh.getKayttajaOid(), henkilo);
        Assert.assertEquals(trh.getNimi(), ryhma);
        Assert.assertNotNull(trh.getId());
    }

    @Test
    @Rollback(true)
    public void testPerusteidenHakeminen() {
        PerusteprojektiDto normaaliDto = teePerusteprojekti(PerusteTyyppi.NORMAALI, KoulutusTyyppi.PERUSTUTKINTO.toString());
        Perusteprojekti normaali = mapper.map(normaaliDto, Perusteprojekti.class);
        normaali.getPeruste().setKoulutusvienti(true);
        normaali.getPeruste().setDiaarinumero(new Diaarinumero("1234"));
        normaali.getPeruste().setVoimassaoloAlkaa(new Date());

        PerusteprojektiDto esikatseltava = teePerusteprojekti(PerusteTyyppi.NORMAALI, KoulutusTyyppi.PERUSTUTKINTO.toString());
        PerusteprojektiDto cDto = teePerusteprojekti(PerusteTyyppi.NORMAALI, KoulutusTyyppi.PERUSTUTKINTO.toString());
        service.updateTila(normaali.getId(), ProjektiTila.JULKAISTU, null);
        em.flush();

        List<Peruste> perusteet = perusteRepository.findAll();

        {   // Haku
            // Vain valmiita perusteita voi hakea tämän rajapinnan avulla
            PerusteQuery pquery = new PerusteQuery();
            pquery.setTila(PerusteTila.VALMIS.toString());
            // Oletuksena älä palauta pohjia
            if (pquery.getPerusteTyyppi() == null) {
                pquery.setPerusteTyyppi(PerusteTyyppi.NORMAALI.toString());
            }
            PageRequest p = new PageRequest(pquery.getSivu(), Math.min(pquery.getSivukoko(), 100));
            Page<PerusteHakuDto> haku = perusteService.findJulkinenBy(p, pquery);
        }
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

        long perusteId = pp.getPeruste().getId();
        PerusteVersionDto versionDto = perusteService.getPerusteVersion(perusteId);
        PerusteenOsaViiteDto.Matala poA = perusteService.addSisalto(pp.getPeruste().getId(), Suoritustapakoodi.NAYTTO, null);
        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());
        PerusteenOsaViiteDto.Matala poB = perusteService.addSisalto(pp.getPeruste().getId(), Suoritustapakoodi.NAYTTO, null);
        versionDto = perusteService.getPerusteVersion(perusteId);

        service.setPerusteenOsaViiteTyoryhmat(pp.getId(), poA.getId(), nimet);
        service.setPerusteenOsaViiteTyoryhmat(pp.getId(), poB.getId(), nimet);
        List<PerusteenOsaTyoryhmaDto> st = service.getSisallonTyoryhmat(pp.getId());
        assertEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());
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

        List<PerusteprojektiListausDto> infoOmat = service.getOmatProjektit();
        Assert.assertEquals(3, infoOmat.size());

        PerusteprojektiDto ppdto = service.get(tpp.getId());
        Assert.assertNotNull(ppdto);
    }

    @Rollback(true)
    public void testOnkoDiaarinumeroKaytossa() {
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.NORMAALI, KoulutusTyyppi.ERIKOISAMMATTITUTKINTO.toString());
        DiaarinumeroHakuDto diaariHaku = service.onkoDiaarinumeroKaytossa(new Diaarinumero(ppdto.getDiaarinumero()));
        Assert.assertTrue(diaariHaku.getLoytyi());
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
        repository.save(pp);
        em.persist(pp);

        TilaUpdateStatus status = service.updateTila(ppdto.getId(), ProjektiTila.VALMIS, null);
        assertThat(status.isVaihtoOk()).isFalse();


        HashSet<Kieli> kielet = new HashSet<>();
        kielet.add(Kieli.FI);
        pp.getPeruste().setKielet(kielet);
        pp.getPeruste().setNimi(TekstiPalanen.of(Kieli.FI, "nimi"));

        ppTestUtils.asetaMuodostumiset(pp.getPeruste().getId());

        ppTestUtils.luoValidiKVLiite(pp.getPeruste().getId());

        repository.save(pp);
        em.persist(pp);

        status = service.updateTila(ppdto.getId(), ProjektiTila.VALMIS, null);
        Assert.assertTrue(status.isVaihtoOk());
        Assert.assertEquals(ProjektiTila.VALMIS, pp.getTila());

        status = service.updateTila(ppdto.getId(), ProjektiTila.LAADINTA, null);
        Assert.assertFalse(status.isVaihtoOk());
    }

    @Test
    @Rollback(true)
    public void testDiaarinumerohaku() {
        PerusteprojektiLuontiDto adto = teePerusteprojektiLuontiDto(PerusteTyyppi.NORMAALI, KoulutusTyyppi.PERUSTUTKINTO.toString());
        PerusteprojektiLuontiDto bdto = teePerusteprojektiLuontiDto(PerusteTyyppi.NORMAALI, KoulutusTyyppi.PERUSTUTKINTO.toString());
        final long now = (new Date()).getTime();

        Function<PerusteprojektiLuontiDto, Perusteprojekti> luontiHelper = (PerusteprojektiLuontiDto luontiDto) -> {
            luontiDto.setReforminMukainen(true);
            PerusteprojektiDto projektiDto = service.save(luontiDto);
            Perusteprojekti pp = repository.findOne(projektiDto.getId());
            Peruste peruste = pp.getPeruste();
            peruste.setDiaarinumero(new Diaarinumero("OPH-12345-1234"));
            pp.getPeruste().setKielet(Stream.of(Kieli.FI).collect(Collectors.toSet()));
            pp.getPeruste().setNimi(TekstiPalanen.of(Kieli.FI, "nimi"));
            pp.getPeruste().setVoimassaoloLoppuu(new Date(now + 100000));
            pp.getPeruste().getSuoritustavat().forEach(st -> st.getRakenne()
                    .setMuodostumisSaanto(new MuodostumisSaanto(new MuodostumisSaanto
                            .Laajuus(0, 180, LaajuusYksikko.OSAAMISPISTE), null)));
            return pp;
        };

        Consumer<Perusteprojekti> julkaise = (Perusteprojekti p) -> {
            TilaUpdateStatus problems = service.updateTila(p.getId(), ProjektiTila.VIIMEISTELY, null);
            Assert.assertTrue(problems.isVaihtoOk());
            service.updateTila(p.getId(), ProjektiTila.VALMIS, null);
            Assert.assertTrue(problems.isVaihtoOk());
            problems = service.updateTila(p.getId(), ProjektiTila.JULKAISTU, TestUtils.createTiedote());
            Assert.assertTrue(problems.isVaihtoOk());
        };

        Perusteprojekti a = luontiHelper.apply(adto);
        a.getPeruste().setVoimassaoloAlkaa(new Date(now - 100));
        ppTestUtils.luoValidiKVLiite(a.getPeruste().getId());
        Perusteprojekti b = luontiHelper.apply(bdto);
        b.getPeruste().setVoimassaoloAlkaa(new Date(now - 200));
        ppTestUtils.luoValidiKVLiite(b.getPeruste().getId());
        repository.save(a);
        em.persist(a);
        repository.save(b);
        em.persist(b);
        julkaise.accept(a);
        julkaise.accept(b);

        PerusteInfoDto diaari = perusteService.getByDiaari(new Diaarinumero("OPH-12345-1234"));
        Assert.assertNotNull(diaari);
        Assert.assertEquals(diaari.getId(), a.getPeruste().getId());
    }
}

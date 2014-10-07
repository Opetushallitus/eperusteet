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

import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.TyoryhmaHenkiloDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

    private final String nimi = TestUtils.uniikkiString();
    private final String diaarinumero = TestUtils.uniikkiString();
    private final String ryhmaId = "1.2.246.562.28.11287634288";
    private final LaajuusYksikko yksikko = LaajuusYksikko.OSAAMISPISTE;
    private final String koulutustyyppi = null;
    private final String yhteistyotaho = TestUtils.uniikkiString();
    private final String tehtava = TestUtils.uniikkiString();

    private PerusteprojektiDto teePerusteprojekti(PerusteTyyppi tyyppi) {
        PerusteprojektiLuontiDto ppldto = null;
        if (tyyppi == PerusteTyyppi.NORMAALI) {
            ppldto = new PerusteprojektiLuontiDto(koulutustyyppi, yksikko, null, null, tyyppi, ryhmaId);
            ppldto.setDiaarinumero(diaarinumero);
            ppldto.setYhteistyotaho(yhteistyotaho);
            ppldto.setTehtava(tehtava);
        } else if (tyyppi == PerusteTyyppi.POHJA) {
            ppldto = new PerusteprojektiLuontiDto(null, null, null, null, tyyppi, ryhmaId);
        }

        Assert.assertNotNull(ppldto);

        ppldto.setNimi(nimi);
        return service.save(ppldto);
    }

    @Test
    public void testPerustprojektiluonti() {
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.NORMAALI);
        Perusteprojekti pp = repository.findOne(ppdto.getId());
        Assert.assertNotNull(pp);
        Assert.assertEquals(pp.getNimi(), nimi);
        Assert.assertEquals(pp.getDiaarinumero(), diaarinumero);
        Assert.assertEquals(ryhmaId, pp.getRyhmaOid());
        Assert.assertEquals(pp.getTila(), ProjektiTila.LAADINTA);
        Assert.assertEquals(pp.getYhteistyotaho(), yhteistyotaho);
        Assert.assertEquals(pp.getTehtava(), tehtava);
    }

    @Test
    public void testPerustpohjaluonti() {
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.POHJA);
        Perusteprojekti pp = repository.findOne(ppdto.getId());
        Assert.assertNotNull(pp);
        Assert.assertEquals(pp.getNimi(), nimi);
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
    public void testTyoryhmat() {
        // Lisääminen
        PerusteprojektiDto ppdto = teePerusteprojekti(PerusteTyyppi.NORMAALI);
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
    public void testPerusteprojektiKopiointi() {

    }

    @Test
    public void testPerusteprojektiSisaltaaMuodostumiset() {

    }
}

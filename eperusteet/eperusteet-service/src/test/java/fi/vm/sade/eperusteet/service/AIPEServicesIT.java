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
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.domain.yl.LaajaalainenOsaaminen;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEKurssiDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEKurssiSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEOppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEOppiaineSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEVaiheDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEVaiheSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import fi.vm.sade.eperusteet.service.yl.AIPEOpetuksenPerusteenSisaltoService;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author nkala
 */
@Transactional
@DirtiesContext
public class AIPEServicesIT extends AbstractIntegrationTest {
    @Autowired
    private AIPEOpetuksenPerusteenSisaltoService sisalto;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteRepository perusteRepository;

    private Peruste peruste;
    private Long perusteId;

    @Before
    public void setup() {
        peruste = perusteService.luoPerusteRunko(KoulutusTyyppi.AIKUISTENPERUSOPETUS, LaajuusYksikko.KURSSI, PerusteTyyppi.NORMAALI);
        AIPEVaiheDto vaihe = sisalto.addVaihe(peruste.getId(), TestUtils.createVaihe());
        LaajaalainenOsaaminenDto l1 = TestUtils.createLaajaalainen();
        LaajaalainenOsaaminenDto l2 = TestUtils.createLaajaalainen();
        LaajaalainenOsaaminenDto l3 = TestUtils.createLaajaalainen();
        sisalto.addLaajaalainen(peruste.getId(), l1);
        sisalto.addLaajaalainen(peruste.getId(), l2);
        sisalto.addLaajaalainen(peruste.getId(), l3);
        AIPEOppiaineDto oa1 = TestUtils.createAIPEOppiaine();
        AIPEOppiaineDto oa2 = TestUtils.createAIPEOppiaine();
        AIPEOppiaineDto oa3 = TestUtils.createAIPEOppiaine();
        sisalto.addOppiaine(peruste.getId(), vaihe.getId(), oa1);
        sisalto.addOppiaine(peruste.getId(), vaihe.getId(), oa2);
        sisalto.addOppiaine(peruste.getId(), vaihe.getId(), oa3);
        perusteRepository.flush();
        perusteId = peruste.getId();

        List<AIPEOppiaineSuppeaDto> oppiaineet = sisalto.getOppiaineet(perusteId, vaihe.getId());
        AIPEKurssiDto kurssi1 = TestUtils.createAIPEKurssi();
        AIPEKurssiDto kurssi2 = TestUtils.createAIPEKurssi();
        AIPEKurssiDto kurssi3 = TestUtils.createAIPEKurssi();
        AIPEKurssiDto kurssi = sisalto.addKurssi(perusteId, vaihe.getId(), oppiaineet.get(2).getId(), kurssi3);

        List<AIPEVaiheSuppeaDto> vaiheet = sisalto.getVaiheet(perusteId);
        sisalto.addKurssi(perusteId, vaiheet.get(0).getId(), oppiaineet.get(0).getId(), kurssi1);
        sisalto.addKurssi(perusteId, vaiheet.get(0).getId(), oppiaineet.get(0).getId(), kurssi2);

        {   // Oppimäärät
            AIPEOppiaineDto oppimaaraDto = TestUtils.createAIPEOppiaine();
            oppimaaraDto = sisalto.addOppimaara(perusteId, vaihe.getId(), oppiaineet.get(1).getId(), oppimaaraDto);
            assertNotNull(oppimaaraDto);
            List<AIPEOppiaineSuppeaDto> oppimaarat = sisalto.getOppimaarat(perusteId, vaihe.getId(), oppiaineet.get(1).getId());
            assertEquals(oppimaarat.size(), 1);

            AIPEKurssiDto omKurssi = TestUtils.createAIPEKurssi();
            sisalto.addKurssi(perusteId, vaihe.getId(), oppimaaraDto.getId(), omKurssi);
//            List<AIPEKurssiSuppeaDto> omKurssit = sisalto.getKurssit(perusteId, vaihe.getId(), oppimaaraDto.getId());
//            assertEquals(omKurssit.size(), 1);
        }

        perusteRepository.flush();

        assertNotNull(kurssi);
        assertNotNull(peruste);
    }

    @Test
    public void testPerusteenLuonti() {
        PerusteenOsaViiteDto.Laaja perusteenSisalto = perusteService.getSuoritustapaSisalto(peruste.getId(), Suoritustapakoodi.AIPE);
        assertEquals(peruste.getKoulutustyyppi(), KoulutusTyyppi.AIKUISTENPERUSOPETUS.toString());
        assertNotNull(perusteenSisalto);
    }

    @Test
    public void testVaiheidenLuonti() {
        List<AIPEVaiheSuppeaDto> vaiheet = sisalto.getVaiheet(perusteId);
        assertTrue(vaiheet.size() == 1);
    }

    @Test
    public void testLaajaalaistenLuonti() {
        Set<LaajaalainenOsaaminen> laajaalaiset = peruste.getAipeOpetuksenPerusteenSisalto().getLaajaalaisetosaamiset();
        assertTrue(laajaalaiset.size() == 3);
    }

    @Test
    public void testOppiaineidenLuonti() {
        List<AIPEVaiheSuppeaDto> vaiheet = sisalto.getVaiheet(perusteId);
        List<AIPEOppiaineSuppeaDto> oppiaineet = sisalto.getOppiaineet(peruste.getId(), vaiheet.get(0).getId());
        assertTrue(oppiaineet.size() == 3);
    }

    @Test
    public void testKurssienLuonti() {
        List<AIPEVaiheSuppeaDto> vaiheet = sisalto.getVaiheet(perusteId);
        List<AIPEOppiaineSuppeaDto> oppiaineet = sisalto.getOppiaineet(peruste.getId(), vaiheet.get(0).getId());
        List<AIPEKurssiSuppeaDto> oa1kurssit = sisalto.getKurssit(perusteId, vaiheet.get(0).getId(), oppiaineet.get(0).getId());
        assertTrue(oa1kurssit.size() == 2);
        List<AIPEKurssiSuppeaDto> oa2kurssit = sisalto.getKurssit(perusteId, vaiheet.get(0).getId(), oppiaineet.get(2).getId());
        assertTrue(oa2kurssit.size() == 1);
    }

    @Test
    public void testSisallonPaivitys() {
        List<AIPEVaiheSuppeaDto> vaiheet = sisalto.getVaiheet(perusteId);
        List<AIPEOppiaineSuppeaDto> oppiaineet = sisalto.getOppiaineet(peruste.getId(), vaiheet.get(0).getId());

        Long vaiheId = vaiheet.get(0).getId();
        Long oppiaineId1 = oppiaineet.get(0).getId();
        Long oppiaineId2 = oppiaineet.get(1).getId();
        Long oppiaineId3 = oppiaineet.get(2).getId();

        List<AIPEKurssiSuppeaDto> kurssit1 = sisalto.getKurssit(perusteId, vaiheId, oppiaineId1);
        Long kurssiId = kurssit1.get(0).getId();
        AIPEKurssiDto kurssi = sisalto.getKurssi(perusteId, vaiheId, oppiaineId1, kurssiId);
        assertEquals(kurssi.getId(), kurssiId);

        assertTrue(kurssi.getNimi().isPresent());
        assertTrue(kurssi.getKuvaus().isPresent());

        { // Kurssin päivittäminen
            AIPEKurssiDto muuttunutKurssi = new AIPEKurssiDto();
            muuttunutKurssi.setKuvaus(TestUtils.olt("uusi kuvaus"));
            String vanhaNimi = kurssi.getNimi().get().get(Kieli.FI);
            muuttunutKurssi = sisalto.updateKurssi(perusteId, vaiheId, oppiaineId1, kurssiId, muuttunutKurssi);
            assertEquals(kurssi.getId(), muuttunutKurssi.getId());
            assertEquals(muuttunutKurssi.getKuvaus().get().get(Kieli.FI), "uusi kuvaus");
            assertEquals(muuttunutKurssi.getNimi().get().get(Kieli.FI), vanhaNimi);
        }

        { // Kurssin poisto
            sisalto.removeKurssi(perusteId, vaiheId, oppiaineId1, kurssi.getId());
            try {
                AIPEKurssiDto poistettuKurssi = sisalto.getKurssi(perusteId, vaiheId, oppiaineId1, kurssi.getId());
                fail("Kurssi ei pitäisi olla olemassa");
            }
            catch (BusinessRuleViolationException ex) {
            }
            finally {
            }
        }
    }

    @Test
    public void testTavoitteet() {

    }
}

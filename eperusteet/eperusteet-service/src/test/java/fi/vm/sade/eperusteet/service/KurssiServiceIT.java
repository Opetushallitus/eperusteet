/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.*;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokurssiTyyppi;
import fi.vm.sade.eperusteet.dto.yl.JarjestettyOppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.LukioKurssiLuontiDto;
import fi.vm.sade.eperusteet.dto.yl.LukiokurssiListausDto;
import fi.vm.sade.eperusteet.dto.yl.LukiokurssiMuokkausDto;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.util.OppiaineUtil.Reference;
import fi.vm.sade.eperusteet.service.yl.KurssiService;
import fi.vm.sade.eperusteet.service.yl.OppiaineService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static fi.vm.sade.eperusteet.service.util.OppiaineUtil.oppiaine;
import static fi.vm.sade.eperusteet.service.util.PerusteTekstiUtil.*;
import static fi.vm.sade.eperusteet.service.yl.OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * User: tommiratamaa
 * Date: 29.9.15
 * Time: 17.01
 */
@DirtiesContext
public class KurssiServiceIT extends AbstractIntegrationTest {

    @Autowired
    private KurssiService kurssiService;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private OppiaineService oppiaineService;

    private Long perusteId;
    private Reference<Long> suomiRef = new Reference<>();
    private Reference<Long> saameRef = new Reference<>();

    @Before
    public void setup() {
        Peruste peruste = perusteService.luoPerusteRunko(KoulutusTyyppi.LUKIOKOULUTUS, LaajuusYksikko.OPINTOVIIKKO, PerusteTyyppi.NORMAALI);
        perusteId = peruste.getId();
        oppiaine(teksti(
                fi("Äidinkieli ja kirjallisuus"),
                sv("Finska")
        )).maara(oppiaine(teksti(fi("Suomi"))).as(suomiRef))
            .maara(oppiaine(teksti(fi("Saame"))).as(saameRef))
        .luo(oppiaineService, perusteId, LUKIOKOULUTUS);
    }


    @Test
    public void testLukiokurssi() {
        long id = kurssiService.luoLukiokurssi(perusteId, LukioKurssiLuontiDto.builder()
                .tyyppi(LukiokurssiTyyppi.PAKOLLINEN)
                .oppiaineet(asList(
                    new JarjestettyOppiaineDto(suomiRef.getId(), 1)
                ))
                .nimi(teksti(fi("Äidinkielen perusteet"), sv("Finska ett")))
                .koodiArvo("AI1")
                .build());
        
        LukiokurssiMuokkausDto muokkausDto = kurssiService.getLukiokurssiMuokkausById(perusteId, id);
        assertNotNull(muokkausDto);
        assertEquals("AI1", muokkausDto.getKoodiArvo());
        assertEquals("Äidinkielen perusteet", muokkausDto.getNimi().get(Kieli.FI));
        assertEquals(1, muokkausDto.getOppiaineet().size());
        assertEquals(new Integer(1), muokkausDto.getOppiaineet().get(0).getJarjestys());
        assertEquals(suomiRef.getId(), muokkausDto.getOppiaineet().get(0).getOppiaineId());
        
        muokkausDto.getOppiaineet().add(new JarjestettyOppiaineDto(saameRef.getId(), 1));
        kurssiService.muokkaaLukiokurssia(perusteId, muokkausDto);
        List<LukiokurssiListausDto> list = kurssiService.findLukiokurssitByPerusteId(perusteId);
        assertEquals(1, list.size());
        assertEquals("Äidinkielen perusteet", list.get(0).getNimi());
        assertEquals("AI1", list.get(0).getKoodiArvo());
        assertEquals(2, list.get(0).getOppiaineet().size());
        assertTrue(list.get(0).getOppiaineet().stream().map(JarjestettyOppiaineDto::getOppiaineId)
                .collect(toSet()).containsAll(asList(suomiRef.getId(), saameRef.getId())));
    }
}

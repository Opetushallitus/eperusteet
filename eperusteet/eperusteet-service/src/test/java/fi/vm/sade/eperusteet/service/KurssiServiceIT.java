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
import fi.vm.sade.eperusteet.dto.yl.*;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.util.OppiaineUtil.Reference;
import fi.vm.sade.eperusteet.service.yl.KurssiLockContext;
import fi.vm.sade.eperusteet.service.yl.KurssiService;
import fi.vm.sade.eperusteet.service.yl.LukioRakenneLockContext;
import fi.vm.sade.eperusteet.service.yl.OppiaineService;
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

    @Autowired
    @LockCtx(KurssiLockContext.class)
    private LockService<KurssiLockContext> lukioKurssiLockService;

    @Autowired
    @LockCtx(LukioRakenneLockContext.class)
    private LockService<LukioRakenneLockContext> lukioRakenneLockService;

    @Dto
    @Autowired
    private DtoMapper dtoMapper;

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
                    new KurssinOppiaineDto(suomiRef.getId(), 1)
                ))
                .nimi(teksti(fi("Äidinkielen perusteet"), sv("Finska ett")))
                .koodiArvo("AI1")
                .build());
        
        LukiokurssiTarkasteleDto dto = kurssiService.getLukiokurssiTarkasteleDtoById(perusteId, id);
        assertNotNull(dto);
        assertEquals("AI1", dto.getKoodiArvo());
        assertEquals("Äidinkielen perusteet", dto.getNimi().get(Kieli.FI));
        assertEquals(1, dto.getOppiaineet().size());
        assertEquals(new Integer(1), dto.getOppiaineet().get(0).getJarjestys());
        assertEquals(suomiRef.getId(), dto.getOppiaineet().get(0).getOppiaineId());

        LukiokurssiMuokkausDto muokkausDto = dtoMapper.map(dto, new LukiokurssiMuokkausDto());
        muokkausDto.setKoodiArvo("ARVO");
        lukioKurssiLockService.lock(new KurssiLockContext(perusteId, dto.getId()));
        kurssiService.muokkaaLukiokurssia(perusteId, muokkausDto);

        LukiokurssiOppaineMuokkausDto liitosDto = new LukiokurssiOppaineMuokkausDto();
        liitosDto.setId(dto.getId());
        liitosDto.getOppiaineet().addAll(dto.getOppiaineet());
        liitosDto.getOppiaineet().add(new KurssinOppiaineDto(saameRef.getId(), 1));
        lukioRakenneLockService.lock(new LukioRakenneLockContext(perusteId));
        kurssiService.muokkaaLukiokurssinOppiaineliitoksia(perusteId, liitosDto);
        List<LukiokurssiListausDto> list = kurssiService.findLukiokurssitByPerusteId(perusteId);
        assertEquals(1, list.size());
        assertEquals("Äidinkielen perusteet", list.get(0).getNimi().get(Kieli.FI));
        assertEquals("ARVO", list.get(0).getKoodiArvo());
        assertEquals(2, list.get(0).getOppiaineet().size());
        assertTrue(list.get(0).getOppiaineet().stream().map(KurssinOppiaineDto::getOppiaineId)
                .collect(toSet()).containsAll(asList(suomiRef.getId(), saameRef.getId())));
    }
}

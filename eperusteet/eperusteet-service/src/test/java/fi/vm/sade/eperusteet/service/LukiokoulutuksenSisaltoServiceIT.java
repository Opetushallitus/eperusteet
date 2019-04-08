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

import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.dto.peruste.PerusteVersionDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto.Laaja;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto.Matala;
import fi.vm.sade.eperusteet.dto.yl.OppiaineDto;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.yl.LukiokoulutuksenPerusteenSisaltoService;
import fi.vm.sade.eperusteet.service.yl.OppiaineService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static fi.vm.sade.eperusteet.domain.Kieli.FI;
import static fi.vm.sade.eperusteet.domain.Kieli.SV;
import static fi.vm.sade.eperusteet.service.util.OppiaineUtil.oppiaine;
import static fi.vm.sade.eperusteet.service.util.PerusteTekstiUtil.*;
import static fi.vm.sade.eperusteet.service.yl.OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;


/**
 * User: tommiratamaa
 * Date: 21.9.15
 * Time: 16.15
 */
@DirtiesContext
public class LukiokoulutuksenSisaltoServiceIT extends AbstractIntegrationTest {

    @Autowired
    private LukiokoulutuksenPerusteenSisaltoService lukiokoulutuksenPerusteenSisaltoService;

    @Autowired
    private OppiaineService oppiaineService;

    @Autowired
    private PerusteService perusteService;

    private Long perusteId;
    private Long paaOsaViiteId;

    @Before
    public void setup() {
        Peruste peruste = perusteService.luoPerusteRunko(KoulutusTyyppi.LUKIOKOULUTUS, null,
                LaajuusYksikko.OPINTOVIIKKO, PerusteTyyppi.NORMAALI);
        perusteId = peruste.getId();
        paaOsaViiteId = peruste.getLukiokoulutuksenPerusteenSisalto().getSisalto().getId();
    }

    @Test
    public void testAddSisalto() {
        PerusteVersionDto versionDto = perusteService.getPerusteVersion(perusteId);
        Matala osa = lukiokoulutuksenPerusteenSisaltoService.addSisalto(perusteId, paaOsaViiteId, perusteOsa(fi("Osa")));
        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());
        assertEquals(0, osa.getLapset().size());
        assertEquals("Osa", osa.getPerusteenOsa().getNimi().get(FI));
    }

    @Test
    public void testGetRootSisalto() {
        assertEquals(paaOsaViiteId, lukiokoulutuksenPerusteenSisaltoService.getSisalto(perusteId, null, PerusteenOsaViiteDto.class).getId());
    }

    @Test
    public void testGetSisalto() {
        Matala osa = lukiokoulutuksenPerusteenSisaltoService.addSisalto(perusteId, paaOsaViiteId, perusteOsa(fi("Osa")));
        PerusteenOsaViiteDto haettu = lukiokoulutuksenPerusteenSisaltoService.getSisalto(perusteId, osa.getId(), PerusteenOsaViiteDto.class);
        assertNotNull(haettu);
        assertEquals("Osa", haettu.getPerusteenOsa().getNimi().get(FI));
        assertEquals(4, lukiokoulutuksenPerusteenSisaltoService.getSisalto(perusteId, null, Laaja.class).getLapset().size());
    }

    @Test
    public void testRemoveSisalto() {
        Matala osa = lukiokoulutuksenPerusteenSisaltoService.addSisalto(perusteId, paaOsaViiteId, perusteOsa(fi("Osa")));
        PerusteVersionDto versionDto = perusteService.getPerusteVersion(perusteId);
        lukiokoulutuksenPerusteenSisaltoService.removeSisalto(perusteId, osa.getId());
        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());
        assertEquals(3, lukiokoulutuksenPerusteenSisaltoService.getSisalto(perusteId, null, Laaja.class).getLapset().size());
    }

    @Test
    public void testAddAndListOppiaineet() {
        PerusteVersionDto versionDto = perusteService.getPerusteVersion(perusteId);
        oppiaine(teksti(
                fi("Äidinkieli ja kirjallisuus"),
                sv("Finska")
        )).maara(oppiaine(teksti(fi("Suomi"))))
            .maara(oppiaine(teksti(fi("Saame"))))
            .luo(oppiaineService, perusteId, LUKIOKOULUTUS);
        assertNotEquals(perusteService.getPerusteVersion(perusteId).getAikaleima(), versionDto.getAikaleima());

        List<OppiaineDto> aineet = lukiokoulutuksenPerusteenSisaltoService.getOppiaineet(perusteId, OppiaineDto.class);
        assertEquals(1, aineet.size());
        assertEquals("Finska", aineet.get(0).getNimi().get().get(SV));
        assertEquals(2, aineet.get(0).getOppimaarat().size());
    }
}

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

import com.google.common.base.Optional;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.dto.util.EntityReference;
import fi.vm.sade.eperusteet.dto.yl.LaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuudenLaajaalainenOsaaminenDto;
import fi.vm.sade.eperusteet.dto.yl.VuosiluokkaKokonaisuusDto;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.yl.PerusopetuksenPerusteenSisaltoService;
import fi.vm.sade.eperusteet.service.yl.VuosiluokkaKokonaisuusContext;
import fi.vm.sade.eperusteet.service.yl.VuosiluokkakokonaisuusService;
import java.io.IOException;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static fi.vm.sade.eperusteet.service.test.util.TestUtils.olt;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author jhyoty
 */
@DirtiesContext
public class VuosiluokkaKokonaisuusServiceIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteService perusteService;
    @Autowired
    private PerusopetuksenPerusteenSisaltoService sisaltoService;
    @Autowired
    private VuosiluokkakokonaisuusService service;
    @Autowired
    @LockCtx(VuosiluokkaKokonaisuusContext.class)
    private LockService<VuosiluokkaKokonaisuusContext> lockService;


    private Long perusteId;
    private EntityReference osaaminen;

    @Before
    public void setup() {
        Peruste peruste = perusteService.luoPerusteRunko("koulutustyyppi_16", LaajuusYksikko.OPINTOVIIKKO, PerusteTila.LUONNOS, PerusteTyyppi.NORMAALI);
        perusteId = peruste.getId();
        LaajaalainenOsaaminenDto lo = new LaajaalainenOsaaminenDto();
        lo.setNimi(olt("Nimi"));
        lo = sisaltoService.addLaajaalainenOsaaminen(perusteId, lo);
        osaaminen = new EntityReference(lo.getId());
    }

    @Test
    public void testAddUpdate() throws IOException {

        VuosiluokkaKokonaisuusDto dto = new VuosiluokkaKokonaisuusDto();
        dto.setNimi(olt("Nimi"));
        VuosiluokkaKokonaisuudenLaajaalainenOsaaminenDto vlo = new VuosiluokkaKokonaisuudenLaajaalainenOsaaminenDto();
        vlo.setKuvaus(olt("KUVAAUS"));
        vlo.setLaajaalainenOsaaminen(Optional.of(osaaminen));
        dto.setLaajaalaisetOsaamiset(Collections.singleton(vlo));
        dto = service.addVuosiluokkaKokonaisuus(perusteId, dto);

        assertEquals(1, dto.getLaajaalaisetOsaamiset().size());
        assertEquals(osaaminen, dto.getLaajaalaisetOsaamiset().iterator().next().getLaajaalainenOsaaminen().get());
        dto.setNimi(olt("Nimi2"));
        dto.getLaajaalaisetOsaamiset().add(vlo);
        final VuosiluokkaKokonaisuusContext ctx = VuosiluokkaKokonaisuusContext.of(perusteId, dto.getId());

        lockService.lock(ctx);
        service.updateVuosiluokkaKokonaisuus(perusteId, dto);
        lockService.unlock(ctx);
        assertEquals(2, dto.getLaajaalaisetOsaamiset().size());
        service.deleteVuosiluokkaKokonaisuus(ctx.getPerusteId(), ctx.getKokonaisuusId());

    }

}

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
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.yl.OppiaineDto;
import fi.vm.sade.eperusteet.repository.OppiaineRepository;
import fi.vm.sade.eperusteet.repository.VuosiluokkaKokonaisuusRepository;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import java.io.IOException;
import java.util.Collections;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

/**
 *
 * @author jhyoty
 */
@DirtiesContext
public class OppiaineServiceIT extends AbstractIntegrationTest {

    @Autowired
    private OppiaineRepository repo;
    @Autowired
    private VuosiluokkaKokonaisuusRepository vkrepo;
    @Autowired
    private PerusopetuksenPerusteenSisaltoService service;

    @Test
    public void testAdd() throws IOException {
        OppiaineDto oppiaineDto = new OppiaineDto();
        oppiaineDto.setNimi(Optional.of(tp("Oppiaine")));
        service.addOppiaine(0L, oppiaineDto);
    }

    private static LokalisoituTekstiDto tp(String teksti) {
        return new LokalisoituTekstiDto(null, Collections.singletonMap(Kieli.FI, teksti));
    }
}

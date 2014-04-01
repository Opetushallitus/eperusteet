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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;

/**
 *
 * @author jhyoty
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AuditedEntityTestIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteenOsaRepository perusteenOsaRepository;

    @Test
    public void testAuditing() {
        final String user1 = "user1";
        final String user2 = "user2";

        setUpSecurityContext(user1);
        TekstiKappale teksti = new TekstiKappale();
        teksti.setNimi(new TekstiPalanen(Collections.singletonMap(Kieli.FI, "Nimi")));
        teksti.setTeksti(new TekstiPalanen(Collections.singletonMap(Kieli.FI, "Nimi")));
        teksti = perusteenOsaRepository.save(teksti);

        assertEquals(user1, teksti.getLuoja());
        assertEquals(user1, teksti.getMuokkaaja());
        final Date luotu = teksti.getLuotu();
        final Date muokattu = teksti.getMuokattu();

        // varmistetaan että aikaa ei voi muokata ulkopuolelta
        teksti.getLuotu().setTime(luotu.getTime() - 1);
        teksti.getMuokattu().setTime(muokattu.getTime() - 1);
        assertTrue(teksti.getLuotu().equals(luotu));
        assertTrue(teksti.getMuokattu().equals(luotu));

        setUpSecurityContext(user2);
        TekstiKappale teksti2 = new TekstiKappale();
        teksti2.setId(teksti.getId());
        teksti2.setNimi(new TekstiPalanen(Collections.singletonMap(Kieli.FI, "Nimi2")));
        teksti2.setTeksti(teksti.getTeksti());

        teksti2 = perusteenOsaRepository.save(teksti2);
        teksti2 = (TekstiKappale)perusteenOsaRepository.findOne(teksti.getId());

        assertEquals(user1, teksti2.getLuoja());
        assertEquals(user2, teksti2.getMuokkaaja());
        assertEquals(luotu, teksti2.getLuotu());
        assertTrue(teksti2.getMuokattu().after(luotu));
        assertTrue(teksti2.getMuokattu().after(luotu));
    }

    private void setUpSecurityContext(String username) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(username,"test"));
        SecurityContextHolder.setContext(ctx);
    }

}

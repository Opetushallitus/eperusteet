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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.Tila;
import fi.vm.sade.eperusteet.domain.TutkinnonOsa;
import fi.vm.sade.eperusteet.dto.ArvioinninKohdealueDto;
import fi.vm.sade.eperusteet.dto.ArviointiDto;
import fi.vm.sade.eperusteet.dto.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import java.util.ArrayList;

/**
 *
 * @author jhyoty
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AuditedEntityTestIT extends AbstractIntegrationTest {

	private static final Logger LOG = LoggerFactory.getLogger(AuditedEntityTestIT.class);

    @Autowired
    private PerusteenOsaRepository perusteenOsaRepository;
    @Autowired
    private PerusteenOsaService perusteenOsaService;

    @Test
    public void testAuditing() {
        final String user1 = "user1";
        final String user2 = "user2";

        setUpSecurityContext(user1);
        TekstiKappale teksti = new TekstiKappale();
        teksti.setNimi(TekstiPalanen.of(Collections.singletonMap(Kieli.FI, "Nimi")));
        teksti.setTeksti(TekstiPalanen.of(Collections.singletonMap(Kieli.FI, "Nimi")));
        teksti.setTila(Tila.LUONNOS);
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
        teksti2.setNimi(TekstiPalanen.of(Collections.singletonMap(Kieli.FI, "Nimi2")));
        teksti2.setTeksti(teksti.getTeksti());
        teksti2.setTila(teksti.getTila());

        teksti2 = perusteenOsaRepository.save(teksti2);
        teksti2 = (TekstiKappale)perusteenOsaRepository.findOne(teksti.getId());

        assertEquals(user1, teksti2.getLuoja());
        assertEquals(user2, teksti2.getMuokkaaja());
        assertEquals(luotu, teksti2.getLuotu());
        assertTrue(teksti2.getMuokattu().after(luotu));
        assertTrue(teksti2.getMuokattu().after(luotu));
    }

    @Test
    public void testAuditRevisions() {

        TekstiKappale teksti = new TekstiKappale();
        teksti.setNimi(TekstiPalanen.of(Collections.singletonMap(Kieli.FI, "Nimi")));
        teksti.setTeksti(TekstiPalanen.of(Collections.singletonMap(Kieli.FI, "Teksti")));
        teksti.setTila(Tila.LUONNOS);
        teksti = perusteenOsaRepository.save(teksti);

        teksti.getNimi().getTeksti().put(Kieli.FI, "nimi, muokattu");
        teksti = perusteenOsaRepository.save(teksti);

        List<Revision> revisions = perusteenOsaService.getRevisions(teksti.getId());

    	assertNotNull(revisions);
        assertEquals(1, revisions.size());

    }

    @Test
    public void testTutkinnonOsaRevisions() {
    	TutkinnonOsaDto tutkinnonOsaDto = new TutkinnonOsaDto();
    	tutkinnonOsaDto.setNimi(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Nimi")));
        tutkinnonOsaDto.setTila(Tila.LUONNOS);
    	tutkinnonOsaDto = perusteenOsaService.add(tutkinnonOsaDto, TutkinnonOsaDto.class, TutkinnonOsa.class);

        perusteenOsaService.lock(tutkinnonOsaDto.getId());

    	tutkinnonOsaDto.setArviointi(new ArviointiDto());
    	tutkinnonOsaDto.getArviointi().setLisatiedot(new LokalisoituTekstiDto(Collections.singletonMap("fi", "lisätiedot")));
        tutkinnonOsaDto.getArviointi().setArvioinninKohdealueet(new ArrayList<ArvioinninKohdealueDto>());
        ArvioinninKohdealueDto ke = new ArvioinninKohdealueDto();
        ke.setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "kohdealue")));
        tutkinnonOsaDto.getArviointi().getArvioinninKohdealueet().add(ke);
    	tutkinnonOsaDto = perusteenOsaService.update(tutkinnonOsaDto, TutkinnonOsaDto.class, TutkinnonOsa.class);

    	tutkinnonOsaDto.getArviointi().setLisatiedot(new LokalisoituTekstiDto(Collections.singletonMap("fi", "lisätiedot, muokattu")));
        tutkinnonOsaDto.getArviointi().getArvioinninKohdealueet().get(0).setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "kohdealue, muokattu")));
    	tutkinnonOsaDto = perusteenOsaService.update(tutkinnonOsaDto, TutkinnonOsaDto.class, TutkinnonOsa.class);

    	tutkinnonOsaDto.setAmmattitaitovaatimukset(new LokalisoituTekstiDto(Collections.singletonMap("fi", "Ammattitaitovaatimukset")));
    	tutkinnonOsaDto = perusteenOsaService.update(tutkinnonOsaDto, TutkinnonOsaDto.class, TutkinnonOsa.class);

    	List<Revision> tutkinnonOsaRevisions = perusteenOsaService.getRevisions(tutkinnonOsaDto.getId());

    	assertNotNull(tutkinnonOsaRevisions);
        assertEquals(4, tutkinnonOsaRevisions.size());

        tutkinnonOsaDto = (TutkinnonOsaDto) perusteenOsaService.getRevision(tutkinnonOsaDto.getId(), 3);
        assertNotNull(tutkinnonOsaDto);
        assertNotNull(tutkinnonOsaDto.getArviointi());
        assertNotNull(tutkinnonOsaDto.getArviointi().getLisatiedot());
        assertNotNull(tutkinnonOsaDto.getArviointi().getLisatiedot().getTekstit());
        assertEquals("lisätiedot, muokattu", tutkinnonOsaDto.getArviointi().getLisatiedot().getTekstit().get(Kieli.FI));
        LOG.debug(tutkinnonOsaDto.getArviointi().getLisatiedot().getTekstit().get(Kieli.FI));

        tutkinnonOsaDto = (TutkinnonOsaDto) perusteenOsaService.getRevision(tutkinnonOsaDto.getId(), 2);
        assertNotNull(tutkinnonOsaDto);
        assertNotNull(tutkinnonOsaDto.getArviointi());
        assertNotNull(tutkinnonOsaDto.getArviointi().getLisatiedot());
        assertNotNull(tutkinnonOsaDto.getArviointi().getLisatiedot().getTekstit());
        assertEquals("lisätiedot", tutkinnonOsaDto.getArviointi().getLisatiedot().getTekstit().get(Kieli.FI));
        LOG.debug(tutkinnonOsaDto.getArviointi().getLisatiedot().getTekstit().get(Kieli.FI));

        perusteenOsaService.unlock(tutkinnonOsaDto.getId());

    }

    private void setUpSecurityContext(String username) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(username,"test"));
        SecurityContextHolder.setContext(ctx);
    }
}

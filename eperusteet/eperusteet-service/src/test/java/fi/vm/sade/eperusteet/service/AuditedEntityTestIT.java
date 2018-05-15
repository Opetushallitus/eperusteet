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
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.ammattitaitovaatimukset.AmmattitaitovaatimuksenKohdealue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsaTyyppi;
import fi.vm.sade.eperusteet.dto.ammattitaitovaatimukset.AmmattitaitovaatimusDto;
import fi.vm.sade.eperusteet.dto.ammattitaitovaatimukset.AmmattitaitovaatimusKohdeDto;
import fi.vm.sade.eperusteet.dto.ammattitaitovaatimukset.AmmattitaitovaatimusKohdealueetDto;
import fi.vm.sade.eperusteet.dto.arviointi.ArvioinninKohdealueDto;
import fi.vm.sade.eperusteet.dto.arviointi.ArviointiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.TutkinnonOsaDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteenOsaRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import org.apache.xpath.SourceTree;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

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
        teksti2.asetaTila(teksti.getTila());

        teksti2 = perusteenOsaRepository.save(teksti2);
        teksti2 = (TekstiKappale)perusteenOsaRepository.findOne(teksti.getId());

        assertEquals(user1, teksti2.getLuoja());
        assertEquals(user2, teksti2.getMuokkaaja());
        assertEquals(luotu, teksti2.getLuotu());
        assertTrue(teksti2.getMuokattu().compareTo(luotu)>=0);
    }

    @Test
    public void testAuditRevisions() {

        TekstiKappale teksti = new TekstiKappale();
        teksti.setNimi(TekstiPalanen.of(Collections.singletonMap(Kieli.FI, "Nimi")));
        teksti.setTeksti(TekstiPalanen.of(Collections.singletonMap(Kieli.FI, "Teksti")));
        teksti = perusteenOsaRepository.save(teksti);

        teksti.getNimi().getTeksti().put(Kieli.FI, "nimi, muokattu");
        teksti = perusteenOsaRepository.save(teksti);

        List<Revision> revisions = perusteenOsaService.getVersiot(teksti.getId());

    	assertNotNull(revisions);
        assertEquals(1, revisions.size());

    }

    @Test
    public void testTutkinnonOsaRevisions() {
    	TutkinnonOsa tutkinnonOsa = new TutkinnonOsa();
    	tutkinnonOsa.setNimi(TekstiPalanen.of(Kieli.FI,"Nimi"));
        tutkinnonOsa.setTyyppi(TutkinnonOsaTyyppi.NORMAALI);
    	tutkinnonOsa = perusteenOsaRepository.save(tutkinnonOsa);

        TutkinnonOsaDto tutkinnonOsaDto = (TutkinnonOsaDto)perusteenOsaService.get(tutkinnonOsa.getId());
        perusteenOsaService.lock(tutkinnonOsa.getId());

    	tutkinnonOsaDto.setArviointi(new ArviointiDto());
    	tutkinnonOsaDto.getArviointi().setLisatiedot(new LokalisoituTekstiDto(Collections.singletonMap("fi", "lisätiedot")));
        tutkinnonOsaDto.getArviointi().setArvioinninKohdealueet(new ArrayList<ArvioinninKohdealueDto>());
        ArvioinninKohdealueDto ke = new ArvioinninKohdealueDto();
        ke.setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "kohdealue")));
        tutkinnonOsaDto.getArviointi().getArvioinninKohdealueet().add(ke);
    	tutkinnonOsaDto = perusteenOsaService.update(tutkinnonOsaDto);

    	tutkinnonOsaDto.getArviointi().setLisatiedot(new LokalisoituTekstiDto(Collections.singletonMap("fi", "lisätiedot, muokattu")));
        tutkinnonOsaDto.getArviointi().getArvioinninKohdealueet().get(0).setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "kohdealue, muokattu")));
    	tutkinnonOsaDto = perusteenOsaService.update(tutkinnonOsaDto);


        tutkinnonOsaDto.setAmmattitaitovaatimukset( new LokalisoituTekstiDto((Collections.singletonMap("fi", "TutkinnonOsanAmmattitaitovaatimukset"))) );

        List<AmmattitaitovaatimusKohdealueetDto> ammattitaitovaatimusLista = new ArrayList<>();
        AmmattitaitovaatimusKohdealueetDto ammattitaitovaatimusKohdealueetDto = new AmmattitaitovaatimusKohdealueetDto();
        ammattitaitovaatimusKohdealueetDto.setOtsikko( new LokalisoituTekstiDto((Collections.singletonMap("fi", "Ammattitaitovaatimuskohdealue"))));

        List<AmmattitaitovaatimusKohdeDto> ammattitaitovaatimusKohteet = new ArrayList<>();
        AmmattitaitovaatimusKohdeDto ammattitaitovaatimusKohdeDto = new AmmattitaitovaatimusKohdeDto();
        ammattitaitovaatimusKohdeDto.setOtsikko( new LokalisoituTekstiDto((Collections.singletonMap("fi", "Ammattitaitovaatimuskohde"))) );
        ammattitaitovaatimusKohdeDto.setSelite( new LokalisoituTekstiDto((Collections.singletonMap("fi", "Ammattitaitovaatimuskohde selite"))) );

        List<AmmattitaitovaatimusDto> ammattitaitovaatimukset = new ArrayList<>();
        AmmattitaitovaatimusDto ammattitaitovaatimusDto = new AmmattitaitovaatimusDto();
        ammattitaitovaatimusDto.setSelite( new LokalisoituTekstiDto((Collections.singletonMap("fi", "AmmattitaitovaatimusV2 selite"))) );
        ammattitaitovaatimusDto.setAmmattitaitovaatimusKoodi("vaatimuskoodi");
        ammattitaitovaatimusDto.setJarjestys(0);

        ammattitaitovaatimukset.add(ammattitaitovaatimusDto);
        ammattitaitovaatimusKohdeDto.setVaatimukset(ammattitaitovaatimukset);

        ammattitaitovaatimusKohteet.add(ammattitaitovaatimusKohdeDto);
        ammattitaitovaatimusKohdealueetDto.setVaatimuksenKohteet(ammattitaitovaatimusKohteet);

        ammattitaitovaatimusLista.add( ammattitaitovaatimusKohdealueetDto );
        tutkinnonOsaDto.setAmmattitaitovaatimuksetLista( ammattitaitovaatimusLista );

        tutkinnonOsaDto = perusteenOsaService.update(tutkinnonOsaDto);

    	List<Revision> tutkinnonOsaRevisions = perusteenOsaService.getVersiot(tutkinnonOsaDto.getId());

    	assertNotNull(tutkinnonOsaRevisions);
        assertEquals(4, tutkinnonOsaRevisions.size());
        assertEquals(1, tutkinnonOsaDto.getAmmattitaitovaatimuksetLista().size());

        AmmattitaitovaatimusKohdealueetDto ammattitaitovaatimuksenKohdealue = tutkinnonOsaDto.getAmmattitaitovaatimuksetLista().get(0);
        assertEquals("Ammattitaitovaatimuskohdealue", ammattitaitovaatimuksenKohdealue.getOtsikko().get(Kieli.FI));
        assertEquals(1, ammattitaitovaatimuksenKohdealue.getVaatimuksenKohteet().size());

        tutkinnonOsaDto = (TutkinnonOsaDto) perusteenOsaService.getVersio(tutkinnonOsaDto.getId(), 3);
        assertNotNull(tutkinnonOsaDto);
        assertNotNull(tutkinnonOsaDto.getArviointi());
        assertNotNull(tutkinnonOsaDto.getArviointi().getLisatiedot());
        assertNotNull(tutkinnonOsaDto.getArviointi().getLisatiedot().getTekstit());
        assertEquals("lisätiedot, muokattu", tutkinnonOsaDto.getArviointi().getLisatiedot().getTekstit().get(Kieli.FI));
        LOG.debug(tutkinnonOsaDto.getArviointi().getLisatiedot().getTekstit().get(Kieli.FI));

        tutkinnonOsaDto = (TutkinnonOsaDto) perusteenOsaService.getVersio(tutkinnonOsaDto.getId(), 2);
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

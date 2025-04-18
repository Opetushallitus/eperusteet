package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
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

        startNewTransaction();

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

        startNewTransaction();

        setUpSecurityContext(user2);
        TekstiKappale teksti2 = new TekstiKappale();
        teksti2.setId(teksti.getId());
        teksti2.setNimi(TekstiPalanen.of(Collections.singletonMap(Kieli.FI, "Nimi2")));
        teksti2.setTeksti(teksti.getTeksti());
        teksti2.asetaTila(teksti.getTila());

        teksti2 = perusteenOsaRepository.save(teksti2);

        startNewTransaction();
        teksti2 = (TekstiKappale)perusteenOsaRepository.findOne(teksti.getId());

        assertEquals(user1, teksti2.getLuoja());
        assertEquals(user2, teksti2.getMuokkaaja());
        assertEquals(luotu, teksti2.getLuotu());
        assertTrue(teksti2.getMuokattu().compareTo(luotu)>=0);

        endTransaction();
    }

    @Test
    public void testAuditRevisions() {

        startNewTransaction();
        TekstiKappale teksti = new TekstiKappale();
        teksti.setNimi(TekstiPalanen.of(Collections.singletonMap(Kieli.FI, "Nimi")));
        teksti.setTeksti(TekstiPalanen.of(Collections.singletonMap(Kieli.FI, "Teksti")));
        teksti = perusteenOsaRepository.saveAndFlush(teksti);

        startNewTransaction();
        teksti.setNimi(TekstiPalanen.of(Collections.singletonMap(Kieli.FI, "Nimi")));
        teksti = perusteenOsaRepository.saveAndFlush(teksti);

        startNewTransaction();
        List<Revision> revisions = perusteenOsaService.getVersiot(teksti.getId());

    	assertNotNull(revisions);
        assertEquals(2, revisions.size());

        endTransaction();
    }

    @Test
    public void testTutkinnonOsaRevisions() {

        startNewTransaction();

    	TutkinnonOsa tutkinnonOsa = new TutkinnonOsa();
    	tutkinnonOsa.setNimi(TekstiPalanen.of(Kieli.FI,"Nimi"));
        tutkinnonOsa.setTyyppi(TutkinnonOsaTyyppi.NORMAALI);
    	tutkinnonOsa = perusteenOsaRepository.save(tutkinnonOsa);

        startNewTransaction();

        TutkinnonOsaDto tutkinnonOsaDto = (TutkinnonOsaDto)perusteenOsaService.get(tutkinnonOsa.getId());

        startNewTransaction();
        perusteenOsaService.lock(tutkinnonOsa.getId());

        startNewTransaction();
    	tutkinnonOsaDto.setArviointi(new ArviointiDto());
    	tutkinnonOsaDto.getArviointi().setLisatiedot(new LokalisoituTekstiDto(Collections.singletonMap("fi", "lisätiedot")));
        tutkinnonOsaDto.getArviointi().setArvioinninKohdealueet(new ArrayList<ArvioinninKohdealueDto>());
        ArvioinninKohdealueDto ke = new ArvioinninKohdealueDto();
        ke.setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "kohdealue")));
        tutkinnonOsaDto.getArviointi().getArvioinninKohdealueet().add(ke);
    	tutkinnonOsaDto = perusteenOsaService.update(tutkinnonOsaDto);

        startNewTransaction();

    	tutkinnonOsaDto.getArviointi().setLisatiedot(new LokalisoituTekstiDto(Collections.singletonMap("fi", "lisätiedot, muokattu")));
        tutkinnonOsaDto.getArviointi().getArvioinninKohdealueet().get(0).setOtsikko(new LokalisoituTekstiDto(Collections.singletonMap("fi", "kohdealue, muokattu")));

    	tutkinnonOsaDto = perusteenOsaService.update(tutkinnonOsaDto);
        startNewTransaction();

        tutkinnonOsaDto.setAmmattitaitovaatimukset( new LokalisoituTekstiDto((Collections.singletonMap("fi", "Ammattitaitovaatimukset"))) );

        List<AmmattitaitovaatimusKohdealueetDto> ammattitaitovaatimusLista = new ArrayList<>();
        AmmattitaitovaatimusKohdealueetDto ammattitaitovaatimusKohdealueetDto = new AmmattitaitovaatimusKohdealueetDto();
        ammattitaitovaatimusKohdealueetDto.setOtsikko( new LokalisoituTekstiDto((Collections.singletonMap("fi", "Ammattitaitovaatimuskohdealue"))));

        List<AmmattitaitovaatimusKohdeDto> ammattitaitovaatimusKohteet = new ArrayList<>();
        AmmattitaitovaatimusKohdeDto ammattitaitovaatimusKohdeDto = new AmmattitaitovaatimusKohdeDto();
        ammattitaitovaatimusKohdeDto.setOtsikko( new LokalisoituTekstiDto((Collections.singletonMap("fi", "Ammattitaitovaatimuskohde"))) );
        ammattitaitovaatimusKohdeDto.setSelite( new LokalisoituTekstiDto((Collections.singletonMap("fi", "Ammattitaitovaatimuskohde selite"))) );

        List<AmmattitaitovaatimusDto> ammattitaitovaatimukset = new ArrayList<>();
        AmmattitaitovaatimusDto ammattitaitovaatimusDto = new AmmattitaitovaatimusDto();
        ammattitaitovaatimusDto.setSelite( new LokalisoituTekstiDto((Collections.singletonMap("fi", "Ammattitaitovaatimus selite"))) );
        ammattitaitovaatimusDto.setAmmattitaitovaatimusKoodi("vaatimuskoodi");
        ammattitaitovaatimusDto.setJarjestys(0);

        ammattitaitovaatimukset.add(ammattitaitovaatimusDto);
        ammattitaitovaatimusKohdeDto.setVaatimukset(ammattitaitovaatimukset);

        ammattitaitovaatimusKohteet.add(ammattitaitovaatimusKohdeDto);
        ammattitaitovaatimusKohdealueetDto.setVaatimuksenKohteet(ammattitaitovaatimusKohteet);

        ammattitaitovaatimusLista.add( ammattitaitovaatimusKohdealueetDto );
        tutkinnonOsaDto.setAmmattitaitovaatimuksetLista( ammattitaitovaatimusLista );

        tutkinnonOsaDto = perusteenOsaService.update(tutkinnonOsaDto);
        startNewTransaction();

    	List<Revision> tutkinnonOsaRevisions = perusteenOsaService.getVersiot(tutkinnonOsaDto.getId());

    	assertNotNull(tutkinnonOsaRevisions);
        assertEquals(4, tutkinnonOsaRevisions.size());
        assertEquals(1, tutkinnonOsaDto.getAmmattitaitovaatimuksetLista().size());

        AmmattitaitovaatimusKohdealueetDto ammattitaitovaatimuksenKohdealue = tutkinnonOsaDto.getAmmattitaitovaatimuksetLista().get(0);
        assertEquals("Ammattitaitovaatimuskohdealue", ammattitaitovaatimuksenKohdealue.getOtsikko().get(Kieli.FI));
        assertEquals(1, ammattitaitovaatimuksenKohdealue.getVaatimuksenKohteet().size());

        tutkinnonOsaDto = (TutkinnonOsaDto) perusteenOsaService.getVersio(tutkinnonOsaDto.getId(), 12);

        assertNotNull(tutkinnonOsaDto);
        assertNotNull(tutkinnonOsaDto.getArviointi());
        assertNotNull(tutkinnonOsaDto.getArviointi().getLisatiedot());
        assertNotNull(tutkinnonOsaDto.getArviointi().getLisatiedot().getTekstit());
        assertEquals("lisätiedot, muokattu", tutkinnonOsaDto.getArviointi().getLisatiedot().getTekstit().get(Kieli.FI));
        LOG.debug(tutkinnonOsaDto.getArviointi().getLisatiedot().getTekstit().get(Kieli.FI));

        tutkinnonOsaDto = (TutkinnonOsaDto) perusteenOsaService.getVersio(tutkinnonOsaDto.getId(), 8);
        assertNotNull(tutkinnonOsaDto);
        assertNotNull(tutkinnonOsaDto.getArviointi());
        assertNotNull(tutkinnonOsaDto.getArviointi().getLisatiedot());
        assertNotNull(tutkinnonOsaDto.getArviointi().getLisatiedot().getTekstit());
        assertEquals("lisätiedot", tutkinnonOsaDto.getArviointi().getLisatiedot().getTekstit().get(Kieli.FI));

        LOG.debug(tutkinnonOsaDto.getArviointi().getLisatiedot().getTekstit().get(Kieli.FI));

        perusteenOsaService.unlock(tutkinnonOsaDto.getId());

        endTransaction();
    }

    private void setUpSecurityContext(String username) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(username,"test"));
        SecurityContextHolder.setContext(ctx);
    }
}

package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.JulkaistuPeruste;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.JulkaisutRepository;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractDockerIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testataan docker-tietokantaa vasten, johon ajetaan migraatiot.
 */
@DirtiesContext
@Transactional
@ActiveProfiles(profiles = {"docker, default"})
public class JulkaisuServiceIT extends AbstractDockerIntegrationTest {

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Autowired
    private JulkaisutRepository julkaisutRepository;

    @Autowired
    private JulkaisutService julkaisutService;

    @Autowired
    @Dto
    private DtoMapper mapper;

    private GregorianCalendar gc;

    @Before
    public void setUp() {
        gc = new GregorianCalendar(2017, Calendar.JUNE, 4);
    }

    @Test
    public void testJulkaistu() throws ExecutionException, InterruptedException {
        PerusteprojektiDto projekti = createPeruste();
        PerusteDto ap = perusteService.get(projekti.getPeruste().getIdLong());
        gc.set(2017, Calendar.JUNE, 3);
        ap.setKoulutukset(new HashSet<>());
        ap.setVoimassaoloAlkaa(gc.getTime());
        ap.setNimi(TestUtils.lt("ap"));
        ap.getNimi().getTekstit().put(Kieli.FI, "ap_fi");
        ap.getNimi().getTekstit().put(Kieli.SV, "ap_sv");
        ap.setDiaarinumero("OPH-12345-1234");
        perusteService.update(ap.getId(), ap);

        assertThat(getJulkaisut(ap)).hasSize(0);
        CompletableFuture<Void> asyncResult = julkaisutService.teeJulkaisu(projekti.getId(), createJulkaisu(ap));
        asyncResult.get();
        assertThat(getJulkaisut(ap)).hasSize(1);
    }

    private JulkaisuBaseDto createJulkaisu(PerusteDto peruste) {
        JulkaisuBaseDto julkaisu = new JulkaisuBaseDto();
        julkaisu.setRevision(1);
        julkaisu.setTiedote(LokalisoituTekstiDto.of(Kieli.FI, "Julkaisu"));
        julkaisu.setJulkinenTiedote(LokalisoituTekstiDto.of(Kieli.FI, "Julkaisu"));
        julkaisu.setLuoja("test");
        julkaisu.setLuotu(new Date());
        julkaisu.setPeruste(peruste);
        julkaisu.setJulkinen(false);
        return julkaisu;
    }

    private List<JulkaistuPeruste> getJulkaisut(PerusteDto ap) {
        return julkaisutRepository.findAllByPeruste(mapper.map(ap, Peruste.class));
    }

    private PerusteprojektiDto createPeruste() {
        // Oman transaction, jotta löytyy teeJulkaisuAsyncin haussa
        TestTransaction.end();
        TestTransaction.start();
        TestTransaction.flagForCommit();

        PerusteprojektiLuontiDto result = new PerusteprojektiLuontiDto();
        result.setReforminMukainen(false);
        result.setNimi(TestUtils.uniikkiString());
        result.setKoulutustyyppi("koulutustyyppi_15");
        result.setLaajuusYksikko(LaajuusYksikko.OSAAMISPISTE);
        result.setReforminMukainen(true);
        result.setTyyppi(PerusteTyyppi.NORMAALI);
        result.setRyhmaOid("000");
        result.setDiaarinumero(TestUtils.uniikkiDiaari());
        PerusteprojektiDto projekti = perusteprojektiService.save(result);

        TestTransaction.end();

        return projekti;
    }
}

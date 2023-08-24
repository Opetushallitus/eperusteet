package fi.vm.sade.eperusteet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractDockerIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Testataan docker-tietokantaa vasten, johon ajetaan migraatiot.
 */
@DirtiesContext
@ActiveProfiles(profiles = {"docker, default"})
@Transactional
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

    private PerusteprojektiDto projekti;

    private PerusteDto peruste;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setUp() {
        gc = new GregorianCalendar(2017, Calendar.JUNE, 4);
        projekti = createPeruste();
        peruste = perusteService.get(projekti.getPeruste().getIdLong());
        gc.set(2017, Calendar.JUNE, 3);
        peruste.setKoulutukset(new HashSet<>());
        peruste.setVoimassaoloAlkaa(gc.getTime());
        peruste.setNimi(TestUtils.lt("ap"));
        peruste.getNimi().getTekstit().put(Kieli.FI, "ap_fi");
        peruste.getNimi().getTekstit().put(Kieli.SV, "ap_sv");
        peruste.setDiaarinumero("OPH-12345-1234");
        perusteService.update(peruste.getId(), peruste);
    }

    @Test
    public void testJulkaisu() throws ExecutionException, InterruptedException {
        assertThat(getJulkaisut(peruste)).hasSize(0);
        CompletableFuture<Void> asyncResult = julkaisutService.teeJulkaisu(projekti.getId(), createJulkaisu(peruste));
        asyncResult.get();
        assertThat(getJulkaisut(peruste)).hasSize(1);
    }

    @Test
    public void testJulkaisu_ilman_muutoksia() throws ExecutionException, InterruptedException {
        expectedEx.expect(BusinessRuleViolationException.class);
        expectedEx.expectMessage("julkaisu-epaonnistui-peruste-ei-muuttunut-viime-julkaisun-jalkeen");

        CompletableFuture<Void> asyncResult = julkaisutService.teeJulkaisu(projekti.getId(), createJulkaisu(peruste));
        asyncResult.get();
        julkaisutService.teeJulkaisu(projekti.getId(), createJulkaisu(peruste));
    }

    @Test
    public void testJulkaisu_data() throws ExecutionException, InterruptedException, JSONException, IOException {
        CompletableFuture<Void> asyncResult = julkaisutService.teeJulkaisu(projekti.getId(), createJulkaisu(peruste));
        asyncResult.get();

        Page<String> julkaisut = julkaisutRepository.findAllJulkisetJulkaisut(
                List.of("koulutustyyppi_15"),
                "",
                "fi",
                0L,
                true,
                true,
                true,
                false,
                false,
                "normaali",
                "",
                "",
                new PageRequest(0, 10));

        Resource resource = new ClassPathResource("material/julkaisu.json");
        ObjectNode julkaisuFile = objectMapper.readValue(resource.getFile(), ObjectNode.class);

        // verrataan relevanttia dataa tiedoston ja julkaistun datan välillä
        assertTrue(JSONCompare.compareJSON(julkaisuFile.toString(), julkaisut.getContent().get(0), JSONCompareMode.LENIENT).passed());
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
        // Oma transaction, jotta löytyy teeJulkaisuAsyncin haussa
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

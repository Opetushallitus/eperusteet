package fi.vm.sade.eperusteet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fi.vm.sade.eperusteet.domain.JulkaistuPeruste;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.dto.JulkaisuSisaltoTyyppi;
import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.JulkaisutRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractDockerIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import org.apache.tika.mime.MimeTypeException;
import org.assertj.core.util.Maps;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertTrue;

/**
 * Testataan docker-tietokantaa vasten, johon ajetaan migraatiot.
 */
@DirtiesContext
@ActiveProfiles(profiles = {"docker"})
@Transactional
@SpringBootTest
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
    private PerusteRepository perusteRepository;

    @Autowired
    private PerusteprojektiRepository perusteprojektiRepository;

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
        projekti = createPeruste();

        startNewTransaction();
        peruste = perusteService.get(projekti.getPeruste().getIdLong());
        peruste.setKoulutukset(new HashSet<>());
        peruste.setVoimassaoloAlkaa(new Date(1496437200000L));
        peruste.setNimi(TestUtils.lt("ap"));
        peruste.getNimi().getTekstit().put(Kieli.FI, "ap_fi");
        peruste.getNimi().getTekstit().put(Kieli.SV, "ap_sv");
        peruste.setDiaarinumero("OPH-12345-1234");
        perusteService.update(peruste.getId(), peruste);
        endTransaction();
    }

    @After
    public void clean() {
        arkistoiPerusteProjektit();
    }

    @Test
    public void testDigitaalinenOsaaminen() {
        assertThat(julkaisutService.getJulkisetJulkaisut(null, peruste.getNimi().get(Kieli.FI), "", "fi", "digitaalinen_osaaminen", true, true, true, false, "", "", JulkaisuSisaltoTyyppi.PERUSTE,
                0, 10).getContent()).hasSize(0);
    }

    @Test
    public void testJulkaise() throws ExecutionException, InterruptedException {
        assertThat(getJulkaisut(peruste)).hasSize(0);

        assertThatThrownBy(() -> {
            perusteService.getJulkaistuSisalto(peruste.getId(), false);
        }).isInstanceOf(NotExistsException.class);

        projekti.setEsikatseltavissa(true);
        perusteprojektiRepository.saveAndFlush(mapper.map(projekti, Perusteprojekti.class));
        assertThat(perusteService.getJulkaistuSisalto(peruste.getId(), false)).isNotNull();

        CompletableFuture<Void> asyncResult = julkaisutService.teeJulkaisu(projekti.getId(), createJulkaisu(peruste));
        asyncResult.get();
        assertThat(getJulkaisut(peruste)).hasSize(1);
        assertThat(perusteRepository.findOne(peruste.getId()).getGlobalVersion().getAikaleima()).isEqualTo(perusteRepository.findOne(peruste.getId()).getJulkaisut().get(0).getLuotu());
        assertThat(julkaisutService.getJulkisetJulkaisut(null, peruste.getNimi().get(Kieli.FI), "", "fi", "normaali", true, true, true, false, "", "", JulkaisuSisaltoTyyppi.PERUSTE,
                0, 10).getContent()).hasSize(1);
    }

    @Test
    public void testJulkaiseUudelleen() throws ExecutionException, InterruptedException {
        startNewTransaction();
        CompletableFuture<Void> asyncResult = julkaisutService.teeJulkaisu(projekti.getId(), createJulkaisu(peruste));
        asyncResult.get();

        startNewTransaction();
        peruste.setNimi(TestUtils.lt("updated"));
        perusteService.update(peruste.getId(), peruste);

        startNewTransaction();
        CompletableFuture<Void> asyncResult2 = julkaisutService.teeJulkaisu(projekti.getId(), createJulkaisu(peruste));
        asyncResult2.get();

        assertThat(getJulkaisut(peruste)).hasSize(2);
        endTransaction();
    }

    @Test
    public void testJulkaiseJaVertaaDataa() throws ExecutionException, InterruptedException, JSONException, IOException {
        CompletableFuture<Void> asyncResult = julkaisutService.teeJulkaisu(projekti.getId(), createJulkaisu(peruste));
        asyncResult.get();

        Page<String> julkaisut = julkaisutRepository.findAllJulkisetJulkaisut(
                List.of("koulutustyyppi_15"),
                "", "",
                "fi",
                0L,
                true,
                true,
                true,
                false,
                "normaali",
                "",
                "",
                "peruste",
                PageRequest.of(0, 10));

        Resource resource = new ClassPathResource("material/julkaisu.json");
        ObjectNode julkaisuFile = objectMapper.readValue(resource.getFile(), ObjectNode.class);

        // verrataan relevanttia dataa tiedoston ja julkaistun datan välillä
        assertTrue(JSONCompare.compareJSON(julkaisuFile.toString(), julkaisut.getContent().get(0), JSONCompareMode.LENIENT).passed());
    }

    @Test
    public void testPaivitaJulkaisu() throws ExecutionException, InterruptedException, IOException, HttpMediaTypeNotSupportedException, MimeTypeException {
        CompletableFuture<Void> asyncResult = julkaisutService.teeJulkaisu(projekti.getId(), createJulkaisu(peruste));
        asyncResult.get();

        JulkaistuPeruste julkaisu = getJulkaisu(peruste);

        assertThat(julkaisu.getTiedote()).extracting(TekstiPalanen::getTeksti).isEqualTo(Maps.newHashMap(Kieli.FI, "Tiedote"));

        julkaisu.setTiedote(TekstiPalanen.of( Kieli.FI, "TiedotePäivitys"));
        julkaisutService.updateJulkaisu(peruste.getId(), mapper.map(julkaisu, JulkaisuBaseDto.class));

        JulkaistuPeruste paivitetty = getJulkaisu(peruste);

        assertThat(paivitetty.getRevision()).isEqualTo(1);
        assertThat(paivitetty.getTiedote()).extracting(TekstiPalanen::getTeksti).isEqualTo(Maps.newHashMap(Kieli.FI, "TiedotePäivitys"));
    }

    private JulkaisuBaseDto createJulkaisu(PerusteDto peruste) {
        JulkaisuBaseDto julkaisu = new JulkaisuBaseDto();
        julkaisu.setTiedote(LokalisoituTekstiDto.of(Kieli.FI, "Tiedote"));
        julkaisu.setJulkinenTiedote(LokalisoituTekstiDto.of(Kieli.FI, "JulkinenTiedote"));
        julkaisu.setLuoja("test");
        julkaisu.setLuotu(new Date());
        julkaisu.setPeruste(peruste);
        julkaisu.setJulkinen(false);
        return julkaisu;
    }

    private JulkaistuPeruste getJulkaisu(PerusteDto perusteDto) {
        return julkaisutRepository.findFirstByPerusteOrderByRevisionDesc(mapper.map(perusteDto, Peruste.class));
    }

    private List<JulkaistuPeruste> getJulkaisut(PerusteDto perusteDto) {
        return julkaisutRepository.findAllByPeruste(mapper.map(perusteDto, Peruste.class));
    }

    private PerusteprojektiDto createPeruste() {
        // Oma transaction, jotta löytyy teeJulkaisuAsyncin haussa
        startNewTransaction();

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

        endTransaction();

        return projekti;
    }

    private void arkistoiPerusteProjektit() {
        perusteprojektiRepository.findAll().forEach(projekti -> {
            perusteprojektiService.updateTila(projekti.getId(), ProjektiTila.POISTETTU, null);
        });
    }
}

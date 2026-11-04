package fi.vm.sade.eperusteet.resource;


import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.utils.dto.SivutettuTulosDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysDto;
import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenJulkaisuData;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.JulkaisutRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.resource.julkinen.ExternalController;
import fi.vm.sade.eperusteet.service.JulkaisutService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.test.AbstractDockerIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@DirtiesContext
@ActiveProfiles(profiles = {"docker"})
@Transactional
@SpringBootTest
public class ExternalControllerIT extends AbstractDockerIntegrationTest {

    @Autowired
    private ExternalController externalController;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private PerusteprojektiService perusteprojektiService;

    @Autowired
    private JulkaisutService julkaisutService;

    @Autowired
    private PerusteprojektiTestUtils perusteprojektiTestUtils;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JulkaisutRepository julkaisutRepository;

    @Test
    public void testPerusteLoytyy() throws ExecutionException, InterruptedException {
        PerusteDto peruste1 = creatPerusteJaJulkaise("peruste1");

        PerusteKaikkiDto perusteKaikkiDto = externalController.getPeruste(peruste1.getId()).getBody();
        Assertions.assertThat(perusteKaikkiDto).isNotNull();
        Assertions.assertThat(perusteKaikkiDto.getId()).isEqualTo(peruste1.getId());

        int revisio = julkaisutService.getJulkaisut(peruste1.getId()).get(0).getRevision();
        Assertions.assertThat(perusteKaikkiDto.getJulkaisuRevisio()).isEqualTo(revisio);
        Assertions.assertThat(julkaisutService.getViimeisinJulkaisuData(peruste1.getId()).get("julkaisuRevisio").asInt()).isEqualTo(revisio);

        startNewTransaction();
        Assertions.assertThat(julkaisutRepository.findFirstByPerusteIdOrderByRevisionDesc(peruste1.getId()).getData().getData().has("julkaisuRevisio")).isFalse();
        endTransaction();
    }

    @Test
    public void testPerusteRevisioLoytyy() throws ExecutionException, InterruptedException {
        PerusteDto peruste = creatPerusteJaJulkaise("peruste1");
        int revisio = julkaisutService.getJulkaisut(peruste.getId()).get(0).getRevision();

        ResponseEntity<PerusteKaikkiDto> response = externalController.getPerusteJulkaisuRevisio(peruste.getId(), revisio);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().getId()).isEqualTo(peruste.getId());
        Assertions.assertThat(response.getBody().getJulkaisuRevisio()).isEqualTo(revisio);
    }

    @Test
    public void testPerusteRevisioEiLoydy() throws ExecutionException, InterruptedException {
        PerusteDto peruste = creatPerusteJaJulkaise("peruste1");

        startNewTransaction();
        perusteRepository.findOne(peruste.getId()).getPerusteprojekti().setEsikatseltavissa(true);
        endTransaction();

        ResponseEntity<PerusteKaikkiDto> response = externalController.getPerusteJulkaisuRevisio(peruste.getId(), 999);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(response.getBody()).isNull();
    }

    @Test
    public void testPerusteRevisioReititys() throws Exception {
        PerusteDto peruste = creatPerusteJaJulkaise("peruste1");
        int revisio = julkaisutService.getJulkaisut(peruste.getId()).get(0).getRevision();
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        mvc.perform(MockMvcRequestBuilders.get("/api/external/peruste/" + peruste.getId() + "/v" + revisio))
                .andExpect(MockMvcResultMatchers.handler().methodName("getPerusteJulkaisuRevisio"));

        mvc.perform(MockMvcRequestBuilders.get("/api/external/peruste/" + peruste.getId() + "/v999"))
                .andExpect(MockMvcResultMatchers.handler().methodName("getPerusteJulkaisuRevisio"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

        mvc.perform(MockMvcRequestBuilders.get("/api/external/peruste/" + peruste.getId() + "/id"))
                .andExpect(MockMvcResultMatchers.handler().methodName("getPerusteDynamicQuery"));

        mvc.perform(MockMvcRequestBuilders.get("/api/external/peruste/" + peruste.getId() + "/nimi/fi"))
                .andExpect(MockMvcResultMatchers.handler().methodName("getPerusteDynamicQueryHidden"));
    }

    @Test
    public void testPerusteetLoytyy() throws ExecutionException, InterruptedException {
        PerusteDto peruste1 = creatPerusteJaJulkaise("peruste1");
        PerusteDto peruste2 = creatPerusteJaJulkaise("peruste2");
        PerusteDto peruste3 = creatPerusteJaJulkaise("peruste3");

        SivutettuTulosDto<PerusteenJulkaisuData> perusteet = externalController.getPerusteet(
                null,
                "",
                "fi",
                true,
                true,
                true,
                false,
                "normaali",
                "",
                "",
                0,
                3);
        Assertions.assertThat(perusteet.getData()).isNotNull();
        Assertions.assertThat(perusteet.getData()).hasSize(3);

        SivutettuTulosDto<PerusteenJulkaisuData> perusteet2 = externalController.getPerusteet(
                null,
                "peruste1",
                "fi",
                true,
                true,
                true,
                false,
                "normaali",
                "",
                "",
                0,
                1);
        Assertions.assertThat(perusteet2.getData()).isNotNull();
        Assertions.assertThat(perusteet2.getData()).hasSize(1);
    }

    private PerusteDto creatPerusteJaJulkaise(String nimi) throws ExecutionException, InterruptedException {
        startNewTransaction();
        PerusteprojektiDto projektiDto = perusteprojektiTestUtils.createPerusteprojekti((perusteprojekti) -> {
            perusteprojekti.setKoulutustyyppi(KoulutusTyyppi.ESIOPETUS.toString());
        });
        PerusteDto peruste = perusteService.get(projektiDto.getPeruste().getIdLong());
        peruste.setVoimassaoloAlkaa(new Date(1496437200000L));
        peruste.setNimi(TestUtils.lt(nimi));
        peruste = perusteService.update(peruste.getId(), peruste);
        endTransaction();

        CompletableFuture<Void> asyncResult = julkaisutService.teeJulkaisu(perusteRepository.findOne(peruste.getId()).getPerusteprojekti().getId(), createJulkaisu(peruste));
        asyncResult.get();

        return peruste;
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
}

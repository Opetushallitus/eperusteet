package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysLiittyyTyyppi;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysTila;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysTyyppi;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysAsiasanaDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysDto;
import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiLuontiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.resource.julkinen.ExternalController;
import fi.vm.sade.eperusteet.service.JulkaisutService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.PerusteprojektiService;
import fi.vm.sade.eperusteet.service.test.AbstractDockerIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@DirtiesContext
@ActiveProfiles(profiles = {"docker"})
@Transactional
@SpringBootTest
public class MaaraysControllerIT extends AbstractDockerIntegrationTest {

    @Autowired
    private MaaraysController maaraysController;

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

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("fi.vm.sade.eperusteet.salli_virheelliset", () -> "true");
    }

    @Test
    public void testMaaraysLoytyy() throws ExecutionException, InterruptedException {
        PerusteDto peruste1 = creatPerusteJaJulkaise("peruste1", perusteprojekti -> {
            perusteprojekti.setKoulutustyyppi(KoulutusTyyppi.ESIOPETUS.toString());
            perusteprojekti.setMaarays(MaaraysDto
                    .builder()
                            .nimi(LokalisoituTekstiDto.of(Kieli.FI, "peruste1"))
                            .tyyppi(MaaraysTyyppi.PERUSTE)
                            .tila(MaaraysTila.LUONNOS)
                            .liitteet(new HashMap<>())
                            .asiasanat(new HashMap<>() {{
                                put(Kieli.FI, new MaaraysAsiasanaDto());
                            }})
                            .liittyyTyyppi(MaaraysLiittyyTyyppi.EI_LIITY)
                            .kuvaus(LokalisoituTekstiDto.of(Kieli.FI, "kuvaus"))
                    .build());
        });

        Page<MaaraysDto> maaraysDtos = maaraysController.getMaaraykset(
                "",
                "fi",
                null,
                null,
                false,
                false,
                false,
                false,
                false,
                0,
                10,
                "nimi",
                "ASC");
        Assertions.assertThat(maaraysDtos).isNotNull();
        Assertions.assertThat(maaraysDtos.getContent()).hasSize(1);
        Assertions.assertThat(maaraysDtos.getContent().get(0).getPeruste().getId()).isEqualTo(peruste1.getId());
    }

    private PerusteDto creatPerusteJaJulkaise(String nimi, Consumer<PerusteprojektiLuontiDto> withPerusteprojekti) throws ExecutionException, InterruptedException {
        startNewTransaction();
        PerusteprojektiDto projektiDto = perusteprojektiTestUtils.createPerusteprojekti(withPerusteprojekti);
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

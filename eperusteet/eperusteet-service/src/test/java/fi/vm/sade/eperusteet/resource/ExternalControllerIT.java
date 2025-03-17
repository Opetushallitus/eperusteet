package fi.vm.sade.eperusteet.resource;


import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysDto;
import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteKaikkiDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenJulkaisuData;
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
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

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

    @Test
    public void testPerusteLoytyy() throws ExecutionException, InterruptedException {
        PerusteDto peruste1 = creatPerusteJaJulkaise("peruste1");

        PerusteKaikkiDto perusteKaikkiDto = externalController.getPeruste(peruste1.getId()).getBody();
        Assertions.assertThat(perusteKaikkiDto).isNotNull();
        Assertions.assertThat(perusteKaikkiDto.getId()).isEqualTo(peruste1.getId());
    }

    @Test
    public void testPerusteetLoytyy() throws ExecutionException, InterruptedException {
        PerusteDto peruste1 = creatPerusteJaJulkaise("peruste1");
        PerusteDto peruste2 = creatPerusteJaJulkaise("peruste2");
        PerusteDto peruste3 = creatPerusteJaJulkaise("peruste3");

        Page<PerusteenJulkaisuData> perusteet = externalController.getPerusteet(
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
                3).getBody();
        Assertions.assertThat(perusteet).isNotNull();
        Assertions.assertThat(perusteet.getContent()).hasSize(3);

        perusteet = externalController.getPerusteet(
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
                1).getBody();
        Assertions.assertThat(perusteet).isNotNull();
        Assertions.assertThat(perusteet.getContent()).hasSize(1);
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

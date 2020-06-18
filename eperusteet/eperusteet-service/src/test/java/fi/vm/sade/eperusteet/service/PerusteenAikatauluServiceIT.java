package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.AikatauluTapahtuma;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteAikataulu;
import fi.vm.sade.eperusteet.dto.peruste.PerusteAikatauluDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@Transactional
@DirtiesContext
public class PerusteenAikatauluServiceIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteRepository repo;

    @Autowired
    private PerusteAikatauluService perusteAikatauluService;

    private Peruste peruste;

    @Before
    public void setup() {
        peruste = TestUtils.teePeruste();
        repo.save(peruste);
    }

    @Test
    public void test_addAikatauluja() {

        List<PerusteAikatauluDto> aikatauluDtos = perusteAikatauluService.save(peruste.getId(), Arrays.asList(
                PerusteAikatauluDto.builder().tapahtuma(AikatauluTapahtuma.JULKAISU).tapahtumapaiva(new Date()).build(),
                PerusteAikatauluDto.builder().tapahtuma(AikatauluTapahtuma.LUOMINEN).tapahtumapaiva(new Date()).build(),
                PerusteAikatauluDto.builder().tapahtuma(AikatauluTapahtuma.TAVOITE).tapahtumapaiva(new Date()).build()
        ));
        assertThat(aikatauluDtos).hasSize(3);

        PerusteDto perusteDto = perusteService.get(peruste.getId());
        assertThat(perusteDto.getPerusteenAikataulut()).hasSize(3);
    }

    @Test
    public void test_modifyAikatauluja() {

        Date date1 = new Date();
        Date date2 = new Date(1000000l);

        perusteAikatauluService.save(peruste.getId(), Arrays.asList(
                PerusteAikatauluDto.builder().tapahtuma(AikatauluTapahtuma.JULKAISU).tapahtumapaiva(date1).build(),
                PerusteAikatauluDto.builder().tapahtuma(AikatauluTapahtuma.LUOMINEN).tapahtumapaiva(date1).build(),
                PerusteAikatauluDto.builder().tapahtuma(AikatauluTapahtuma.TAVOITE).tapahtumapaiva(date1).build()
        ));

        PerusteDto perusteDto = perusteService.get(peruste.getId());

        List<PerusteAikatauluDto> aikataulut = perusteDto.getPerusteenAikataulut().stream().filter(aikataulu -> aikataulu.getTapahtuma().equals(AikatauluTapahtuma.JULKAISU)).collect(Collectors.toList());
        aikataulut.add(PerusteAikatauluDto.builder().tapahtuma(AikatauluTapahtuma.LUOMINEN).tapahtumapaiva(date2).build());

        List<PerusteAikatauluDto> aikatauluDtos = perusteAikatauluService.save(peruste.getId(), aikataulut);
        assertThat(aikatauluDtos).hasSize(2);

        perusteDto = perusteService.get(peruste.getId());
        assertThat(perusteDto.getPerusteenAikataulut()).hasSize(2);
        assertThat(perusteDto.getPerusteenAikataulut()).extracting("tapahtuma").containsExactlyInAnyOrder(AikatauluTapahtuma.JULKAISU, AikatauluTapahtuma.LUOMINEN);
        assertThat(perusteDto.getPerusteenAikataulut()).extracting("tapahtumapaiva").containsExactlyInAnyOrder(date1, date2);
    }


}

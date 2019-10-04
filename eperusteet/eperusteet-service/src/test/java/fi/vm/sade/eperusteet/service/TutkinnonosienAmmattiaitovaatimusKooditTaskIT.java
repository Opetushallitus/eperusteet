package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import fi.vm.sade.eperusteet.repository.SkeduloituajoRepository;
import fi.vm.sade.eperusteet.service.impl.TutkinnonosienAmmattiaitovaatimusKooditTask;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.utils.client.OphClientHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@DirtiesContext
public class TutkinnonosienAmmattiaitovaatimusKooditTaskIT extends AbstractIntegrationTest {

    @Autowired
    TutkinnonosienAmmattiaitovaatimusKooditTask task;

    @Autowired
    SkeduloituajoRepository skeduloituajoRepository;

    @Autowired
    OphClientHelper ophClientHelper;

    @Test
    public void testExecute() {

        assertThat(skeduloituajoRepository.findAll()).hasSize(0);

        task.execute();

        assertThat(skeduloituajoRepository.findByNimi("TutkinnonosienAmmattiaitovaatimusKooditTask")).isNotNull();
        assertThat(skeduloituajoRepository.findByNimi("TutkinnonosienAmmattiaitovaatimusKooditTask").getViimeisinajo()).isNotNull();
        verify(ophClientHelper).post("", "koodistorelaatio" + KoodistoUriArvo.KOULUTUS + KoodistoUriArvo.TUTKINNONOSAT);
        verify(ophClientHelper).post("", "koodistorelaatio" + KoodistoUriArvo.TUTKINNONOSAT + KoodistoUriArvo.AMMATTITAITOVAATIMUKSET);
    }
}

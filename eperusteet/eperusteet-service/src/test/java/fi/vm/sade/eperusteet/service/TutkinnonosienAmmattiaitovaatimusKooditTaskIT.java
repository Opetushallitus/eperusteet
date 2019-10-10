package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.SkeduloituAjo;
import fi.vm.sade.eperusteet.domain.SkeduloituAjoStatus;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoUriArvo;
import fi.vm.sade.eperusteet.repository.SkeduloituajoRepository;
import fi.vm.sade.eperusteet.service.exception.SkeduloituAjoAlreadyRunningException;
import fi.vm.sade.eperusteet.service.impl.TutkinnonosienAmmattiaitovaatimusKooditTask;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.utils.client.OphClientHelper;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@DirtiesContext
public class TutkinnonosienAmmattiaitovaatimusKooditTaskIT extends AbstractIntegrationTest {

    @Autowired
    TutkinnonosienAmmattiaitovaatimusKooditTask task;

    @Autowired
    SkeduloituajoRepository skeduloituajoRepository;

    @Autowired
    SkeduloituajoService skeduloituajoService;

    @Autowired
    OphClientHelper ophClientHelper;

    @Before
    public void setup() {
        skeduloituajoRepository.deleteAll();
        Mockito.doNothing().when(ophClientHelper).post(Mockito.anyString(), Mockito.anyString());
        Mockito.clearInvocations(ophClientHelper);
    }

    @Test
    public void testExecute_ok() {

        assertThat(skeduloituajoRepository.findAll()).hasSize(0);

        task.execute();

        assertThat(skeduloituajoRepository.findByNimi("TutkinnonosienAmmattiaitovaatimusKooditTask")).isNotNull();
        assertThat(skeduloituajoRepository.findByNimi("TutkinnonosienAmmattiaitovaatimusKooditTask").getViimeisinAjoKaynnistys()).isNotNull();
        assertThat(skeduloituajoRepository.findByNimi("TutkinnonosienAmmattiaitovaatimusKooditTask").getViimeisinAjoLopetus()).isNotNull();
        assertThat(skeduloituajoRepository.findByNimi("TutkinnonosienAmmattiaitovaatimusKooditTask").getStatus()).isEqualTo(SkeduloituAjoStatus.PYSAYTETTY);
        verify(ophClientHelper).post("", "koodistorelaatio" + KoodistoUriArvo.KOULUTUS + KoodistoUriArvo.TUTKINNONOSAT);
        verify(ophClientHelper).post("", "koodistorelaatio" + KoodistoUriArvo.TUTKINNONOSAT + KoodistoUriArvo.AMMATTITAITOVAATIMUKSET);
    }

    @Test
    public void testExecute_virhe() {
        Mockito.doThrow(new RuntimeException()).when(ophClientHelper).post(Mockito.anyString(), Mockito.anyString());

        assertThat(skeduloituajoRepository.findAll()).hasSize(0);
        Assertions.assertThatThrownBy(() -> task.execute()).isInstanceOf(RuntimeException.class);

        assertThat(skeduloituajoRepository.findByNimi("TutkinnonosienAmmattiaitovaatimusKooditTask")).isNotNull();
        assertThat(skeduloituajoRepository.findByNimi("TutkinnonosienAmmattiaitovaatimusKooditTask").getViimeisinAjoKaynnistys()).isNotNull();
        assertThat(skeduloituajoRepository.findByNimi("TutkinnonosienAmmattiaitovaatimusKooditTask").getViimeisinAjoLopetus()).isNull();
        assertThat(skeduloituajoRepository.findByNimi("TutkinnonosienAmmattiaitovaatimusKooditTask").getStatus()).isEqualTo(SkeduloituAjoStatus.AJOVIRHE);

    }

    @Test
    public void testExecute_joAjossa() {
        assertThat(skeduloituajoRepository.findAll()).hasSize(0);
        SkeduloituAjo ajo = skeduloituajoService.lisaaUusiAjo("TutkinnonosienAmmattiaitovaatimusKooditTask");
        skeduloituajoService.paivitaAjoStatus(ajo, SkeduloituAjoStatus.AJOSSA);

        Assertions.assertThatThrownBy(() -> task.execute()).isInstanceOf(SkeduloituAjoAlreadyRunningException.class);

    }
}

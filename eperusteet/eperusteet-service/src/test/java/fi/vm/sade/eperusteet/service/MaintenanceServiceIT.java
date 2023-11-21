package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.maarays.Maarays;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysTila;
import fi.vm.sade.eperusteet.dto.peruste.JulkaisuBaseDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.JulkaisutRepository;
import fi.vm.sade.eperusteet.repository.MaaraysRepository;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.test.AbstractDockerIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@ActiveProfiles(profiles = {"docker, default"})
@Transactional
public class MaintenanceServiceIT  extends AbstractDockerIntegrationTest {

    @Autowired
    private PerusteprojektiTestUtils ppTestUtils;

    @Autowired
    private MaintenanceService maintenanceService;

    @Autowired
    private JulkaisutService julkaisutService;

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private JulkaisutRepository julkaisutRepository;

    @Autowired
    private MaaraysRepository maaraysRepository;

    @Before
    public void setup() {
        IntStream.range(0, 15).forEach(lkm -> {
            PerusteprojektiDto projekti = createProjekti(PerusteTila.VALMIS);
            PerusteDto peruste = perusteService.get(projekti.getPeruste().getIdLong());
            try {
                julkaisutService.teeJulkaisu(projekti.getId(), createJulkaisu(peruste)).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });

        IntStream.range(0, 10).forEach(lkm -> createProjekti(PerusteTila.LUONNOS));
        IntStream.range(0, 1).forEach(lkm -> createProjekti(PerusteTila.POISTETTU));
    }

    @Test
    public void testMaaraysEraAjo() {
        assertThat(perusteRepository.findAll()).hasSize(26);
        assertThat(julkaisutRepository.findAll()).hasSize(15);
        assertThat(maaraysRepository.findAll()).hasSize(0);

        maintenanceService.teeMaarayksetPerusteille();
        List<Maarays> maaraykset = maaraysRepository.findAll();
        assertThat(maaraykset.stream().filter(maarays -> maarays.getTila().equals(MaaraysTila.LUONNOS))).hasSize(10);
        assertThat(maaraykset.stream().filter(maarays -> maarays.getTila().equals(MaaraysTila.JULKAISTU))).hasSize(15);
    }

    private PerusteprojektiDto createProjekti(PerusteTila tila ) {
        // Oma transaction, jotta löytyy teeJulkaisuAsyncin haussa
        if (TestTransaction.isActive()) {
            TestTransaction.end();
        }
        TestTransaction.start();
        TestTransaction.flagForCommit();

        PerusteprojektiDto projekti = ppTestUtils.createPerusteprojekti(dto -> dto.setKoulutustyyppi(KoulutusTyyppi.ESIOPETUS.toString()));
        Peruste peruste = perusteRepository.findOne(projekti.getPeruste().getIdLong());
        peruste.setKoulutukset(new HashSet<>());
        peruste.setVoimassaoloAlkaa(DateTime.now().toDate());
        peruste.setNimi(TekstiPalanen.of(TestUtils.uniikkiString(), TestUtils.uniikkiString()));
        peruste.setDiaarinumero(new Diaarinumero(TestUtils.uniikkiDiaari()));
        peruste.asetaTila(tila);
        perusteRepository.save(peruste);

        TestTransaction.end();

        return projekti;
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

package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.LaajuusYksikko;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.dto.yl.AIPEKurssiBaseDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEKurssiDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEKurssiSuppeaDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEOppiaineBaseDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEOppiaineDto;
import fi.vm.sade.eperusteet.dto.yl.AIPEOppiaineSuppeaDto;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.TestUtils;
import fi.vm.sade.eperusteet.service.yl.AIPEOpetuksenPerusteenSisaltoService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@DirtiesContext
public class AIPERevisionIT extends AbstractIntegrationTest {

    @Autowired
    private AIPEOpetuksenPerusteenSisaltoService sisalto;

    @Autowired
    private PerusteService perusteService;

    private Long perusteId;
    private Long vaiheId;
    private Long oppiaineId;
    private Long oppimaara1Id;
    private Long oppimaara2Id;

    @Before
    public void setup() {
        TestTransaction.flagForCommit();
        perusteId = perusteService.luoPerusteRunko(KoulutusTyyppi.AIKUISTENPERUSOPETUS, null, LaajuusYksikko.KURSSI, PerusteTyyppi.NORMAALI).getId();
        vaiheId = sisalto.addVaihe(perusteId, TestUtils.createVaihe()).getId();
        oppiaineId = sisalto.addOppiaine(perusteId, vaiheId, TestUtils.createAIPEOppiaine()).getId();
        oppimaara1Id = sisalto.addOppimaara(perusteId, vaiheId, oppiaineId, TestUtils.createAIPEOppiaine()).getId();
        oppimaara2Id = sisalto.addOppimaara(perusteId, vaiheId, oppiaineId, TestUtils.createAIPEOppiaine()).getId();
        sisalto.addKurssi(perusteId, vaiheId, oppimaara1Id, TestUtils.createAIPEKurssi());
        sisalto.addKurssi(perusteId, vaiheId, oppimaara1Id, TestUtils.createAIPEKurssi());
        sisalto.addKurssi(perusteId, vaiheId, oppimaara2Id, TestUtils.createAIPEKurssi());
        commit();
    }

    @After
    public void cleanup() {
        if (TestTransaction.isActive()) {
            TestTransaction.end();
        }
    }

    private void commit() {
        startNewTransaction();
    }

    private void muutaTehtava(Long id, String teksti) {
        AIPEOppiaineDto dto = sisalto.getOppiaine(perusteId, vaiheId, id, null);
        dto.setTehtava(TestUtils.oto("otsikko", teksti));
        sisalto.updateOppiaine(perusteId, vaiheId, id, dto);
        commit();
    }

    private Integer ensimmainenRevisio(Long id) {
        return sisalto.getOppiaineRevisions(perusteId, vaiheId, id).stream()
                .map(Revision::getNumero)
                .min(Comparator.naturalOrder())
                .orElseThrow();
    }

    private void assertRakenneEhja() {
        assertThat(sisalto.getOppimaarat(perusteId, vaiheId, oppiaineId, null))
                .extracting(AIPEOppiaineSuppeaDto::getId)
                .containsExactlyInAnyOrder(oppimaara1Id, oppimaara2Id);
        assertThat(sisalto.getKurssit(perusteId, vaiheId, oppimaara1Id)).hasSize(2);
        assertThat(sisalto.getKurssit(perusteId, vaiheId, oppimaara2Id)).hasSize(1);
        assertThat(sisalto.getOppiaine(perusteId, vaiheId, oppimaara1Id, null).getOppiaine().getIdLong()).isEqualTo(oppiaineId);
        assertThat(sisalto.getOppiaine(perusteId, vaiheId, oppimaara2Id, null).getOppiaine().getIdLong()).isEqualTo(oppiaineId);
    }

    @Test
    public void oppimaarienJarjestaminen() {
        List<AIPEOppiaineBaseDto> jarjestys = new ArrayList<>(sisalto.getOppimaarat(perusteId, vaiheId, oppiaineId, null));
        Collections.reverse(jarjestys);
        sisalto.updateOppimaaratJarjestys(perusteId, vaiheId, oppiaineId, jarjestys);
        commit();

        assertThat(sisalto.getOppimaarat(perusteId, vaiheId, oppiaineId, null))
                .extracting(AIPEOppiaineSuppeaDto::getId)
                .containsExactly(jarjestys.get(0).getId(), jarjestys.get(1).getId());
        assertRakenneEhja();
    }

    @Test
    public void oppimaaranMuokkausSailyttaaJarjestyksen() {
        List<AIPEOppiaineBaseDto> jarjestys = new ArrayList<>(sisalto.getOppimaarat(perusteId, vaiheId, oppiaineId, null));
        sisalto.updateOppimaaratJarjestys(perusteId, vaiheId, oppiaineId, jarjestys);
        commit();

        muutaTehtava(jarjestys.get(1).getId(), "uusi");

        assertThat(sisalto.getOppimaarat(perusteId, vaiheId, oppiaineId, null))
                .extracting(AIPEOppiaineSuppeaDto::getId)
                .containsExactly(jarjestys.get(0).getId(), jarjestys.get(1).getId());
    }

    @Test
    public void oppimaaranPalautusSailyttaaJarjestyksen() {
        List<AIPEOppiaineBaseDto> jarjestys = new ArrayList<>(sisalto.getOppimaarat(perusteId, vaiheId, oppiaineId, null));
        sisalto.updateOppimaaratJarjestys(perusteId, vaiheId, oppiaineId, jarjestys);
        commit();
        Long viimeinen = jarjestys.get(1).getId();
        Integer rev = ensimmainenRevisio(viimeinen);

        sisalto.revertOppiaine(perusteId, vaiheId, viimeinen, rev);
        commit();

        assertThat(sisalto.getOppimaarat(perusteId, vaiheId, oppiaineId, null))
                .extracting(AIPEOppiaineSuppeaDto::getId)
                .containsExactly(jarjestys.get(0).getId(), viimeinen);
    }

    @Test
    public void kurssinMuokkausSailyttaaJarjestyksen() {
        List<AIPEKurssiBaseDto> jarjestys = new ArrayList<>(sisalto.getKurssit(perusteId, vaiheId, oppimaara1Id));
        sisalto.updateKurssitJarjestys(perusteId, vaiheId, oppimaara1Id, jarjestys);
        commit();

        Long viimeinen = jarjestys.get(1).getId();
        AIPEKurssiDto kurssi = sisalto.getKurssi(perusteId, vaiheId, oppimaara1Id, viimeinen, null);
        kurssi.setKuvaus(TestUtils.olt("uusi kuvaus"));
        sisalto.updateKurssi(perusteId, vaiheId, oppimaara1Id, viimeinen, kurssi);
        commit();

        assertThat(sisalto.getKurssit(perusteId, vaiheId, oppimaara1Id))
                .extracting(AIPEKurssiSuppeaDto::getId)
                .containsExactly(jarjestys.get(0).getId(), viimeinen);
    }

    @Test
    public void vanhanOppimaaraRevisionHaku() {
        muutaTehtava(oppimaara1Id, "uusi");
        Integer rev = ensimmainenRevisio(oppimaara1Id);

        AIPEOppiaineDto vanha = sisalto.getOppiaine(perusteId, vaiheId, oppimaara1Id, rev);
        assertThat(vanha.getTehtava().get().getTeksti().get().get(Kieli.FI)).isNotEqualTo("uusi");
        assertThat(vanha.getOppiaine().getIdLong()).isEqualTo(oppiaineId);
        assertThat(vanha.getKurssit()).hasSize(2);
    }

    @Test
    public void vanhanOppiaineRevisionHaku() {
        muutaTehtava(oppiaineId, "uusi");
        Integer rev = ensimmainenRevisio(oppiaineId);

        AIPEOppiaineDto vanha = sisalto.getOppiaine(perusteId, vaiheId, oppiaineId, rev);
        assertThat(vanha.getOppiaine()).isNull();
    }

    @Test
    public void oppimaaranPalautus() {
        muutaTehtava(oppimaara1Id, "uusi");
        Integer rev = ensimmainenRevisio(oppimaara1Id);

        sisalto.revertOppiaine(perusteId, vaiheId, oppimaara1Id, rev);
        commit();

        assertThat(sisalto.getOppiaine(perusteId, vaiheId, oppimaara1Id, null).getTehtava().get().getTeksti().get()
                .get(Kieli.FI)).isNotEqualTo("uusi");
        assertRakenneEhja();
    }

    @Test
    public void oppiaineenPalautus() {
        muutaTehtava(oppiaineId, "uusi");
        Integer rev = ensimmainenRevisio(oppiaineId);

        sisalto.revertOppiaine(perusteId, vaiheId, oppiaineId, rev);
        commit();

        assertThat(sisalto.getOppiaine(perusteId, vaiheId, oppiaineId, null).getTehtava().get().getTeksti().get()
                .get(Kieli.FI)).isNotEqualTo("uusi");
        assertRakenneEhja();
    }

    @Test
    public void oppimaaranPalautusPoistetunKurssinJalkeen() {
        Integer rev = ensimmainenRevisio(oppimaara1Id);
        List<AIPEKurssiSuppeaDto> kurssit = sisalto.getKurssit(perusteId, vaiheId, oppimaara1Id);
        sisalto.removeKurssi(perusteId, vaiheId, oppimaara1Id, kurssit.get(0).getId());
        commit();
        assertThat(sisalto.getKurssit(perusteId, vaiheId, oppimaara1Id)).hasSize(1);

        sisalto.revertOppiaine(perusteId, vaiheId, oppimaara1Id, rev);
        commit();

        assertRakenneEhja();
    }

    @Test
    public void oppimaaranPalautusPoistetunTavoitteenJalkeen() {
        Integer rev = ensimmainenRevisio(oppimaara1Id);
        AIPEOppiaineDto dto = sisalto.getOppiaine(perusteId, vaiheId, oppimaara1Id, null);
        dto.setTavoitteet(new ArrayList<>());
        sisalto.updateOppiaine(perusteId, vaiheId, oppimaara1Id, dto);
        commit();

        sisalto.revertOppiaine(perusteId, vaiheId, oppimaara1Id, rev);
        commit();

        assertThat(sisalto.getOppiaine(perusteId, vaiheId, oppimaara1Id, null).getTavoitteet()).hasSize(1);
        assertRakenneEhja();
    }
}

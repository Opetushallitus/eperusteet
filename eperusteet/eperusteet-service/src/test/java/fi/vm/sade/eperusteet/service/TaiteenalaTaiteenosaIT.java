package fi.vm.sade.eperusteet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.peruste.NavigationNodeDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.dto.peruste.PerusteDto;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.dto.yl.TaiteenalaDto;
import fi.vm.sade.eperusteet.dto.yl.TaiteenosaDto;
import fi.vm.sade.eperusteet.service.exception.LockingException;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;

@DirtiesContext
@Transactional
public class TaiteenalaTaiteenosaIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteService perusteService;

    @Autowired
    private PerusteenOsaService perusteenOsaService;

    @Autowired
    private PerusteenOsaViiteService perusteenOsaViiteService;

    @Test
    public void testTaiteenalanLisays() {
        PerusteDto peruste = luoTpoPeruste();
        TaiteenalaDto taiteenala = lisaaTaiteenala(peruste);

        assertThat(taiteenala.getId()).isNotNull();
        assertThat(taiteenala.getKoodi().getUri()).isEqualTo("taiteenalat_x");

        PerusteenOsaViiteDto.Laaja sisalto = perusteService.getSuoritustapaSisalto(peruste.getId(), Suoritustapakoodi.TPO);
        assertThat(sisalto.getLapset()).hasSize(1);
        assertThat(sisalto.getLapset().get(0).getPerusteenOsa()).isInstanceOf(TaiteenalaDto.class);
    }

    @Test
    public void testTaiteenalanMuokkaus() {
        PerusteDto peruste = luoTpoPeruste();
        TaiteenalaDto taiteenala = lisaaTaiteenala(peruste);
        Long viiteId = haeTaiteenalaViiteId(peruste);

        taiteenala.setLaajuus(new BigDecimal("25.5"));
        taiteenala.setTeksti(LokalisoituTekstiDto.of("päivitetty teksti"));
        perusteenOsaService.lock(taiteenala.getId());
        taiteenala = perusteenOsaService.update(taiteenala);

        TaiteenalaDto haettu = haeTaiteenala(viiteId);
        assertThat(haettu.getLaajuus()).isEqualByComparingTo("25.5");
        assertThat(haettu.getTeksti().get(Kieli.FI)).isEqualTo("päivitetty teksti");
    }

    @Test
    public void testTaiteenalanMuokkausVaatiiLukon() {
        PerusteDto peruste = luoTpoPeruste();
        TaiteenalaDto taiteenala = lisaaTaiteenala(peruste);

        taiteenala.setTeksti(LokalisoituTekstiDto.of("ei lukkoa"));

        assertThatThrownBy(() -> perusteenOsaService.update(taiteenala))
                .isInstanceOf(LockingException.class);
    }

    @Test
    public void testTaiteenalanPoisto() {
        PerusteDto peruste = luoTpoPeruste();
        TaiteenalaDto taiteenala = lisaaTaiteenala(peruste);
        Long viiteId = haeTaiteenalaViiteId(peruste);

        perusteenOsaViiteService.removeSisalto(peruste.getId(), viiteId);

        PerusteenOsaViiteDto.Laaja sisalto = perusteService.getSuoritustapaSisalto(peruste.getId(), Suoritustapakoodi.TPO);
        assertThat(sisalto.getLapset()).isEmpty();
    }

    @Test
    public void testTaiteenOsanLisays() {
        PerusteDto peruste = luoTpoPeruste();
        TaiteenalaDto taiteenala = lisaaTaiteenala(peruste);
        Long viiteId = haeTaiteenalaViiteId(peruste);

        TaiteenosaDto uusiTaiteenosa = new TaiteenosaDto();
        uusiTaiteenosa.setNimi(LokalisoituTekstiDto.of("Tanssin perusteet"));
        taiteenala.setTaiteenOsat(new ArrayList<>(List.of(uusiTaiteenosa)));

        perusteenOsaService.lock(taiteenala.getId());
        perusteenOsaService.update(taiteenala);

        TaiteenalaDto haettu = haeTaiteenala(viiteId);
        assertThat(haettu.getTaiteenOsat()).hasSize(1);
        assertThat(haettu.getTaiteenOsat().get(0).getId()).isNotNull();
        assertThat(haettu.getTaiteenOsat().get(0).getNimi().get(Kieli.FI)).isEqualTo("Tanssin perusteet");
    }

    @Test
    public void testTaiteenOsanMuokkaus() {
        PerusteDto peruste = luoTpoPeruste();
        TaiteenalaDto taiteenala = lisaaTaiteenalaJaTaiteenosa(peruste);
        Long viiteId = haeTaiteenalaViiteId(peruste);
        TaiteenosaDto taiteenosa = taiteenala.getTaiteenOsat().get(0);

        taiteenosa.setNimi(LokalisoituTekstiDto.of("Muokattu taiteenosa"));
        taiteenosa.setLaajuus(new BigDecimal("3"));
        taiteenosa.setKuvaus(LokalisoituTekstiDto.of("kuvaus teksti"));
        taiteenosa.setTavoitteet(List.of(
                LokalisoituTekstiDto.of("tavoite 1"),
                LokalisoituTekstiDto.of("tavoite 2")
        ));
        taiteenala.setTaiteenOsat(new ArrayList<>(List.of(taiteenosa)));

        perusteenOsaService.lock(taiteenala.getId());
        perusteenOsaService.update(taiteenala);

        TaiteenosaDto haettu = haeTaiteenala(viiteId).getTaiteenOsat().get(0);
        assertThat(haettu.getNimi().get(Kieli.FI)).isEqualTo("Muokattu taiteenosa");
        assertThat(haettu.getLaajuus()).isEqualByComparingTo("3");
        assertThat(haettu.getKuvaus().get(Kieli.FI)).isEqualTo("kuvaus teksti");
        assertThat(haettu.getTavoitteet())
                .extracting(t -> t.get(Kieli.FI))
                .containsExactly("tavoite 1", "tavoite 2");
    }

    @Test
    public void testTaiteenOsanPoisto() {
        PerusteDto peruste = luoTpoPeruste();
        TaiteenalaDto taiteenala = lisaaTaiteenalaJaTaiteenosa(peruste);
        Long viiteId = haeTaiteenalaViiteId(peruste);

        taiteenala.setTaiteenOsat(Collections.emptyList());
        perusteenOsaService.lock(taiteenala.getId());
        perusteenOsaService.update(taiteenala);

        TaiteenalaDto haettu = haeTaiteenala(viiteId);
        assertThat(haettu.getTaiteenOsat()).isNullOrEmpty();
    }

    @Test
    public void testNavigaatioSisaltaaTaiteenOsan() {
        PerusteDto peruste = luoTpoPeruste();
        TaiteenalaDto taiteenala = lisaaTaiteenalaJaTaiteenosa(peruste);
        Long viiteId = haeTaiteenalaViiteId(peruste);
        Long taiteenosaId = taiteenala.getTaiteenOsat().get(0).getId();

        NavigationNodeDto navigation = perusteService.buildNavigation(peruste.getId(), "fi");
        NavigationNodeDto taiteenalaNode = navigation.getChildren().stream()
                .filter(node -> viiteId.equals(node.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(taiteenalaNode.getChildren())
                .extracting(NavigationNodeDto::getType, NavigationNodeDto::getId)
                .contains(tuple(NavigationType.taiteenosa, taiteenosaId));
    }

    private PerusteDto luoTpoPeruste() {
        PerusteprojektiDto pp = ppTestUtils.createPerusteprojekti(ppl ->
                ppl.setKoulutustyyppi(KoulutusTyyppi.TPO.toString()));
        return ppTestUtils.initPeruste(pp.getPeruste().getIdLong());
    }

    private TaiteenalaDto lisaaTaiteenala(PerusteDto peruste) {
        TaiteenalaDto taiteenalaDto = new TaiteenalaDto();
        taiteenalaDto.setKoodi(KoodiDto.of("taiteenalat", "x"));
        taiteenalaDto.setTeksti(LokalisoituTekstiDto.of("taiteenala teksti"));
        PerusteenOsaViiteDto.Matala viite = new PerusteenOsaViiteDto.Matala();
        viite.setPerusteenOsa(taiteenalaDto);
        viite = perusteService.addSisaltoUUSI(peruste.getId(), Suoritustapakoodi.TPO, viite);
        return (TaiteenalaDto) viite.getPerusteenOsa();
    }

    private TaiteenalaDto lisaaTaiteenalaJaTaiteenosa(PerusteDto peruste) {
        TaiteenalaDto taiteenala = lisaaTaiteenala(peruste);

        TaiteenosaDto taiteenosa = new TaiteenosaDto();
        taiteenosa.setNimi(LokalisoituTekstiDto.of("Taiteenosa"));
        taiteenala.setTaiteenOsat(new ArrayList<>(List.of(taiteenosa)));

        perusteenOsaService.lock(taiteenala.getId());
        return perusteenOsaService.update(taiteenala);
    }

    private Long haeTaiteenalaViiteId(PerusteDto peruste) {
        return perusteService.getSuoritustapaSisalto(peruste.getId(), Suoritustapakoodi.TPO)
                .getLapset()
                .get(0)
                .getId();
    }

    private TaiteenalaDto haeTaiteenala(Long viiteId) {
        return (TaiteenalaDto) perusteenOsaService.getByViite(viiteId);
    }
}

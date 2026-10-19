package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteenOsaViite;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.TekstiKappale;
import fi.vm.sade.eperusteet.domain.lukutaitokoulutus.LukutaitokoulutusTavoitteetJaSisaltoalueet;
import fi.vm.sade.eperusteet.domain.lukutaitokoulutus.LukutaitokoulutusTavoitealue;
import fi.vm.sade.eperusteet.dto.lukutaitokoulutus.LukutaitokoulutusTavoitteetJaSisaltoalueetDto;
import fi.vm.sade.eperusteet.dto.lukutaitokoulutus.LukutaitokoulutusTavoitealueDto;
import fi.vm.sade.eperusteet.dto.peruste.NavigationType;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.dto.peruste.PerusteenOsaViiteDto;
import fi.vm.sade.eperusteet.dto.peruste.TekstiKappaleDto;
import fi.vm.sade.eperusteet.dto.perusteprojekti.PerusteprojektiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.impl.validators.ValidatorLukutaitokoulutus;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import fi.vm.sade.eperusteet.service.test.util.PerusteprojektiTestUtils;
import fi.vm.sade.eperusteet.service.util.Validointi;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@DirtiesContext
public class LukutaitokoulutusIT extends AbstractIntegrationTest {

    @Autowired
    private PerusteenOsaViiteService perusteenOsaViiteService;

    @Autowired
    private PerusteenOsaService perusteenOsaService;

    @Autowired
    private PerusteRepository perusteRepository;

    @Autowired
    private PerusteprojektiTestUtils testUtils;

    @Autowired
    private EntityManager em;

    @Autowired
    private ValidatorLukutaitokoulutus validatorLukutaitokoulutus;

    private Peruste peruste;

    private Long perusteprojektiId;

    @Before
    public void setUp() {
        PerusteprojektiDto projektiDto = testUtils.createPerusteprojekti(pp ->
                pp.setKoulutustyyppi(KoulutusTyyppi.LUKUTAITOKOULUTUS.toString()));
        peruste = perusteRepository.findOne(projektiDto.getPeruste().getIdLong());
        perusteprojektiId = projektiDto.getId();
    }

    @Test
    public void testPerusteenLuonti() {
        assertThat(peruste.getKoulutustyyppi()).isEqualTo(KoulutusTyyppi.LUKUTAITOKOULUTUS.toString());
        assertThat(peruste.getToteutus()).isEqualTo(KoulutustyyppiToteutus.KOTOUTUMISKOULUTUS);
        assertThat(peruste.getLukutaitokoulutusSisalto()).isNotNull();
        assertThat(peruste.getLukutaitokoulutusSisalto().getSisalto()).isNotNull();
        assertThat(peruste.getVstSisalto()).isNull();
        assertThat(peruste.getSisallot()).containsExactly(peruste.getLukutaitokoulutusSisalto());
    }

    @Test
    public void testTekstikappaleenLisays() {
        PerusteenOsaViite juuri = peruste.getLukutaitokoulutusSisalto().getSisalto();
        PerusteenOsaViiteDto.Matala added = lisaaTekstikappale(juuri, "Tekstikappale");

        assertThat(added.getId()).isNotNull();
        assertThat(added.getPerusteenOsa()).isInstanceOf(TekstiKappaleDto.class);

        em.flush();
        em.refresh(juuri);
        assertThat(juuri.getLapset()).hasSize(1);
        assertThat(juuri.getLapset().get(0).getPeruste().getId()).isEqualTo(peruste.getId());
        assertThat(peruste.containsViite(juuri.getLapset().get(0))).isTrue();
    }

    @Test
    public void testKloonaus() {
        lisaaTekstikappale(peruste.getLukutaitokoulutusSisalto().getSisalto(), "Pohjan tekstikappale");
        em.flush();

        PerusteprojektiDto uusiProjekti = testUtils.createPerusteprojekti(pp -> {
            pp.setKoulutustyyppi(KoulutusTyyppi.LUKUTAITOKOULUTUS.toString());
            pp.setPerusteId(peruste.getId());
        });
        Peruste uusi = perusteRepository.findOne(uusiProjekti.getPeruste().getIdLong());

        assertThat(uusi.getId()).isNotEqualTo(peruste.getId());
        assertThat(uusi.getVstSisalto()).isNull();
        assertThat(uusi.getLukutaitokoulutusSisalto()).isNotNull();
        assertThat(uusi.getLukutaitokoulutusSisalto().getId())
                .isNotEqualTo(peruste.getLukutaitokoulutusSisalto().getId());

        PerusteenOsaViite uusiJuuri = uusi.getLukutaitokoulutusSisalto().getSisalto();
        assertThat(uusiJuuri.getLapset()).hasSize(1);
        assertThat(uusiJuuri.getLapset().get(0).getPerusteenOsa()).isInstanceOf(TekstiKappale.class);
        assertThat(uusiJuuri.getLapset().get(0).getPerusteenOsa().getNimi().getTeksti().get(Kieli.FI))
                .isEqualTo("Pohjan tekstikappale");
    }

    @Test
    public void testTavoitteidenJaSisaltoalueidenLisays() {
        PerusteenOsaViite juuri = peruste.getLukutaitokoulutusSisalto().getSisalto();
        PerusteenOsaViiteDto.Matala added = perusteenOsaViiteService.addSisalto(peruste.getId(), juuri.getId(),
                new PerusteenOsaViiteDto.Matala(new LukutaitokoulutusTavoitteetJaSisaltoalueetDto()));

        assertThat(added.getId()).isNotNull();
        assertThat(added.getPerusteenOsa()).isInstanceOf(LukutaitokoulutusTavoitteetJaSisaltoalueetDto.class);

        LukutaitokoulutusTavoitteetJaSisaltoalueetDto dto = (LukutaitokoulutusTavoitteetJaSisaltoalueetDto) perusteenOsaService.get(added.getPerusteenOsa().getId());
        assertThat(dto.getOsanTyyppi()).isEqualTo("lukutaitokoulutus_tavoitteetjasisaltoalueet");
        assertThat(dto.getNavigationType()).isEqualTo(NavigationType.lukutaitokoulutus_tavoitteetjasisaltoalueet);
        assertThat(dto.getTavoitealueet()).isEmpty();

        em.flush();
        em.refresh(juuri);
        assertThat(juuri.getLapset()).hasSize(1);
        assertThat(juuri.getLapset().get(0).getPerusteenOsa()).isInstanceOf(LukutaitokoulutusTavoitteetJaSisaltoalueet.class);
        assertThat(peruste.containsViite(juuri.getLapset().get(0))).isTrue();
    }

    @Test
    public void testTavoitteidenJaSisaltoalueidenPaivitys() {
        LukutaitokoulutusTavoitteetJaSisaltoalueetDto dto = lisaaTavoitteetJaSisaltoalueet();
        dto.setNimi(LokalisoituTekstiDto.of("Tavoitteet ja sisältöalueet"));
        dto.setTavoitealueet(luoTavoitealueet());

        dto = perusteenOsaService.update(dto);

        em.flush();
        em.clear();
        dto = (LukutaitokoulutusTavoitteetJaSisaltoalueetDto) perusteenOsaService.get(dto.getId());

        assertThat(dto.getNimi().get(Kieli.FI)).isEqualTo("Tavoitteet ja sisältöalueet");
        assertThat(tavoitealueNimet(dto)).containsExactly("tavoitealue1", "tavoitealue2");
        assertThat(tavoitealueenTavoitteet(dto.getTavoitealueet().get(0))).containsExactly("tavoite1.1", "tavoite1.2");
        assertThat(tavoitealueenTavoitteet(dto.getTavoitealueet().get(1))).containsExactly("tavoite2.1", "tavoite2.2");
    }

    @Test
    public void testTavoitteidenJaSisaltoalueidenJarjestyksenMuutos() {
        LukutaitokoulutusTavoitteetJaSisaltoalueetDto dto = lisaaTavoitteetJaSisaltoalueet();
        dto.setNimi(LokalisoituTekstiDto.of("Tavoitteet ja sisältöalueet"));
        dto.setTavoitealueet(luoTavoitealueet());
        dto = perusteenOsaService.update(dto);

        Collections.reverse(dto.getTavoitealueet());
        dto.getTavoitealueet().forEach(tavoitealue -> Collections.reverse(tavoitealue.getTavoitteet()));
        perusteenOsaService.update(dto);

        em.flush();
        em.clear();
        dto = (LukutaitokoulutusTavoitteetJaSisaltoalueetDto) perusteenOsaService.get(dto.getId());

        assertThat(tavoitealueNimet(dto)).containsExactly("tavoitealue2", "tavoitealue1");
        assertThat(tavoitealueenTavoitteet(dto.getTavoitealueet().get(0))).containsExactly("tavoite2.2", "tavoite2.1");
        assertThat(tavoitealueenTavoitteet(dto.getTavoitealueet().get(1))).containsExactly("tavoite1.2", "tavoite1.1");
    }

    @Test
    public void testTavoitteidenJaSisaltoalueidenPoistoListoista() {
        LukutaitokoulutusTavoitteetJaSisaltoalueetDto dto = lisaaTavoitteetJaSisaltoalueet();
        dto.setNimi(LokalisoituTekstiDto.of("Tavoitteet ja sisältöalueet"));
        dto.setTavoitealueet(luoTavoitealueet());
        dto = perusteenOsaService.update(dto);
        em.flush();

        LukutaitokoulutusTavoitteetJaSisaltoalueet entity = em.find(LukutaitokoulutusTavoitteetJaSisaltoalueet.class, dto.getId());
        List<Long> vanhatTavoitealueIdt = entity.getTavoitealueet().stream()
                .map(LukutaitokoulutusTavoitealue::getId)
                .collect(Collectors.toList());

        dto.getTavoitealueet().remove(1);
        dto.getTavoitealueet().get(0).getTavoitteet().remove(0);
        perusteenOsaService.update(dto);

        em.flush();
        em.clear();
        dto = (LukutaitokoulutusTavoitteetJaSisaltoalueetDto) perusteenOsaService.get(dto.getId());

        assertThat(tavoitealueNimet(dto)).containsExactly("tavoitealue1");
        assertThat(tavoitealueenTavoitteet(dto.getTavoitealueet().get(0))).containsExactly("tavoite1.2");

        vanhatTavoitealueIdt.forEach(id -> assertThat(em.find(LukutaitokoulutusTavoitealue.class, id)).isNull());
    }

    @Test
    public void testTavoitteidenJaSisaltoalueidenKloonaus() {
        LukutaitokoulutusTavoitteetJaSisaltoalueetDto dto = lisaaTavoitteetJaSisaltoalueet();
        dto.setNimi(LokalisoituTekstiDto.of("Tavoitteet ja sisältöalueet"));
        dto.setTavoitealueet(luoTavoitealueet());
        dto = perusteenOsaService.update(dto);
        perusteenOsaService.unlock(dto.getId());
        em.flush();

        PerusteprojektiDto uusiProjekti = testUtils.createPerusteprojekti(pp -> {
            pp.setKoulutustyyppi(KoulutusTyyppi.LUKUTAITOKOULUTUS.toString());
            pp.setPerusteId(peruste.getId());
        });
        Peruste uusi = perusteRepository.findOne(uusiProjekti.getPeruste().getIdLong());
        em.flush();

        PerusteenOsaViite uusiJuuri = uusi.getLukutaitokoulutusSisalto().getSisalto();
        assertThat(uusiJuuri.getLapset()).hasSize(1);
        assertThat(uusiJuuri.getLapset().get(0).getPerusteenOsa()).isInstanceOf(LukutaitokoulutusTavoitteetJaSisaltoalueet.class);

        LukutaitokoulutusTavoitteetJaSisaltoalueet klooni = (LukutaitokoulutusTavoitteetJaSisaltoalueet) uusiJuuri.getLapset().get(0).getPerusteenOsa();
        assertThat(klooni.getId()).isNotEqualTo(dto.getId());
        assertThat(klooni.getNimi().getTeksti().get(Kieli.FI)).isEqualTo("Tavoitteet ja sisältöalueet");
        assertThat(klooni.getTavoitealueet()).extracting(t -> t.getNimi().getTeksti().get(Kieli.FI)).containsExactly("tavoitealue1", "tavoitealue2");
        assertThat(klooni.getTavoitealueet()).extracting(LukutaitokoulutusTavoitealue::getId)
                .doesNotContainAnyElementsOf(dto.getTavoitealueet().stream()
                        .map(LukutaitokoulutusTavoitealueDto::getId)
                        .collect(Collectors.toList()));
        assertThat(klooni.getTavoitealueet().get(1).getTavoitteet())
                .extracting(t -> t.getTeksti().get(Kieli.FI)).containsExactly("tavoite2.1", "tavoite2.2");

        klooni.getTavoitealueet().get(0).setTavoitteet(new ArrayList<>());
        klooni.getTavoitealueet().remove(1);
        em.flush();
        em.clear();

        LukutaitokoulutusTavoitteetJaSisaltoalueetDto alkuperainen = (LukutaitokoulutusTavoitteetJaSisaltoalueetDto) perusteenOsaService.get(dto.getId());
        assertThat(tavoitealueNimet(alkuperainen)).containsExactly("tavoitealue1", "tavoitealue2");
        assertThat(tavoitealueenTavoitteet(alkuperainen.getTavoitealueet().get(0))).containsExactly("tavoite1.1", "tavoite1.2");
    }

    @Test
    public void testTavoitteidenJaSisaltoalueidenVersio() {
        LukutaitokoulutusTavoitteetJaSisaltoalueetDto dto = lisaaTavoitteetJaSisaltoalueet();
        dto.setNimi(LokalisoituTekstiDto.of("Tavoitteet ja sisältöalueet"));
        dto.setTavoitealueet(luoTavoitealueet());
        dto = perusteenOsaService.update(dto);
        em.flush();
        Long id = dto.getId();
        TestTransaction.flagForCommit();
        startNewTransaction();

        List<Revision> versiot = perusteenOsaService.getVersiot(id);
        assertThat(versiot).isNotEmpty();

        LukutaitokoulutusTavoitteetJaSisaltoalueetDto historia = (LukutaitokoulutusTavoitteetJaSisaltoalueetDto) perusteenOsaService.getVersio(
                id, versiot.get(0).getNumero());

        assertThat(historia.getNimi().get(Kieli.FI)).isEqualTo("Tavoitteet ja sisältöalueet");
        assertThat(tavoitealueNimet(historia)).containsExactly("tavoitealue1", "tavoitealue2");
        assertThat(tavoitealueenTavoitteet(historia.getTavoitealueet().get(0))).containsExactly("tavoite1.1", "tavoite1.2");
        assertThat(tavoitealueenTavoitteet(historia.getTavoitealueet().get(1))).containsExactly("tavoite2.1", "tavoite2.2");
        endTransaction();
    }

    @Test
    public void testTavoitteidenJaSisaltoalueidenValidointiPuuttuvatKielet() {
        peruste.setKielet(Set.of(Kieli.FI, Kieli.SV));
        LukutaitokoulutusTavoitteetJaSisaltoalueetDto dto = lisaaTavoitteetJaSisaltoalueet();
        dto.setNimi(LokalisoituTekstiDto.of("Tavoitteet ja sisältöalueet"));
        dto.setTavoitealueet(luoTavoitealueet());
        perusteenOsaService.update(dto);
        em.flush();
        em.clear();

        Validointi validointi = validoi();

        assertThat(validointi.getVirheet())
                .extracting(Validointi.Virhe::getKuvaus)
                .containsExactlyInAnyOrder(
                        "peruste-validointi-lukutaitokoulutus-tavoitteet-ja-sisaltoalueet-nimi",
                        "peruste-validointi-lukutaitokoulutus-tavoitealue-otsikko",
                        "peruste-validointi-lukutaitokoulutus-tavoitealue-tavoitteet");
        assertThat(validointi.getVirheet())
                .extracting(virhe -> virhe.getNavigationNode().getType())
                .containsOnly(NavigationType.lukutaitokoulutus_tavoitteetjasisaltoalueet);
    }

    @Test
    public void testTavoitteidenJaSisaltoalueidenValidointiKaikkiKielet() {
        peruste.setKielet(Set.of(Kieli.FI, Kieli.SV));
        LukutaitokoulutusTavoitteetJaSisaltoalueetDto dto = lisaaTavoitteetJaSisaltoalueet();
        dto.setNimi(kaksikielinen("Tavoitteet ja sisältöalueet"));
        dto.setTavoitealueet(new ArrayList<>(List.of(LukutaitokoulutusTavoitealueDto.builder()
                .nimi(kaksikielinen("tavoitealue"))
                .tavoitteet(new ArrayList<>(List.of(kaksikielinen("tavoite1"), kaksikielinen("tavoite2"))))
                .build())));
        perusteenOsaService.update(dto);
        em.flush();
        em.clear();

        Validointi validointi = validoi();

        assertThat(validointi.getVirheet()).isEmpty();
    }

    @Test
    public void testTavoitteidenJaSisaltoalueidenValidointiTyhjaTavoitealue() {
        LukutaitokoulutusTavoitteetJaSisaltoalueetDto dto = lisaaTavoitteetJaSisaltoalueet();
        dto.setNimi(LokalisoituTekstiDto.of("Tavoitteet ja sisältöalueet"));
        dto.setTavoitealueet(new ArrayList<>(List.of(LukutaitokoulutusTavoitealueDto.builder()
                .tavoitteet(new ArrayList<>())
                .build())));
        perusteenOsaService.update(dto);
        em.flush();
        em.clear();

        Validointi validointi = validoi();

        assertThat(validointi.getVirheet())
                .extracting(Validointi.Virhe::getKuvaus)
                .containsExactly("peruste-validointi-lukutaitokoulutus-tavoitealue-otsikko");
    }

    private Validointi validoi() {
        List<Validointi> validoinnit = validatorLukutaitokoulutus.validate(perusteprojektiId, ProjektiTila.JULKAISTU);
        assertThat(validoinnit).hasSize(1);
        return validoinnit.get(0);
    }

    private LokalisoituTekstiDto kaksikielinen(String teksti) {
        return LokalisoituTekstiDto.of(Map.of(Kieli.FI, teksti, Kieli.SV, teksti + " sv"));
    }

    private LukutaitokoulutusTavoitteetJaSisaltoalueetDto lisaaTavoitteetJaSisaltoalueet() {
        PerusteenOsaViite juuri = peruste.getLukutaitokoulutusSisalto().getSisalto();
        PerusteenOsaViiteDto.Matala added = perusteenOsaViiteService.addSisalto(peruste.getId(), juuri.getId(),
                new PerusteenOsaViiteDto.Matala(new LukutaitokoulutusTavoitteetJaSisaltoalueetDto()));
        LukutaitokoulutusTavoitteetJaSisaltoalueetDto dto = (LukutaitokoulutusTavoitteetJaSisaltoalueetDto) perusteenOsaService.get(added.getPerusteenOsa().getId());
        perusteenOsaService.lock(dto.getId());
        return dto;
    }

    private List<LukutaitokoulutusTavoitealueDto> luoTavoitealueet() {
        List<LukutaitokoulutusTavoitealueDto> tavoitealueet = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            tavoitealueet.add(LukutaitokoulutusTavoitealueDto.builder()
                    .nimi(LokalisoituTekstiDto.of("tavoitealue" + i))
                    .tavoitteet(new ArrayList<>(List.of(
                            LokalisoituTekstiDto.of("tavoite" + i + ".1"),
                            LokalisoituTekstiDto.of("tavoite" + i + ".2"))))
                    .build());
        }
        return tavoitealueet;
    }

    private List<String> tavoitealueNimet(LukutaitokoulutusTavoitteetJaSisaltoalueetDto dto) {
        return dto.getTavoitealueet().stream().map(t -> t.getNimi().get(Kieli.FI)).collect(Collectors.toList());
    }

    private List<String> tavoitealueenTavoitteet(LukutaitokoulutusTavoitealueDto tavoitealue) {
        return tavoitealue.getTavoitteet().stream().map(t -> t.get(Kieli.FI)).collect(Collectors.toList());
    }

    private PerusteenOsaViiteDto.Matala lisaaTekstikappale(PerusteenOsaViite vanhempi, String nimi) {
        TekstiKappaleDto tekstikappale = new TekstiKappaleDto();
        tekstikappale.setNimi(LokalisoituTekstiDto.of(nimi));
        PerusteenOsaViiteDto.Matala viiteDto = new PerusteenOsaViiteDto.Matala();
        viiteDto.setPerusteenOsa(tekstikappale);
        return perusteenOsaViiteService.addSisalto(peruste.getId(), vanhempi.getId(), viiteDto);
    }
}

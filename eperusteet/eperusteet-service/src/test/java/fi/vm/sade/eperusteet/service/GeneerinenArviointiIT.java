package fi.vm.sade.eperusteet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.GeneerinenArviointiasteikko;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.Osaamistaso;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.OsaAlue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.OsaAlueTyyppi;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Osaamistavoite;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.TutkinnonOsa;
import fi.vm.sade.eperusteet.dto.Arviointi2020Dto;
import fi.vm.sade.eperusteet.dto.GeneerinenArviointiasteikkoDto;
import fi.vm.sade.eperusteet.dto.GeneerisenArvioinninOsaamistasonKriteeriDto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.arviointi.ArviointiAsteikkoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.*;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.GeneerinenArviointiasteikkoRepository;
import fi.vm.sade.eperusteet.repository.OsaamistasoRepository;
import fi.vm.sade.eperusteet.repository.TutkinnonOsaRepository;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@DirtiesContext
@ActiveProfiles(profiles = {"test", "realPermissions"})
public class GeneerinenArviointiIT extends AbstractPerusteprojektiTest {

    @Autowired
    private GeneerinenArviointiasteikkoService geneerinenArviointiasteikkoService;

    @Autowired
    private GeneerinenArviointiasteikkoRepository geneerinenArviointiasteikkoRepository;

    @Autowired
    private OsaamistasoRepository osaamistasoRepository;

    @Autowired
    private TutkinnonOsaRepository tutkinnonOsaRepository;

    @Test
    @Rollback
    public void testGeneeristenArviointiasteikoidenLisays() {
        ArviointiAsteikkoDto asteikko = addArviointiasteikot();

        Set<GeneerisenArvioinninOsaamistasonKriteeriDto> kriteerit = Stream.of(
                GeneerisenArvioinninOsaamistasonKriteeriDto.builder()
                        .osaamistaso(Reference.of(asteikko.getOsaamistasot().get(0).getId()))
                        .build(),
                GeneerisenArvioinninOsaamistasonKriteeriDto.builder()
                        .osaamistaso(Reference.of(asteikko.getOsaamistasot().get(1).getId()))
                        .kriteerit(Stream.of(
                                LokalisoituTekstiDto.of("a"),
                                LokalisoituTekstiDto.of("b"),
                                LokalisoituTekstiDto.of("c"))
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toSet());

        GeneerinenArviointiasteikkoDto geneerinenDto = GeneerinenArviointiasteikkoDto.builder()
                .arviointiAsteikko(Reference.of(asteikko.getId()))
                .nimi(LokalisoituTekstiDto.of("otsikko"))
                .kohde(LokalisoituTekstiDto.of("otsikko"))
                .osaamistasonKriteerit(kriteerit)
                .koulutustyypit(Sets.newHashSet(KoulutusTyyppi.ERIKOISAMMATTITUTKINTO, KoulutusTyyppi.PERUSTUTKINTO))
                .build();

        GeneerinenArviointiasteikkoDto geneerinen = geneerinenArviointiasteikkoService.add(geneerinenDto);

        assertThat(geneerinen.getId()).isNotNull();
        assertThat(geneerinen.getArviointiAsteikko()).isNotNull();
        assertThat(geneerinen.getKohde()).isNotNull();
        assertThat(geneerinen.getOsaamistasonKriteerit()).hasSize(3);
        assertThat(geneerinen.getKoulutustyypit()).hasSize(2);
        assertThat(geneerinen.getKoulutustyypit()).containsExactlyInAnyOrder(KoulutusTyyppi.ERIKOISAMMATTITUTKINTO, KoulutusTyyppi.PERUSTUTKINTO);

    }

    @Test
    @Rollback
    public void testJulkaisu() {
        GeneerinenArviointiasteikkoDto geneerinenDto = buildGeneerinenArviointiasteikkoDto(0);
        GeneerinenArviointiasteikkoDto geneerinen = geneerinenArviointiasteikkoService.add(geneerinenDto);

        geneerinen.setJulkaistu(true);
        GeneerinenArviointiasteikkoDto julkaistu = geneerinenArviointiasteikkoService.update(geneerinen.getId(), geneerinen);
        assertThat(julkaistu.isJulkaistu()).isTrue();
    }

    @Test
    @Rollback
    public void testPalautaOletusValintaKeskeneraiseksi() {
        loginAsUser("testOphAdmin");
        GeneerinenArviointiasteikkoDto geneerinenDto = buildGeneerinenArviointiasteikkoDto(0);
        geneerinenDto.setJulkaistu(true);
        geneerinenDto.setOletusvalinta(true);
        GeneerinenArviointiasteikkoDto geneerinen = geneerinenArviointiasteikkoService.add(geneerinenDto);
        assertThat(geneerinen.isJulkaistu()).isTrue();

        geneerinen.setJulkaistu(false);
        GeneerinenArviointiasteikkoDto julkaistu = geneerinenArviointiasteikkoService.update(geneerinen.getId(), geneerinen);
        assertThat(julkaistu.isOletusvalinta()).isFalse();
    }

    @Test
    @Rollback
    public void testAsetaOletukseksi() {
        loginAsUser("testOphAdmin");
        GeneerinenArviointiasteikkoDto oletusGeneerinenDto = buildGeneerinenArviointiasteikkoDto(0);
        oletusGeneerinenDto.setJulkaistu(true);
        oletusGeneerinenDto.setOletusvalinta(true);
        GeneerinenArviointiasteikkoDto toBeOletusGeneerinenDto = buildGeneerinenArviointiasteikkoDto(0);
        toBeOletusGeneerinenDto.setJulkaistu(true);

        GeneerinenArviointiasteikkoDto oletusGeneerinen = geneerinenArviointiasteikkoService.add(oletusGeneerinenDto);
        GeneerinenArviointiasteikkoDto toBeOletusGeneerinen = geneerinenArviointiasteikkoService.add(toBeOletusGeneerinenDto);
        assertThat(oletusGeneerinen.isOletusvalinta()).isTrue();
        assertThat(toBeOletusGeneerinen.isOletusvalinta()).isFalse();

        toBeOletusGeneerinen.setOletusvalinta(true);
        GeneerinenArviointiasteikkoDto newOletus = geneerinenArviointiasteikkoService.update(toBeOletusGeneerinen.getId(), toBeOletusGeneerinen);
        assertThat(newOletus.isOletusvalinta()).isTrue();

        GeneerinenArviointiasteikkoDto oldOletus = geneerinenArviointiasteikkoService.getOne(oletusGeneerinen.getId());
        assertThat(oldOletus.isOletusvalinta()).isFalse();
    }

    @Test
    @Rollback
    public void testJulkaistun_muokkaus() {
        loginAsUser("testOphAdmin");

        GeneerinenArviointiasteikkoDto geneerinenDto = buildGeneerinenArviointiasteikkoDto(0);
        GeneerinenArviointiasteikkoDto geneerinen = geneerinenArviointiasteikkoService.add(geneerinenDto);

        geneerinen.setJulkaistu(true);
        GeneerinenArviointiasteikkoDto julkaistu = geneerinenArviointiasteikkoService.update(geneerinen.getId(), geneerinen);

        assertThat(julkaistu.getOsaamistasonKriteerit().stream()
                .noneMatch(osaamistasonKriteeri -> osaamistasonKriteeri.getKriteerit().stream()
                        .anyMatch(kriteeri -> kriteeri.containsKey(Kieli.SV)))).isTrue();

        julkaistu.setOsaamistasonKriteerit(julkaistu.getOsaamistasonKriteerit().stream().map(osaamistasonKriteeri -> {
            osaamistasonKriteeri.setKriteerit(osaamistasonKriteeri.getKriteerit().stream().map(kriteeri -> {
                kriteeri.add(Kieli.SV, "ruotsiksi");
                return kriteeri;
            }).collect(Collectors.toList()));
            return osaamistasonKriteeri;
        }).collect(Collectors.toSet()));

        GeneerinenArviointiasteikkoDto paivitettyJulkaistu = geneerinenArviointiasteikkoService.update(julkaistu.getId(), julkaistu);

        assertThat(paivitettyJulkaistu.getOsaamistasonKriteerit().stream()
                .noneMatch(osaamistasonKriteeri -> osaamistasonKriteeri.getKriteerit().stream()
                        .anyMatch(kriteeri -> kriteeri.containsKey(Kieli.SV)))).isFalse();

        loginAsUser("test");

        assertThatThrownBy(() -> geneerinenArviointiasteikkoService.update(paivitettyJulkaistu.getId(), paivitettyJulkaistu))
                .hasMessage("julkaistua-ei-voi-rakenteellisesti-muuttaa");
    }

    @Test
    @Rollback
    public void testJulkaistun_muokkaus_rakenneVirhe() {
        loginAsUser("testOphAdmin");

        GeneerinenArviointiasteikkoDto geneerinenDto = buildGeneerinenArviointiasteikkoDto(0);
        GeneerinenArviointiasteikkoDto geneerinen = geneerinenArviointiasteikkoService.add(geneerinenDto);

        geneerinen.setJulkaistu(true);
        geneerinenArviointiasteikkoService.update(geneerinen.getId(), geneerinen);

        {
            GeneerinenArviointiasteikkoDto virheellinen = geneerinenArviointiasteikkoService.getOne(geneerinen.getId());
            ArviointiAsteikkoDto arviointiasteikko = addArviointiasteikot();
            virheellinen.setArviointiAsteikko(Reference.of(arviointiasteikko.getId()));

            assertThatThrownBy(() -> geneerinenArviointiasteikkoService.update(virheellinen.getId(), virheellinen))
                    .hasMessage("julkaistua-ei-voi-rakenteellisesti-muuttaa");
        }

        {
            GeneerinenArviointiasteikkoDto virheellinen = geneerinenArviointiasteikkoService.getOne(geneerinen.getId());
            virheellinen.setOsaamistasonKriteerit(virheellinen.getOsaamistasonKriteerit().stream().skip(2).collect(Collectors.toSet()));

            assertThatThrownBy(() -> geneerinenArviointiasteikkoService.update(virheellinen.getId(), virheellinen))
                    .hasMessage("julkaistua-ei-voi-rakenteellisesti-muuttaa");
        }

        {
            GeneerinenArviointiasteikkoDto virheellinen = geneerinenArviointiasteikkoService.getOne(geneerinen.getId());
            virheellinen.setOsaamistasonKriteerit(virheellinen.getOsaamistasonKriteerit().stream().map(osaamistasonKriteeri -> {
                osaamistasonKriteeri.setKriteerit(osaamistasonKriteeri.getKriteerit().stream().skip(2).collect(Collectors.toList()));
                return osaamistasonKriteeri;
            }).collect(Collectors.toSet()));

            assertThatThrownBy(() -> geneerinenArviointiasteikkoService.update(virheellinen.getId(), virheellinen))
                    .hasMessage("julkaistua-ei-voi-rakenteellisesti-muuttaa");
        }

    }

    @Test
    @Rollback
    public void testValittavissa() {
        GeneerinenArviointiasteikkoDto geneerinenDto = buildGeneerinenArviointiasteikkoDto(0);
        GeneerinenArviointiasteikkoDto geneerinen = geneerinenArviointiasteikkoService.add(geneerinenDto);

        assertThat(geneerinen.isValittavissa()).isFalse();

        geneerinen.setValittavissa(true);
        GeneerinenArviointiasteikkoDto valittavissa = geneerinenArviointiasteikkoService.update(geneerinen.getId(), geneerinen);
        assertThat(valittavissa.isValittavissa()).isTrue();
    }

    @Test
    @Rollback
    public void testPoisto() {
        loginAsUser("test");

        geneerinenArviointiasteikkoService.add(buildGeneerinenArviointiasteikkoDto(0));
        GeneerinenArviointiasteikkoDto keskenerainen = geneerinenArviointiasteikkoService.add(buildGeneerinenArviointiasteikkoDto(10));
        GeneerinenArviointiasteikkoDto julkaistu = geneerinenArviointiasteikkoService.add(buildGeneerinenArviointiasteikkoDto(20));
        julkaistu.setJulkaistu(true);
        julkaistu = geneerinenArviointiasteikkoService.update(julkaistu.getId(), julkaistu);
        assertThat(julkaistu.isJulkaistu()).isTrue();

        geneerinenArviointiasteikkoService.remove(keskenerainen.getId());
        geneerinenArviointiasteikkoService.remove(julkaistu.getId());

        assertThat(geneerinenArviointiasteikkoRepository.findOne(keskenerainen.getId())).isNull();
        assertThat(geneerinenArviointiasteikkoRepository.findOne(julkaistu.getId())).isNull();
        assertThat(geneerinenArviointiasteikkoRepository.count()).isEqualTo(1);
    }

    @Test
    @Rollback
    @SuppressWarnings("unchecked")
    public void testPoisto_kaytossaTutkinnonOsassa() {
        loginAsUser("test");

        GeneerinenArviointiasteikkoDto geneerinen = geneerinenArviointiasteikkoService.add(buildGeneerinenArviointiasteikkoDto(0));

        TutkinnonOsa tosa = new TutkinnonOsa();
        tosa.setNimi(TekstiPalanen.of(Kieli.FI, "tutkinnon osa"));
        tosa.setGeneerinenArviointiasteikko(geneerinenArviointiasteikkoRepository.getReferenceById(geneerinen.getId()));
        tosa = tutkinnonOsaRepository.save(tosa);
        Long tosaId = tosa.getId();

        assertThatThrownBy(() -> geneerinenArviointiasteikkoService.remove(geneerinen.getId()))
                .hasMessage("geneerinen-arviointiasteikko-kaytossa-tutkinnon-osissa")
                .isInstanceOfSatisfying(BusinessRuleViolationException.class, e ->
                        assertThat((List<TutkinnonOsaKevytDto>) e.getData())
                                .extracting(TutkinnonOsaKevytDto::getId)
                                .containsExactly(tosaId));

        assertThat(geneerinenArviointiasteikkoRepository.findOne(geneerinen.getId())).isNotNull();
    }

    @Test
    @Rollback
    @SuppressWarnings("unchecked")
    public void testPoisto_kaytossaOsaAlueessa() {
        loginAsUser("test");

        GeneerinenArviointiasteikkoDto geneerinen = geneerinenArviointiasteikkoService.add(buildGeneerinenArviointiasteikkoDto(0));
        GeneerinenArviointiasteikko asteikko = geneerinenArviointiasteikkoRepository.getReferenceById(geneerinen.getId());

        OsaAlue osaAlue1 = new OsaAlue();
        osaAlue1.setTyyppi(OsaAlueTyyppi.OSAALUE2020);
        osaAlue1.setGeneerinenArviointiasteikko(asteikko);
        OsaAlue osaAlue2 = new OsaAlue();
        osaAlue2.setTyyppi(OsaAlueTyyppi.OSAALUE2020);
        osaAlue2.setGeneerinenArviointiasteikko(asteikko);

        TutkinnonOsa tosa = new TutkinnonOsa();
        tosa.setNimi(TekstiPalanen.of(Kieli.FI, "tutkinnon osa"));
        tosa.setOsaAlueet(new ArrayList<>(Arrays.asList(osaAlue1, osaAlue2)));
        tosa = tutkinnonOsaRepository.save(tosa);
        Long tosaId = tosa.getId();

        assertThatThrownBy(() -> geneerinenArviointiasteikkoService.remove(geneerinen.getId()))
                .hasMessage("geneerinen-arviointiasteikko-kaytossa-tutkinnon-osissa")
                .isInstanceOfSatisfying(BusinessRuleViolationException.class, e ->
                        assertThat((List<TutkinnonOsaKevytDto>) e.getData())
                                .extracting(TutkinnonOsaKevytDto::getId)
                                .containsExactly(tosaId));

        assertThat(geneerinenArviointiasteikkoRepository.findOne(geneerinen.getId())).isNotNull();
    }

    @Test
    @Rollback
    public void testKopiointi() {
        ArviointiAsteikkoDto asteikko = addArviointiasteikot();

        Set<GeneerisenArvioinninOsaamistasonKriteeriDto> kriteerit = Stream.of(
                GeneerisenArvioinninOsaamistasonKriteeriDto.builder()
                        .osaamistaso(Reference.of(asteikko.getOsaamistasot().get(0).getId()))
                        .build(),
                GeneerisenArvioinninOsaamistasonKriteeriDto.builder()
                        .osaamistaso(Reference.of(asteikko.getOsaamistasot().get(1).getId()))
                        .kriteerit(Stream.of(
                                LokalisoituTekstiDto.of("a"),
                                LokalisoituTekstiDto.of("b"),
                                LokalisoituTekstiDto.of("c"))
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toSet());

        GeneerinenArviointiasteikkoDto geneerinenDto = GeneerinenArviointiasteikkoDto.builder()
                .arviointiAsteikko(Reference.of(asteikko.getId()))
                .nimi(LokalisoituTekstiDto.of("otsikko"))
                .kohde(LokalisoituTekstiDto.of("otsikko"))
                .osaamistasonKriteerit(kriteerit)
                .build();

        GeneerinenArviointiasteikkoDto geneerinen = geneerinenArviointiasteikkoService.add(geneerinenDto);
        final Long geneerinenId = geneerinen.getId();

        assertThatThrownBy(() -> {
            geneerinenArviointiasteikkoService.kopioi(geneerinenId);
        }).hasMessage("vain-julkaistun-voi-kopioida");

        geneerinen.setJulkaistu(true);
        geneerinen = geneerinenArviointiasteikkoService.update(geneerinen.getId(), geneerinen);

        GeneerinenArviointiasteikkoDto kopio = geneerinenArviointiasteikkoService.kopioi(geneerinen.getId());
        assertThat(kopio.getId()).isNotEqualTo(geneerinen.getId());
        assertThat(kopio.isJulkaistu()).isFalse();
        assertThat(kopio.isValittavissa()).isTrue();
        assertThat(kopio.getOsaamistasonKriteerit()).hasSize(3);
    }


    @SneakyThrows
    @Test
    @Rollback
    public void testOsaamistavoiteMapping() {
        ObjectMapper objectMapper = new ObjectMapper();
        OsaamistavoiteLaajaDto a = new OsaamistavoiteLaajaDto();

//        { // Vanha
//            String str = objectMapper.writeValueAsString(a);
//            OsaamistavoiteDto target = objectMapper.readValue(str, OsaamistavoiteDto.class);
//            assertThat(objectMapper.readTree(str).get("type").asText()).isEqualTo("osaamistavoite2014");
//            assertThat(target).isExactlyInstanceOf(OsaamistavoiteLaajaDto.class);
//        }
    }

    @Test
    @Rollback
    public void testGeneerinenArviointiMapping() {
        GeneerinenArviointiasteikkoDto asteikkoDto = buildGeneerinenArviointiasteikkoDto(10);
        List<Osaamistaso> osaamistasot = osaamistasoRepository.findAll();
        asteikkoDto.setOsaamistasonKriteerit(Stream.of(
                GeneerisenArvioinninOsaamistasonKriteeriDto.builder()
                        .osaamistaso(Reference.of(osaamistasot.get(0).getId()))
                        .kriteerit(Stream.of(
                                LokalisoituTekstiDto.of("x"),
                                LokalisoituTekstiDto.of("y"),
                                LokalisoituTekstiDto.of("z"))
                                .collect(Collectors.toList()))
                        .build(),
                GeneerisenArvioinninOsaamistasonKriteeriDto.builder()
                        .osaamistaso(Reference.of(osaamistasot.get(1).getId()))
                        .kriteerit(Stream.of(
                                LokalisoituTekstiDto.of("a"),
                                LokalisoituTekstiDto.of("b"),
                                LokalisoituTekstiDto.of("c"))
                                .collect(Collectors.toList()))
                        .build(),
                GeneerisenArvioinninOsaamistasonKriteeriDto.builder()
                        .osaamistaso(Reference.of(osaamistasot.get(2).getId()))
                        .build())
                .collect(Collectors.toSet()));

        asteikkoDto = geneerinenArviointiasteikkoService.add(asteikkoDto);
        GeneerinenArviointiasteikko geneerinen = geneerinenArviointiasteikkoRepository.getOne(asteikkoDto.getId());

        { // Arviointi
            Arviointi2020Dto arviointiDto = mapper.map(geneerinen, Arviointi2020Dto.class);
            assertThat(arviointiDto.getOsaamistasonKriteerit().get(0).getOsaamistaso().getOtsikko().get(Kieli.FI))
                    .isEqualTo("Taso 1");
            assertThat(arviointiDto.getOsaamistasonKriteerit().get(0).getKriteerit().get(0).get(Kieli.FI))
                    .isEqualTo("x");
            assertThat(arviointiDto.getOsaamistasonKriteerit().get(2).getOsaamistaso().getOtsikko().get(Kieli.FI))
                    .isEqualTo("Taso 3");
        }

        { // Osaamistavoite
            Osaamistavoite osaamistavoite = new Osaamistavoite();
            Osaamistavoite2020Dto tavoiteDto = mapper.map(osaamistavoite, Osaamistavoite2020Dto.class);
        }

        { // OsaAlue
            OsaAlueLaajaDto oaDto = new OsaAlueLaajaDto();
            Arviointi2020Dto arviointi2020Dto = new Arviointi2020Dto();
            arviointi2020Dto.setId(geneerinen.getId());
            oaDto.setArviointi(arviointi2020Dto);
            OsaAlue oa = mapper.map(oaDto, OsaAlue.class);
            assertThat(oa.getGeneerinenArviointiasteikko()).isNotNull();
        }
    }

    private GeneerinenArviointiasteikkoDto buildGeneerinenArviointiasteikkoDto(long plusId) {
        ArviointiAsteikkoDto asteikko = addArviointiasteikot(plusId);

        Set<GeneerisenArvioinninOsaamistasonKriteeriDto> kriteerit = Stream.of(
                GeneerisenArvioinninOsaamistasonKriteeriDto.builder()
                        .osaamistaso(Reference.of(asteikko.getOsaamistasot().get(0).getId()))
                        .build(),
                GeneerisenArvioinninOsaamistasonKriteeriDto.builder()
                        .osaamistaso(Reference.of(asteikko.getOsaamistasot().get(1).getId()))
                        .kriteerit(Stream.of(
                                LokalisoituTekstiDto.of("a"),
                                LokalisoituTekstiDto.of("b"),
                                LokalisoituTekstiDto.of("c"))
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toSet());

        return GeneerinenArviointiasteikkoDto.builder()
                .arviointiAsteikko(Reference.of(asteikko.getId()))
                .nimi(LokalisoituTekstiDto.of("otsikko"))
                .kohde(LokalisoituTekstiDto.of("otsikko"))
                .osaamistasonKriteerit(kriteerit)
                .build();
    }

}

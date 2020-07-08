package fi.vm.sade.eperusteet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import fi.vm.sade.eperusteet.domain.GeneerinenArviointiasteikko;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.OsaAlue;
import fi.vm.sade.eperusteet.domain.tutkinnonosa.Osaamistavoite;
import fi.vm.sade.eperusteet.dto.Arviointi2020Dto;
import fi.vm.sade.eperusteet.dto.GeneerinenArviointiasteikkoDto;
import fi.vm.sade.eperusteet.dto.GeneerisenArvioinninOsaamistasonKriteeriDto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.arviointi.ArviointiAsteikkoDto;
import fi.vm.sade.eperusteet.dto.tutkinnonosa.*;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.GeneerinenArviointiasteikkoRepository;

import lombok.SneakyThrows;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
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
        TestTransaction.end();
        loginAsUser("test");

        {
            TestTransaction.start();
            TestTransaction.flagForRollback();

            geneerinenArviointiasteikkoService.add(buildGeneerinenArviointiasteikkoDto(0));
            GeneerinenArviointiasteikkoDto geneerinenDto = buildGeneerinenArviointiasteikkoDto(10);
            GeneerinenArviointiasteikkoDto geneerinen = geneerinenArviointiasteikkoService.add(geneerinenDto);

            geneerinenArviointiasteikkoService.remove(geneerinen.getId());
            assertThat(geneerinenArviointiasteikkoRepository.findOne(geneerinen.getId())).isNull();
            assertThat(geneerinenArviointiasteikkoRepository.count()).isEqualTo(1);
            TestTransaction.end();
        }

        {
            TestTransaction.start();
            TestTransaction.flagForCommit();

            geneerinenArviointiasteikkoService.add(buildGeneerinenArviointiasteikkoDto(0));
            geneerinenArviointiasteikkoService.add(buildGeneerinenArviointiasteikkoDto(10));
            GeneerinenArviointiasteikkoDto geneerinenNotDelete = geneerinenArviointiasteikkoService.add(buildGeneerinenArviointiasteikkoDto(20));
            geneerinenNotDelete.setJulkaistu(true);
            geneerinenArviointiasteikkoService.update(geneerinenNotDelete.getId(), geneerinenNotDelete);

            GeneerinenArviointiasteikkoDto geneerinenDto = buildGeneerinenArviointiasteikkoDto(30);
            GeneerinenArviointiasteikkoDto geneerinen = geneerinenArviointiasteikkoService.add(geneerinenDto);

            geneerinen.setJulkaistu(true);
            GeneerinenArviointiasteikkoDto julkaistu = geneerinenArviointiasteikkoService.update(geneerinen.getId(), geneerinen);
            assertThat(julkaistu.isJulkaistu()).isTrue();

            TestTransaction.end(); // commit jotta audit data ilmestyy tauluun

            assertThatThrownBy(() -> {
                geneerinenArviointiasteikkoService.remove(geneerinen.getId());
            }).hasMessage("julkaistua-ei-voi-poistaa");

            assertThat(geneerinenArviointiasteikkoRepository.count()).isEqualTo(4);
        }
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
        asteikkoDto.setOsaamistasonKriteerit(Stream.of(
                GeneerisenArvioinninOsaamistasonKriteeriDto.builder()
                        .osaamistaso(Reference.of(12L))
                        .kriteerit(Stream.of(
                                LokalisoituTekstiDto.of("x"),
                                LokalisoituTekstiDto.of("y"),
                                LokalisoituTekstiDto.of("z"))
                                .collect(Collectors.toList()))
                        .build(),
                GeneerisenArvioinninOsaamistasonKriteeriDto.builder()
                        .osaamistaso(Reference.of(13L))
                        .kriteerit(Stream.of(
                                LokalisoituTekstiDto.of("a"),
                                LokalisoituTekstiDto.of("b"),
                                LokalisoituTekstiDto.of("c"))
                                .collect(Collectors.toList()))
                        .build(),
                GeneerisenArvioinninOsaamistasonKriteeriDto.builder()
                        .osaamistaso(Reference.of(14L))
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

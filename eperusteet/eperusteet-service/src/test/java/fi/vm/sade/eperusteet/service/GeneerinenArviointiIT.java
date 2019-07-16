package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.GeneerinenArviointiasteikkoDto;
import fi.vm.sade.eperusteet.dto.GeneerisenArvioinninOsaamistasonKriteeriDto;
import fi.vm.sade.eperusteet.dto.Reference;
import fi.vm.sade.eperusteet.dto.arviointi.ArviointiAsteikkoDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DirtiesContext
public class GeneerinenArviointiIT extends AbstractPerusteprojektiTest {

    @Autowired
    private GeneerinenArviointiasteikkoService geneerinenArviointiasteikkoService;

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
                .build();

        GeneerinenArviointiasteikkoDto geneerinen = geneerinenArviointiasteikkoService.add(geneerinenDto);

        assertThat(geneerinen.getId()).isNotNull();
        assertThat(geneerinen.getArviointiAsteikko()).isNotNull();
        assertThat(geneerinen.getKohde()).isNotNull();
        assertThat(geneerinen.getOsaamistasonKriteerit()).hasSize(3);
    }

    @Test
    @Rollback
    public void testJulkaisu() {
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

        geneerinen.setJulkaistu(true);
        GeneerinenArviointiasteikkoDto julkaistu = geneerinenArviointiasteikkoService.update(geneerinen.getId(), geneerinen);

        assertThat(julkaistu.isJulkaistu()).isTrue();

        assertThatThrownBy(() -> {
            geneerinenArviointiasteikkoService.update(geneerinen.getId(), geneerinen);
        }).hasMessage("julkaistua-ei-voi-muokata");

        assertThatThrownBy(() -> {
            geneerinenArviointiasteikkoService.remove(geneerinen.getId());
        }).hasMessage("julkaistua-ei-voi-muokata");
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
        assertThat(kopio.getOsaamistasonKriteerit()).hasSize(3);
    }

}

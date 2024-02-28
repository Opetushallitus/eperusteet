package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysLiittyyTyyppi;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysTila;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysTyyppi;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysAsiasanaDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysKevytDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysKieliLiitteetDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysQueryDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.mapping.Dto;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@DirtiesContext
public class MaarayServiceIT extends AbstractIntegrationTest {

    @Autowired
    private MaaraysService maaraysService;

    @Dto
    @Autowired
    private DtoMapper dtoMapper;

    @Test
    public void testAdd() {
        MaaraysDto maaraysDto = maaraysService.addMaarays(createDto().build());
        assertThat(maaraysDto).isNotNull();
        assertThat(maaraysDto.getId()).isNotNull();
    }

    @Test
    public void testFetch() {
        maaraysService.addMaarays(createDto().build());
        maaraysService.addMaarays(createDto()
                .nimi(LokalisoituTekstiDto.of("nimi"))
                .diaarinumero("diaari")
                .tila(MaaraysTila.LUONNOS)
                .tyyppi(MaaraysTyyppi.AMMATILLINEN_MUU)
                .koulutustyypit(Arrays.asList(KoulutusTyyppi.TUTKINTOONVALMENTAVA.toString(), KoulutusTyyppi.PERUSOPETUS.toString()))
                .voimassaoloAlkaa(DateTime.now().minusDays(5).toDate())
                .maarayspvm(new Date())
                .build());
        maaraysService.addMaarays(createDto()
                .nimi(LokalisoituTekstiDto.of("nimi"))
                .diaarinumero("diaari")
                .tila(MaaraysTila.LUONNOS)
                .tyyppi(MaaraysTyyppi.OPETUSHALLITUKSEN_MUU)
                .koulutustyypit(Arrays.asList(KoulutusTyyppi.TUTKINTOONVALMENTAVA.toString()))
                .voimassaoloAlkaa(DateTime.now().plusDays(5).toDate())
                .maarayspvm(new Date())
                .build());
        maaraysService.addMaarays(createDto()
                .nimi(LokalisoituTekstiDto.of("nimi"))
                .diaarinumero("diaari")
                .tila(MaaraysTila.LUONNOS)
                .tyyppi(MaaraysTyyppi.OPETUSHALLITUKSEN_MUU)
                .koulutustyypit(Arrays.asList(KoulutusTyyppi.ESIOPETUS.toString()))
                .voimassaoloAlkaa(DateTime.now().minusDays(6).toDate())
                .maarayspvm(new Date())
                .build());
        maaraysService.addMaarays(createDto()
                .nimi(LokalisoituTekstiDto.of("nimi"))
                .diaarinumero("diaari")
                .tila(MaaraysTila.LUONNOS)
                .tyyppi(MaaraysTyyppi.OPETUSHALLITUKSEN_MUU)
                .koulutustyypit(Arrays.asList(KoulutusTyyppi.ESIOPETUS.toString()))
                .voimassaoloAlkaa(DateTime.now().minusDays(6).toDate())
                .voimassaoloLoppuu(DateTime.now().minusDays(5).toDate())
                .maarayspvm(new Date())
                .tila(MaaraysTila.JULKAISTU)
                .build());
        assertThat(maaraysService.getMaaraykset(new MaaraysQueryDto()).getContent()).hasSize(5);
        assertThat(maaraysService.getMaaraykset(createQuery()
                .tyyppi(MaaraysTyyppi.OPETUSHALLITUKSEN_MUU)
                .build()).getContent())
                .hasSize(3);
        assertThat(maaraysService.getMaaraykset(createQuery()
                .tyyppi(MaaraysTyyppi.OPETUSHALLITUKSEN_MUU)
                .koulutustyypit(List.of(KoulutusTyyppi.ESIOPETUS.toString(), KoulutusTyyppi.ESIOPETUS.toString()))
                .build()).getContent())
                .hasSize(2);
        assertThat(maaraysService.getMaaraykset(createQuery()
                .tyyppi(MaaraysTyyppi.OPETUSHALLITUKSEN_MUU)
                .koulutustyypit(List.of(KoulutusTyyppi.ESIOPETUS.toString()))
                .paattynyt(true)
                .luonnos(false)
                .julkaistu(true)
                .build()).getContent())
                .hasSize(1);

        assertThat(maaraysService.getMaaraykset(createQuery()
                .tyyppi(MaaraysTyyppi.OPETUSHALLITUKSEN_MUU)
                .koulutustyypit(List.of(KoulutusTyyppi.ESIOPETUS.toString()))
                .paattynyt(true)
                .luonnos(true)
                .julkaistu(false)
                .build()).getContent())
                .hasSize(0);

        assertThat(maaraysService.getMaaraykset(createQuery()
                .tyyppi(MaaraysTyyppi.OPETUSHALLITUKSEN_MUU)
                .koulutustyypit(List.of(KoulutusTyyppi.ESIOPETUS.toString()))
                .paattynyt(true)
                .luonnos(true)
                .julkaistu(true)
                .build()).getContent())
                .hasSize(1);

        assertThat(maaraysService.getMaarayksienKoulutustyypit())
                .containsExactlyInAnyOrder(KoulutusTyyppi.TUTKINTOONVALMENTAVA.toString(), KoulutusTyyppi.PERUSOPETUS.toString(), KoulutusTyyppi.ESIOPETUS.toString());
    }

    @Test
    public void testAllAsiasanat() {
        maaraysService.addMaarays(
                createDto()
                        .asiasanat(Map.of(
                                Kieli.FI, MaaraysAsiasanaDto.builder().asiasana(Arrays.asList("sana1", "sana2")).build(),
                                Kieli.SV, MaaraysAsiasanaDto.builder().asiasana(Arrays.asList("sverige")).build()))
                        .build());

        Map<Kieli, List<String>> asiasanat = maaraysService.getAsiasanat();
        assertThat(asiasanat.keySet()).contains(Kieli.FI, Kieli.SV);
        assertThat(asiasanat.get(Kieli.FI)).containsExactlyInAnyOrder("sana1", "sana2");
        assertThat(asiasanat.get(Kieli.SV)).containsExactlyInAnyOrder("sverige");

        assertThat(maaraysService.getMaaraykset(createQuery()
                .nimi("sana1")
                .build()).getContent())
                .hasSize(1);
        assertThat(maaraysService.getMaaraykset(createQuery()
                .nimi("sana3")
                .build()).getContent())
                .hasSize(0);

        assertThat(maaraysService.getMaaraykset(createQuery()
                .nimi("sverige")
                .kieli(Kieli.SV)
                .build()).getContent())
                .hasSize(1);
        assertThat(maaraysService.getMaaraykset(createQuery()
                .nimi("sana1")
                .kieli(Kieli.SV)
                .build()).getContent())
                .hasSize(0);
    }

    @Test
    public void testJarjestys() {
        maaraysService.addMaarays(createDto()
                .nimi(LokalisoituTekstiDto.of("nimi1"))
                .voimassaoloAlkaa(DateTime.now().toDate())
                .build());

        maaraysService.addMaarays(createDto()
                .nimi(LokalisoituTekstiDto.of("nimi-2"))
                .voimassaoloAlkaa(DateTime.now().minusDays(2).toDate())
                .build());

        maaraysService.addMaarays(createDto()
                .nimi(LokalisoituTekstiDto.of("nimi3"))
                .voimassaoloAlkaa(DateTime.now().plusDays(1).toDate())
                .build());

        List<MaaraysDto> maaraykset = maaraysService.getMaaraykset(createQuery()
                        .jarjestys(Sort.Direction.DESC)
                        .jarjestysTapa("voimassaoloAlkaa")
                .build()).getContent();

        assertThat(maaraykset.get(0).getNimi().get(Kieli.FI)).isEqualTo("nimi3");
        assertThat(maaraykset.get(1).getNimi().get(Kieli.FI)).isEqualTo("nimi1");
        assertThat(maaraykset.get(2).getNimi().get(Kieli.FI)).isEqualTo("nimi-2");
    }

    @Test
    public void testKorvaavatMuuttavatMaaraykset() {
        MaaraysDto maarayskorvaava = maaraysService.addMaarays(createDto().build());
        MaaraysDto maaraysmuuttava = maaraysService.addMaarays(createDto().build());
        MaaraysDto maarays = maaraysService.addMaarays(createDto().build());

        maarayskorvaava.setKorvattavatMaaraykset(Collections.singletonList(dtoMapper.map(maarays, MaaraysKevytDto.class)));
        maaraysmuuttava.setMuutettavatMaaraykset(Collections.singletonList(dtoMapper.map(maarays, MaaraysKevytDto.class)));
        maarayskorvaava = maaraysService.updateMaarays(maarayskorvaava);
        maaraysmuuttava = maaraysService.updateMaarays(maaraysmuuttava);

        assertThat(maarayskorvaava.getKorvattavatMaaraykset().get(0).getId()).isEqualTo(maarays.getId());
        assertThat(maaraysmuuttava.getMuutettavatMaaraykset().get(0).getId()).isEqualTo(maarays.getId());

        maarays = maaraysService.getMaarays(maarays.getId());
        assertThat(maarays.getKorvaavatMaaraykset().get(0).getId()).isEqualTo(maarayskorvaava.getId());
        assertThat(maarays.getMuuttavatMaaraykset().get(0).getId()).isEqualTo(maaraysmuuttava.getId());
    }

    private MaaraysDto.MaaraysDtoBuilder createDto() {
        return MaaraysDto.builder()
                .nimi(LokalisoituTekstiDto.of(Map.of(Kieli.FI, "nimi", Kieli.SV, "nimi")))
                .diaarinumero("diaari")
                .tila(MaaraysTila.LUONNOS)
                .liittyyTyyppi(MaaraysLiittyyTyyppi.EI_LIITY)
                .tyyppi(MaaraysTyyppi.AMMATILLINEN_MUU)
                .koulutustyypit(Arrays.asList(KoulutusTyyppi.TUTKINTOONVALMENTAVA.toString()))
                .voimassaoloAlkaa(new Date())
                .maarayspvm(new Date())
                .liitteet(Map.of(Kieli.FI, new MaaraysKieliLiitteetDto()))
                .asiasanat(Map.of(Kieli.FI, new MaaraysAsiasanaDto()));
    }

    private MaaraysQueryDto.MaaraysQueryDtoBuilder createQuery() {
        return MaaraysQueryDto.builder()
                .kieli(Kieli.FI)
                .jarjestysTapa("nimi")
                .jarjestys(Sort.Direction.ASC)
                .sivu(0)
                .sivukoko(10);
    }
}

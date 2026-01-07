package fi.vm.sade.eperusteet.service.impl.validators;

import fi.vm.sade.eperusteet.domain.Diaarinumero;
import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.KoulutustyyppiToteutus;
import fi.vm.sade.eperusteet.domain.Maarayskirje;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteTyyppi;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.TekstiPalanen;
import fi.vm.sade.eperusteet.domain.liite.Liite;
import fi.vm.sade.eperusteet.domain.maarays.Maarays;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysLiiteTyyppi;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysKieliLiitteetDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysLiiteDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.MaaraysService;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.util.Validointi;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class ValidatorPerusteTiedotTest {

    @Mock
    private PerusteprojektiRepository perusteprojektiRepository;

    @Mock
    private DtoMapper mapper;

    @Mock
    private MaaraysService maaraysService;

    @InjectMocks
    private ValidatorPerusteTiedot validator;

    private Perusteprojekti perusteprojekti;
    private Peruste peruste;

    @Before
    public void setup() {
        perusteprojekti = new Perusteprojekti();
        peruste = new Peruste();
        perusteprojekti.setPeruste(peruste);
        peruste.asetaTila(PerusteTila.VALMIS);
        peruste.setId(1L);
        peruste.setKielet(Set.of(Kieli.FI));
        peruste.setKoulutustyyppi(KoulutusTyyppi.ESIOPETUS.toString());
        peruste.setTyyppi(PerusteTyyppi.NORMAALI);

        Mockito.when(perusteprojektiRepository.findById(Mockito.any())).thenReturn(Optional.of(perusteprojekti));
        Mockito.when(maaraysService.getPerusteenMaarays(Mockito.any())).thenReturn(null);
        Mockito.when(maaraysService.getPerusteenMuutosmaaraykset(Mockito.any())).thenReturn(List.of());
    }

    @Test
    public void testValidateWithValidPeruste() {
        peruste.setVoimassaoloAlkaa(new Date());
        peruste.setDiaarinumero(new Diaarinumero("123/456/2023"));

        List<Validointi> validoinnit = validator.validate(1L, ProjektiTila.JULKAISTU);

        List<String> virheet = validoinnit.stream()
                .map(Validointi::getVirheet)
                .flatMap(Collection::stream)
                .map(Validointi.Virhe::getKuvaus)
                .collect(Collectors.toList());

        Assertions.assertThat(virheet).isEmpty();
    }

    @Test
    public void testValidateWithMissingVoimassaoloAlkaa() {
        peruste.setVoimassaoloAlkaa(null);
        peruste.setDiaarinumero(new Diaarinumero("123/456/2023"));

        List<Validointi> validoinnit = validator.validate(1L, ProjektiTila.JULKAISTU);

        List<String> virheet = validoinnit.stream()
                .map(Validointi::getVirheet)
                .flatMap(Collection::stream)
                .map(Validointi.Virhe::getKuvaus)
                .collect(Collectors.toList());

        Assertions.assertThat(virheet).contains("peruste-ei-voimassaolon-alkamisaikaa");
    }

    @Test
    public void testValidateWithMissingDiaarinumero() {
        peruste.setVoimassaoloAlkaa(new Date());
        peruste.setDiaarinumero(null);

        List<Validointi> validoinnit = validator.validate(1L, ProjektiTila.JULKAISTU);

        List<String> virheet = validoinnit.stream()
                .map(Validointi::getVirheet)
                .flatMap(Collection::stream)
                .map(Validointi.Virhe::getKuvaus)
                .collect(Collectors.toList());

        Assertions.assertThat(virheet).contains("peruste-ei-diaarinumeroa");
    }

    @Test
    public void testValidateWithBlankDiaarinumero() {
        peruste.setVoimassaoloAlkaa(new Date());
        peruste.setDiaarinumero(new Diaarinumero(""));

        List<Validointi> validoinnit = validator.validate(1L, ProjektiTila.JULKAISTU);

        List<String> virheet = validoinnit.stream()
                .map(Validointi::getVirheet)
                .flatMap(Collection::stream)
                .map(Validointi.Virhe::getKuvaus)
                .collect(Collectors.toList());

        Assertions.assertThat(virheet).contains("peruste-ei-diaarinumeroa");
    }

    @Test
    public void testValidateWithInvalidDiaarinumero() {
        peruste.setVoimassaoloAlkaa(new Date());
        peruste.setDiaarinumero(new Diaarinumero("invalid-format"));

        List<Validointi> validoinnit = validator.validate(1L, ProjektiTila.JULKAISTU);

        List<String> virheet = validoinnit.stream()
                .map(Validointi::getVirheet)
                .flatMap(Collection::stream)
                .map(Validointi.Virhe::getKuvaus)
                .collect(Collectors.toList());

        Assertions.assertThat(virheet).contains("diaarinumero-ei-validi");
    }

    @Test
    public void testValidateWithMaaraysWithoutKuvaus() {
        peruste.setVoimassaoloAlkaa(new Date());
        peruste.setDiaarinumero(new Diaarinumero("123/456/2023"));
        
        MaaraysDto maaraysDto = MaaraysDto.builder()
                .kuvaus(null)
                .build();
        
        Maarays maarays = new Maarays();
        maarays.setKuvaus(null);

        Mockito.when(maaraysService.getPerusteenMaarays(Mockito.any())).thenReturn(maaraysDto);
        Mockito.when(mapper.map(Mockito.any(), Mockito.eq(Maarays.class))).thenReturn(maarays);

        List<Validointi> validoinnit = validator.validate(1L, ProjektiTila.JULKAISTU);

        List<String> virheet = validoinnit.stream()
                .map(Validointi::getVirheet)
                .flatMap(Collection::stream)
                .map(Validointi.Virhe::getKuvaus)
                .collect(Collectors.toList());

        Assertions.assertThat(virheet).isNotEmpty();
    }

    @Test
    public void testValidateWithMaaraysWithoutDokumentti() {
        peruste.setVoimassaoloAlkaa(new Date());
        peruste.setDiaarinumero(new Diaarinumero("123/456/2023"));
        
        Map<Kieli, String> tekstit = new HashMap<>();
        tekstit.put(Kieli.FI, "Kuvaus");
        TekstiPalanen kuvaus = TekstiPalanen.of(tekstit, null);
        
        MaaraysDto maaraysDto = MaaraysDto.builder().build();
        Maarays maarays = new Maarays();
        maarays.setKuvaus(kuvaus);

        Mockito.when(maaraysService.getPerusteenMaarays(Mockito.any())).thenReturn(maaraysDto);
        Mockito.when(mapper.map(Mockito.any(), Mockito.eq(Maarays.class))).thenReturn(maarays);

        List<Validointi> validoinnit = validator.validate(1L, ProjektiTila.JULKAISTU);

        List<String> virheet = validoinnit.stream()
                .map(Validointi::getVirheet)
                .flatMap(Collection::stream)
                .map(Validointi.Virhe::getKuvaus)
                .collect(Collectors.toList());

        Assertions.assertThat(virheet).contains("peruste-validointi-maarays-dokumentti");
    }

    @Test
    public void testValidateWithMaaraysWithDokumentti() {
        peruste.setVoimassaoloAlkaa(new Date());
        peruste.setDiaarinumero(new Diaarinumero("123/456/2023"));
        
        Map<Kieli, String> tekstit = new HashMap<>();
        tekstit.put(Kieli.FI, "Kuvaus");
        TekstiPalanen kuvaus = TekstiPalanen.of(tekstit, null);
        
        Maarayskirje maarayskirje = new Maarayskirje();
        Map<Kieli, Liite> liitteet = new HashMap<>();
        Liite liite = Mockito.mock(Liite.class);
        liitteet.put(Kieli.FI, liite);
        maarayskirje.setLiitteet(liitteet);
        peruste.setMaarayskirje(maarayskirje);
        
        MaaraysDto maaraysDto = MaaraysDto.builder().build();
        Maarays maarays = new Maarays();
        maarays.setKuvaus(kuvaus);

        Mockito.when(maaraysService.getPerusteenMaarays(Mockito.any())).thenReturn(maaraysDto);
        Mockito.when(mapper.map(Mockito.any(), Mockito.eq(Maarays.class))).thenReturn(maarays);

        List<Validointi> validoinnit = validator.validate(1L, ProjektiTila.JULKAISTU);

        List<String> virheet = validoinnit.stream()
                .map(Validointi::getVirheet)
                .flatMap(Collection::stream)
                .map(Validointi.Virhe::getKuvaus)
                .collect(Collectors.toList());

        Assertions.assertThat(virheet).doesNotContain("peruste-validointi-maarays-dokumentti");
    }

    @Test
    public void testValidateWithMuutosmaarayksetOk() {
        peruste.setVoimassaoloAlkaa(new Date());
        peruste.setDiaarinumero(new Diaarinumero("123/456/2023"));

        mockMuutosmaarayksetWithDokumentti(Kieli.FI);

        List<Validointi> validoinnit = validator.validate(1L, ProjektiTila.JULKAISTU);

        List<String> huomautukset = validoinnit.stream()
                .map(Validointi::getHuomautukset)
                .flatMap(Collection::stream)
                .map(Validointi.Virhe::getKuvaus)
                .collect(Collectors.toList());

        Assertions.assertThat(huomautukset).doesNotContain("peruste-validointi-muutosmaarays-dokumentti-kieli-puute");
    }

    @Test
    public void testValidateWithMuutosmaarayksetMissingDokumentti() {
        peruste.setVoimassaoloAlkaa(new Date());
        peruste.setDiaarinumero(new Diaarinumero("123/456/2023"));

        mockMuutosmaarayksetWithoutDokumentti();

        List<Validointi> validoinnit = validator.validate(1L, ProjektiTila.JULKAISTU);

        List<String> huomautukset = validoinnit.stream()
                .map(Validointi::getHuomautukset)
                .flatMap(Collection::stream)
                .map(Validointi.Virhe::getKuvaus)
                .collect(Collectors.toList());

        Assertions.assertThat(huomautukset).contains("peruste-validointi-muutosmaarays-dokumentti-kieli-puute");
    }

    @Test
    public void testValidateWithMuutosmaarayksetWrongLanguage() {
        peruste.setVoimassaoloAlkaa(new Date());
        peruste.setDiaarinumero(new Diaarinumero("123/456/2023"));
        peruste.setKielet(Set.of(Kieli.FI, Kieli.SV));

        mockMuutosmaarayksetWithDokumentti(Kieli.FI);

        List<Validointi> validoinnit = validator.validate(1L, ProjektiTila.JULKAISTU);

        List<String> huomautukset = validoinnit.stream()
                .map(Validointi::getHuomautukset)
                .flatMap(Collection::stream)
                .map(Validointi.Virhe::getKuvaus)
                .collect(Collectors.toList());

        Assertions.assertThat(huomautukset).contains("peruste-validointi-muutosmaarays-dokumentti-kieli-puute");
    }

    @Test
    public void testIsDiaariValidWithNull() {
        Assertions.assertThat(validator.isDiaariValid(null)).isTrue();
    }

    @Test
    public void testIsDiaariValidWithNullString() {
        Diaarinumero diaari = new Diaarinumero(null);
        Assertions.assertThat(validator.isDiaariValid(diaari)).isTrue();
    }

    @Test
    public void testIsDiaariValidWithEmptyString() {
        Diaarinumero diaari = new Diaarinumero("");
        Assertions.assertThat(validator.isDiaariValid(diaari)).isTrue();
    }

    @Test
    public void testIsDiaariValidWithAmosaaYhteiset() {
        Diaarinumero diaari = new Diaarinumero("amosaa/yhteiset");
        Assertions.assertThat(validator.isDiaariValid(diaari)).isTrue();
    }

    @Test
    public void testIsDiaariValidWithValidFormat1() {
        Diaarinumero diaari = new Diaarinumero("123/456/2023");
        Assertions.assertThat(validator.isDiaariValid(diaari)).isTrue();
    }

    @Test
    public void testIsDiaariValidWithValidFormat2() {
        Diaarinumero diaari = new Diaarinumero("1/001/2020");
        Assertions.assertThat(validator.isDiaariValid(diaari)).isTrue();
    }

    @Test
    public void testIsDiaariValidWithValidOPHFormat() {
        Diaarinumero diaari = new Diaarinumero("OPH-12345-2023");
        Assertions.assertThat(validator.isDiaariValid(diaari)).isTrue();
    }

    @Test
    public void testIsDiaariValidWithValidOPHFormat2() {
        Diaarinumero diaari = new Diaarinumero("OPH-1-2020");
        Assertions.assertThat(validator.isDiaariValid(diaari)).isTrue();
    }

    @Test
    public void testIsDiaariValidWithInvalidFormat() {
        Diaarinumero diaari = new Diaarinumero("invalid-format");
        Assertions.assertThat(validator.isDiaariValid(diaari)).isFalse();
    }

    @Test
    public void testIsDiaariValidWithInvalidFormat2() {
        Diaarinumero diaari = new Diaarinumero("123-456-2023");
        Assertions.assertThat(validator.isDiaariValid(diaari)).isFalse();
    }

    @Test
    public void testIsDiaariValidWithInvalidFormat3() {
        Diaarinumero diaari = new Diaarinumero("1234/456/2023");
        Assertions.assertThat(validator.isDiaariValid(diaari)).isFalse();
    }

    @Test
    public void testIsDiaariValidWithInvalidOPHFormat() {
        Diaarinumero diaari = new Diaarinumero("OPH-123456-2023");
        Assertions.assertThat(validator.isDiaariValid(diaari)).isFalse();
    }

    @Test
    public void testApplicablePerustetyyppiWithNormaali() {
        Assertions.assertThat(validator.applicablePerustetyyppi(PerusteTyyppi.NORMAALI)).isTrue();
    }

    @Test
    public void testApplicablePerustetyyppiWithOpas() {
        Assertions.assertThat(validator.applicablePerustetyyppi(PerusteTyyppi.OPAS)).isFalse();
    }

    @Test
    public void testApplicableKoulutustyyppi() {
        Assertions.assertThat(validator.applicableKoulutustyyppi(KoulutusTyyppi.ESIOPETUS)).isTrue();
        Assertions.assertThat(validator.applicableKoulutustyyppi(KoulutusTyyppi.PERUSOPETUS)).isTrue();
    }

    @Test
    public void testApplicableToteutus() {
        Assertions.assertThat(validator.applicableToteutus(KoulutustyyppiToteutus.YKSINKERTAINEN)).isTrue();
        Assertions.assertThat(validator.applicableToteutus(KoulutustyyppiToteutus.LOPS2019)).isTrue();
    }

    private void mockMuutosmaarayksetWithDokumentti(Kieli kieli) {
        Mockito.when(maaraysService.getPerusteenMuutosmaaraykset(Mockito.any())).thenReturn(List.of(
                MaaraysDto.builder()
                        .liitteet(Map.of(kieli, MaaraysKieliLiitteetDto.builder()
                                .liitteet(List.of(MaaraysLiiteDto.builder()
                                        .tyyppi(MaaraysLiiteTyyppi.MAARAYSDOKUMENTTI)
                                        .build()))
                                .build()))
                        .build()
        ));
    }

    private void mockMuutosmaarayksetWithoutDokumentti() {
        Mockito.when(maaraysService.getPerusteenMuutosmaaraykset(Mockito.any())).thenReturn(List.of(
                MaaraysDto.builder()
                        .liitteet(Map.of())
                        .build()
        ));
    }
}


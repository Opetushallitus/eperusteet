package fi.vm.sade.eperusteet.service.impl.validators;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.domain.KoulutusTyyppi;
import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.domain.ProjektiTila;
import fi.vm.sade.eperusteet.domain.maarays.MaaraysLiiteTyyppi;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysKieliLiitteetDto;
import fi.vm.sade.eperusteet.dto.maarays.MaaraysLiiteDto;
import fi.vm.sade.eperusteet.dto.peruste.KVLiiteTasoDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.MaaraysService;
import fi.vm.sade.eperusteet.service.PerusteService;
import fi.vm.sade.eperusteet.service.TutkintonimikeKoodiService;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class ValidatorMaaraysTest {

    @Mock
    private PerusteRepository perusteRepository;

    @Mock
    private PerusteService perusteService;

    @Mock
    private PerusteprojektiRepository perusteprojektiRepository;

    @Mock
    private DtoMapper mapper;

    @Mock
    private TutkintonimikeKoodiService tutkintonimikeKoodiService;

    @Mock
    private MaaraysService maaraysService;

    @InjectMocks
    ValidatorPerusteTiedot validatorPeruste = new ValidatorPerusteTiedot();

    @Before
    public void setup() {
        initPerusteprojekti(Set.of(Kieli.FI));
    }

    @Test
    public void testEiMuutosmaarayksia() {
        Assertions.assertThat(validatorPeruste.validate(null, ProjektiTila.JULKAISTU).stream().map(Validointi::getHuomautukset).flatMap(Collection::stream).collect(Collectors.toList())).isEmpty();
        Mockito.verify(maaraysService).getPerusteenMuutosmaaraykset(Mockito.any());
    }

    @Test
    public void testMuutosmaarayksetOk() {
        mockitoMuutosmaaraysFI(Kieli.FI);

        Assertions.assertThat(validatorPeruste.validate(null, ProjektiTila.JULKAISTU).stream().map(Validointi::getHuomautukset).flatMap(Collection::stream).collect(Collectors.toList())).isEmpty();
        Mockito.verify(maaraysService).getPerusteenMuutosmaaraykset(Mockito.any());
    }

    @Test
    public void testMuutosmaarayksetEiOk() {
        mockitoMuutosmaaraysFI(Kieli.SV);

        Assertions.assertThat(validatorPeruste.validate(null, ProjektiTila.JULKAISTU).stream().map(Validointi::getHuomautukset).flatMap(Collection::stream).collect(Collectors.toList())).isNotEmpty();
        Mockito.verify(maaraysService).getPerusteenMuutosmaaraykset(Mockito.any());
    }

    @Test
    public void testMuutosmaarayksetEiOkFISV() {
        initPerusteprojekti(Set.of(Kieli.FI, Kieli.SV));
        mockitoMuutosmaaraysFI(Kieli.FI);

        Assertions.assertThat(validatorPeruste.validate(null, ProjektiTila.JULKAISTU).stream().map(Validointi::getHuomautukset).flatMap(Collection::stream).collect(Collectors.toList())).isNotEmpty();
        Mockito.verify(maaraysService).getPerusteenMuutosmaaraykset(Mockito.any());
    }

    private void mockitoMuutosmaaraysFI(Kieli kieli) {
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

    private void initPerusteprojekti(Set<Kieli> kielet) {
        Perusteprojekti projekti = new Perusteprojekti();
        Peruste peruste = new Peruste();
        projekti.setPeruste(peruste);
        peruste.asetaTila(PerusteTila.VALMIS);
        peruste.setId(1l);
        peruste.setKielet(kielet);
        peruste.setKoulutustyyppi(KoulutusTyyppi.ESIOPETUS.toString());
        Mockito.when(perusteprojektiRepository.findById(Mockito.any())).thenReturn(Optional.of(projekti));
        Mockito.when(maaraysService.getPerusteenMuutosmaaraykset(Mockito.any())).thenReturn(List.of());
    }
}

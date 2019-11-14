package fi.vm.sade.eperusteet.service.impl.validators;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.dto.peruste.KVLiiteTasoDto;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.service.PerusteService;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

@RunWith(MockitoJUnitRunner.class)
public class ValidatorKvLiiteTasoTest {

    @Mock
    private PerusteRepository perusteRepository;

    @Mock
    private PerusteService perusteService;

    @InjectMocks
    ValidatorKvliiteTaso validatorKvliiteTaso = new ValidatorKvliiteTaso();

    @Before
    public void setup() {
        Mockito.when(perusteRepository.findByPerusteprojektiId(Mockito.any())).thenReturn(new Peruste());
    }

    @Test
    public void testKaikkiLoyty_one() {
        Mockito.when(perusteService.haeTasot(Mockito.any(), Mockito.any())).thenReturn(buildTasot("isced_xxxxxxxxx"));
        Assertions.assertThat(validatorKvliiteTaso.validate(null, null).getInfot()).isNotEmpty();
    }

    @Test
    public void testKaikkiLoyty_two() {
        Mockito.when(perusteService.haeTasot(Mockito.any(), Mockito.any())).thenReturn(buildTasot("isced_xxxxxxxxx", "nqf_xxxxx"));
        Assertions.assertThat(validatorKvliiteTaso.validate(null, null).getInfot()).isNotEmpty();
    }

    @Test
    public void testKaikkiLoyty_all() {
        Mockito.when(perusteService.haeTasot(Mockito.any(), Mockito.any())).thenReturn(buildTasot("eqf", "nqf", "isced"));
        Assertions.assertThat(validatorKvliiteTaso.validate(null, null).getInfot()).isEmpty();
    }

    @Test
    public void testKaikkiLoyty_multi() {
        Mockito.when(perusteService.haeTasot(Mockito.any(), Mockito.any())).thenReturn(
                buildTasot("nqf123213", "eqf12332", "isced1313", "nqf3232", "eqf32323", "isced12", "nqf32321", "eqf1233", "isced3232", "nqf123", "eqf2131", "isced3231"));
        Assertions.assertThat(validatorKvliiteTaso.validate(null, null).getInfot()).isEmpty();
    }

    @Test
    public void testKaikkiLoyty_multi_different() {
        Mockito.when(perusteService.haeTasot(Mockito.any(), Mockito.any())).thenReturn(
                buildTasot("XXX", "XXXX", "isced", "nqf", "XXXeqf", "XXd", "XXX", "XXXX", "XXX", "XXXX", "eqf", "XXXX"));
        Assertions.assertThat(validatorKvliiteTaso.validate(null, null).getInfot()).isEmpty();
    }

    @Test
    public void testKaikkiLoyty_multi_not_found() {
        Mockito.when(perusteService.haeTasot(Mockito.any(), Mockito.any())).thenReturn(
                buildTasot("XXX", "XXXX", "XXXX", "nqf", "XXXeqf", "XXd", "XXX", "XXXX", "XXX", "XXXX", "eqf", "XXXX"));
        Assertions.assertThat(validatorKvliiteTaso.validate(null, null).getInfot()).isNotEmpty();
    }

    private List<KVLiiteTasoDto> buildTasot(String... codes) {
        return Arrays.asList(codes).stream().map((code -> KVLiiteTasoDto.builder().codeUri(code).build())).collect(Collectors.toList());
    }

}


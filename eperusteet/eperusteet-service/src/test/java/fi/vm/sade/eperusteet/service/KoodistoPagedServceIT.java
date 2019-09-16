package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoMetadataDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoPageDto;
import fi.vm.sade.eperusteet.service.impl.KoodistoPagedServiceImpl;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;

@RunWith(MockitoJUnitRunner.class)
public class KoodistoPagedServceIT{

    @Mock
    KoodistoClient koodistoClient;

    @InjectMocks
    KoodistoPagedService koodistoPagedService = new KoodistoPagedServiceImpl();

    @Before
    public void setup() {
        Mockito.when(koodistoClient.getAll(Mockito.any())).thenReturn(createKoodisto());
    }

    @Test
    public void testGetAllPaged() {
        Page<KoodistoKoodiDto> page = koodistoPagedService.getAllPaged("",  null, new KoodistoPageDto());
        Assertions.assertThat(page.getTotalElements()).isEqualTo(40);
        Assertions.assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    public void testGetAllPagedNimiFilter() {
        Page<KoodistoKoodiDto> page = koodistoPagedService.getAllPaged("",  "nimi", new KoodistoPageDto());
        Assertions.assertThat(page.getTotalElements()).isEqualTo(30);
        Assertions.assertThat(page.getTotalPages()).isEqualTo(2);
        Assertions.assertThat(page.getContent().get(0).getMetadataName("fi").getNimi()).isEqualTo("1nimi0");
    }

    @Test
    public void testGetAllPagedWithPage() {
        Page<KoodistoKoodiDto> page = koodistoPagedService.getAllPaged("",  "nimi", KoodistoPageDto.builder().sivu(2).sivukoko(10).kieli("fi").build());
        Assertions.assertThat(page.getTotalElements()).isEqualTo(30);
        Assertions.assertThat(page.getTotalPages()).isEqualTo(3);
        Assertions.assertThat(page.getContent().get(0).getMetadataName("ru").getNimi()).isEqualTo("3nimi0");
    }

    private List<KoodistoKoodiDto> createKoodisto() {

        return Stream.of(
                IntStream.range(0, 10).mapToObj(i -> {
                    KoodistoKoodiDto koodisto = new KoodistoKoodiDto();
                    koodisto.setMetadata(new KoodistoMetadataDto[]{KoodistoMetadataDto.of("2nimi"+i, "sv", "kuvaus"+1)});
                    return koodisto;
                }),
                IntStream.range(0, 10).mapToObj(i -> {
                    KoodistoKoodiDto koodisto = new KoodistoKoodiDto();
                    koodisto.setMetadata(new KoodistoMetadataDto[]{KoodistoMetadataDto.of("1nimi"+i, "fi", "kuvaus"+1)});
                    return koodisto;
                }),
                IntStream.range(0, 10).mapToObj(i -> {
                    KoodistoKoodiDto koodisto = new KoodistoKoodiDto();
                    koodisto.setMetadata(new KoodistoMetadataDto[]{KoodistoMetadataDto.of("3nimi"+i, "ru", "kuvaus"+1)});
                    return koodisto;
                }),
                IntStream.range(0, 10).mapToObj(i -> {
                    KoodistoKoodiDto koodisto = new KoodistoKoodiDto();
                    return koodisto;
                }))
            .flatMap(i -> i).collect(Collectors.toList());
    }
}

package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Kieli;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoMetadataDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.impl.KoodistoClientImpl;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.DtoMapperImpl;
import fi.vm.sade.eperusteet.service.util.RestClientFactory;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.javautils.http.OphHttpResponse;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.Callable;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KoodistoClientImplTest {

    @Mock
    RestClientFactory restClientFactory;

    @Mock
    KoodistoClient self;

    @Mock
    CacheManager cacheManager;

    @InjectMocks
    @Spy
    KoodistoClientImpl koodistoClient;

    @Before
    public void setup() {
        when(self.getAll("koodisto")).thenReturn(emptyList());
        doReturn(koodistoKoodiDto()).when(koodistoClient).addKoodi(any(KoodistoKoodiDto.class));
        when(cacheManager.getCache("koodistot")).thenReturn(cache());
    }

    @Test
    public void testAddKoodiNimella() {
        KoodistoKoodiDto koodistoKoodiDto = koodistoClient.addKoodiNimella("koodisto", LokalisoituTekstiDto.of(Kieli.FI, "koodinimi"));

        assertThat(koodistoKoodiDto)
                .extracting("koodiUri", "koodiArvo", "versio", "koodisto.koodistoUri")
                .containsExactly("koodiUri_1000", "1000", "1", "koodisto");
        assertThat(koodistoKoodiDto.getMetadataName("FI"))
                .isEqualToComparingFieldByField(KoodistoMetadataDto.of("koodinimi","fi","koodinimi"));

        verify(cacheManager).getCache(eq("koodistot"));
    }

    @Test
    public void testAddKoodiNimella_null() {
        doReturn(null).when(koodistoClient).addKoodi(any(KoodistoKoodiDto.class));
        KoodistoKoodiDto koodistoKoodiDto = koodistoClient.addKoodiNimella("koodisto", LokalisoituTekstiDto.of(Kieli.FI, "koodinimi"));

        assertThat(koodistoKoodiDto).isNull();
        verifyZeroInteractions(cacheManager);
    }

    private KoodistoKoodiDto koodistoKoodiDto() {
        KoodistoKoodiDto koodisto = new KoodistoKoodiDto();
        koodisto.setKoodiUri("koodiUri_1000");
        koodisto.setKoodiArvo("1000");
        koodisto.setVersio("1");
        koodisto.setKoodisto(new KoodistoDto());
        koodisto.getKoodisto().setKoodistoUri("koodisto");
        koodisto.setMetadata(new KoodistoMetadataDto[]{KoodistoMetadataDto.of("koodinimi","fi","koodinimi")});
        koodisto.setVoimassaAlkuPvm(new Date());

        return koodisto;
    }

    private Cache cache() {
        return new Cache() {

            @Override
            public String getName() {
                return null;
            }

            @Override
            public Object getNativeCache() {
                return null;
            }

            @Override
            public ValueWrapper get(Object o) {
                return null;
            }

            @Override
            public <T> T get(Object o, Class<T> aClass) {
                return null;
            }

            @Override
            public <T> T get(Object o, Callable<T> callable) {
                return null;
            }

            @Override
            public void put(Object o, Object o1) {

            }

            @Override
            public ValueWrapper putIfAbsent(Object o, Object o1) {
                return null;
            }

            @Override
            public void evict(Object o) {

            }

            @Override
            public void clear() {

            }
        };
    }

}

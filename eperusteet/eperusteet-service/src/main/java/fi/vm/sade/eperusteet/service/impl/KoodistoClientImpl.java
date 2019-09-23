/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import fi.vm.sade.eperusteet.domain.Koodi;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.util.RestClientFactory;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpEntity;
import fi.vm.sade.javautils.http.OphHttpRequest;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiLaajaDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoMetadataDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.Koodisto;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static javax.servlet.http.HttpServletResponse.*;

/**
 *
 * @author nkala
 */
@Slf4j
@Service
@Profile("default")
public class KoodistoClientImpl implements KoodistoClient {

    @Value("${koodisto.service.url:https://virkailija.opintopolku.fi/koodisto-service}")
    private String koodistoServiceUrl;

    private static final String KOODISTO_API = "/rest/json/";
    private static final String YLARELAATIO = "relaatio/sisaltyy-ylakoodit/";
    private static final String ALARELAATIO = "relaatio/sisaltyy-alakoodit/";
    private static final String RINNASTEINEN = "relaatio/rinnasteinen/";
    private static final String CODEELEMENT = "/rest/codeelement";
    private static final String LATEST = CODEELEMENT + "/latest/";

    @Autowired
    RestClientFactory restClientFactory;

    @Autowired
    KoodistoClient self; // for cacheable

    @Autowired
    CacheManager cacheManager;

    @Autowired
    @Koodisto
    private DtoMapper mapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Cacheable("koodistot")
    public List<KoodistoKoodiDto> getAll(String koodisto) {
        RestTemplate restTemplate = new RestTemplate();
        String url = koodistoServiceUrl + KOODISTO_API + koodisto + "/koodi/";
        try {
            KoodistoKoodiDto[] koodistot = restTemplate.getForObject(url, KoodistoKoodiDto[].class);
            List<KoodistoKoodiDto> koodistoDtot = mapper.mapAsList(Arrays.asList(koodistot), KoodistoKoodiDto.class);
            return koodistoDtot;
        }
        catch (HttpServerErrorException ex) {
            throw new BusinessRuleViolationException("koodistoa-ei-loytynyt");
        }
    }

    @Override
    public KoodistoKoodiDto get(String koodistoUri, String koodiUri) {
        return get(koodistoUri, koodiUri, null);
    }

    @Override
    @Cacheable("koodistokoodit")
    public KoodistoKoodiDto get(String koodistoUri, String koodiUri, Long versio) {
        if (koodistoUri == null || koodiUri == null) {
            return null;
        }
        RestTemplate restTemplate = new RestTemplate();
        String url = koodistoServiceUrl + KOODISTO_API + koodistoUri + "/koodi/" + koodiUri + (versio != null ? "?koodistoVersio=" + versio.toString() : "");
        KoodistoKoodiDto re = restTemplate.getForObject(url, KoodistoKoodiDto.class);
        return re;
    }

    @Override
    public List<KoodistoKoodiDto> filterBy(String koodisto, String haku) {
        return getAll(koodisto).stream()
                .filter(koodi -> koodi.getKoodiArvo().contains(haku) || Arrays.stream(koodi.getMetadata())
                            .filter(Objects::nonNull)
                            .map(KoodistoMetadataDto::getNimi)
                            .filter(Objects::nonNull)
                            .anyMatch(str -> StringUtils.contains(StringUtils.lowerCase(str), StringUtils.lowerCase(haku))))
                .collect(Collectors.toList());
    }

    private Map<String, String> metadataToLocalized(KoodistoKoodiDto koodistoKoodi) {
        return Arrays.stream(koodistoKoodi.getMetadata())
                .collect(Collectors.toMap(k -> k.getKieli().toLowerCase(), KoodistoMetadataDto::getNimi));
    }

    @Override
    @Cacheable(value = "koodistot", key="'alarelaatio:'+#p0")
    public List<KoodistoKoodiDto> getAlarelaatio(String koodi) {
        RestTemplate restTemplate = new RestTemplate();
        String url = koodistoServiceUrl + KOODISTO_API + ALARELAATIO + koodi;
        KoodistoKoodiDto[] koodistot = restTemplate.getForObject(url, KoodistoKoodiDto[].class);
        List<KoodistoKoodiDto> koodistoDtot = mapper.mapAsList(Arrays.asList(koodistot), KoodistoKoodiDto.class);
        return koodistoDtot;
    }

    @Override
    public KoodistoKoodiLaajaDto getAllByVersio(String koodi, String versio) {
        RestTemplate restTemplate = new RestTemplate();
        String url = koodistoServiceUrl + CODEELEMENT + "/" + koodi + "/" + versio;
        KoodistoKoodiLaajaDto koodiVersio = restTemplate.getForObject(url, KoodistoKoodiLaajaDto.class);
        return koodiVersio;
    }

    @Override
    public KoodistoKoodiDto getLatest(String koodi) {
        RestTemplate restTemplate = new RestTemplate();
        String url = koodistoServiceUrl + LATEST + koodi;
        KoodistoKoodiDto result = restTemplate.getForObject(url, KoodistoKoodiDto.class);
        return result;
    }

    @Override
    @Cacheable(value = "koodistot", key="'ylarelaatio:'+#p0")
    public List<KoodistoKoodiDto> getYlarelaatio(String koodi) {
        RestTemplate restTemplate = new RestTemplate();
        String url = koodistoServiceUrl + KOODISTO_API + YLARELAATIO + koodi;
        KoodistoKoodiDto[] koodistot = restTemplate.getForObject(url, KoodistoKoodiDto[].class);
        List<KoodistoKoodiDto> koodistoDtot = mapper.mapAsList(Arrays.asList(koodistot), KoodistoKoodiDto.class);
        return koodistoDtot;
    }

    @Override
    @Cacheable(value = "koodistot", key="'rinnasteiset:'+#p0")
    public List<KoodistoKoodiDto> getRinnasteiset(String koodi) {
        RestTemplate restTemplate = new RestTemplate();
        String url = koodistoServiceUrl + KOODISTO_API + RINNASTEINEN + koodi;
        KoodistoKoodiDto[] koodistot = restTemplate.getForObject(url, KoodistoKoodiDto[].class);
        List<KoodistoKoodiDto> koodistoDtot = mapper.mapAsList(Arrays.asList(koodistot), KoodistoKoodiDto.class);
        return koodistoDtot;
    }

    @Override
    public void addNimiAndUri(KoodiDto koodi) {
        KoodistoKoodiDto koodistoKoodi = get(koodi.getKoodisto(), koodi.getUri(), koodi.getVersio());
        if (koodistoKoodi != null) {
            koodi.setArvo(koodistoKoodi.getKoodiArvo());
            koodi.setNimi(metadataToLocalized(koodistoKoodi));
        }
    }

    @Override
    public KoodiDto getKoodi(String koodisto, String koodiUri) {
        return getKoodi(koodisto, koodiUri, null);
    }

    @Override
    public KoodiDto getKoodi(String koodisto, String koodiUri, Long versio) {
        KoodiDto koodi = new KoodiDto();
        koodi.setUri(koodiUri);
        koodi.setKoodisto(koodisto);
        koodi.setVersio(versio);
        addNimiAndUri(koodi);
        return koodi;
    }

    @Override
    public KoodistoKoodiDto addKoodi(KoodistoKoodiDto koodi) {
        OphHttpClient client = restClientFactory.get(koodistoServiceUrl, true);

        String url = koodistoServiceUrl
                + CODEELEMENT + "/"
                + koodi.getKoodisto().getKoodistoUri();
//                    + "/j_spring_cas_security_check";
        try {
            String dataStr = objectMapper.writeValueAsString(koodi);
            OphHttpRequest request = OphHttpRequest.Builder
                    .post(url)
                    .addHeader("Content-Type", "application/json;charset=UTF-8")
                    .setEntity(new OphHttpEntity.Builder()
                            .content(dataStr)
                            .contentType(ContentType.APPLICATION_JSON)
                            .build())
                    .build();

            return client.<KoodistoKoodiDto>execute(request)
                    .handleErrorStatus(SC_UNAUTHORIZED, SC_FORBIDDEN, SC_METHOD_NOT_ALLOWED, SC_BAD_REQUEST, SC_INTERNAL_SERVER_ERROR)
                    .with(res -> {
                        return Optional.empty();
                    })
                    .expectedStatus(SC_OK, SC_CREATED)
                    .mapWith(text -> {
                        try {
                            return objectMapper.readValue(text, KoodistoKoodiDto.class);
                        } catch (IOException e) {
                            throw new BusinessRuleViolationException("koodin-parsinta-epaonnistui");
                        }
                    })
                    .orElse(null);
        } catch (JsonProcessingException e) {
            throw new BusinessRuleViolationException("koodin-lisays-epaonnistui");
        }
    }

    @Override
    public KoodistoKoodiDto addKoodiNimella(String koodistonimi, LokalisoituTekstiDto koodinimi) {
        long seuraavaKoodi = nextKoodiId(koodistonimi);

        KoodistoKoodiDto uusiKoodi = KoodistoKoodiDto.builder()
                .koodiArvo(Long.toString(seuraavaKoodi))
                .koodiUri(koodistonimi + "_" + seuraavaKoodi)
                .koodisto(KoodistoDto.of(koodistonimi))
                .voimassaAlkuPvm(new Date())
                .metadata(koodinimi.getTekstit().entrySet().stream()
                        .map((k) -> KoodistoMetadataDto.of(k.getValue(), k.getKey().toString().toUpperCase(), k.getValue()))
                        .toArray(KoodistoMetadataDto[]::new))
                .build();
        KoodistoKoodiDto lisattyKoodi = addKoodi(uusiKoodi);
        if (lisattyKoodi == null
                || lisattyKoodi.getKoodisto() == null
                || lisattyKoodi.getKoodisto().getKoodistoUri() == null
                || lisattyKoodi.getKoodiUri() == null) {
            log.error("Koodin lisääminen epäonnistui {} {}", uusiKoodi, lisattyKoodi);
        } else {
            cacheManager.getCache("koodistot").evict(koodistonimi);
        }

        return lisattyKoodi;
    }

    @Override
    public long nextKoodiId(String koodistonimi) {
        List<KoodistoKoodiDto> koodit = self.getAll(koodistonimi);
        if (koodit.size() == 0) {
            return 1000L;
        }
        else {
            koodit.sort(Comparator.comparing(KoodistoKoodiDto::getKoodiArvo));
            for (int idx = 0; idx < koodit.size() - 2; ++idx) {
                long a = Long.parseLong(koodit.get(idx).getKoodiArvo()) + 1;
                long b = Long.parseLong(koodit.get(idx + 1).getKoodiArvo());
                if (a < b) {
                    return a;
                }
            }
            return Long.parseLong(koodit.get(koodit.size() - 1).getKoodiArvo()) + 1;
        }
    }
}

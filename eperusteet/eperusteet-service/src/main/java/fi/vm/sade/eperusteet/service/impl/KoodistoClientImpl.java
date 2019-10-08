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
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.eperusteet.domain.KoodiRelaatioTyyppi;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiLaajaDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoMetadataDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoSuhdeDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoSuhteillaDto;
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.Koodisto;
import fi.vm.sade.eperusteet.utils.client.OphClientException;
import fi.vm.sade.eperusteet.utils.client.OphClientHelper;
import fi.vm.sade.eperusteet.utils.client.RestClientFactory;
import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpEntity;
import fi.vm.sade.javautils.http.OphHttpRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

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
    private static final String KOODISTO_REST_API = "/rest";
    private static final String YLARELAATIO = "relaatio/sisaltyy-ylakoodit/";
    private static final String ALARELAATIO = "relaatio/sisaltyy-alakoodit/";
    private static final String RINNASTEINEN = "relaatio/rinnasteinen/";
    private static final String CODEELEMENT = "/rest/codeelement";
    private static final String LATEST = CODEELEMENT + "/latest/";
    private static final String CODES = "/rest/codes";
    private static final String GET_CODES_WITH_URI = CODES + "/{codesUri}";
    private static final String GET_CODES_WITH_URI_AND_VERSION = GET_CODES_WITH_URI + "/{version}";
    private static final String ADD_CODE_ELEMENT_RELATION = CODEELEMENT + "/addrelation/{codeElementUri}/{codeElementUriToAdd}/{relationType}";
    private static final String ADD_CODE_RELATION = CODES + "/addrelation/{codesUri}/{codesUriToAdd}/{relationType}";

    @Autowired
    RestClientFactory restClientFactory;

    @Autowired
    KoodistoClient self; // for cacheable

    @Autowired
    CacheManager cacheManager;

    @Autowired
    OphClientHelper ophClientHelper;

    @Autowired
    @Koodisto
    private DtoMapper mapper;

    @Autowired
    HttpEntity httpEntity;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<KoodistoKoodiDto> getAll(String koodisto) {
        return self.getAll(koodisto, false);
    }

    @Override
    @Cacheable("koodistot")
    public List<KoodistoKoodiDto> getAll(String koodisto, boolean onlyValidKoodis) {
        RestTemplate restTemplate = new RestTemplate();
        String url = koodistoServiceUrl + KOODISTO_API + koodisto + "/koodi?onlyValidKoodis=" + onlyValidKoodis;
        try {
            ResponseEntity<KoodistoKoodiDto[]> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, KoodistoKoodiDto[].class);
            List<KoodistoKoodiDto> koodistoDtot = mapper.mapAsList(Arrays.asList(response.getBody()), KoodistoKoodiDto.class);
            return koodistoDtot;
        } catch (HttpServerErrorException ex) {
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
        ResponseEntity<KoodistoKoodiDto> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, KoodistoKoodiDto.class);
        return response.getBody();
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
        ResponseEntity<KoodistoKoodiDto[]> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, KoodistoKoodiDto[].class);
        List<KoodistoKoodiDto> koodistoDtot = mapper.mapAsList(Arrays.asList(response.getBody()), KoodistoKoodiDto.class);
        return koodistoDtot;
    }

    @Override
    public KoodistoKoodiLaajaDto getAllByVersio(String koodi, String versio) {
        RestTemplate restTemplate = new RestTemplate();
        String url = koodistoServiceUrl + CODEELEMENT + "/" + koodi + "/" + versio;
        ResponseEntity<KoodistoKoodiLaajaDto> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, KoodistoKoodiLaajaDto.class);
        return response.getBody();
    }

    @Override
    public KoodistoKoodiDto getLatest(String koodi) {
        RestTemplate restTemplate = new RestTemplate();
        String url = koodistoServiceUrl + LATEST + koodi;
        ResponseEntity<KoodistoKoodiDto> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, KoodistoKoodiDto.class);
        return response.getBody();
    }

    @Override
    @Cacheable(value = "koodistot", key="'ylarelaatio:'+#p0")
    public List<KoodistoKoodiDto> getYlarelaatio(String koodi) {
        RestTemplate restTemplate = new RestTemplate();
        String url = koodistoServiceUrl + KOODISTO_API + YLARELAATIO + koodi;
        ResponseEntity<KoodistoKoodiDto[]> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, KoodistoKoodiDto[].class);
        List<KoodistoKoodiDto> koodistoDtot = mapper.mapAsList(Arrays.asList(response.getBody()), KoodistoKoodiDto.class);
        return koodistoDtot;
    }

    @Override
    @Cacheable(value = "koodistot", key="'rinnasteiset:'+#p0")
    public List<KoodistoKoodiDto> getRinnasteiset(String koodi) {
        RestTemplate restTemplate = new RestTemplate();
        String url = koodistoServiceUrl + KOODISTO_API + RINNASTEINEN + koodi;
        ResponseEntity<KoodistoKoodiDto[]> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, KoodistoKoodiDto[].class);
        List<KoodistoKoodiDto> koodistoDtot = mapper.mapAsList(Arrays.asList(response.getBody()), KoodistoKoodiDto.class);
        return koodistoDtot;
    }

    @Override
    public void addNimiAndUri(KoodiDto koodi) {
        KoodistoKoodiDto koodistoKoodi = get(koodi.getKoodisto(), koodi.getUri());
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
        return addKoodiNimella(koodistonimi, koodinimi, seuraavaKoodi);
    }

    @Override
    public KoodistoKoodiDto addKoodiNimella(String koodistonimi, LokalisoituTekstiDto koodinimi, long seuraavaKoodi) {
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
            return null;
        } else {
            cacheManager.getCache("koodistot").evict(koodistonimi);
        }

        return lisattyKoodi;
    }

    @Override
    public long nextKoodiId(String koodistonimi) {
        return nextKoodiId(koodistonimi, 1).stream().findFirst().get();
    }

    @Override
    public Collection<Long> nextKoodiId(String koodistonimi, int count) {
        List<KoodistoKoodiDto> koodit = self.getAll(koodistonimi);
        if (koodit.size() == 0) {
            koodit = Collections.singletonList(KoodistoKoodiDto.builder().koodiArvo("999").build());
        }

        List<Long> ids = new ArrayList<>();
        List<Long> currentIds = koodit.stream().map(k -> Long.parseLong(k.getKoodiArvo())).collect(Collectors.toList());
        Long max = currentIds.stream().mapToLong(Long::longValue).max().getAsLong();
        Long min = currentIds.stream().mapToLong(Long::longValue).min().getAsLong();

        for(Long ind = min; ind <= max + count && ids.size() < count; ind++) {
            if(!currentIds.contains(ind)) {
                ids.add(ind);
            }
        }

       return ids;
    }

    @Override
    public void addKoodirelaatio(String parentKoodi, String lapsiKoodi, KoodiRelaatioTyyppi koodiRelaatioTyyppi) {

        UriComponents uri = UriComponentsBuilder.fromHttpUrl(koodistoServiceUrl)
                .path(ADD_CODE_ELEMENT_RELATION)
                .buildAndExpand(parentKoodi, lapsiKoodi, koodiRelaatioTyyppi.name());

        log.debug("url: {}", uri.toString());
        ophClientHelper.post(koodistoServiceUrl, uri.toString());

        if (koodiRelaatioTyyppi.equals(KoodiRelaatioTyyppi.SISALTYY)) {
            cacheManager.getCache("koodistot").evict("alarelaatio:" + parentKoodi);
            cacheManager.getCache("koodistot").evict("ylarelaatio:" + lapsiKoodi);
        } else if (koodiRelaatioTyyppi.equals(KoodiRelaatioTyyppi.SISALTYY)) {
            cacheManager.getCache("koodistot").evict("rinnasteiset:" + parentKoodi);
        }
    }

    @Override
    public void addKoodistoRelaatio(String parentKoodi, String lapsiKoodi, KoodiRelaatioTyyppi koodiRelaatioTyyppi) {

        UriComponents uri = UriComponentsBuilder.fromHttpUrl(koodistoServiceUrl)
                .path(GET_CODES_WITH_URI)
                .buildAndExpand(parentKoodi);

        log.debug("uri : {}", uri.toString());

        KoodistoDto koodistoDto = ophClientHelper.get(koodistoServiceUrl, uri.toString(), KoodistoDto.class);
        if (koodistoDto == null) {
            throw new BusinessRuleViolationException("koodistoa " + parentKoodi + " ei loydy.");
        }

        uri = UriComponentsBuilder.fromHttpUrl(koodistoServiceUrl)
                .path(GET_CODES_WITH_URI_AND_VERSION)
                .buildAndExpand(parentKoodi, koodistoDto.getLatestKoodistoVersio().getVersio());
        KoodistoSuhteillaDto koodistoSuhteillaDto = ophClientHelper.get(koodistoServiceUrl, uri.toString(), KoodistoSuhteillaDto.class);

        if (koodiRelaatioTyyppi.equals(KoodiRelaatioTyyppi.SISALTYY) &&
                koodistoSuhteillaDto.getIncludesCodes().stream().filter(koodistoSuhdeDto -> koodistoSuhdeDto.getCodesUri().equals(lapsiKoodi)).findFirst().isPresent()) {

            log.debug("{} sisaltyy jo koodistoon {}", lapsiKoodi, parentKoodi);
            return;
        }

        uri = UriComponentsBuilder.fromHttpUrl(koodistoServiceUrl)
                .path(ADD_CODE_RELATION)
                .buildAndExpand(parentKoodi, lapsiKoodi, koodiRelaatioTyyppi.name());

        ophClientHelper.post(koodistoServiceUrl, uri.toString());

        log.debug("lisättiin koodi {} koodin {} lapseksi", parentKoodi, lapsiKoodi);
    }

}

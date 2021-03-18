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

import fi.vm.sade.eperusteet.dto.LokalisointiDto;
import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.util.Lokalisoitava;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.TekstiPalanenRepositoryCustom;
import fi.vm.sade.eperusteet.service.LokalisointiService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

/**
 *
 * @author jussi
 */
@Service
public class LokalisointiServiceImpl implements LokalisointiService {

    private static final Logger LOG = LoggerFactory.getLogger(LokalisointiService.class);

    @Value("${lokalisointi.service.url:https://virkailija.opintopolku.fi/lokalisointi/cxf/rest/v1/localisation?}")
    private String lokalisointiServiceUrl;

    @Value("${lokalisointi.service.category:eperusteet}")
    private String category;

    @Autowired
    private TekstiPalanenRepositoryCustom tekstiPalanenRepository;

    @Autowired
    HttpEntity httpEntity;

    @Autowired
    HttpHeaders httpHeaders;

    @Override
    public List<LokalisointiDto> getAllByCategoryAndLocale(String category, String locale) {
        RestTemplate restTemplate = new RestTemplate();
        String url = lokalisointiServiceUrl + "category=" + category + "&locale=" + locale;
        try {
            ResponseEntity<LokalisointiDto[]> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, LokalisointiDto[].class);
            return Arrays.asList(response.getBody());
        } catch (RestClientException e) {
            LOG.error(e.getLocalizedMessage());
            return new ArrayList<>();
        }
    }

    @Override
    @Cacheable("lokalisoinnit")
    public LokalisointiDto get(String key, String locale) {
        RestTemplate restTemplate = new RestTemplate();
        String url = lokalisointiServiceUrl + "category=" + category + "&locale=" + locale + "&key=" + key;
        LokalisointiDto[] re;
        try {
            ResponseEntity<LokalisointiDto[]> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, LokalisointiDto[].class);
            re = response.getBody();

        } catch (RestClientException ex) {
            LOG.error("Rest client error: {}", ex.getLocalizedMessage());
            re = new LokalisointiDto[]{};
        }

        if (re.length > 1) {
            LOG.warn("Got more than one object: {} from {}", re, url);
        }
        if (re.length > 0) {
            return re[0];
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public <T extends Lokalisoitava, C extends Collection<T>> C lokalisoi(C list) {
        lokalisoi(list.stream().flatMap(Lokalisoitava::lokalisoitavatTekstit));
        return list;
    }

    protected void lokalisoi(Stream<LokalisoituTekstiDto> lokalisoitava) {
        Map<Long,List<LokalisoituTekstiDto>> byId = lokalisoitava
                .filter(v -> v != null && v.getId() != null).collect(groupingBy(LokalisoituTekstiDto::getId));
        if (!byId.isEmpty()) {
            tekstiPalanenRepository.findLokalisoitavatTekstit(byId.keySet())
                    .forEach(haettu -> byId.get(haettu.getId())
                            .forEach(palanen -> palanen.getTekstit().put(haettu.getKieli(), haettu.getTeksti())));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public <T extends Lokalisoitava> T lokalisoi(T dto) {
        if (dto != null) {
            lokalisoi(dto.lokalisoitavatTekstit());
        }
        return dto;
    }

    @Override
    @Transactional
    public void save(List<LokalisointiDto> kaannokset) {
        final Set<String> sallitutKielet = Stream.of("fi", "en", "sv").collect(Collectors.toSet());
        final Set<String> sallitutKategoriat = Stream.of("eperusteet", "eperusteet-ylops", "eperusteet-opintopolku").collect(Collectors.toSet());
        for (LokalisointiDto kaannos : kaannokset) {
            if (!sallitutKategoriat.contains(kaannos.getCategory())) {
                throw new BusinessRuleViolationException("vain-eperusteiden-kaannokset-sallittu");
            }
            else if (StringUtils.isEmpty(kaannos.getKey())) {
                throw new BusinessRuleViolationException("kaannosavainta-ei-maaritetty");
            }
            else if (!sallitutKielet.contains(kaannos.getLocale())) {
                throw new BusinessRuleViolationException("kaannoskieli-virheellinen");
            }
            else if (kaannos.getValue() == null) {
                throw new BusinessRuleViolationException("virheellinen-kaannos-arvo");
            }
            kaannos.setDescription("");
            kaannos.setId(null);
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = lokalisointiServiceUrl.substring(0, lokalisointiServiceUrl.length() - 1) + "/update";

        try {
            LOG.info("Päivitetään käännökset: ", kaannokset);
            HttpEntity<List<LokalisointiDto>> request = new HttpEntity<>(kaannokset, httpHeaders);
            restTemplate.put(url, request);
        }
        catch (HttpClientErrorException error) {
            throw new BusinessRuleViolationException("ei-riittavia-oikeuksia");
        }
    }

}

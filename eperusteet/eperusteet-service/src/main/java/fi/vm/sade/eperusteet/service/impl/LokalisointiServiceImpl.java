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
import fi.vm.sade.eperusteet.dto.util.Lokalisoitava;
import fi.vm.sade.eperusteet.dto.util.LokalisoituTekstiDto;
import fi.vm.sade.eperusteet.repository.TekstiPalanenRepositoryCustom;
import fi.vm.sade.eperusteet.service.LokalisointiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

/**
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


    @Override
    @Cacheable("lokalisoinnit")
    public LokalisointiDto get(String key, String locale) {
        RestTemplate restTemplate = new RestTemplate();
        String url = lokalisointiServiceUrl + "category=" + category + "&locale=" + locale + "&key=" + key;
        LokalisointiDto[] re;
        try {
            re = restTemplate.getForObject(url, LokalisointiDto[].class);
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
        Map<Long, List<LokalisoituTekstiDto>> byId = lokalisoitava
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

}

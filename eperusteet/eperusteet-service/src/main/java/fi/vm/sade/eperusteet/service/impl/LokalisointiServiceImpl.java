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
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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


    @Override
    @Cacheable("lokalisoinnit")
    public LokalisointiDto get(String key, String locale) {
        RestTemplate restTemplate = new RestTemplate();
        String url = lokalisointiServiceUrl + "category=" + category + "&locale=" + locale + "&key=" + key;
        LOG.debug("get lokalisointi url: {}", url);
        LokalisointiDto[] re = restTemplate.getForObject(url, LokalisointiDto[].class);

        // mitäs tehdään jos tulee useampi kuin yksi käännös?
        // palautetaan oletuksena ensimmäinen. Palvelusta saadaan tarvittaessa
        // myös tieto, koska lokalisointi on luotu/muokattu, joten tätä voisi
        // käyttää myös.
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
        Map<Long,List<LokalisoituTekstiDto>> byId = list.stream().flatMap(Lokalisoitava::lokalisoitavatTekstit)
                .filter(v -> v != null && v.getId() != null).collect(groupingBy(LokalisoituTekstiDto::getId));
        if (!byId.isEmpty()) {
            tekstiPalanenRepository.findLokalisoitavatTekstit(byId.keySet())
                .stream().forEach(haettu -> byId.get(haettu.getId())
                    .stream().forEach(palanen -> palanen.getTekstit().put(haettu.getKieli(), haettu.getTeksti())));
        }
        return list;
    }

}

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

import fi.vm.sade.eperusteet.dto.koodisto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.KoulutusalaDto;
import fi.vm.sade.eperusteet.dto.OpintoalaDto;
import fi.vm.sade.eperusteet.service.KoulutusalaService;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.Koodisto;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author jussini
 */
@Service
public class KoulutusalaServiceImpl implements KoulutusalaService {

    private static final String KOODISTO_REST_URL = "https://virkailija.opintopolku.fi/koodisto-service/rest/json/";
    private static final String KOODISTO_RELAATIO_ALA = "relaatio/sisaltyy-alakoodit/";
    private static final String KOULUTUSALA_URI = "koulutusalaoph2002";
    //private static final String OPINTOALA_URI = "opintoalaoph2002";

    @Autowired
    @Koodisto
    private DtoMapper mapper;

    @Autowired
    HttpEntity httpEntity;

    @Override
    @Cacheable("koulutusalat")
    public List<KoulutusalaDto> getAll() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<KoodistoKoodiDto[]> response = restTemplate.exchange(KOODISTO_REST_URL + KOULUTUSALA_URI + "/koodi/", HttpMethod.GET, httpEntity, KoodistoKoodiDto[].class);
        List<KoulutusalaDto> koulutusalatDtos = mapper.mapAsList(Arrays.asList(response.getBody()), KoulutusalaDto.class);

        for (KoulutusalaDto koulutusalaDto : koulutusalatDtos) {
            ResponseEntity<KoodistoKoodiDto[]> opintoalat = restTemplate.exchange(KOODISTO_REST_URL + KOODISTO_RELAATIO_ALA + koulutusalaDto.getKoodi(), HttpMethod.GET, httpEntity, KoodistoKoodiDto[].class);
            koulutusalaDto.setOpintoalat(mapper.mapAsList(Arrays.asList(opintoalat.getBody()), OpintoalaDto.class));
        }

        return koulutusalatDtos;
    }


}

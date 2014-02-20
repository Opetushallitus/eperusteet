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

import fi.vm.sade.eperusteet.domain.Opintoala;
import fi.vm.sade.eperusteet.dto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.repository.OpintoalaRepository;
import fi.vm.sade.eperusteet.service.OpintoalaService;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.Koodisto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author harrik
 */
@Service
public class OpintoalaServiceImpl implements OpintoalaService {
    private static final Logger LOG = LoggerFactory.getLogger(KoulutusalaServiceImpl.class);
    
    @Autowired
    private OpintoalaRepository repository;

    @Autowired
    @Koodisto
    private DtoMapper mapper;
    
    private static final String KOODISTO_REST_URL = "https://virkailija.opintopolku.fi/koodisto-service/rest/json/";
    private static final String OPINTOALA_URI = "opintoalaoph2002";

    @Override
    @Transactional
    public void opintoalaLammitys() {
        RestTemplate restTemplate = new RestTemplate();
        KoodistoKoodiDto[] opintoalat = restTemplate.getForObject(KOODISTO_REST_URL + OPINTOALA_URI + "/koodi", KoodistoKoodiDto[].class);
        Opintoala opintoalaEntity;
        for (KoodistoKoodiDto opintoala : opintoalat) {
            opintoalaEntity = new Opintoala();
            opintoalaEntity.setKoodi(opintoala.getKoodiUri());
            if (repository.findOneByKoodi(opintoala.getKoodiUri()) == null) {
                repository.save(opintoalaEntity);
            }
        }
    }
}

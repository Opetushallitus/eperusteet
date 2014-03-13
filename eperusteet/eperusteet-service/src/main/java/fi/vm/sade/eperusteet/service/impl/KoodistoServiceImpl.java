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

import com.googlecode.ehcache.annotations.Cacheable;
import fi.vm.sade.eperusteet.dto.KoodistoKoodiDto;
import fi.vm.sade.eperusteet.dto.KoodistoMetadataDto;
import fi.vm.sade.eperusteet.service.KoodistoService;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.Koodisto;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author nkala
 */
@Service
public class KoodistoServiceImpl implements KoodistoService {

    private static final Logger LOG = LoggerFactory.getLogger(KoodistoServiceImpl.class);
    private static final String KOODISTO_API = "https://virkailija.opintopolku.fi/koodisto-service/rest/json/";
    private static final String YLARELAATIO = "relaatio/sisaltyy-ylakoodit/";
    private static final String ALARELAATIO = "relaatio/sisaltyy-alakoodit/";

    @Autowired
    @Koodisto
    private DtoMapper mapper;

    @Override
    @Cacheable(cacheName = "koodistot")
    public List<KoodistoKoodiDto> getAll(String koodisto) {
        RestTemplate restTemplate = new RestTemplate();
        String url = KOODISTO_API + koodisto + "/koodi/";
        KoodistoKoodiDto[] koodistot = restTemplate.getForObject(url, KoodistoKoodiDto[].class);
        List<KoodistoKoodiDto> koodistoDtot = mapper.mapAsList(Arrays.asList(koodistot), KoodistoKoodiDto.class);
        return koodistoDtot;
    }

    @Override
    public KoodistoKoodiDto get(String koodisto, String koodi) {
        RestTemplate restTemplate = new RestTemplate();
        String url = KOODISTO_API + koodisto + "/koodi/" + koodi;
        LOG.debug(url);
        KoodistoKoodiDto re = restTemplate.getForObject(url, KoodistoKoodiDto.class);
        return re;
    }

    @Override
    public List<KoodistoKoodiDto> filterBy(String koodisto, String koodi) {
        List<KoodistoKoodiDto> filter = getAll(koodisto);
        List<KoodistoKoodiDto> tulos = new ArrayList<>();

        for (KoodistoKoodiDto x : filter) {
            Boolean nimessa = false;

            for (KoodistoMetadataDto y : x.getMetadata()) {
                if (y.getNimi().toLowerCase().contains(koodi.toLowerCase())) {
                    nimessa = true;
                    break;
                }
            }

            if (x.getKoodiUri().contains(koodi) || nimessa)
                tulos.add(x);
        }
        return tulos;
    }

    @Override
    public List<KoodistoKoodiDto> getAlarelaatio(String koodi) {
        RestTemplate restTemplate = new RestTemplate();
        String url = KOODISTO_API + ALARELAATIO + koodi;
        KoodistoKoodiDto[] koodistot = restTemplate.getForObject(url, KoodistoKoodiDto[].class);
        List<KoodistoKoodiDto> koodistoDtot = mapper.mapAsList(Arrays.asList(koodistot), KoodistoKoodiDto.class);
        return koodistoDtot;
    }
}

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
import fi.vm.sade.eperusteet.dto.tutkinnonrakenne.KoodiDto;
import fi.vm.sade.eperusteet.service.KoodistoClient;
import fi.vm.sade.eperusteet.service.mapping.DtoMapper;
import fi.vm.sade.eperusteet.service.mapping.Koodisto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author nkala
 */
@Service
@Profile(value = "default")
public class KoodistoClientImpl implements KoodistoClient {

    @Value("${koodisto.service.url:https://virkailija.opintopolku.fi/koodisto-service}")
    private String koodistoServiceUrl;

    private static final String KOODISTO_API = "/rest/json/";
    private static final String YLARELAATIO = "relaatio/sisaltyy-ylakoodit/";
    private static final String ALARELAATIO = "relaatio/sisaltyy-alakoodit/";

    @Autowired
    @Koodisto
    private DtoMapper mapper;

    @Override
    @Cacheable("koodistot")
    public List<KoodistoKoodiDto> getAll(String koodisto) {
        RestTemplate restTemplate = new RestTemplate();
        String url = koodistoServiceUrl + KOODISTO_API + koodisto + "/koodi/";
        KoodistoKoodiDto[] koodistot = restTemplate.getForObject(url, KoodistoKoodiDto[].class);
        List<KoodistoKoodiDto> koodistoDtot = mapper.mapAsList(Arrays.asList(koodistot), KoodistoKoodiDto.class);
        return koodistoDtot;
    }

    @Override
    public KoodistoKoodiDto get(String koodistoUri, String koodiUri) {
        return get(koodistoUri, koodiUri, null);
    }

    @Override
    @Cacheable("koodistokoodit")
    public KoodistoKoodiDto get(String koodistoUri, String koodiUri, Long versio) {
        RestTemplate restTemplate = new RestTemplate();
        String url = koodistoServiceUrl + KOODISTO_API + koodistoUri + "/koodi/" + koodiUri + (versio != null ? "?koodistoVersio=" + versio.toString() : "");
        KoodistoKoodiDto re = restTemplate.getForObject(url, KoodistoKoodiDto.class);
        return re;
    }

    @Override
    public Stream<KoodistoKoodiDto> filterBy(String koodisto, String haku) {
        return getAll(koodisto).stream()
                .filter(koodi -> koodi.getKoodiArvo().contains(haku) || Arrays.stream(koodi.getMetadata())
                            .anyMatch(meta -> meta.getNimi().toLowerCase().contains(haku.toLowerCase())));
    }

    private Map<String, String> metadataToLocalized(KoodistoKoodiDto koodistoKoodi) {
        return Arrays.asList(koodistoKoodi.getMetadata()).stream()
                .collect(Collectors.toMap(k -> k.getKieli().toLowerCase(), k -> k.getNimi()));
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
    @Cacheable(value = "koodistot", key="'ylarelaatio:'+#p0")
    public List<KoodistoKoodiDto> getYlarelaatio(String koodi) {
        RestTemplate restTemplate = new RestTemplate();
        String url = koodistoServiceUrl + KOODISTO_API + YLARELAATIO + koodi;
        KoodistoKoodiDto[] koodistot = restTemplate.getForObject(url, KoodistoKoodiDto[].class);
        List<KoodistoKoodiDto> koodistoDtot = mapper.mapAsList(Arrays.asList(koodistot), KoodistoKoodiDto.class);
        return koodistoDtot;
    }

    @Override
    public void addNimiAndUri(KoodiDto koodi) {
        KoodistoKoodiDto koodistoKoodi = get(koodi.getKoodisto(), koodi.getUri(), koodi.getVersio());
        koodi.setArvo(koodistoKoodi.getKoodiArvo());
        koodi.setNimi(metadataToLocalized(koodistoKoodi));
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

}

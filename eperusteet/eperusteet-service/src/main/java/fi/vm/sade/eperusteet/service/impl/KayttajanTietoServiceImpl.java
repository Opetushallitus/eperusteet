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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanProjektitiedotDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import static fi.vm.sade.eperusteet.service.mapping.KayttajanTietoParser.parsiKayttaja;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import fi.vm.sade.eperusteet.service.util.RestClientFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;

/**
 *
 * @author nkala
 */
@Service
@Profile("default")
public class KayttajanTietoServiceImpl implements KayttajanTietoService {

    @Value("${cas.service.oppijanumerorekisteri-service:''}")
    private String onrServiceUrl;

    @Value("${cas.service.kayttooikeus-service:''}")
    private String koServiceUrl;

    private static final String HENKILO_API = "/henkilo/";

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    RestClientFactory restClientFactory;

    @PostConstruct
    public void configureMapper() {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    @Async
    public Future<KayttajanTietoDto> haeAsync(String oid) {
        return new AsyncResult<>(hae(oid));
    }

    @Override
    @Cacheable("kayttajat")
    public KayttajanTietoDto hae(String oid) {
        if (ObjectUtils.isEmpty(oid)) {
            throw new BusinessRuleViolationException("Haettua käyttäjää ei ole olemassa");
        }

        OphHttpClient client = restClientFactory.get(onrServiceUrl);

        String url = onrServiceUrl + HENKILO_API + oid;

        OphHttpRequest request = OphHttpRequest.Builder
                .get(url)
                .build();

        return client.<KayttajanTietoDto>execute(request)
                .handleErrorStatus(SC_UNAUTHORIZED)
                .with(res -> Optional.empty())
                .expectedStatus(SC_OK)
                .mapWith(text -> {
                    try {
                        JsonNode json = mapper.readTree(text);
                        return parsiKayttaja(json);
                    } catch (IOException e) {
                        return null;
                    }
                })
                .orElse(null);
    }

    @Override
    public List<KayttajanProjektitiedotDto> haePerusteprojektit(String oid) {
        if (oid == null || oid.isEmpty()) {
            throw new BusinessRuleViolationException("Haettua käyttäjää ei ole olemassa");
        }

        OphHttpClient client = restClientFactory.get(koServiceUrl);
        String url = koServiceUrl + HENKILO_API + oid + "/organisaatiohenkilo";


        List<KayttajanProjektitiedotDto> kpp = new ArrayList<>();

        OphHttpRequest request = OphHttpRequest.Builder
                .get(url)
                .build();

        Optional<List<KayttajanProjektitiedotDto>> unfiltered = client.<List<KayttajanProjektitiedotDto>>execute(request)
                .handleErrorStatus(SC_UNAUTHORIZED)
                .with(res -> Optional.empty())
                .expectedStatus(SC_OK)
                .mapWith(text -> {
                    try {
                        return mapper.readValue(text, new TypeReference<List<KayttajanProjektitiedotDto>>(){});
                    } catch (IOException ex) {
                        return new ArrayList<>();
                    }
                });

        if (unfiltered.isPresent()) {
            for (KayttajanProjektitiedotDto kp : unfiltered.get()) {
                Perusteprojekti pp = perusteprojektiRepository.findOneByRyhmaOid(kp.getOrganisaatioOid());
                if (pp != null) {
                    kp.setPerusteprojekti(pp.getId());
                    kpp.add(kp);
                }
            }
        }
        return kpp;
    }

    @Override
    @Cacheable("kayttajan_projekti")
    public KayttajanProjektitiedotDto haePerusteprojekti(String oid, Long projektiId) {
        if (oid == null || oid.isEmpty()) {
            throw new BusinessRuleViolationException("Haettua käyttäjää ei ole olemassa");
        }

        Perusteprojekti pp = perusteprojektiRepository.findOne(projektiId);

        if (pp == null) {
            throw new BusinessRuleViolationException("Käyttäjällä ei ole kyseistä perusteprojektia");
        }

        OphHttpClient client = restClientFactory.get(koServiceUrl);
        String url = koServiceUrl + HENKILO_API + oid + "/organisaatiohenkilo/" + pp.getRyhmaOid();

        OphHttpRequest request = OphHttpRequest.Builder
                .get(url)
                .build();

        Optional<KayttajanProjektitiedotDto> dto = client.<KayttajanProjektitiedotDto>execute(request)
                .handleErrorStatus(SC_UNAUTHORIZED)
                .with(res -> Optional.empty())
                .expectedStatus(SC_OK)
                .mapWith(text -> {
                    try {
                        return mapper.readValue(text, KayttajanProjektitiedotDto.class);
                    } catch (IOException e) {
                        return null;
                    }
                });

        KayttajanProjektitiedotDto kayttajanProjektitiedotDto = null;
        if (dto.isPresent()) {
            kayttajanProjektitiedotDto = dto.get();
            kayttajanProjektitiedotDto.setPerusteprojekti(pp.getId());
        }

        return kayttajanProjektitiedotDto;
    }
}

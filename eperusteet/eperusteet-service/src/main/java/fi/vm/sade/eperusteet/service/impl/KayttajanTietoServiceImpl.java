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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.eperusteet.domain.Perusteprojekti;
import fi.vm.sade.eperusteet.dto.KayttajanProjektitiedotDto;
import fi.vm.sade.eperusteet.dto.KayttajanTietoDto;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.util.RestClientFactory;
import fi.vm.sade.generic.rest.CachingRestClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 *
 * @author nkala
 */
@Service
public class KayttajanTietoServiceImpl implements KayttajanTietoService {

    @Value("${cas.service.authentication-service}")
    private String serviceUrl;

    private static final String KAYTTAJA_API = "/resources/henkilo/";
    private static final String OMAT_TIEDOT_API = "/resources/omattiedot/";

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    PerusteprojektiRepository perusteprojektiRepository;

    @Autowired
    RestClientFactory restClientFactory;

    private String getField(JsonNode json, String... fields) {
        for (String field : fields) {
            if (json != null) {
                json = json.get(field);
            } else {
                return null;
            }
        }
        return json != null ? json.asText() : null;
    }

    @Override
    public KayttajanTietoDto parsiKayttaja(JsonNode json) {
        KayttajanTietoDto ktd = new KayttajanTietoDto();
        ktd.setUsername(getField(json, "kayttajatiedot", "username"));
        ktd.setEtunimet(getField(json, "etunimet"));
        ktd.setKieliKoodi(getField(json, "asiointiKieli", "kieliKoodi"));
        ktd.setKutsumanimi(getField(json, "kutsumanimi"));
        ktd.setSukunimi(getField(json, "sukunimi"));
        ktd.setOidHenkilo(getField(json, "oidHenkilo"));
        ktd.setYhteystiedot(json.get("yhteystiedotRyhma"));
        return ktd;
    }

    @Override
    public List<KayttajanTietoDto> parsiKayttajat(JsonNode jsonList) {
        List<KayttajanTietoDto> ktds = new ArrayList<>();
        for (JsonNode json : jsonList) {
            ktds.add(parsiKayttaja(json));
        }
        return ktds;
    }

    @Override
    @Cacheable("kayttajat")
    public KayttajanTietoDto hae(String oid) {
        CachingRestClient crc = restClientFactory.create(serviceUrl);

        try {
            String url = serviceUrl + (oid == null ? OMAT_TIEDOT_API : KAYTTAJA_API + oid);
            JsonNode json = mapper.readTree(crc.getAsString(url));
            return parsiKayttaja(json);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public List<KayttajanProjektitiedotDto> haePerusteprojektit(String oid) {
        if (oid == null || oid.isEmpty()) {
            throw new BusinessRuleViolationException("Haettua käyttäjää ei ole olemassa");
        }

        CachingRestClient crc = restClientFactory.create(serviceUrl);
        String url = serviceUrl + KAYTTAJA_API + oid + "/organisaatiohenkilo";

        try {
            List<KayttajanProjektitiedotDto> kpp = new ArrayList<>();
            List<KayttajanProjektitiedotDto> unfiltered = Arrays.asList(crc.get(url, KayttajanProjektitiedotDto[].class));
            for (KayttajanProjektitiedotDto kp : unfiltered) {
                Perusteprojekti pp = perusteprojektiRepository.findOneByOid(kp.getOrganisaatioOid());
                if (pp != null) {
                    kp.setPerusteprojekti(pp.getId());
                    kpp.add(kp);
                }
            }
            return kpp;
        } catch (IOException ex) {
            throw new BusinessRuleViolationException("Käyttäjän perusteprojektitietojen hakeminen epäonnistui");
        }
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

        CachingRestClient crc = restClientFactory.create(serviceUrl);
        String url = serviceUrl + KAYTTAJA_API + oid + "/organisaatiohenkilo/" + pp.getOid();

        try {
            KayttajanProjektitiedotDto ppt = crc.get(url, KayttajanProjektitiedotDto.class);
            ppt.setPerusteprojekti(pp.getId());
            return ppt;
        } catch (IOException ex) {
            throw new BusinessRuleViolationException("Käyttäjän perusteprojektitietojen hakeminen epäonnistui");
        }
    }
}

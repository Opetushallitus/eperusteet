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
import fi.vm.sade.eperusteet.dto.KayttajanTietoDto;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.util.RestClientFactory;
import fi.vm.sade.generic.rest.CachingRestClient;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author nkala
 */
@Service
public class KayttajanTietoServiceImpl implements KayttajanTietoService {
    private static final String KAYTTAJA_API = "https://itest-virkailija.oph.ware.fi/authentication-service/resources/henkilo/";

    private final ObjectMapper mapper = new ObjectMapper();
    @Override
//    @Cacheable("kayttajantiedot")
    public KayttajanTietoDto hae(String oid) {
        if (oid == null || oid.isEmpty()) {
            throw new BusinessRuleViolationException("Päivitettävää perustetta ei ole olemassa");
        }

        CachingRestClient crc = RestClientFactory.create("https://itest-virkailija.oph.ware.fi/authentication-service/j_spring_cas_security_check");
        KayttajanTietoDto ktd = new KayttajanTietoDto();

        try {
            JsonNode json = mapper.readTree(crc.getAsString(KAYTTAJA_API + oid));
            ktd.setUsername(json.get("kayttajatiedot").get("username").asText());
            ktd.setEtunimet(json.get("etunimet").asText());
            ktd.setKieliKoodi(json.get("asiointiKieli").get("kieliKoodi").asText());
            ktd.setKutsumanimi(json.get("kayttajatiedot").asText());
            ktd.setSukunimi(json.get("kayttajatiedot").asText());
            ktd.setOidHenkilo(json.get("oidHenkilo").asText());
        } catch (IOException e) {
            throw new BusinessRuleViolationException("Käyttäjän tietojen hakeminen epäonnistui");
        }
        return ktd;
    }
}

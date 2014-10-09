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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import fi.vm.sade.eperusteet.service.UlkopuolisetService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.util.RestClientFactory;
import fi.vm.sade.generic.rest.CachingRestClient;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author nkala
 */
@Service
public class UlkopuolisetServiceImpl implements UlkopuolisetService {

    @Value("${cas.service.organisaatio-service:''}")
    private String serviceUrl;

    private static final String OMAT_TIEDOT_API = "/rest/organisaatio/1.2.246.562.10.00000000001/ryhmat";

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    RestClientFactory restClientFactory;

    @Override
    @Transactional
    public JsonNode getRyhmat() {
        CachingRestClient crc = restClientFactory.get(serviceUrl);
        try {
            String url = serviceUrl + OMAT_TIEDOT_API;
            JsonNode tree = mapper.readTree(crc.getAsString(url));
            ArrayNode response = JsonNodeFactory.instance.arrayNode();

            for (JsonNode ryhma : tree) {
                JsonNode kayttoryhmat = ryhma.get("kayttoryhmat");
                for (JsonNode kayttoryhma : kayttoryhmat) {
                    if ("perusteiden_laadinta".equals(kayttoryhma.asText())) {
                        response.add(ryhma);
                        break;
                    }
                }
            }
            return response;
        } catch (IOException ex) {
            throw new BusinessRuleViolationException("Työryhmätietojen hakeminen epäonnistui", ex);
        }
    }
}

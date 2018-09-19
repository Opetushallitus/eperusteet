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
import java.io.IOException;
import java.util.Optional;

import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.OphHttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

/**
 *
 * @author nkala
 */
@Service
public class UlkopuolisetServiceImpl implements UlkopuolisetService {

    @Value("${cas.service.organisaatio-service:''}")
    private String serviceUrl;

    private static final String ORGANISAATIOT = "/rest/organisaatio/";
    private static final String ORGANISAATIORYHMAT = ORGANISAATIOT + "1.2.246.562.10.00000000001/ryhmat";

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    RestClientFactory restClientFactory;

    @Override
    @Transactional
    public JsonNode getRyhma(String organisaatioOid) {

        OphHttpClient client = restClientFactory.get(serviceUrl);

        String url = serviceUrl + ORGANISAATIOT + organisaatioOid;

        OphHttpRequest request = OphHttpRequest.Builder
                .get(url)
                .build();

        return client.<JsonNode>execute(request)
                .handleErrorStatus(SC_UNAUTHORIZED)
                .with(res -> Optional.empty())
                .expectedStatus(SC_OK)
                .mapWith(text -> {
                    try {
                        return mapper.readTree(text);
                    } catch (IOException ex) {
                        throw new BusinessRuleViolationException("Työryhmän tietojen hakeminen epäonnistui", ex);
                    }
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public JsonNode getRyhmat() {

        OphHttpClient client = restClientFactory.get(serviceUrl);

        String url = serviceUrl + ORGANISAATIORYHMAT;

        OphHttpRequest request = OphHttpRequest.Builder
                .get(url)
                .build();

        ArrayNode response = JsonNodeFactory.instance.arrayNode();

        client.<JsonNode>execute(request)
                .handleErrorStatus(SC_UNAUTHORIZED)
                .with(res -> Optional.empty())
                .expectedStatus(SC_OK)
                .mapWith(text -> {
                    try {
                        return mapper.readTree(text);
                    } catch (IOException ex) {
                        throw new BusinessRuleViolationException("Työryhmätietojen hakeminen epäonnistui", ex);
                    }
                })
                .ifPresent(tree -> {
                    for (JsonNode ryhma : tree) {
                        JsonNode kayttoryhmat = ryhma.get("kayttoryhmat");
                        for (JsonNode kayttoryhma : kayttoryhmat) {
                            if ("perusteiden_laadinta".equals(kayttoryhma.asText())) {
                                response.add(ryhma);
                                break;
                            }
                        }
                    }
                });

        return response;
    }
}

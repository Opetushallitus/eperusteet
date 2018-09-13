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

package fi.vm.sade.eperusteet.service.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import fi.vm.sade.javautils.http.OphHttpClient;
import fi.vm.sade.javautils.http.auth.CasAuthenticator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author nkala
 */
@Component
public class RestClientFactory {
    private static final int TIMEOUT = 60000;

    @Value("${fi.vm.sade.eperusteet.oph_username:''}")
    private String username;

    @Value("${fi.vm.sade.eperusteet.oph_password:''}")
    private String password;

    @Value("${web.url.cas:''}")
    private String casUrl;

    private final ConcurrentMap<String, OphHttpClient> cache = new ConcurrentHashMap<>();

    public OphHttpClient get(String service) {
        if (cache.containsKey(service)) {
            return cache.get(service);
        } else {
            CasAuthenticator casAuthenticator = new CasAuthenticator.Builder()
                    .username(username)
                    .password(password)
                    .webCasUrl(casUrl)
                    .casServiceUrl(service)
                    .build();

            OphHttpClient client = new OphHttpClient.Builder(service)
                    .authenticator(casAuthenticator)
                    .timeoutMs(TIMEOUT)
                    .build();

            cache.putIfAbsent(service, client);
            return cache.get(service);
        }
    }
}

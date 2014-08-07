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

import com.fasterxml.jackson.databind.node.ObjectNode;
import fi.vm.sade.eperusteet.dto.KayttajanTietoDto;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.generic.rest.CachingRestClient;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author nkala
 */
@Service
public class KayttajanTietoServiceImpl implements KayttajanTietoService {
    private static final String KAYTTAJA_API = "https://itest-virkailija.oph.ware.fi/authentication-service/resources/henkilo/";

    @Value("${fi.vm.sade.eperusteet.oph_username}")
    private String username;

    @Value("${fi.vm.sade.eperusteet.oph_password}")
    private String password;

    @Override
//    @Cacheable("kayttajantiedot")
    public KayttajanTietoDto hae(String oid) {
        if (oid == null || oid.isEmpty()) {
            throw new BusinessRuleViolationException("Päivitettävää perustetta ei ole olemassa");
        }

        CachingRestClient crc = new CachingRestClient();
        crc.setUsername(username);
        crc.setPassword(password);
        crc.setWebCasUrl("https://itest-virkailija.oph.ware.fi/cas");
        crc.setCasService(KAYTTAJA_API);
        KayttajanTietoDto ktd = new KayttajanTietoDto();
        String res;

        try {
            res = crc.getAsString(KAYTTAJA_API);
        } catch (IOException e) {
            throw new BusinessRuleViolationException("Käyttäjän tietojen hakeminen epäonnistui");
        }

//        ktd.setUsername(json.get("kayttajatiedot").get("username").toString());
//        ktd.setEtunimet(json.get("etunimet").toString());
//        ktd.setKieliKoodi(json.get("asiointiKieli").get("kielikoodi").toString());
//        ktd.setKutsumanimi(json.get("kayttajatiedot").toString());
//        ktd.setSukunimi(json.get("kayttajatiedot").toString());
        return ktd;
    }

}

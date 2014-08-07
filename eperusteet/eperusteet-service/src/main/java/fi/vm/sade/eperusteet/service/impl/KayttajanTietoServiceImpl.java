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

    @Override
//    @Cacheable("kayttajantiedot")
    public KayttajanTietoDto hae(String oid) {
        if (oid == null || oid.isEmpty()) {
            throw new BusinessRuleViolationException("Päivitettävää perustetta ei ole olemassa");
        }
        RestTemplate rt = new RestTemplate();
        ObjectNode json = rt.getForObject(KAYTTAJA_API, ObjectNode.class);
        KayttajanTietoDto ktd = new KayttajanTietoDto();

        ktd.setUsername(json.get("kayttajatiedot").get("username").toString());
        ktd.setEtunimet(json.get("etunimet").toString());
        ktd.setKieliKoodi(json.get("asiointiKieli").get("kielikoodi").toString());
        ktd.setKutsumanimi(json.get("kayttajatiedot").toString());
        ktd.setSukunimi(json.get("kayttajatiedot").toString());
        return ktd;
    }
}

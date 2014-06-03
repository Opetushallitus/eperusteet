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
package fi.vm.sade.eperusteet.service.security;

import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.PerusteprojektiRepository;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

/**
 * Oikeuksien tarkistelu.
 * @author jhyoty
 */
public class PermissionEvaluator implements org.springframework.security.access.PermissionEvaluator {

    @Autowired
    private PerusteRepository perusteet;

    @Autowired
    private PerusteprojektiRepository perusteProjektit;

    private static final Logger LOG = LoggerFactory.getLogger(PermissionEvaluator.class);

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        //TODO
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        //TODO. oikeuksien tarkastelu, nyt varmistaa vain että kohde on olemassa ja käyttäjä on autentikoitunut.
        boolean verdict = exists(targetType, targetId) && authentication.isAuthenticated();
        LOG.warn(String.format("%s to %s{id=%s} by %s: verdict: %s", permission, targetType, targetId, authentication, verdict));
        return verdict;
    }

    private boolean exists(String targetType, Serializable targetId) {

        if (!(targetId instanceof Long )) {
            throw new IllegalArgumentException("Expected Long");
        }

        switch (targetType.toLowerCase()) {
            case "peruste":
                return perusteet.exists((Long) targetId);
            case "perusteprojekti":
                return perusteProjektit.exists((Long) targetId);
            default:
                throw new IllegalArgumentException(targetType);
        }
    }

}

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

import java.security.Principal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author jhyoty
 */
public final class SecurityUtil {

    private SecurityUtil() {
        //helper class
    }

    public static Principal getAuthenticatedPrincipal() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static void allow(String principalName) {
        Principal p = getAuthenticatedPrincipal();
        if ( p == null || !p.getName().equals(principalName)) {
            if (p != null) {
                throw new AccessDeniedException("Pääsy evätty (" + p + " != " + principalName + ")");
            } else {
                throw new AccessDeniedException("Pääsy evätty (null != " + principalName + ")");
            }
        }
    }

    public static boolean isAuthenticated() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.isAuthenticated();
    }
}

package fi.vm.sade.eperusteet.service.util;

import java.security.Principal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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

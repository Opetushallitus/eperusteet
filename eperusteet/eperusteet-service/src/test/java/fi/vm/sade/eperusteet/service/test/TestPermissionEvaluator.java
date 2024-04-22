package fi.vm.sade.eperusteet.service.test;

import java.io.Serializable;
import org.springframework.security.core.Authentication;

/**
 * Oikeuksien tarkistelu.
 */
public class TestPermissionEvaluator implements org.springframework.security.access.PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        return authentication.isAuthenticated();
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return authentication.isAuthenticated();
    }
}

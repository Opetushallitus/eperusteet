package fi.vm.sade.eperusteet.service.security;

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.ReferenceableEntity;
import fi.vm.sade.eperusteet.service.security.PermissionManager.Permission;
import fi.vm.sade.eperusteet.service.security.PermissionManager.Target;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import java.io.Serializable;

/**
 * Oikeuksien tarkistelu.
 */
public class PermissionEvaluator implements org.springframework.security.access.PermissionEvaluator {

    @Autowired
    PermissionManager manager;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (targetDomainObject instanceof ReferenceableEntity) {
            String targetType;
            if (targetDomainObject instanceof PerusteenOsa) {
                targetType = "perusteenosa";
            } else {
                targetType = targetDomainObject.getClass().getSimpleName();
            }
            return hasPermission(authentication, ((ReferenceableEntity) targetDomainObject).getId(), targetType, permission);
        }
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (permission instanceof Object[]) {
            Object[] perms = (Object[]) permission;
            for (Object p : perms) {
                final boolean result
                    = manager.hasPermission(
                        authentication,
                        targetId,
                        Target.valueOf(targetType.toUpperCase()), Permission.valueOf(p.toString().toUpperCase()));
                if (result) {
                    return true;
                }
            }
            return false;
        }

        if (targetId instanceof String) {
            targetId = Long.valueOf((String)targetId);
        }

        return manager
            .hasPermission(authentication, targetId, Target.valueOf(targetType.toUpperCase()), Permission.valueOf(permission.toString().toUpperCase()));
    }
}

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

import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.ReferenceableEntity;
import fi.vm.sade.eperusteet.service.security.PermissionManager.Permission;
import fi.vm.sade.eperusteet.service.security.PermissionManager.Target;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import java.io.Serializable;

/**
 * Oikeuksien tarkistelu.
 *
 * @author jhyoty
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

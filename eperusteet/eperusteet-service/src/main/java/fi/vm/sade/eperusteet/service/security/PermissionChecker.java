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

import java.io.Serializable;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Apuluokka oikeuksien ohjelmalliseen tarkisteluun tilanteissa joissa annotaatiot eivät riitä.
 * @author jhyoty
 */
@Service
public class PermissionChecker {

    @PreAuthorize("hasPermission(#o,#p)")
    public void checkPermission(@P("o") Object entity, @P("p") String permission) {
        //this function is intentionally left blank
    }

    @PreAuthorize("hasPermission(#tid, #t.toString(), #p.toString())")
    public void checkPermission(@P("tid") Serializable targetId, @P("t") PermissionManager.Target targetType, @P("p") PermissionManager.Permission perm) {
        //this function is intentionally left blank
    }
}

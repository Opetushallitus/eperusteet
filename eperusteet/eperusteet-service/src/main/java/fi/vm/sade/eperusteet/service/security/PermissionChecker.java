package fi.vm.sade.eperusteet.service.security;

import java.io.Serializable;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Apuluokka oikeuksien ohjelmalliseen tarkisteluun tilanteissa joissa annotaatiot eivät riitä.
 */
@Service
public class PermissionChecker {

    @PreAuthorize("hasPermission(#o,#p)")
    public void checkPermission(@P("o") Object entity, @P("p") String permission) {
        //this function is intentionally left blank
    }

    @PreAuthorize("hasPermission(#tid, #t.toString(), #p)")
    public void checkPermission(@P("tid") Serializable targetId, @P("t") PermissionManager.Target targetType, @P("p") PermissionManager.Permission... perm) {
        //this function is intentionally left blank
    }
}

/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.yl.lukio.Aihekokonaisuus;
import fi.vm.sade.eperusteet.service.impl.AbstractLockService;
import fi.vm.sade.eperusteet.service.security.PermissionManager;
import fi.vm.sade.eperusteet.service.yl.AihekokonaisuusLockContext;

/**
 * User: jsikio
 */
public abstract class AbstractAihekokonaisuusLockService extends AbstractLockService<AihekokonaisuusLockContext> {
    @Override
    protected Long getLockId(AihekokonaisuusLockContext ctx) {
        return ctx.getAihekokonaisuusId();
    }

    protected void checkPermissionToPEruste(AihekokonaisuusLockContext ctx, boolean readOnly) {
        if ( readOnly ) {
            permissionChecker.checkPermission(ctx.getPerusteId(), PermissionManager.Target.PERUSTE, PermissionManager.Permission.LUKU);
        } else {
            permissionChecker.checkPermission(ctx.getPerusteId(), PermissionManager.Target.PERUSTE, PermissionManager.Permission.MUOKKAUS, PermissionManager.Permission.KORJAUS);
        }
    }

    protected abstract Aihekokonaisuus getAihekokonaisuus(AihekokonaisuusLockContext ctx);

    @Override
    protected Long validateCtx(AihekokonaisuusLockContext ctx, boolean readOnly) {
        checkPermissionToPEruste(ctx, readOnly);
        Aihekokonaisuus aihekokonaisuus = getAihekokonaisuus(ctx);
        return aihekokonaisuus.getId();
    }
}

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
package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.yl.LaajaalainenOsaaminen;
import fi.vm.sade.eperusteet.repository.LaajaalainenOsaaminenRepository;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.impl.AbstractLockService;
import fi.vm.sade.eperusteet.service.security.PermissionManager;
import fi.vm.sade.eperusteet.service.yl.LaajaalainenOsaaminenContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author jhyoty
 */
@Service
@LockCtx(LaajaalainenOsaaminenContext.class)
public class LaajaalainenOsaaminenLockServiceImpl extends AbstractLockService<LaajaalainenOsaaminenContext> {

    @Autowired
    private LaajaalainenOsaaminenRepository osaaminenRepository;

    @Override
    protected Long getLockId(LaajaalainenOsaaminenContext ctx) {
        return ctx.getOsaaminenId();
    }

    @Override
    protected final Long validateCtx(LaajaalainenOsaaminenContext ctx, boolean readOnly) {
        if ( readOnly ) {
            permissionChecker.checkPermission(ctx.getPerusteId(), PermissionManager.Target.PERUSTE, PermissionManager.Permission.LUKU);
        } else {
            permissionChecker.checkPermission(ctx.getPerusteId(), PermissionManager.Target.PERUSTE, PermissionManager.Permission.MUOKKAUS, PermissionManager.Permission.KORJAUS);
        }
        LaajaalainenOsaaminen osaaminen = osaaminenRepository.findBy(ctx.getPerusteId(), ctx.getOsaaminenId());
        if (osaaminen == null) {
            throw new BusinessRuleViolationException("Virheellinen lukitus");
        }
        return osaaminen.getId();
    }

    @Override
    protected final int latestRevision(LaajaalainenOsaaminenContext ctx) {
        //olettaa että lockcontext on validi (ei tarkisteta erikseen)
        return osaaminenRepository.getLatestRevisionId(ctx.getOsaaminenId()).getNumero();
    }
}

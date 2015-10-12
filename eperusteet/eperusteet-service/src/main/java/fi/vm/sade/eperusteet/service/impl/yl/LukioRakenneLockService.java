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

import fi.vm.sade.eperusteet.repository.LukiokoulutuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.impl.AbstractLockService;
import fi.vm.sade.eperusteet.service.yl.LukioRakenneLockContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static fi.vm.sade.eperusteet.service.util.OptionalUtil.found;

/**
 * User: tommiratamaa
 * Date: 12.10.15
 * Time: 15.47
 */
@Service
@LockCtx(LukioRakenneLockContext.class)
public class LukioRakenneLockService extends AbstractLockService<LukioRakenneLockContext>
            implements LockService<LukioRakenneLockContext> {
    @Autowired
    private LukiokoulutuksenPerusteenSisaltoRepository lukiokoulutuksenPerusteenSisaltoRepository;

    @Override
    protected Long getLockId(LukioRakenneLockContext ctx) {
        return found(lukiokoulutuksenPerusteenSisaltoRepository.findByPerusteId(ctx.getPerusteId())).getId();
    }

    @Override
    protected Long validateCtx(LukioRakenneLockContext ctx, boolean readOnly) {
        checkPermissionToPeruste(ctx, readOnly);
        return getLockId(ctx);
    }

    @Override
    protected int latestRevision(LukioRakenneLockContext ctx) {
        return lukiokoulutuksenPerusteenSisaltoRepository.getLatestRevisionId(getLockId(ctx)).getNumero();
    }
}

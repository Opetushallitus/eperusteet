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
package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.ReferenceableEntity;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.internal.LockManager;
import fi.vm.sade.eperusteet.service.security.PermissionChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author jhyoty
 */
public abstract class AbstractLockService<T> implements LockService<T> {

    @Autowired
    private LockManager manager;

    @Autowired
    protected PermissionChecker permissionChecker;

    @Override
    @Transactional(readOnly = true)
    public LukkoDto getLock(T ctx) {
        return LukkoDto.of(manager.getLock(validateCtx(ctx).getId()));
    }

    @Override
    @Transactional
    public LukkoDto lock(T ctx) {
        return LukkoDto.of(manager.lock(validateCtx(ctx).getId()));
    }

    @Override
    @Transactional
    public void unlock(T ctx) {
        manager.unlock(validateCtx(ctx).getId());
    }

    @Override
    @Transactional(readOnly = true)
    public void assertLock(T ctx) {
        manager.ensureLockedByAuthenticatedUser(validateCtx(ctx).getId());
    }

    /**
     * Varmistaa että lukituskonteksti on validi
     * @param ctx
     * @return kontekstia vastaavan lukittavan entiteetin
     */
    protected abstract ReferenceableEntity validateCtx(T ctx);

}

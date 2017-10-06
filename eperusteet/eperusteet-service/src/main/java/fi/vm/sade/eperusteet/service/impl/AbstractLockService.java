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

import fi.vm.sade.eperusteet.domain.Lukko;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.PerusteAware;
import fi.vm.sade.eperusteet.service.internal.LockManager;
import fi.vm.sade.eperusteet.service.security.PermissionChecker;
import fi.vm.sade.eperusteet.service.security.PermissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
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
        Lukko lock = manager.getLock(validateCtx(ctx, true));
        return lock == null ? null : LukkoDto.of(lock, latestRevision(ctx));
    }

    @Override
    @Transactional
    public LukkoDto lock(T ctx) {
        return lock(ctx, null);
    }

    @Override
    @Transactional
    public LukkoDto lock(T ctx, Integer ifMatchRevision) {
        Long key = validateCtx(ctx, false);
        final int latestRevision = latestRevision(ctx);
        if (ifMatchRevision == null || latestRevision == ifMatchRevision) {
            return LukkoDto.of(manager.lock(key), latestRevision);
        }
        return null;
    }

    @Override
    @Transactional
    public void unlock(T ctx) {
        Long lockId = getLockId(ctx);
        if (lockId != null) {
            manager.unlock(lockId);
        }
    }

    protected void checkPermissionToPeruste(PerusteAware ctx, boolean readOnly) {
        if (readOnly) {
            permissionChecker.checkPermission(ctx.getPerusteId(), PermissionManager.Target.PERUSTE, PermissionManager.Permission.LUKU);
        } else {
            permissionChecker.checkPermission(ctx.getPerusteId(), PermissionManager.Target.PERUSTE, PermissionManager.Permission.MUOKKAUS, PermissionManager.Permission.KORJAUS);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public void assertLock(T ctx) {
        manager.ensureLockedByAuthenticatedUser(validateCtx(ctx, true));
    }

    /**
     * Palauttaa kontekstia vastaavan lukittavan entiteetin pääavaimen tai null jos tätä ei voi selvittää (entiteetti on poistettu tms.).
     *
     * @param ctx
     * @return kontekstia vastaavan lukittavan entiteetin pääavaimen
     */
    protected abstract Long getLockId(T ctx);

    /**
     * Varmistaa että lukituskonteksti on validi ja käyttäjällä on oikeudet lukitukseen tai sen kyselyyn.
     *
     * @param ctx
     * @return kontekstia vastaavan lukittavan entiteetin pääavaimen (Mahdollistaa optimoinnin jos getLockId joutuu tekemään tietokantahakuja).
     */
    protected abstract Long validateCtx(T ctx, boolean readOnly);

    /**
     * Varmistaa että lukituskonteksti on validi
     *
     * @param ctx
     * @return kontekstia vastaavan lukittavan entiteetin
     */
    protected abstract int latestRevision(T ctx);

}

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

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.RakenneRepository;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.TutkinnonRakenneLockContext;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.security.PermissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author jhyoty
 */
@Service
@LockCtx(TutkinnonRakenneLockContext.class)
public class RakenneLockServiceImpl extends AbstractLockService<TutkinnonRakenneLockContext> {

    @Autowired
    private PerusteRepository perusteet;
    @Autowired
    private RakenneRepository rakenteet;

    @Override
    protected Long getLockId(TutkinnonRakenneLockContext ctx) {
        Peruste peruste = perusteet.findOne(ctx.getPerusteId());
        if (peruste != null) {
            return peruste.getSuoritustapa(ctx.getKoodi()).getRakenne().getId();
        }
        return null;
    }

    @Override
    protected int latestRevision(TutkinnonRakenneLockContext ctx) {
        final RakenneModuuli rakenne = perusteet.findOne(ctx.getPerusteId()).getSuoritustapa(ctx.getKoodi()).getRakenne();
        return rakenteet.getLatestRevisionId(rakenne.getId()).getNumero();
    }

    @Override
    protected Long validateCtx(TutkinnonRakenneLockContext ctx, boolean readOnly) {
        Peruste peruste = perusteet.findOne(ctx.getPerusteId());
        if (peruste != null) {
            if (readOnly) {
                permissionChecker.checkPermission(ctx.getPerusteId(), PermissionManager.Target.PERUSTE, PermissionManager.Permission.LUKU);
            } else if (peruste.getTila() == PerusteTila.VALMIS) {
                permissionChecker.checkPermission(ctx.getPerusteId(), PermissionManager.Target.PERUSTE, PermissionManager.Permission.KORJAUS);
            } else {
                permissionChecker.checkPermission(ctx.getPerusteId(), PermissionManager.Target.PERUSTE, PermissionManager.Permission.MUOKKAUS);
            }
            return peruste.getSuoritustapa(ctx.getKoodi()).getRakenne().getId();
        }
        throw new BusinessRuleViolationException("Perustetta ei ole");
    }

}

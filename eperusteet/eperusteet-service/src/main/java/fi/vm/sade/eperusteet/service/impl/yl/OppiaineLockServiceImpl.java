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

import fi.vm.sade.eperusteet.domain.yl.AbstractOppiaineOpetuksenSisalto;
import fi.vm.sade.eperusteet.domain.yl.Oppiaine;
import fi.vm.sade.eperusteet.domain.yl.OppiaineenVuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.domain.yl.VuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.repository.OppiaineRepository;
import fi.vm.sade.eperusteet.repository.OppiaineenVuosiluokkakokonaisuusRepository;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.impl.AbstractLockService;
import fi.vm.sade.eperusteet.service.security.PermissionManager;
import fi.vm.sade.eperusteet.service.yl.OppiaineLockContext;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 *
 * @author jhyoty
 */
@Service
@LockCtx(OppiaineLockContext.class)
public class OppiaineLockServiceImpl extends AbstractLockService<OppiaineLockContext> {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private OppiaineRepository oppiaineRepository;

    @Autowired
    private OppiaineenVuosiluokkakokonaisuusRepository vuosiluokkakokonaisuusRepository;

    @Override
    protected Long getLockId(OppiaineLockContext ctx) {
        return ctx.getKokonaisuusId() == null ? ctx.getOppiaineId() : ctx.getKokonaisuusId();
    }

    @Override
    protected final Long validateCtx(OppiaineLockContext ctx, boolean readOnly) {
        checkPermissionToPeruste(ctx, readOnly);

        //TODO: haun optimointi
        AbstractOppiaineOpetuksenSisalto s = ctx.getTyyppi().getRepository(applicationContext).findByPerusteId(ctx.getPerusteId());

        Oppiaine aine = oppiaineRepository.findOne(ctx.getOppiaineId());
        if (s == null || !s.containsOppiaine(aine)) {
            throw new BusinessRuleViolationException("Virheellinen lukitus");
        }

        if (ctx.getKokonaisuusId() != null) {
            OppiaineenVuosiluokkaKokonaisuus ovk = vuosiluokkakokonaisuusRepository.findByIdAndOppiaineId(ctx.getKokonaisuusId(), ctx.getOppiaineId());
            if (ovk == null) {
                throw new BusinessRuleViolationException("Virheellinen lukitus");
            }
            return ovk.getId();
        }
        return aine.getId();
    }

    @Override
    protected final int latestRevision(OppiaineLockContext ctx) {
        //olettaa että lockcontext on validi (ei tarkisteta erikseen)
        if ( ctx.getKokonaisuusId() != null ) {
            return vuosiluokkakokonaisuusRepository.getLatestRevisionId(ctx.getKokonaisuusId()).getNumero();
        }

        return oppiaineRepository.getLatestRevisionId(ctx.getOppiaineId()).getNumero();
    }

}

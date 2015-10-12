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
import fi.vm.sade.eperusteet.repository.LukioAihekokonaisuusRepository;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.impl.AbstractLockService;
import fi.vm.sade.eperusteet.service.yl.AihekokonaisuusLockContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static fi.vm.sade.eperusteet.service.util.OptionalUtil.found;

/**
 * User: jsikio
 */
@Service
@LockCtx(AihekokonaisuusLockContext.class)
public class LukioAihekokonaisuusLockService extends AbstractLockService<AihekokonaisuusLockContext> {

    @Autowired
    private LukioAihekokonaisuusRepository lukioAihekokonaisuusRepository;

    protected Aihekokonaisuus getAihekokonaisuus(AihekokonaisuusLockContext ctx) {
        return found(lukioAihekokonaisuusRepository.getOne(ctx.getAihekokonaisuusId()), Aihekokonaisuus.inPeruste(ctx.getPerusteId()));
    }

    @Override
    protected int latestRevision(AihekokonaisuusLockContext ctx) {
        return lukioAihekokonaisuusRepository.getLatestRevisionId(ctx.getAihekokonaisuusId()).getNumero();
    }

    @Override
    protected Long getLockId(AihekokonaisuusLockContext ctx) {
        return ctx.getAihekokonaisuusId();
    }

    @Override
    protected Long validateCtx(AihekokonaisuusLockContext ctx, boolean readOnly) {
        checkPermissionToPeruste(ctx, readOnly);
        Aihekokonaisuus aihekokonaisuus = getAihekokonaisuus(ctx);
        return aihekokonaisuus.getId();
    }
}

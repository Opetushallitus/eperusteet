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

import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.VuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.repository.PerusopetuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.repository.VuosiluokkaKokonaisuusRepository;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.impl.AbstractLockService;
import fi.vm.sade.eperusteet.service.security.PermissionManager;
import fi.vm.sade.eperusteet.service.yl.VuosiluokkaKokonaisuusContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author jhyoty
 */
@Service
@LockCtx(VuosiluokkaKokonaisuusContext.class)
public class VuosiluokkakokonaisuusLockServiceImpl extends AbstractLockService<VuosiluokkaKokonaisuusContext> {

    @Autowired
    private PerusopetuksenPerusteenSisaltoRepository sisallot;

    @Autowired
    private VuosiluokkaKokonaisuusRepository kokonaisuudet;

    @Override
    protected final Long validateCtx(VuosiluokkaKokonaisuusContext ctx, boolean readOnly) {
        final PermissionManager.Permission permission = readOnly ? PermissionManager.Permission.LUKU : PermissionManager.Permission.MUOKKAUS;
        permissionChecker.checkPermission(ctx.getPerusteId(), PermissionManager.Target.PERUSTE, permission);

        PerusopetuksenPerusteenSisalto s = sisallot.findByPerusteId(ctx.getPerusteId());
        VuosiluokkaKokonaisuus kokonaisuus = kokonaisuudet.findOne(ctx.getKokonaisuusId());
        if (s == null || !s.containsVuosiluokkakokonaisuus(kokonaisuus)) {
            throw new BusinessRuleViolationException("Virheellinen lukitus");
        }
        return kokonaisuus.getId();

    }

    @Override
    protected final int latestRevision(VuosiluokkaKokonaisuusContext ctx) {
        //olettaa että lockcontext on validi (ei tarkisteta erikseen)
        return kokonaisuudet.getLatestRevisionId(ctx.getKokonaisuusId());
    }

}

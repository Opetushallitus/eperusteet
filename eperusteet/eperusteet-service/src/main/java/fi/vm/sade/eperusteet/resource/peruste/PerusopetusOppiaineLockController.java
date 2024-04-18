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
package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.resource.AbstractLockService;
import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.yl.OppiaineLockContext;
import fi.vm.sade.eperusteet.service.yl.OppiaineOpetuksenSisaltoTyyppi;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 *
 * @author jhyoty
 */
@RestController
@InternalApi
@Api(value = "PerusopetusOppiaineLukko")
@RequestMapping("/api/perusteet/{perusteId}/perusopetus/oppiaineet/{oppiaineId}/lukko")
public class PerusopetusOppiaineLockController extends AbstractLockService<OppiaineLockContext> {

    @Autowired
    @LockCtx(OppiaineLockContext.class)
    private LockService<OppiaineLockContext> service;

    @Override
    protected void handleContext(OppiaineLockContext ctx) {
        ctx.setTyyppi(OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS);
    }

    @Override
    protected LockService<OppiaineLockContext> service() {
        return service;
    }

    @RequestMapping(method = GET)
    public ResponseEntity<LukkoDto> checkLockPerusopetusOppiaine(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("oppiaineId") final Long oppiaineId) {
        return super.checkLock(OppiaineLockContext.of(OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS, perusteId, oppiaineId, null));
    }

    @RequestMapping(method = POST)
    public ResponseEntity<LukkoDto> lockPerusopetusOppiaine(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("oppiaineId") final Long oppiaineId,
            @RequestHeader(value = "If-Match", required = false) String eTag) {
        return super.lock(OppiaineLockContext.of(OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS, perusteId, oppiaineId, null), eTag);
    }

    @RequestMapping(method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlockPerusopetusOppiaine(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("oppiaineId") final Long oppiaineId) {
        super.unlock(OppiaineLockContext.of(OppiaineOpetuksenSisaltoTyyppi.PERUSOPETUS, perusteId, oppiaineId, null));
    }
}

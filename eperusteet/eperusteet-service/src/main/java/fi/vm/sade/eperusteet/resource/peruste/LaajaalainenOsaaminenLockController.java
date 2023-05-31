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
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.yl.LaajaalainenOsaaminenContext;
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

@RestController
@InternalApi
@Api(value = "PerusopetusLaajaAlainenOsaaminenLukko")
@RequestMapping("/perusteet/{perusteId}/perusopetus/laajaalaisetosaamiset/{osaaminenId}/lukko")
public class LaajaalainenOsaaminenLockController extends AbstractLockService<LaajaalainenOsaaminenContext> {
    @Autowired
    @LockCtx(LaajaalainenOsaaminenContext.class)
    private LockService<LaajaalainenOsaaminenContext> service;

    @Override
    protected LockService<LaajaalainenOsaaminenContext> service() {
        return service;
    }

    @RequestMapping(method = GET)
    public ResponseEntity<LukkoDto> checkLockPerusopetusLao(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("osaaminenId") final Long osaaminenId) {
        return super.checkLock(LaajaalainenOsaaminenContext.of(perusteId, osaaminenId));
    }

    @RequestMapping(method = POST)
    public ResponseEntity<LukkoDto> lockPerusopetusLao(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("osaaminenId") final Long osaaminenId,
            @RequestHeader(value = "If-Match", required = false) String eTag) {
       return super.lock(LaajaalainenOsaaminenContext.of(perusteId, osaaminenId), eTag);
    }

    @RequestMapping(method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlockPerusopetusLao(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("osaaminenId") final Long osaaminenId) {
        super.unlock(LaajaalainenOsaaminenContext.of(perusteId, osaaminenId));
    }
}

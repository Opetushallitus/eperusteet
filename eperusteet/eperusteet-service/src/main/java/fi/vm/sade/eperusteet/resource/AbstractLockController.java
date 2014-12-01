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
package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.service.LockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import static fi.vm.sade.eperusteet.resource.util.Etags.eTagHeader;
import static fi.vm.sade.eperusteet.resource.util.Etags.revisionOf;
import fi.vm.sade.eperusteet.service.internal.LockManager;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 *
 * @author jhyoty
 * @param <T>
 */
public abstract class AbstractLockController<T> {

    @Autowired
    private LockManager lukkomanageri;

    @RequestMapping(method = GET)
    public ResponseEntity<LukkoDto> checkLock(T ctx) {
        LukkoDto lock = service().getLock(ctx);
        lukkomanageri.lisaaNimiLukkoon(lock);
        return lock == null ? new ResponseEntity<LukkoDto>(HttpStatus.NOT_FOUND) : new ResponseEntity<>(lock, eTagHeader(lock.getRevisio()), HttpStatus.OK);
    }

    @RequestMapping(method = POST)
    public ResponseEntity<LukkoDto> lock(T ctx,
        @RequestHeader(value = "If-Match", required = false) String eTag) {
        LukkoDto lock = service().lock(ctx, revisionOf(eTag));
        if (lock == null) {
            return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
        } else {
            return new ResponseEntity<>(lock, eTagHeader(lock.getRevisio()), HttpStatus.CREATED);
        }

    }

    @RequestMapping(method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlock(T ctx) {
        service().unlock(ctx);
    }

    protected abstract LockService<T> service();
}

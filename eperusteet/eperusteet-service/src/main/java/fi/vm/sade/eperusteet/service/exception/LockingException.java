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

package fi.vm.sade.eperusteet.service.exception;

import fi.vm.sade.eperusteet.dto.LukkoDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author jhyoty
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class LockingException extends ServiceException {

    private final LukkoDto lukko;

    public LockingException(String message) {
        super(message);
        lukko = null;
    }

    public LockingException(String message, Throwable cause) {
        super(message, cause);
        this.lukko = null;
    }

    public LockingException(String message, LukkoDto lukko) {
        super(message);
        this.lukko = lukko;
    }

    public LukkoDto getLukko() {
        return lukko;
    }
    
}

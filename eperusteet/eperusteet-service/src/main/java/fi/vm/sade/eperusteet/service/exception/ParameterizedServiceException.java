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

import lombok.Getter;

import java.util.Map;

public abstract class ParameterizedServiceException extends ServiceException {

    @Getter
    private Map<String, Object> parameters;

    public ParameterizedServiceException(String message) {
        super(message);
    }

    public ParameterizedServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParameterizedServiceException(String message, Map<String, Object> parameters) {
        super(message);
        this.parameters = parameters;
    }

    public ParameterizedServiceException(String message, Throwable cause, Map<String, Object> parameters) {
        super(message, cause);
        this.parameters = parameters;
    }
}

package fi.vm.sade.eperusteet.service.exception;

import org.springframework.core.NestedRuntimeException;

/**
 *  Kantaluokka palvelukerroksen poikkeuksille
 */
public abstract class ServiceException extends NestedRuntimeException {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}

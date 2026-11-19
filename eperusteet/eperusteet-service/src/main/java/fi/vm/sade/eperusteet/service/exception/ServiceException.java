package fi.vm.sade.eperusteet.service.exception;

import org.springframework.core.NestedRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *  Kantaluokka palvelukerroksen poikkeuksille
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ServiceException extends NestedRuntimeException {

    public ServiceException(Throwable cause) {
      super("jarjestelmavirhe-ohje", cause);
    }

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}

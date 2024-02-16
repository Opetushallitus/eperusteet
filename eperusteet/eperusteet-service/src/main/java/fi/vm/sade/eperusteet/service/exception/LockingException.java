package fi.vm.sade.eperusteet.service.exception;

import fi.vm.sade.eperusteet.dto.LukkoDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

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

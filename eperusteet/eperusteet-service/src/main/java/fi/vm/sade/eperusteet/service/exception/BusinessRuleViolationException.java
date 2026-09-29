package fi.vm.sade.eperusteet.service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessRuleViolationException extends ServiceException {

    @Getter
    private Object data;

    public BusinessRuleViolationException(String message) {
        super(message);
    }

    public BusinessRuleViolationException(String message, Object data) {
        super(message);
        this.data = data;
    }

    public BusinessRuleViolationException(String message, Throwable cause) {
        super(message, cause);
    }

}

package fi.vm.sade.eperusteet.service.exception;

import fi.vm.sade.eperusteet.service.util.Validointi;
import lombok.Getter;
import org.springframework.core.NestedRuntimeException;

public class ValidointiException extends NestedRuntimeException {
    @Getter
    Validointi validointi;

    public ValidointiException(Validointi validointi) {
        super("ops-validointivirheita");
        this.validointi = validointi;
    }

}

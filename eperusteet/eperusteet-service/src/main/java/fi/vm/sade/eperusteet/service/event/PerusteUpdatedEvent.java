package fi.vm.sade.eperusteet.service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class PerusteUpdatedEvent extends ApplicationEvent {
    @Getter
    private final Long perusteId;

    public PerusteUpdatedEvent(Object source, Long perusteId) {
        super(source);
        this.perusteId = perusteId;
    }

    public static PerusteUpdatedEvent of(Object source, Long perusteId) {
        return new PerusteUpdatedEvent(source, perusteId);
    }

}

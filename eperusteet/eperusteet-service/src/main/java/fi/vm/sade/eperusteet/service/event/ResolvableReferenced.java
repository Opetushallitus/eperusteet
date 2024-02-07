package fi.vm.sade.eperusteet.service.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class ResolvableReferenced {
    private final Class<?> entityClass;
    private final long id;

    public ResolvableReferenced(Class<?> clz, long id) {
        this.entityClass = clz;
        this.id = id;
    }
}

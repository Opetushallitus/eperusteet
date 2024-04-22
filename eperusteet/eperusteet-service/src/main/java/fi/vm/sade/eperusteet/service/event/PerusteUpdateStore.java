package fi.vm.sade.eperusteet.service.event;

import java.util.Set;

public interface PerusteUpdateStore {
    void perusteUpdated(long perusteId);

    void resolveRelationLater(Class<?> clz, long id);

    void enter();

    int leave();

    Set<Long> getAndClearUpdatedPerusteIds();

    Set<ResolvableReferenced> getAndClearReferenced();
}

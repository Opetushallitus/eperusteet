package fi.vm.sade.eperusteet.service.event.impl;

import fi.vm.sade.eperusteet.service.event.PerusteUpdateStore;
import fi.vm.sade.eperusteet.service.event.ResolvableReferenced;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Not thread-safe and do not have to be (since request-scoped).
 */
@Component
@Scope(value="request", proxyMode = ScopedProxyMode.INTERFACES)
public class PerusteUpdateStoreImpl implements PerusteUpdateStore {
    private Set<Long> perusteIds = new HashSet<>();
    private int stackDept = 0;
    private Set<ResolvableReferenced> resolvableReferences = new HashSet<>();

    @Override
    public void perusteUpdated(long perusteId) {
        this.perusteIds.add(perusteId);
    }

    @Override
    public void resolveRelationLater(Class<?> clz, long id) {
        this.resolvableReferences.add(new ResolvableReferenced(clz, id));
    }

    @Override
    public void enter() {
        stackDept++;
    }

    @Override
    public int leave() {
        return --stackDept;
    }

    @Override
    public Set<Long> getAndClearUpdatedPerusteIds() {
        Set<Long> value = new HashSet<>(perusteIds);
        this.perusteIds = new HashSet<>();
        return value;
    }

    @Override
    public Set<ResolvableReferenced> getAndClearReferenced() {
        Set<ResolvableReferenced> list = new HashSet<>(this.resolvableReferences);
        this.resolvableReferences = new HashSet<>();
        return list;
    }
}

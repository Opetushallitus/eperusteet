/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.service.event.impl;

import fi.vm.sade.eperusteet.service.event.PerusteUpdateStore;
import fi.vm.sade.eperusteet.service.event.ResolvableReferenced;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Should be a thread-safe
 *
 * User: tommiratamaa
 * Date: 27.11.2015
 * Time: 13.42
 */
@Component
public class PerusteUpdateStoreImpl implements PerusteUpdateStore {
    private Set<Long> perusteIds = Collections.synchronizedSet(new HashSet<>());
    private AtomicInteger stackDept = new AtomicInteger();
    private Set<ResolvableReferenced> resolvableReferences = Collections.synchronizedSet(new HashSet<>());

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
        stackDept.incrementAndGet();
    }

    @Override
    public int leave() {
        return stackDept.getAndDecrement();
    }

    @Override
    public synchronized Set<Long> getAndClearUpdatedPerusteIds() {
        Set<Long> value = new HashSet<>(perusteIds);
        this.perusteIds = new HashSet<>();
        return value;
    }

    @Override
    public synchronized Set<ResolvableReferenced> getAndClearReferenced() {
        Set<ResolvableReferenced> list = new HashSet<>(this.resolvableReferences);
        this.resolvableReferences = new HashSet<>();
        return list;
    }
}

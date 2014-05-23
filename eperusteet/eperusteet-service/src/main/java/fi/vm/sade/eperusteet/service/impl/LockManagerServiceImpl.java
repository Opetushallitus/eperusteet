/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.service.LockManagerService;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 *
 * @author jhyoty
 */
@Service
public class LockManagerServiceImpl implements LockManagerService {

    //TODO. Väliaikainen toteutus, ei ota huomioon hajautusta.
    //Täytyy toteuttaa tietokannan (ehcache?) tms. avulla jotta palvelun kahdennus toimii.
    private final ConcurrentMap<Serializable, LukkoDto> locks = new ConcurrentHashMap<>(256);

    @Override
    @PreAuthorize("isAuthenticated()")
    public boolean lock(Serializable id) {
        final String oid = SecurityUtil.getAuthenticatedPrincipal().getName();
        final LukkoDto newLukko = new LukkoDto(oid);

        LukkoDto current = locks.putIfAbsent(id, newLukko);
        if (current != null) {
            if (oid.equals(current.getHaltijaOid())) {
                //yritä uusia lukko
                locks.replace(id, current, newLukko);
                current = locks.get(id);
            } else if (current.getLuotu().plusMinutes(10).isBeforeNow()) {
                //yritä "varastaa" lukko
                locks.remove(id, current);
                current = locks.putIfAbsent(id, newLukko);
            }
        }
        return current == null || oid.equals(current.getHaltijaOid());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public boolean isLockedByAuthenticatedUser(Serializable id) {
        return isOwnedByAuthenticatedUser(locks.get(id));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public boolean unlock(Serializable id) {
        LukkoDto l = getLock(id);
        return l == null || (isOwnedByAuthenticatedUser(l) && locks.remove(id, l));
    }

    @Override
    public LukkoDto getLock(Serializable id) {
        return locks.get(id);
    }

    private boolean isOwnedByAuthenticatedUser(LukkoDto lukko) {
        final String oid = SecurityUtil.getAuthenticatedPrincipal().getName();
        return lukko != null && Objects.equals(lukko.getHaltijaOid(), oid);
    }

}

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

import fi.vm.sade.eperusteet.domain.Lukko;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.service.LockManager;
import fi.vm.sade.eperusteet.service.exception.LockingException;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 * @author jhyoty
 */
@Component
public class LockManagerImpl implements LockManager {

    private final TransactionTemplate transaction;

    @Autowired
    private EntityManager em;

    @Autowired
    public LockManagerImpl(PlatformTransactionManager manager) {
        transaction = new TransactionTemplate(manager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Value("${fi.vm.sade.eperusteet.lukitus.aikaMinuutteina}")
    private int maxLockTime;

    @PreAuthorize("isAuthenticated()")
    @Override
    public Lukko lock(final Long id) {

        final String oid = SecurityUtil.getAuthenticatedPrincipal().getName();
        final Lukko newLukko = new Lukko(id, oid);

        Lukko lukko;
        try {
            lukko = transaction.execute(new TransactionCallback<Lukko>() {
                @Override
                public Lukko doInTransaction(TransactionStatus status) {
                    Lukko current = em.find(Lukko.class, id);
                    if (current != null) {
                        em.refresh(current, LockModeType.PESSIMISTIC_WRITE);
                        if (oid.equals(current.getHaltijaOid())) {
                            current.refresh();
                        } else if (current.getLuotu().plusMinutes(maxLockTime).isBeforeNow()) {
                            em.remove(current);
                            em.persist(newLukko);
                            current = newLukko;
                        }
                    } else {
                        em.persist(newLukko);
                        current = newLukko;
                    }
                    return current;
                }
            });
        } catch (TransactionException t) {
            // (todennäköisesti) samanaikaisesti toisessa transaktiossa lisätty sama lukko, yritetään lukea tämä.
            lukko = transaction.execute(new TransactionCallback<Lukko>() {
                @Override
                public Lukko doInTransaction(TransactionStatus status) {
                    return em.find(Lukko.class, id);
                }
            });
        }

        if (lukko == null || !oid.equals(lukko.getHaltijaOid())) {
            throw new LockingException("Kohde on lukittu", LukkoDto.of(lukko));
        }

        return lukko;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional(readOnly = true)
    public boolean isLockedByAuthenticatedUser(Long id) {
        return isLockedByAuthenticatedUser(getLock(id));
    }

    /**
     * Varmistaa, että autentikoitunut käyttäjä omistaa lukon. Huom! lukitsee lukon transaktion ajaksi siten että sitä
     * ei voi muuttaa/poistaa.
     *
     * @param id lukon tunniste
     */
    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional
    public void ensureLockedByAuthenticatedUser(Long id) {
        Lukko lukko = em.find(Lukko.class, id, LockModeType.PESSIMISTIC_READ);
        if (!isLockedByAuthenticatedUser(lukko)) {
            throw new LockingException("Käyttäjä ei omista lukkoa", LukkoDto.of(lukko));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional(readOnly = false)
    public boolean unlock(Long id) {
        final Lukko lukko = em.find(Lukko.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (isLockedByAuthenticatedUser(lukko)) {
            em.remove(lukko);
            return true;
        } else {
            throw new LockingException("Käyttäjä ei omista poistettavaa lukkoa", LukkoDto.of(lukko));
        }

    }

    @Override
    @Transactional(readOnly = true)
    public Lukko getLock(Long id) {
        return em.find(Lukko.class, id);
    }

    private boolean isLockedByAuthenticatedUser(Lukko lukko) {
        final String oid = SecurityUtil.getAuthenticatedPrincipal().getName();
        return lukko != null && Objects.equals(lukko.getHaltijaOid(), oid);
    }

}

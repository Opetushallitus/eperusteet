package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.Lukko;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.dto.kayttaja.KayttajanTietoDto;
import fi.vm.sade.eperusteet.service.KayttajanTietoService;
import fi.vm.sade.eperusteet.service.exception.LockingException;
import fi.vm.sade.eperusteet.service.internal.LockManager;
import fi.vm.sade.eperusteet.service.util.SecurityUtil;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class LockManagerImpl implements LockManager {

    private final TransactionTemplate transaction;

    @Autowired
    private KayttajanTietoService kayttajat;

    @Autowired
    private EntityManager em;

    @Autowired
    public LockManagerImpl(PlatformTransactionManager manager) {
        transaction = new TransactionTemplate(manager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Value("${fi.vm.sade.eperusteet.lukitus.aikaSekunteina}")
    private int maxLockTime;

    @PreAuthorize("isAuthenticated()")
    @Override
    public Lukko lock(final Long id) {

        final String oid = SecurityUtil.getAuthenticatedPrincipal().getName();

        Lukko lukko;
        try {
            lukko = transaction.execute(new TransactionCallback<Lukko>() {
                @Override
                public Lukko doInTransaction(TransactionStatus status) {
                    final Lukko newLukko = new Lukko(id, oid, maxLockTime);
                    Lukko current = getLock(id);
                    if (current != null) {
                        em.refresh(current, LockModeType.PESSIMISTIC_WRITE);
                        if (oid.equals(current.getHaltijaOid())) {
                            current.refresh();
                        } else if (current.getVanhentuu().isBeforeNow()) {
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
        } catch (TransactionException | DataAccessException | PersistenceException t) {

            // (todennäköisesti) samanaikaisesti toisessa transaktiossa lisätty sama lukko, yritetään lukea tämä.
            lukko = transaction.execute(new TransactionCallback<Lukko>() {
                @Override
                public Lukko doInTransaction(TransactionStatus status) {
                    return getLock(id);
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
    public void lisaaNimiLukkoon(LukkoDto lock) {
        if (lock != null && lock.getHaltijaOid() != null) {
            Future<KayttajanTietoDto> fktd = kayttajat.haeAsync(lock.getHaltijaOid());
            try {
                KayttajanTietoDto ktd = fktd.get(1, TimeUnit.SECONDS);
                if (ktd != null) {
                    String kutsumanimi = ktd.getKutsumanimi();
                    String sukunimi = ktd.getSukunimi();
                    String haltijaNimi = Stream.of(kutsumanimi, sukunimi)
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining(" "));
                    lock.setHaltijaNimi(haltijaNimi);
                }
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            }
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional(readOnly = true)
    public boolean isLockedByAuthenticatedUser(Long id) {
        return isLockedByAuthenticatedUser(getLock(id));
    }

    /**
     * Varmistaa, että autentikoitunut käyttäjä omistaa lukon. Huom! lukitsee lukon transaktion ajaksi siten että sitä ei voi muuttaa/poistaa.
     *
     * @param id lukon tunniste
     */
    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional
    public void ensureLockedByAuthenticatedUser(Long id) {
        Lukko lukko = em.find(Lukko.class, id, LockModeType.PESSIMISTIC_READ);
        if (lukko != null) {
            lukko.setVanhentumisAika(maxLockTime);
        }
        if (!isLockedByAuthenticatedUser(lukko)) {
            throw new LockingException("Käyttäjä ei omista lukkoa", LukkoDto.of(lukko));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional(readOnly = false)
    public boolean unlock(Long id) {
        final Lukko lukko = em.find(Lukko.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (lukko == null) {
            return false;
        }
        if (isLockedByAuthenticatedUser(lukko)) {
            em.remove(lukko);
            return true;
        }
        throw new LockingException("Käyttäjä ei omista lukkoa", LukkoDto.of(lukko));
    }

    @Override
    @Transactional(readOnly = true)
    public Lukko getLock(Long id) {
        Lukko l = em.find(Lukko.class, id);
        if (l != null) {
            l.setVanhentumisAika(maxLockTime);
        }
        return l;
    }

    private boolean isLockedByAuthenticatedUser(Lukko lukko) {
        final String oid = SecurityUtil.getAuthenticatedPrincipal().getName();
        return lukko != null && Objects.equals(lukko.getHaltijaOid(), oid);
    }

}

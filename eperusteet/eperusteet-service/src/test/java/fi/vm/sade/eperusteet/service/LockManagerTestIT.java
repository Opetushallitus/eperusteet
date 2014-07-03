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
package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.domain.Lukko;
import fi.vm.sade.eperusteet.service.exception.LockingException;
import fi.vm.sade.eperusteet.service.test.AbstractIntegrationTest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author jhyoty
 */
@DirtiesContext
public class LockManagerTestIT extends AbstractIntegrationTest {

    @Autowired
    private LockManager lockManager;

    @Test
    public void testLock() {
        Lukko lock = lockManager.lock(1L);
        lockManager.ensureLockedByAuthenticatedUser(1L);
        assertEquals("test", lock.getHaltijaOid());
        lockManager.unlock(1L);
    }

    @Test
    public void testUnLock() {
        lockManager.lock(2L);
        lockManager.unlock(2L);
        Assert.assertNull(lockManager.getLock(2L));
    }

    @Test(expected = LockingException.class)
    public void testEnsureLocked() {
        //lockManager.unlock(3L);
        lockManager.ensureLockedByAuthenticatedUser(3L);
    }

    @Test
    public void testConcurrentLocking() throws InterruptedException, ExecutionException {

        final CountDownLatch latch = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(8);

        List<Future<Lukko>> results = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            final int id = i;
            Callable<Lukko> task = new Callable<Lukko>() {
                @Override
                public Lukko call() throws Exception {
                    latch.await();
                    SecurityContext ctx = SecurityContextHolder.createEmptyContext();
                    ctx.setAuthentication(new UsernamePasswordAuthenticationToken("test" + id, "test"));
                    SecurityContextHolder.setContext(ctx);
                    return lockManager.lock(42L);
                }
            };
            results.add(pool.submit(task));
        }
        latch.countDown();
        pool.shutdown();
        pool.awaitTermination(3, TimeUnit.MINUTES);
        int locked = 0;
        String lockedBy = null;

        //varmistetaan että vain yksi lukitus onnistui ja että kaikissa tapauksissa ollaan samaa mieltä siitä
        //kenellä on lukko.
        for (Future<Lukko> f : results) {
            String haltijaOid = "";
            try {
                haltijaOid = f.get().getHaltijaOid();
            } catch (ExecutionException l) {
                if (l.getCause() instanceof LockingException) {
                    locked++;
                    haltijaOid = ((LockingException) l.getCause()).getLukko().getHaltijaOid();
                }
            }
            if (lockedBy == null) {
                lockedBy = haltijaOid;
            } else {
                assertEquals(lockedBy, haltijaOid);
            }
        }
        assertEquals(results.size() - 1, locked);

        //poistetaan lukitus sinä käyttäjänä joka lukituksen teki.
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(lockedBy, "test"));
        SecurityContextHolder.setContext(ctx);
        assertEquals(true,lockManager.unlock(42L));
    }
}

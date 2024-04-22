package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.service.internal.LockManager;
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
                    ctx.setAuthentication(new UsernamePasswordAuthenticationToken("test" + (id+1), "test"));
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
            } catch (ExecutionException ee) {
                if (ee.getCause() instanceof LockingException) {
                    locked++;
                    haltijaOid = ((LockingException) ee.getCause()).getLukko().getHaltijaOid();
                } else {
                    throw ee;
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

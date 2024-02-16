package fi.vm.sade.eperusteet.service;

import fi.vm.sade.eperusteet.dto.LukkoDto;
import org.springframework.security.access.prepost.PreAuthorize;

public interface LockService<T> {

    @PreAuthorize("isAuthenticated()")
    LukkoDto getLock(T ctx);

    @PreAuthorize("isAuthenticated()")
    LukkoDto lock(T ctx);

    @PreAuthorize("isAuthenticated()")
    LukkoDto lock(T ctx, Integer ifMatchRevision);

    @PreAuthorize("isAuthenticated()")
    void unlock(T ctx);

    @PreAuthorize("isAuthenticated()")
    void assertLock(T ctx);

}

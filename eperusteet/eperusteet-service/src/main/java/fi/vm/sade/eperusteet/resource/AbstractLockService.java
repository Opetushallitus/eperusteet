package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.internal.LockManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static fi.vm.sade.eperusteet.resource.util.Etags.eTagHeader;
import static fi.vm.sade.eperusteet.resource.util.Etags.revisionOf;

public abstract class AbstractLockService<T> {

    @Autowired
    private LockManager lukkomanageri;

    public ResponseEntity<LukkoDto> checkLock(T ctx) {
        handleContext(ctx);
        LukkoDto lock = service().getLock(ctx);
        lukkomanageri.lisaaNimiLukkoon(lock);
        return lock == null ? new ResponseEntity<>(HttpStatus.NOT_FOUND) : new ResponseEntity<>(lock, eTagHeader(lock.getRevisio()), HttpStatus.OK);
    }

    public ResponseEntity<LukkoDto> lock(T ctx, String eTag) {
        handleContext(ctx);
        LukkoDto lock = service().lock(ctx, revisionOf(eTag));
        if (lock == null) {
            return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
        } else {
            return new ResponseEntity<>(lock, eTagHeader(lock.getRevisio()), HttpStatus.CREATED);
        }
    }

    protected void handleContext(T ctx) {
    }

    public void unlock(T ctx) {
        handleContext(ctx);
        service().unlock(ctx);
    }

    protected abstract LockService<T> service();
}

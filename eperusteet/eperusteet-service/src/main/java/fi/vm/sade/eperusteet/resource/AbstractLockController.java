package fi.vm.sade.eperusteet.resource;

import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import static fi.vm.sade.eperusteet.resource.util.Etags.eTagHeader;
import static fi.vm.sade.eperusteet.resource.util.Etags.revisionOf;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.internal.LockManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@InternalApi
public abstract class AbstractLockController<T> {

    @Autowired
    private LockManager lukkomanageri;

    @RequestMapping(method = GET)
    public ResponseEntity<LukkoDto> checkLock(T ctx) {
        handleContext(ctx);
        LukkoDto lock = service().getLock(ctx);
        lukkomanageri.lisaaNimiLukkoon(lock);
        return lock == null ? new ResponseEntity<>(HttpStatus.NOT_FOUND) : new ResponseEntity<>(lock, eTagHeader(lock.getRevisio()), HttpStatus.OK);
    }

    @RequestMapping(method = POST)
    public ResponseEntity<LukkoDto> lock(
            @RequestBody T ctx,
            @RequestHeader(value = "If-Match", required = false) String eTag) {
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

    @RequestMapping(method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlock(T ctx) {
        handleContext(ctx);
        service().unlock(ctx);
    }

    protected abstract LockService<T> service();
}

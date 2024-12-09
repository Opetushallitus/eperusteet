package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.dto.LukkoDto;
import fi.vm.sade.eperusteet.resource.AbstractLockController;
import fi.vm.sade.eperusteet.resource.AbstractLockService;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.TutkinnonRakenneLockContext;
import fi.vm.sade.eperusteet.service.yl.OppiaineLockContext;
import fi.vm.sade.eperusteet.service.yl.OppiaineOpetuksenSisaltoTyyppi;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@InternalApi
@RequestMapping(value = "/api/perusteet/{perusteId}/suoritustavat/{koodi}/rakenne/lukko")
@Tag(name = "TutkinnonRakenneLock")
public class TutkinnonRakenneLockController extends AbstractLockService<TutkinnonRakenneLockContext> {
    @Autowired
    @LockCtx(TutkinnonRakenneLockContext.class)
    private LockService<TutkinnonRakenneLockContext> service;

    @Override
    protected LockService<TutkinnonRakenneLockContext> service() {
        return service;
    }


    @RequestMapping(method = GET)
    public ResponseEntity<LukkoDto> checkLockTutkinnonRakenne(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("koodi") final String koodi) {
        return super.checkLock(TutkinnonRakenneLockContext.of(perusteId, Suoritustapakoodi.of(koodi)));
    }

    @RequestMapping(method = POST)
    public ResponseEntity<LukkoDto> lockTutkinnonRakenne(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("koodi") final String koodi,
            @RequestHeader(value = "If-Match", required = false) String eTag) {
        return super.lock(TutkinnonRakenneLockContext.of(perusteId, Suoritustapakoodi.of(koodi)), eTag);
    }

    @RequestMapping(method = DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlockTutkinnonRakenne(
            @PathVariable("perusteId") final Long perusteId,
            @PathVariable("koodi") final String koodi) {
        super.unlock(TutkinnonRakenneLockContext.of(perusteId, Suoritustapakoodi.of(koodi)));
    }
}

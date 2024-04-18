package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.resource.AbstractLockController;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.TutkinnonRakenneLockContext;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@InternalApi
@RequestMapping(value = "/api/perusteet/{perusteId}/suoritustavat/{koodi}/rakenne/lukko")
@Api("TutkinnonRakenneLock")
public class TutkinnonRakenneLockController extends AbstractLockController<TutkinnonRakenneLockContext> {
    @Autowired
    @LockCtx(TutkinnonRakenneLockContext.class)
    private LockService<TutkinnonRakenneLockContext> service;

    @Override
    protected LockService<TutkinnonRakenneLockContext> service() {
        return service;
    }
}

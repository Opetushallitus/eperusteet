package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.resource.AbstractLockController;
import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.yl.OppiaineLockContext;
import fi.vm.sade.eperusteet.service.yl.OppiaineOpetuksenSisaltoTyyppi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@InternalApi
@RequestMapping(value = {
        LukioOppiaineLockController.BASE + "/lukko",
})
public class LukioOppiaineLockController extends AbstractLockController<OppiaineLockContext> {
    static final String BASE = "/api/perusteet/{perusteId}/lukiokoulutus/oppiaineet/{oppiaineId}";
    @Autowired
    @LockCtx(OppiaineLockContext.class)
    private LockService<OppiaineLockContext> service;

    @Override
    protected void handleContext(OppiaineLockContext ctx) {
        ctx.setTyyppi(OppiaineOpetuksenSisaltoTyyppi.LUKIOKOULUTUS);
    }

    @Override
    protected LockService<OppiaineLockContext> service() {
        return service;
    }
}

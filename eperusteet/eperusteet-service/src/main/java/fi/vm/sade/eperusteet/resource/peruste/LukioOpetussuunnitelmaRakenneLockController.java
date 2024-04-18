package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.resource.AbstractLockController;
import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.yl.LukioOpetussuunnitelmaRakenneLockContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@InternalApi
@RequestMapping(value = {
        LukioOpetussuunnitelmaRakenneLockController.BASE + "/lukko",
})
public class LukioOpetussuunnitelmaRakenneLockController extends AbstractLockController<LukioOpetussuunnitelmaRakenneLockContext> {
    public static final String BASE = "/api/perusteet/{perusteId}/lukiokoulutus/rakenne";

    @Autowired
    @LockCtx(LukioOpetussuunnitelmaRakenneLockContext.class)
    private LockService<LukioOpetussuunnitelmaRakenneLockContext> service;

    @Override
    protected LockService<LukioOpetussuunnitelmaRakenneLockContext> service() {
        return service;
    }
}

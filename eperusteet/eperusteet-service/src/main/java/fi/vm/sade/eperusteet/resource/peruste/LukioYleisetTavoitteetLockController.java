package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.resource.AbstractLockController;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.yl.LukioYleisetTavoitteetLockContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@InternalApi
@RequestMapping(value = {
        LukioYleisetTavoitteetLockController.BASE + "/lukko",
})
public class LukioYleisetTavoitteetLockController extends AbstractLockController<LukioYleisetTavoitteetLockContext> {
    public static final String BASE = "/perusteet/{perusteId}/lukiokoulutus/yleisettavoitteet";

    @Autowired
    @LockCtx(LukioYleisetTavoitteetLockContext.class)
    private LockService<LukioYleisetTavoitteetLockContext> service;

    @Override
    protected LockService<LukioYleisetTavoitteetLockContext> service() {
        return service;
    }
}

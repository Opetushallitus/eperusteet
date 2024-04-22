package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.domain.Suoritustapakoodi;
import fi.vm.sade.eperusteet.resource.AbstractLockController;
import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.Suoritustavalle;
import fi.vm.sade.eperusteet.service.yl.KurssiLockContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@InternalApi
@RequestMapping(value = {
        LukiokurssiLockController.BASE + "/lukko",
})
public class LukiokurssiLockController extends AbstractLockController<KurssiLockContext> {
    public static final String BASE = "/api/perusteet/{perusteId}/lukiokoulutus/kurssit/{kurssiId}";

    @Autowired
    @LockCtx(KurssiLockContext.class)
    @Suoritustavalle(Suoritustapakoodi.LUKIOKOULUTUS)
    private LockService<KurssiLockContext> kurssiLockService;

    @Override
    protected LockService<KurssiLockContext> service() {
        return kurssiLockService;
    }
}

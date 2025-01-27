package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.resource.AbstractLockController;
import fi.vm.sade.eperusteet.config.InternalApi;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.yl.LukioAihekokonaisuudetLockContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//@RestController
//@InternalApi
//@RequestMapping(value = {
//        LukioAihekokonaisuudetLockController.BASE + "/lukko",
//})
public class LukioAihekokonaisuudetLockController extends AbstractLockController<LukioAihekokonaisuudetLockContext> {
    public static final String BASE = "/api/perusteet/{perusteId}/lukiokoulutus/aihekokonaisuudet";

    @Autowired
    @LockCtx(LukioAihekokonaisuudetLockContext.class)
    private LockService<LukioAihekokonaisuudetLockContext> service;

    @Override
    protected LockService<LukioAihekokonaisuudetLockContext> service() {
        return service;
    }
}

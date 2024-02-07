package fi.vm.sade.eperusteet.resource.peruste;

import fi.vm.sade.eperusteet.resource.AbstractLockController;
import fi.vm.sade.eperusteet.resource.config.InternalApi;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.yl.LukioAihekokonaisuusLockContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@InternalApi
@RequestMapping(value = {
        LukioAihekokonaisuusLockController.BASE + "/lukko",
})
public class LukioAihekokonaisuusLockController extends AbstractLockController<LukioAihekokonaisuusLockContext> {
    public static final String BASE = "/perusteet/{perusteId}/lukiokoulutus/aihekokonaisuudet/{aihekokonaisuusId}";

    @Autowired
    @LockCtx(LukioAihekokonaisuusLockContext.class)
    private LockService<LukioAihekokonaisuusLockContext> aihekokonaisuusLockService;

    @Override
    protected LockService<LukioAihekokonaisuusLockContext> service() {
        return aihekokonaisuusLockService;
    }

}

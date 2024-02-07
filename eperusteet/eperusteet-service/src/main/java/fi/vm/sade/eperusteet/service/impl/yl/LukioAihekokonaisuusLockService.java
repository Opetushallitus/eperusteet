package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.yl.lukio.Aihekokonaisuus;
import fi.vm.sade.eperusteet.repository.LukioAihekokonaisuusRepository;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.impl.AbstractLockService;
import fi.vm.sade.eperusteet.service.yl.LukioAihekokonaisuusLockContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static fi.vm.sade.eperusteet.service.util.OptionalUtil.found;

@Service
@LockCtx(LukioAihekokonaisuusLockContext.class)
public class LukioAihekokonaisuusLockService extends AbstractLockService<LukioAihekokonaisuusLockContext> {

    @Autowired
    private LukioAihekokonaisuusRepository lukioAihekokonaisuusRepository;

    protected Aihekokonaisuus getAihekokonaisuus(LukioAihekokonaisuusLockContext ctx) {
        return found(lukioAihekokonaisuusRepository.getOne(ctx.getAihekokonaisuusId()), Aihekokonaisuus.inPeruste(ctx.getPerusteId()));
    }

    @Override
    protected int latestRevision(LukioAihekokonaisuusLockContext ctx) {
        return lukioAihekokonaisuusRepository.getLatestRevisionId(ctx.getAihekokonaisuusId()).getNumero();
    }

    @Override
    protected Long getLockId(LukioAihekokonaisuusLockContext ctx) {
        return ctx.getAihekokonaisuusId();
    }

    @Override
    protected Long validateCtx(LukioAihekokonaisuusLockContext ctx, boolean readOnly) {
        checkPermissionToPeruste(ctx, readOnly);
        Aihekokonaisuus aihekokonaisuus = getAihekokonaisuus(ctx);
        return aihekokonaisuus.getId();
    }
}

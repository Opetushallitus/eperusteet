package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.repository.LukiokoulutuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.impl.AbstractLockService;
import fi.vm.sade.eperusteet.service.yl.LukioAihekokonaisuudetLockContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static fi.vm.sade.eperusteet.service.util.OptionalUtil.found;

@Service
@LockCtx(LukioAihekokonaisuudetLockContext.class)
public class AihekokonaisuudetLockService extends AbstractLockService<LukioAihekokonaisuudetLockContext>
            implements LockService<LukioAihekokonaisuudetLockContext> {
    @Autowired
    private LukiokoulutuksenPerusteenSisaltoRepository lukiokoulutuksenPerusteenSisaltoRepository;

    @Override
    protected Long getLockId(LukioAihekokonaisuudetLockContext ctx) {
        return found(lukiokoulutuksenPerusteenSisaltoRepository.findByPerusteId(ctx.getPerusteId())).getId();
    }

    @Override
    protected Long validateCtx(LukioAihekokonaisuudetLockContext ctx, boolean readOnly) {
        checkPermissionToPeruste(ctx, readOnly);
        return getLockId(ctx);
    }

    @Override
    protected int latestRevision(LukioAihekokonaisuudetLockContext ctx) {
        return lukiokoulutuksenPerusteenSisaltoRepository.getLatestRevisionId(getLockId(ctx)).getNumero();
    }
}

package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.repository.LukiokoulutuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.impl.AbstractLockService;
import fi.vm.sade.eperusteet.service.yl.LukioYleisetTavoitteetLockContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static fi.vm.sade.eperusteet.service.util.OptionalUtil.found;

@Service
@LockCtx(LukioYleisetTavoitteetLockContext.class)
public class YleisetTavoitteetLockService extends AbstractLockService<LukioYleisetTavoitteetLockContext>
            implements LockService<LukioYleisetTavoitteetLockContext> {
    @Autowired
    private LukiokoulutuksenPerusteenSisaltoRepository lukiokoulutuksenPerusteenSisaltoRepository;

    @Override
    protected Long getLockId(LukioYleisetTavoitteetLockContext ctx) {
        return found(lukiokoulutuksenPerusteenSisaltoRepository.findByPerusteId(ctx.getPerusteId())).getId();
    }

    @Override
    protected Long validateCtx(LukioYleisetTavoitteetLockContext ctx, boolean readOnly) {
        checkPermissionToPeruste(ctx, readOnly);
        return getLockId(ctx);
    }

    @Override
    protected int latestRevision(LukioYleisetTavoitteetLockContext ctx) {
        return lukiokoulutuksenPerusteenSisaltoRepository.getLatestRevisionId(getLockId(ctx)).getNumero();
    }
}

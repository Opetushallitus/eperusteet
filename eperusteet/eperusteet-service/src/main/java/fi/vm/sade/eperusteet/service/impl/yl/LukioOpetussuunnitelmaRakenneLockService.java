package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.repository.LukioOpetussuunnitelmaRakenneRepository;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.LockService;
import fi.vm.sade.eperusteet.service.impl.AbstractLockService;
import fi.vm.sade.eperusteet.service.yl.LukioOpetussuunnitelmaRakenneLockContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static fi.vm.sade.eperusteet.service.util.OptionalUtil.found;

@Service
@LockCtx(LukioOpetussuunnitelmaRakenneLockContext.class)
public class LukioOpetussuunnitelmaRakenneLockService extends AbstractLockService<LukioOpetussuunnitelmaRakenneLockContext>
            implements LockService<LukioOpetussuunnitelmaRakenneLockContext> {
    @Autowired
    private LukioOpetussuunnitelmaRakenneRepository rakenneRepository;

    @Override
    protected Long getLockId(LukioOpetussuunnitelmaRakenneLockContext ctx) {
        return found(rakenneRepository.findByPerusteId(ctx.getPerusteId())).getId();
    }

    @Override
    protected Long validateCtx(LukioOpetussuunnitelmaRakenneLockContext ctx, boolean readOnly) {
        checkPermissionToPeruste(ctx, readOnly);
        return getLockId(ctx);
    }

    @Override
    protected int latestRevision(LukioOpetussuunnitelmaRakenneLockContext ctx) {
        return rakenneRepository.getLatestRevisionId(getLockId(ctx)).getNumero();
    }
}

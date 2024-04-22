package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.yl.Kurssi;
import fi.vm.sade.eperusteet.service.impl.AbstractLockService;
import fi.vm.sade.eperusteet.service.yl.KurssiLockContext;

public abstract class AbstractKurssiLockService extends AbstractLockService<KurssiLockContext> {
    @Override
    protected Long getLockId(KurssiLockContext ctx) {
        return ctx.getKurssiId();
    }

    protected abstract Kurssi getKurssi(KurssiLockContext ctx);

    @Override
    protected Long validateCtx(KurssiLockContext ctx, boolean readOnly) {
        checkPermissionToPeruste(ctx, readOnly);
        Kurssi kurssi = getKurssi(ctx);
        return kurssi.getId();
    }
}

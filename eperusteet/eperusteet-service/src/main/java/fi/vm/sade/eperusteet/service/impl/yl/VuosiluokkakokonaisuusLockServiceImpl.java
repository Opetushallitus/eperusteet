package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.VuosiluokkaKokonaisuus;
import fi.vm.sade.eperusteet.repository.PerusopetuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.repository.VuosiluokkaKokonaisuusRepository;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.impl.AbstractLockService;
import fi.vm.sade.eperusteet.service.security.PermissionManager;
import fi.vm.sade.eperusteet.service.yl.VuosiluokkaKokonaisuusContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@LockCtx(VuosiluokkaKokonaisuusContext.class)
public class VuosiluokkakokonaisuusLockServiceImpl extends AbstractLockService<VuosiluokkaKokonaisuusContext> {

    @Autowired
    private PerusopetuksenPerusteenSisaltoRepository sisallot;

    @Autowired
    private VuosiluokkaKokonaisuusRepository kokonaisuudet;

    @Override
    protected Long getLockId(VuosiluokkaKokonaisuusContext ctx) {
        return ctx.getKokonaisuusId();
    }

    @Override
    protected final Long validateCtx(VuosiluokkaKokonaisuusContext ctx, boolean readOnly) {
        if (readOnly) {
            permissionChecker.checkPermission(ctx.getPerusteId(), PermissionManager.Target.PERUSTE, PermissionManager.Permission.LUKU);
        } else {
            permissionChecker.checkPermission(ctx.getPerusteId(), PermissionManager.Target.PERUSTE, PermissionManager.Permission.MUOKKAUS, PermissionManager.Permission.KORJAUS);
        }
        PerusopetuksenPerusteenSisalto s = sisallot.findByPerusteId(ctx.getPerusteId());
        VuosiluokkaKokonaisuus kokonaisuus = kokonaisuudet.findOne(ctx.getKokonaisuusId());
        if (s == null || !s.containsVuosiluokkakokonaisuus(kokonaisuus)) {
            throw new BusinessRuleViolationException("Virheellinen lukitus");
        }
        return kokonaisuus.getId();
    }

    @Override
    protected final int latestRevision(VuosiluokkaKokonaisuusContext ctx) {
        //olettaa että lockcontext on validi (ei tarkisteta erikseen)
        return kokonaisuudet.getLatestRevisionId(ctx.getKokonaisuusId()).getNumero();
    }

}

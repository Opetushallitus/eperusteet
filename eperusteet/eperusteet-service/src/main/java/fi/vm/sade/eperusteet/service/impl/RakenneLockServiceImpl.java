package fi.vm.sade.eperusteet.service.impl;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.tutkinnonrakenne.RakenneModuuli;
import fi.vm.sade.eperusteet.repository.PerusteRepository;
import fi.vm.sade.eperusteet.repository.RakenneRepository;
import fi.vm.sade.eperusteet.repository.version.Revision;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.TutkinnonRakenneLockContext;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.security.PermissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@LockCtx(TutkinnonRakenneLockContext.class)
public class RakenneLockServiceImpl extends AbstractLockService<TutkinnonRakenneLockContext> {

    @Autowired
    private PerusteRepository perusteet;
    @Autowired
    private RakenneRepository rakenteet;

    @Override
    protected Long getLockId(TutkinnonRakenneLockContext ctx) {
        Peruste peruste = perusteet.findOne(ctx.getPerusteId());
        if (peruste != null) {
            return peruste.getSuoritustapa(ctx.getKoodi()).getRakenne().getId();
        }
        return null;
    }

    @Override
    protected int latestRevision(TutkinnonRakenneLockContext ctx) {
        final RakenneModuuli rakenne = perusteet.findOne(ctx.getPerusteId()).getSuoritustapa(ctx.getKoodi()).getRakenne();
        Revision rev = rakenteet.getLatestRevisionId(rakenne.getId());
        return rev != null ? rev.getNumero() : 0;
    }

    @Override
    protected Long validateCtx(TutkinnonRakenneLockContext ctx, boolean readOnly) {
        Peruste peruste = perusteet.findOne(ctx.getPerusteId());
        if (peruste != null) {
            if (readOnly) {
                permissionChecker.checkPermission(ctx.getPerusteId(), PermissionManager.Target.PERUSTE, PermissionManager.Permission.LUKU);
            } else if (peruste.getTila() == PerusteTila.VALMIS) {
                permissionChecker.checkPermission(ctx.getPerusteId(), PermissionManager.Target.PERUSTE, PermissionManager.Permission.KORJAUS);
            } else {
                permissionChecker.checkPermission(ctx.getPerusteId(), PermissionManager.Target.PERUSTE, PermissionManager.Permission.MUOKKAUS);
            }
            return peruste.getSuoritustapa(ctx.getKoodi()).getRakenne().getId();
        }
        throw new BusinessRuleViolationException("Perustetta ei ole");
    }

}

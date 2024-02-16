package fi.vm.sade.eperusteet.service.impl.yl;

import fi.vm.sade.eperusteet.domain.yl.LaajaalainenOsaaminen;
import fi.vm.sade.eperusteet.repository.LaajaalainenOsaaminenRepository;
import fi.vm.sade.eperusteet.service.LockCtx;
import fi.vm.sade.eperusteet.service.exception.BusinessRuleViolationException;
import fi.vm.sade.eperusteet.service.impl.AbstractLockService;
import fi.vm.sade.eperusteet.service.yl.LaajaalainenOsaaminenContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@LockCtx(LaajaalainenOsaaminenContext.class)
public class LaajaalainenOsaaminenLockServiceImpl extends AbstractLockService<LaajaalainenOsaaminenContext> {

    @Autowired
    private LaajaalainenOsaaminenRepository osaaminenRepository;

    @Override
    protected Long getLockId(LaajaalainenOsaaminenContext ctx) {
        return ctx.getOsaaminenId();
    }

    @Override
    protected final Long validateCtx(LaajaalainenOsaaminenContext ctx, boolean readOnly) {
        checkPermissionToPeruste(ctx, readOnly);
        LaajaalainenOsaaminen osaaminen = osaaminenRepository.findBy(ctx.getPerusteId(), ctx.getOsaaminenId());
        if (osaaminen == null) {
            throw new BusinessRuleViolationException("Virheellinen lukitus");
        }
        return osaaminen.getId();
    }

    @Override
    protected final int latestRevision(LaajaalainenOsaaminenContext ctx) {
        //olettaa että lockcontext on validi (ei tarkisteta erikseen)
        return osaaminenRepository.getLatestRevisionId(ctx.getOsaaminenId()).getNumero();
    }
}

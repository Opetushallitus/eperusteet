package fi.vm.sade.eperusteet.service.yl;

import fi.vm.sade.eperusteet.domain.yl.AbstractOppiaineOpetuksenSisalto;
import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.repository.LukiokoulutuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.repository.OppiaineSisaltoRepository;
import fi.vm.sade.eperusteet.repository.PerusopetuksenPerusteenSisaltoRepository;
import org.springframework.context.ApplicationContext;

public enum OppiaineOpetuksenSisaltoTyyppi {
    PERUSOPETUS(PerusopetuksenPerusteenSisalto.class, PerusopetuksenPerusteenSisaltoRepository.class),
    LUKIOKOULUTUS(LukiokoulutuksenPerusteenSisalto.class, LukiokoulutuksenPerusteenSisaltoRepository.class);

    private final Class<? extends AbstractOppiaineOpetuksenSisalto> entityType;
    private final Class<? extends OppiaineSisaltoRepository<? extends AbstractOppiaineOpetuksenSisalto>> repositoryClz;

    <EType extends AbstractOppiaineOpetuksenSisalto> OppiaineOpetuksenSisaltoTyyppi(
            Class<EType> entityType,
            Class<? extends OppiaineSisaltoRepository<EType>> repositoryClz) {
        this.entityType = entityType;
        this.repositoryClz = repositoryClz;
    }

    public Class<? extends AbstractOppiaineOpetuksenSisalto> getEntityType() {
        return entityType;
    }

    public OppiaineSisaltoRepository<? extends AbstractOppiaineOpetuksenSisalto> getRepository(ApplicationContext ctx) {
        return ctx.getBean(repositoryClz);
    }

    public AbstractOppiaineOpetuksenSisalto getLockedByPerusteId(ApplicationContext ctx, Long perusteId) {
        OppiaineSisaltoRepository repository = getRepository(ctx);
        AbstractOppiaineOpetuksenSisalto sisalto = repository.findByPerusteId(perusteId);
        if (sisalto != null) {
            //noinspection unchecked
            repository.lock(sisalto);
        }
        return sisalto;
    }
}

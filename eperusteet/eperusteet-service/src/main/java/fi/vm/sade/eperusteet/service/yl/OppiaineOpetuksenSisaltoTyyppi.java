/*
 *  Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 *  This program is free software: Licensed under the EUPL, Version 1.1 or - as
 *  soon as they will be approved by the European Commission - subsequent versions
 *  of the EUPL (the "Licence");
 *
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  European Union Public Licence for more details.
 */

package fi.vm.sade.eperusteet.service.yl;

import fi.vm.sade.eperusteet.domain.yl.AbstractOppiaineOpetuksenSisalto;
import fi.vm.sade.eperusteet.domain.yl.PerusopetuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.domain.yl.lukio.LukiokoulutuksenPerusteenSisalto;
import fi.vm.sade.eperusteet.repository.LukiokoulutuksenPerusteenSisaltoRepository;
import fi.vm.sade.eperusteet.repository.OppiaineSisaltoRepository;
import fi.vm.sade.eperusteet.repository.PerusopetuksenPerusteenSisaltoRepository;
import org.springframework.context.ApplicationContext;

/**
 * User: tommiratamaa
 * Date: 21.9.15
 * Time: 13.35
 */
public enum OppiaineOpetuksenSisaltoTyyppi {
    PERUSOPETUS(PerusopetuksenPerusteenSisalto.class, PerusopetuksenPerusteenSisaltoRepository.class),
    LUKIOKOULUTUS(LukiokoulutuksenPerusteenSisalto.class, LukiokoulutuksenPerusteenSisaltoRepository.class);

    private final Class<? extends AbstractOppiaineOpetuksenSisalto> entityType;
    private final Class<? extends OppiaineSisaltoRepository<? extends AbstractOppiaineOpetuksenSisalto>> repositoryClz;

    <EType extends AbstractOppiaineOpetuksenSisalto> OppiaineOpetuksenSisaltoTyyppi(Class<EType> entityType,
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

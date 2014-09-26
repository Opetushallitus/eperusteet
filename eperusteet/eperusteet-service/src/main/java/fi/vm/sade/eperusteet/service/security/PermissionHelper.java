/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://ec.europa.eu/idabc/eupl
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.eperusteet.service.security;

import fi.vm.sade.eperusteet.domain.Peruste;
import fi.vm.sade.eperusteet.domain.PerusteTila;
import fi.vm.sade.eperusteet.domain.PerusteenOsa;
import fi.vm.sade.eperusteet.domain.WithPerusteTila;
import java.io.Serializable;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 *
 * @author jhyoty
 */
@Component
public class PermissionHelper {

    @Autowired
    private EntityManager em;

    @Cacheable(value = "tila", unless = "#result != T(fi.vm.sade.eperusteet.domain.PerusteTila).VALMIS")
    public PerusteTila findPerusteTilaFor(PermissionManager.Target targetType, Serializable id) {
        switch (targetType) {
            case PERUSTEENOSA:
                return findPerusteTilaFor(PerusteenOsa.class, id);
            case PERUSTE:
                return findPerusteTilaFor(Peruste.class, id);
            default:
                return null;
        }
    }

    private PerusteTila findPerusteTilaFor(Class<? extends WithPerusteTila> entity, Serializable id) {
        if (id == null) {
            return null;
        }
        WithPerusteTila e = em.find(entity, id);
        return e == null ? null : e.getTila();
    }
}

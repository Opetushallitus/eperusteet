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
import fi.vm.sade.eperusteet.service.exception.NotExistsException;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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
        PerusteTila tila = null;
        switch (targetType) {
            case PERUSTEENOSA:
                tila = findPerusteTilaFor(PerusteenOsa.class, id);
                break;
            case PERUSTE:
                tila = findPerusteTilaFor(Peruste.class, id);
                break;
            default:
                return null;
        }
        if (tila == null) {
            throw new NotExistsException("tilaa-ei-asetettu");
        }
        return tila;
    }

    private PerusteTila findPerusteTilaFor(Class<? extends WithPerusteTila> entity, Serializable id) {
        if (id == null) {
            return null;
        }
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<PerusteTila> query = cb.createQuery(PerusteTila.class);
        Root<? extends WithPerusteTila> root = query.from(entity);
        query.select(root.<PerusteTila>get("tila")).where(cb.equal(root.get("id"), id));
        List<PerusteTila> result = em.createQuery(query).getResultList();
        return result.isEmpty() ? null : result.get(0);
    }
}
